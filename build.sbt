name := "sbermegamarket"

version := "0.2"

scalaVersion := "2.12.14"

idePackagePrefix := Some("com.marketboss")

// set the main class for 'sbt run'
mainClass in (Compile, run) := Some("com.marketboss.SbMM")

val sparkVersion = "2.4.0"
val circeVersion = "0.14.1"
val postgresVersion = "42.2.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion
  , "org.apache.spark" %% "spark-core" % sparkVersion
  , "org.postgresql" % "postgresql" % postgresVersion
  , "com.softwaremill.sttp.client3" %% "core" % "3.3.18"
)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)


assembly / assemblyMergeStrategy := {
  case "META-INF/services/org.apache.spark.sql.sources.DataSourceRegister" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}