import sbt._

object Version {

  val akka = "10.1.0"
  val akkaHttpCirce = "1.20.0"
  val swaggerAkkaHttp = "0.14.0"
  val swaggerAkkaHttpUI = "1.1.0"

  val circe = "0.9.2"
  val config = "1.3.3"
  val commonsValidator = "1.6"
  val commonsLang3 = "3.7"

  val scalaLogging = "3.8.0"
  val slf4j = "1.7.25"

  val slick = "3.2.2"
  val flyway = "5.0.7"
  val oracle = "11.2.0.4"
  val h2 = "1.4.197"
  val paradise = "2.1.1"

  val scalatest = "3.0.5"
  val scalamock = "3.6.0"
}

object Dependencies {

  val akka = (name: String) => "com.typesafe.akka" %% s"akka-$name" % Version.akka
  val circe = (name: String) => "io.circe" %% s"circe-$name" % Version.circe

  val akkaHttp = akka("http")
  val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpCirce
  val swaggerAkkaHttp = "com.github.swagger-akka-http" %% "swagger-akka-http" % Version.swaggerAkkaHttp
  val swaggerAkkaHttpUI = "co.pragmati"%% "swagger-ui-akka-http" % Version.swaggerAkkaHttpUI

  val circeGeneric = circe("generic")
  val config = "com.typesafe" % "config" % Version.config
  val commonsValidator = "commons-validator" % "commons-validator" % Version.commonsValidator
  val commonsLang3 = "org.apache.commons" % "commons-lang3" % Version.commonsLang3

  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % Version.scalaLogging
  val slf4j = "org.slf4j" % "slf4j-log4j12" % Version.slf4j

  val slick = "com.typesafe.slick" %% "slick" % Version.slick
  val slickHikariCP = "com.typesafe.slick" %% "slick-hikaricp" % Version.slick
  val flyway = "org.flywaydb" % "flyway-core" % Version.flyway
  val oracle = "com.oracle" % "ojdbc6" % Version.oracle
  val h2 = "com.h2database" % "h2" % Version.h2
  val macroParadise = compilerPlugin("org.scalamacros" % "paradise" % Version.paradise cross CrossVersion.full)

  val scalatest = "org.scalatest" %% "scalatest" % Version.scalatest % Test
  val scalamock = "org.scalamock" %% "scalamock-scalatest-support" % Version.scalamock % Test
  val akkaHttpTestkit = akka("http-testkit") % Test

  val invitationApi = Seq(
    circeGeneric,
    macroParadise,

    commonsValidator,
    commonsLang3,

    scalatest
  )

  val invitationService = Seq(
    akkaHttp,
    akkaHttpCirce,
    swaggerAkkaHttp,
    swaggerAkkaHttpUI,
    circeGeneric,
    config,

    scalaLogging,
    slf4j,

    slick,
    slickHikariCP,
    flyway,
    oracle,
    h2,

    macroParadise,

    akkaHttpTestkit,
    scalatest,
    scalamock
  )
}