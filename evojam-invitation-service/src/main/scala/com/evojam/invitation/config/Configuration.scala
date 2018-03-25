package com.evojam.invitation.config

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._
import scala.util.Try

case class Configuration(config: Config = ConfigFactory.load()) {

  import Configuration._

  private lazy val root = config.getConfig("evojam.invitations")

  lazy val api: ApiConfig = ApiConfig(root.getConfig("api"))
  lazy val db: DbConfig = DbConfig(root.getConfig("db"))
}

object Configuration {

  case class ApiConfig(config: Config) {

    lazy val scheme: String = if (ssl.enabled) "https" else "http"
    lazy val host: String = config.getString("host")
    lazy val port: Int = config.getInt("port")
    lazy val ssl: SslConfig = SslConfig(config.getConfig("ssl"))
    lazy val realm: String = config.getString("realm")
    lazy val users: List[UserConfig] = config.getConfigList("users").asScala.toList.map(UserConfig)
  }

  case class SslConfig(config: Config) {

    lazy val enabled: Boolean = config.getBoolean("enabled")
    lazy val certificate: String = config.getString("certificate")
    lazy val password: String = config.getString("password")
  }

  case class DbConfig(config: Config) {

    lazy val jdbcUrl: String = config.getString("url")
    lazy val profile: String = config.getString("profile")
    lazy val username: Option[String] = config.getOptionalString("user")
    lazy val password: Option[String] = config.getOptionalString("password")
    lazy val schema: Option[String] = config.getOptionalString("schema")
    lazy val driver: Option[String] = config.getOptionalString("driver")
    lazy val migrationDirs: Seq[String] = config.getStringList("migration-dirs").asScala
  }

  case class UserConfig(config: Config) {

    lazy val username: String = config.getString("username")
    lazy val password: String = config.getString("password")
    lazy val roles: List[String] = config.getStringList("roles").asScala.toList
  }

  implicit class ConfigOps(config: Config) {

    def getOptionalString(key: String): Option[String] =
      Try(Option(config.getString(key)))
        .getOrElse(None)
        .flatMap(value => if (StringUtils.isEmpty(value)) None else Some(value))
  }
}
