package com.evojam.invitation.service

import com.evojam.invitation.database.DbExecutor
import com.evojam.invitation.database.repository.InvitationsRepository
import com.evojam.invitation.database.repository.InvitationsRepository.InvitationsEntity
import com.evojam.invitation.dto.{Invitation, Invitations}
import com.evojam.invitation.exception.{InternalServerException, NotFoundException}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

case class InvitationsService(invitationsRepository: InvitationsRepository, dbExecutor: DbExecutor)(
    implicit ec: ExecutionContext) {

  import dbExecutor._
  import driver.api._
  import invitationsRepository.invitationsTable

  def getInvitations: Future[Invitations] =
    for {
      invitations <- invitationsTable
        .sortBy(_.invitee)
        .result
        .run

      // We could return empty list rather then 404
      _ = if (invitations.isEmpty)
        throw NotFoundException("No invitations were found in DB")

    } yield Invitations(invitations.map(i => Invitation(i.invitee, i.email)).toList)

  def createInvitation(invitee: String, email: String): Future[Int] = {
    val createInternalError = InternalServerException(
      s"Invitation was not created for invitee: $invitee, email: $email")

    for {
      invitation <- (invitationsTable += InvitationsEntity(invitee, email)).runTransactionally
        .recover {
          case NonFatal(e) => throw createInternalError.copy(cause = Option(e))
        }

      _ = if (invitation != 1) throw createInternalError

    } yield invitation
  }
}
