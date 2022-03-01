package com.marketboss
package com.marketboss.com.marketboss

class UtilsTest {


}


object UtilsTest extends App {

  import dto.{ChatNGood, TlgMessage}
  import util.Utils._

  val link = "https://sbermegamarket.ru/catalog/details/noutbuk-transformer-asus-zenbook-flip-15-ux564ei-ez006t-90nb0sb1-m01070-gray-100029631838/"

  val tlgMessages = List(
    new TlgMessage("777", "notebook 777", 1000, 250, 750, 75.0, 1500, 1250, link, "335231553"),
    new TlgMessage("666", "notebook 666", 1000, 2250, -1250, -125.0, 1500, -750, link, "335231553")
  )
//
//  tlgMessages.foreach( tlgMessage =>
//    sendTelegramNotification("335231553", createMsgString(tlgMessage))
//  )
}