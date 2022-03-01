package com.marketboss
package util

import const.Constants._
import dto.{ChatNRequest, TlgMessage}

import com.marketboss.MarketBoss.botToken
import io.circe.parser.parse
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import sttp.client3.{HttpURLConnectionBackend, UriContext, basicRequest}

import java.io.File
import java.util.Calendar
import scala.io.Source
import scala.language.postfixOps

object Utils {
  private val calendar = Calendar.getInstance
  private val isNotify = calendar.get(Calendar.HOUR_OF_DAY) < 9 || calendar.get(Calendar.HOUR_OF_DAY) > 21


  private def readRequestsFiles(path: String): Seq[String] = {
    val arr = for (
      file <- new File(path).listFiles.filter(_.getName.endsWith(".json"))
    ) yield {
      val bufferedSource = Source.fromFile(file)
      val text = bufferedSource.getLines().mkString
      bufferedSource.close()

      text
    }

    arr.distinct
  }

  def getRequestsFromFiles(path: String): Seq[ChatNRequest] = {
    val jsons = readRequestsFiles(path)
    jsons.map(jstr =>
      parse(jstr) match {
        case Right(qJs) if (qJs.hcursor.downField("isActive").as[Boolean].getOrElse(false)) =>
          val chatId = qJs \\ "chatId" collectFirst { case chatId => chatId.asString.get } get
          val reqBody = qJs \\ "body"
          val reqUrl = qJs \\ "url"
          val request =
            if (reqBody.nonEmpty) reqBody.head.toString
            else reqUrl collectFirst { case url => url.asString.get } get

          ChatNRequest(chatId, request)
      }).distinct
  }

  def createTlgMessagesDs(resultDs: DataFrame)(implicit spark: SparkSession) = {
    import spark.implicits._
    resultDs.map(r => TlgMessage(
      r.getAs[String](GOOD_ID),
      r.getAs[String](TITLE),
      r.getAs[Int](PRICE_FROM),
      r.getAs[Int](PRICE_TO),
      r.getAs[Int](PRICE_DIFF_RUR),
      r.getAs[Double](PRICE_DIFF_PERC),
      r.getAs[Int](AVG_PRICE),
      r.getAs[Int](PRICE_DIFF_AVG),
      r.getAs[String](GOOD_URL),
      r.getAs[String](CHAT_ID)
    ))
  }

  def sendTlgNotifications(tlgMessageDs: Dataset[TlgMessage]) = {
    tlgMessageDs.foreach(tlgMessage => {
      val msgString = createMsgString(tlgMessage)
      val condition =
        tlgMessage.price_diff_perc <= -5 ||
          tlgMessage.price_diff_perc >= 5 ||
          tlgMessage.price_from == 0 ||
          tlgMessage.avg_price * 0.95 > tlgMessage.price_to

      if (condition)
        sendTelegramNotification(tlgMessage.chat_id, msgString)
    })
  }



  private def createMsgString(msg: TlgMessage): String = {
    val isPriceUp = msg.price_diff_rur < 0
    val emoji = if (isPriceUp) EMOJI_RED_UP else EMOJI_GREEN_CHECK
    val bigDiscountsEmoji = if (msg.price_diff_perc >= 8) EMOJI_HAPPY else if (msg.price_diff_perc <= -8) EMOJI_CRY else ""
    val avgDownOrUp = if (msg.avg_diff > 0) s"ниже средней на ${msg.avg_diff}" else s"выше средней на ${msg.avg_diff * -1}"

    s"""<code><i>${msg.good_id}</i></code>
       |<b>${msg.title}</b>
       |
       | <s>${msg.price_from} руб</s>  →  ${msg.price_to} руб ${bigDiscountsEmoji}
       |  $emoji ${msg.price_diff_rur * -1} руб (${msg.price_diff_perc.toFloat * -1}%)
       |
       | <i>средняя цена ${msg.avg_price} руб
       | $avgDownOrUp руб</i>
       |
       |${msg.url}
       |"""
      .stripMargin
      .replace("\"", "''")
  }

  private def sendTelegramNotification(chatId: String, msg: String): Unit = {
    val body =
      s"""{
         |"chat_id":$chatId,
         |"text":"$msg",
         |"parse_mode":"html",
         |"disable_notification":"$isNotify"
         |}""".stripMargin

    basicRequest
      .post(uri"https://api.telegram.org/bot$botToken/sendMessage")
      .header("Content-Type", "application/json")
      .body(body)
      .send(HttpURLConnectionBackend())

    println(msg)

    Thread.sleep(30)
  }


}
