package com.marketboss
package api
import dto.{ChatNGood, ChatNRequest, ChatNResponse}

class Ozon extends MarketApi {
  override val dirName: String = "Ozon"

  override def getResponseNChat(req: ChatNRequest): ChatNResponse = ???

  override def getChatNGoodSeq(jstr: String, chatId: String): Seq[ChatNGood] = ???

  override val tableName: String = ???
}
