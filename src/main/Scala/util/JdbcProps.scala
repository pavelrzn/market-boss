package com.marketboss
package util


import api.MarketApi


class JdbcProps (usr: String, pass: String, address: String)(implicit api: MarketApi) {

  val driver = "org.postgresql.Driver"
  val jdbcUrl = s"jdbc:postgresql://$address:5432/asus_boss"
  val table = s"markets.${api.tableName}"
  val user = usr
  val password = pass

}
