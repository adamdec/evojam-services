package com.evojam.invitation.service

import com.evojam.invitation.database.DbExecutor
import com.evojam.invitation.database.repository.InvitationsRepository
import com.evojam.invitation.dto.{Invitation, Invitations}
import com.evojam.invitation.exception.{InternalServerException, NotFoundException}
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class InvitationsServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfter {

  private val jdbcProfile = slick.jdbc.H2Profile
  private val executor = DbExecutor(jdbcProfile)
  private val invitationsRepository = InvitationsRepository(jdbcProfile)
  private val service = InvitationsService(invitationsRepository, executor)

  import executor._
  import invitationsRepository._
  import jdbcProfile.api._

  val invitee1 = "Ula Kalinowska"
  val email1 = "ukalinowska@evojam.com"

  val invitee2 = "Adam Dec"
  val email2 = "adec@evojam.com"

  before {
    Await.result(invitationsTable.schema.create.run, 10.second)
  }

  after {
    Await.result(invitationsTable.schema.drop.run, 10.second)
  }

  "InvitationsService" should {

    "create and return sorted invitations" in {
      val invitations = Invitations(Invitation(invitee2, email2), Invitation(invitee1, email1))

      for {
        _ <- service.createInvitation(invitee1, email1)
        _ <- service.createInvitation(invitee2, email2)
        r <- service.getInvitations
      } yield r shouldBe invitations
    }

    "throw an exception" when {

      "there are no invitations" in {
        recoverToSucceededIf[NotFoundException] {
          service.getInvitations
        }
      }

      "there is an internal server error" in {
        recoverToSucceededIf[InternalServerException] {
          service.createInvitation(null, email1)
        }
      }
    }
  }
}
