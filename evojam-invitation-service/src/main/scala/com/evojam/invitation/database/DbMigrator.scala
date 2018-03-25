package com.evojam.invitation.database

import com.evojam.invitation.config.Configuration.DbConfig
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway

import scala.util.Try
import scala.util.control.NonFatal

case class DbMigrator(config: DbConfig) extends LazyLogging {

  private val flyway = new Flyway()
  flyway.setDataSource(config.jdbcUrl, config.username.orNull, config.password.orNull)
  flyway.setLocations(config.migrationDirs: _*)
  flyway.setCleanDisabled(true)
  config.schema.foreach(flyway.setSchemas(_))

  def migrate(): Try[Unit] =
    Try {
      flyway.migrate()
    }.recover {
        case NonFatal(ex) =>
          logger.error("Flyway migration failed, doing a repair and retrying ...", ex)
          flyway.repair()
          flyway.migrate()
      }
      .map(_ => ())

  def version(): Try[Option[String]] = Try(Option(flyway.info().current()).map(_.getVersion.toString))
}
