package com.marketboss

import api.{MarketApi, Ozon, SberMega, Wb}
import const.Constants._
import dto.ChatNGood
import util.JdbcProps
import util.SparkOps.{readDfFromDb, writeDf}
import util.Utils._

import org.apache.spark.api.java.StorageLevels
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Column, SparkSession}

import java.util.Calendar
import scala.language.postfixOps

object MarketBoss extends App {

  implicit val spark = SparkSession.builder()
    .appName("markets")
    .config("spark.master", "local[*]")
    .getOrCreate()
  spark.sparkContext.setLogLevel("WARN")

  val mode = args(1)
  val botToken = args(5)
  implicit lazy val jdbcProps = new JdbcProps(usr = args(2), pass = args(3), address = args(4))

  implicit val api: MarketApi = mode toLowerCase match {
    case "sbermegamarket" => new SberMega
    case "wildberries" => new Wb
    case "ozon" => new Ozon
    // todo выход при неизвестном режиме
  }

  val requests = getRequestsFromFiles(path = args(0) + "\\" + api.dirName)
  val responses = requests.map(req => api.getResponseNChat(req))

  // в каждом запросе получаем результат поиска с несколькими товарами (набор товаров). В итоге получаем набор запросов (набор с наборами товаров)
  // Seq с парами chat_id -> товар
  val chatNGoods: Seq[ChatNGood] = responses.flatMap(r =>
    r.response.body.map(js => api.getChatNGoodSeq(js, r.chatId)).getOrElse(List())
  )


  import spark.implicits._

  val allGoodIDs: Seq[String] = chatNGoods.map(_.good.good_id)
  val byGoodIdOrdered = Window.partitionBy(GOOD_ID_COL).orderBy(LOAD_DATE_TIME_COL.desc)
  val byGoodId = Window.partitionBy(GOOD_ID_COL)
  // получаем из БД датасет товаров с прошлыми ценами (по good_id из списка выполненного поискового запроса)
  val goodsOldDs = readDfFromDb
    .where(GOOD_ID_COL isin (allGoodIDs: _*) and PRICE_FROM_COL =!= 0)
    .withColumn(AVG_PRICE, (avg(PRICE_FROM_COL) over byGoodId) cast "int")
    .withColumn(LOAD_DATE_TIME, concat_ws(" ", LOAD_DATE_COL, LOAD_TIME_COL)) // Берем с учетом даты, что бы не потерять записи со старыми датами
    .withColumn(TITLE, first(TITLE_COL) over byGoodIdOrdered) // скорее всего название товара можно и не обновлять, но на всякий случай берем последнее
    .withColumn(PRICE_FROM, first(PRICE_FROM_COL) over byGoodIdOrdered) // только последняя цена
    .select(GOOD_ID_COL, TITLE_COL, PRICE_FROM_COL, GOOD_URL_COL, AVG_PRICE_COL)
    .distinct()
    .persist(StorageLevels.MEMORY_ONLY)

  // получаем датасет с товарами и служебными полями по результатам поискового запроса
  val goodsNewDs = chatNGoods.toDS()
    .select(col("good.*"), CHAT_ID_COL)
    .where(PRICE_FROM_COL =!= 0)
    .persist(StorageLevels.MEMORY_ONLY)


  // в будущем, если нагрузка станет большой, можно будет убрать лишние персисты и show()
  println(s"New: (${goodsNewDs.count} rows)")
  goodsNewDs.show(55)
  println(s"Old: (${goodsOldDs.count} rows)")
  goodsOldDs.show(55)



  val priceDiff: Column = goodsOldDs(PRICE_FROM) - goodsNewDs(PRICE_FROM)
  val pricePercentDiff: Column = lit(100) / goodsOldDs(PRICE_FROM) * priceDiff

  def fillColumn(colName: String) = when(goodsNewDs(colName) isNull, goodsOldDs(colName)) otherwise goodsNewDs(colName) as colName
  // получаем датасет с данными для записи в БД и отправки оповещений
  val resultDs = goodsNewDs.join(goodsOldDs, Seq(GOOD_ID), "full")
    .withColumn(PRICE_DIFF_RUR, priceDiff)
    .withColumn(PRICE_DIFF_PERC, pricePercentDiff)
    .withColumn(PRICE_DIFF_AVG, AVG_PRICE_COL - goodsNewDs(PRICE_FROM))
    .select(GOOD_ID_COL, fillColumn(TITLE), goodsOldDs(PRICE_FROM), goodsNewDs(PRICE_FROM) as PRICE_TO, PRICE_DIFF_RUR_COL, PRICE_DIFF_PERC_COL, AVG_PRICE_COL, PRICE_DIFF_AVG_COL, fillColumn(GOOD_URL), CHAT_ID_COL)
    .where((PRICE_DIFF_RUR_COL =!= 0) or (PRICE_FROM_COL isNull))
    .persist(StorageLevels.MEMORY_ONLY)

  println("Results:")
  resultDs.show(55)


  // save new prices in DB
  val loadDate = dateFormat.format(Calendar.getInstance().getTime)
  val loadTime = timeFormat.format(Calendar.getInstance().getTime)
  writeDf(resultDs
    .select(GOOD_ID, TITLE, PRICE_TO, GOOD_URL)
    .withColumnRenamed(PRICE_TO, PRICE_FROM)  // в БД называется 'price_from' , т.к. это минимальная цена, в мегамаркете это "цена от". Небольшая путаница возникает
    .withColumn(LOAD_DATE, lit(loadDate))
    .withColumn(LOAD_TIME, lit(loadTime))
    .dropDuplicates(GOOD_ID) // перед записью новой партии товаров удаляем дубликаты, которые нашлись из смежных поисковых запросов (от разных получателей)
  )



  // creating telegram messages
  val tlgMessageDs = createTlgMessagesDs(resultDs)
  // sending results over Telegram
  sendTlgNotifications(tlgMessageDs)

}
