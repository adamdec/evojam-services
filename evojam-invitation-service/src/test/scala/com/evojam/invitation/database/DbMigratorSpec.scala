package com.evojam.invitation.database

import com.evojam.invitation.config.Configuration
import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

import scala.util.Success

class DbMigratorSpec extends WordSpec with Matchers with BeforeAndAfter {

  // DB_CLOSE_DELAY=-1 (Keep the content of an in-memory database as long as the virtual machine is alive)
  private val config = Configuration(
    ConfigFactory
      .parseString("""
        |evojam.invitations.db.url = "jdbc:h2:mem:invitations_migration;DB_CLOSE_DELAY=-1"
      """.stripMargin)
      .withFallback(ConfigFactory.load()))

  private val dbMigrator = DbMigrator(config.db)

  before {
    cleanDb()
  }

  after {
    cleanDb()
  }

  "DbMigrator" should {

    "successfully migrate the in-memory database" in {
      dbMigrator.migrate() shouldBe Success(())
    }

    "successfully retrieve schema version" when {

      "no migrations were applied" in {
        dbMigrator.version().get shouldBe None
      }

      "migrations were applied" in {
        dbMigrator.migrate().get
        dbMigrator.version().get.get shouldBe "002"
      }
    }
  }

  private def cleanDb() = {
    val flyway = new Flyway()
    flyway.setDataSource(config.db.jdbcUrl, config.db.username.orNull, config.db.password.orNull)
    flyway.setLocations(config.db.migrationDirs: _*)
    flyway.clean()
  }
}
