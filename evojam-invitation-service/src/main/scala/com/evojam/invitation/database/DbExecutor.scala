package com.evojam.invitation.database

import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class DbExecutor(driver: JdbcProfile) {

  import driver.api._

  val db: driver.backend.DatabaseDef = Database.forConfig("evojam.invitations.db")

  implicit class DBIOOps[T](dbio: DBIO[T]) {
    def run: Future[T] = db.run(dbio)
    def runTransactionally: Future[T] = db.run(dbio.transactionally)
  }
}
