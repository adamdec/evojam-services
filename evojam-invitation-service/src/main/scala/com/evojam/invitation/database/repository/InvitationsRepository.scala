package com.evojam.invitation.database.repository

import slick.jdbc.JdbcProfile
import slick.lifted.{Index, ProvenShape}

case class InvitationsRepository(driver: JdbcProfile, schema: Option[String] = None) {

  import InvitationsRepository._
  import driver.api._

  class Invitations(tag: Tag) extends Table[InvitationsEntity](tag, schema, "INVITATIONS") {

    // $COVERAGE-OFF$
    def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    // $COVERAGE-ON$

    def invitee: Rep[String] = column[String]("INVITEE")
    def email: Rep[String] = column[String]("EMAIL")

    def idx: Index = index("INVITATIONS_IDX", (invitee, email))

    def * : ProvenShape[InvitationsEntity] =
      (invitee, email) <> (InvitationsEntity.tupled, InvitationsEntity.unapply)
  }

  val invitationsTable: TableQuery[Invitations] = TableQuery[Invitations]
}

object InvitationsRepository {

  case class InvitationsEntity(invitee: String, email: String)
}
