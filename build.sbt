ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "DiscountCalculator"
  )
libraryDependencies ++= Seq(
  "org.apache.logging.log4j" %% "log4j-api-scala" % "13.1.0"
)
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.20"