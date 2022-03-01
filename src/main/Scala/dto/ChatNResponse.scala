package com.marketboss
package dto

import sttp.client3.{Identity, Response}

case class ChatNResponse(chatId: String, response: Identity[Response[Either[String, String]]])
