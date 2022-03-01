package com.marketboss
package api
import dto.{ChatNGood, ChatNRequest, ChatNResponse, Good}

import io.circe.Json
import io.circe.parser.parse
import sttp.client3.{HttpURLConnectionBackend, UriContext, basicRequest}

class Wb extends MarketApi{

  override val dirName: String = "Wildberries"
  override val tableName: String = "wildberries"

  override def getResponseNChat(req: ChatNRequest): ChatNResponse = {
    val response = basicRequest
      .get(uri"${req.requestBody}")
      .send(HttpURLConnectionBackend())

    ChatNResponse(req.chatId, response)
  }


  def getChatNGoodSeq(jstr: String, chatId: String): Seq[ChatNGood] = {
    parse(jstr) match {
      case Right(testJs) => {
        testJs.hcursor.downField("data").downField("products").as[List[Json]]
        .map(js =>
            js.map(jss => {
              val id = jss.hcursor.downField("id").as[Long].getOrElse("").toString
              val brand = jss.hcursor.downField("brand").as[String].getOrElse("")
              val name = jss.hcursor.downField("name").as[String].getOrElse("")
              val price = jss.hcursor.downField("salePriceU").as[Int].getOrElse(0) / 100  // цены на WB пишутся в копейках, делим на 100
              val link = s"https://www.wildberries.ru/catalog/$id/detail.aspx"

              val good = Good(id, s"$brand $name", price, link)
              ChatNGood(chatId, good)
            })
          )
      }.getOrElse(List())

      case _ => List()
    }
  }

}
