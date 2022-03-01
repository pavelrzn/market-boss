package com.marketboss
package api

import dto.{ChatNGood, ChatNRequest, ChatNResponse}

trait MarketApi {

  val dirName: String
  val tableName: String

  def getResponseNChat(req: ChatNRequest): ChatNResponse

  def getChatNGoodSeq(jstr: String, chatId: String): Seq[ChatNGood]
}
