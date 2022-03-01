package com.marketboss.const

import org.apache.spark.sql.functions.col

import java.text.SimpleDateFormat

object Constants {

  // date formats
  val dateFormatStr = "yyyyMMdd"
  val dateFormat = new SimpleDateFormat(dateFormatStr)
  val timeFormat = new SimpleDateFormat("HH:mm")

  // columns
  val GOOD_ID = "good_id"
  val GOOD_ID_COL = col(GOOD_ID)
  val TITLE = "title"
  val TITLE_COL = col(TITLE)
  val PRICE_FROM = "price_from"
  val PRICE_FROM_COL = col(PRICE_FROM)
  val PRICE_TO = "price_to"
  val PRICE_TO_COL = col(PRICE_FROM)
  val PRICE_DIFF_RUR = "price_diff_rur"
  val PRICE_DIFF_RUR_COL = col(PRICE_DIFF_RUR)
  val PRICE_DIFF_AVG = "price_diff_avg"
  val PRICE_DIFF_AVG_COL = col(PRICE_DIFF_AVG)
  val PRICE_DIFF_PERC = "price_diff_perc"
  val PRICE_DIFF_PERC_COL = col(PRICE_DIFF_PERC)
  val LOAD_DATE = "load_date"
  val LOAD_DATE_COL = col(LOAD_DATE)
  val LOAD_TIME = "load_time"
  val LOAD_TIME_COL = col(LOAD_TIME)
  val LOAD_DATE_TIME = "load_date_time"
  val LOAD_DATE_TIME_COL = col(LOAD_DATE_TIME)
  val GOOD_URL = "url"
  val GOOD_URL_COL = col(GOOD_URL)
  val CHAT_ID = "chat_id"
  val CHAT_ID_COL = col("chat_id")
  val AVG_PRICE = "avg_price"
  val AVG_PRICE_COL = col(AVG_PRICE)
  
  // emoji
  val EMOJI_SMILE = "\uD83D\uDE42"
  val EMOJI_SAD = "☹️"
  val EMOJI_HAPPY = "\uD83D\uDE03"
  val EMOJI_CRY = "\uD83D\uDE25"
  val EMOJI_RED_UP = "\uD83D\uDD3A"
  val EMOJI_GREEN_CHECK = "✅"
}
