package com.marketboss
package api

import dto.{ChatNGood, ChatNRequest, ChatNResponse, Good}

import io.circe.Json
import io.circe.parser.parse
import sttp.client3.{HttpURLConnectionBackend, UriContext, basicRequest}

class SberMega extends MarketApi{

  override val dirName: String = "SberMegaMarket"
  override val tableName: String = "sber_mega_market"

  // SberMegaMarket
  val SberSearchUri = "https://sbermegamarket.ru/api/mobile/v1/catalogService/catalog/search"
  val SberHeaders = Map(
    "Referer" -> "https://sbermegamarket.ru/catalog/",
    "Content-Type" -> "application/json",
  )

  override def getResponseNChat(req: ChatNRequest): ChatNResponse = {
    val response = basicRequest
      .post(uri"$SberSearchUri")
      .headers(SberHeaders)
      .body(req.requestBody)
      .send(HttpURLConnectionBackend())

    ChatNResponse(req.chatId, response)
  }

  def getChatNGoodSeq(jstr: String, chatId: String): Seq[ChatNGood] = {
    parse(jstr) match {
      case Right(testJs) => {
        testJs.hcursor.downField("items").as[List[Json]]
          .map(js =>
            js.map(jss => {
              val good = Good(
                jss.hcursor.downField("goods").downField("goodsId").as[String].getOrElse(""),
                jss.hcursor.downField("goods").downField("title").as[String].getOrElse(""),
                jss.hcursor.downField("priceFrom").as[Int].getOrElse(0),
                jss.hcursor.downField("goods").downField("webUrl").as[String].getOrElse(""))

              ChatNGood(chatId, good)
            })
          )
      }.getOrElse(List())

      case _ => List()
    }
  }

}
