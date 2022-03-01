package com.marketboss
package util

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object SparkOps {

  def writeDf(df: DataFrame)(implicit jdbcProps: JdbcProps): Unit = {
    df.write
      .mode(SaveMode.Append)
      .option("driver", jdbcProps.driver)
      .format("org.apache.spark.sql.execution.datasources.jdbc.JdbcRelationProvider")
      .option("url", jdbcProps.jdbcUrl)
      .option("dbtable", jdbcProps.table)
      .option("user", jdbcProps.user)
      .option("password", jdbcProps.password)
      .save()
  }

  def readDfFromDb(implicit spark: SparkSession, jdbcProps: JdbcProps): DataFrame = {
    spark.read
      .format("org.apache.spark.sql.execution.datasources.jdbc.JdbcRelationProvider")
      .option("driver", jdbcProps.driver)
      .option("url", jdbcProps.jdbcUrl)
      .option("dbtable", jdbcProps.table)
      .option("user", jdbcProps.user)
      .option("password", jdbcProps.password)
      .load()
    //        .jdbc(jdbcProps.jdbcUrl, jdbcProps.table, jdbcProps.connProps)
  }


}




