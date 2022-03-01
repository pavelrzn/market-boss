package com.marketboss
package dto

case class TlgMessage(good_id: String, title: String, price_from: Int, price_to: Int, price_diff_rur: Int, price_diff_perc: Double, avg_price: Int, avg_diff: Int, url: String, chat_id: String)
