package com.evojam.invitation.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import com.evojam.invitation.auth.{Roles, User, UserAuthenticator}
import com.evojam.invitation.dto.{ExceptionResponse, Invitation, Invitations}
import com.evojam.invitation.exception.{InternalServerException, NotFoundException}
import com.evojam.invitation.service.InvitationsService
import com.evojam.invitation.utils.{CustomExceptionHandler, CustomRejectionHandler}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class InvitationsRouteSpec extends WordSpec with Matchers with ScalatestRouteTest with MockFactory {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  private val username = "user"
  private val password = "pass"

  private val authenticator = UserAuthenticator(
    List(
      User(username, password, List(Roles.ManageInvitations).map(_.roleName)),
      User("other", "other", List.empty)
    ))
  private val realm = "realm"
  private val service = mock[InvitationsService]

  private implicit val customExceptionHandler: ExceptionHandler = CustomExceptionHandler()
  private implicit val customRejectionHandler: RejectionHandler = CustomRejectionHandler()

  private val route = InvitationRoute(service, authenticator, realm).route
  private val sealedRoute = Route.seal(route)

  private val validCredentials = BasicHttpCredentials(username, password)
  private val invalidUsername = BasicHttpCredentials("invalid", password)
  private val invalidPassword = BasicHttpCredentials(username, "invalid")
  private val credentialsWithoutRoles = BasicHttpCredentials("other", "other")

  val invitee = "Adam Dec"
  val email = "adec@evojam.com"

  val invitations = Invitations(Invitation(invitee, email))

  "InvitationsRoute" should {

    "return 200 OK" when {

      "GET request to /invitation is made" in {
        (service.getInvitations _)
          .expects()
          .returning(Future.successful(invitations))

        Get("/v1/invitation") ~> addCredentials(validCredentials) ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.OK
          contentType shouldBe ContentType(MediaTypes.`application/json`)
          charset shouldBe HttpCharsets.`UTF-8`
          responseAs[Invitations] shouldBe invitations
        }
      }
    }

    "return 201 Created" when {

      "POST request to /invitation is made" in {
        (service.createInvitation _)
          .expects(invitee, email)
          .returning(Future.successful(1))

        Post("/v1/invitation", Invitation(invitee, email)) ~> addCredentials(validCredentials) ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.Created
          contentType shouldBe ContentTypes.`text/plain(UTF-8)`
          charset shouldBe HttpCharsets.`UTF-8`
        }
      }
    }

    "return 400 BadRequest" when {

      "requested body is missing" in {
        Post("/v1/invitation", jsonBody("")) ~> addCredentials(validCredentials) ~> sealedRoute ~> check {

          status shouldEqual StatusCodes.BadRequest
        }
      }

      "requested body is malformed" in {
        Post("/v1/invitation", jsonBody(s"""{}""")) ~> addCredentials(validCredentials) ~> sealedRoute ~> check {

          status shouldEqual StatusCodes.BadRequest
        }
      }

      "requested invitee is empty" in {
        Post("/v1/invitation", jsonBody(s"""{"invitee" : "", "email" : "$email"}""")) ~> addCredentials(
          validCredentials) ~> sealedRoute ~> check {

          status shouldEqual StatusCodes.BadRequest
          responseAs[ExceptionResponse].message should include("Invitee name must not be empty")
        }
      }

      "requested email is empty" in {
        Post("/v1/invitation", jsonBody(s"""{"invitee" : "$invitee", "email" : ""}""")) ~> addCredentials(
          validCredentials) ~> sealedRoute ~> check {

          status shouldEqual StatusCodes.BadRequest
          responseAs[ExceptionResponse].message should include("Invitee email must not be empty")
        }
      }

      "requested email is not valid" in {
        Post("/v1/invitation", jsonBody(s"""{"invitee" : "$invitee", "email" : "BLA"}""")) ~> addCredentials(
          validCredentials) ~> sealedRoute ~> check {

          status shouldEqual StatusCodes.BadRequest
          responseAs[ExceptionResponse].message should include("Invitee email must be valid")
        }
      }
    }

    "return 401 Unauthorized" when {

      "credentials are not provided" in {
        Get("/v1/invitation") ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.Unauthorized
        }

        Post("/v1/invitation", validRequest) ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.Unauthorized
        }
      }

      "username is invalid" in {

        Get("/v1/invitation") ~> addCredentials(invalidUsername) ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.Unauthorized
        }

        Post("/v1/invitation", validRequest) ~> addCredentials(invalidUsername) ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.Unauthorized
        }
      }

      "password is invalid" in {
        Get("/v1/invitation") ~> addCredentials(invalidPassword) ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.Unauthorized
        }

        Post("/v1/invitation", validRequest) ~> addCredentials(invalidPassword) ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.Unauthorized
        }
      }
    }

    "return 403 Forbidden" when {

      "user does not have required role" in {

        Get("/v1/invitation") ~>
          addCredentials(credentialsWithoutRoles) ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.Forbidden
        }

        Post("/v1/invitation", validRequest) ~>
          addCredentials(credentialsWithoutRoles) ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.Forbidden
        }
      }
    }

    "return 404 Not Found" when {

      "GET request to /invitation is made" in {
        (service.getInvitations _)
          .expects()
          .returning(Future.failed(NotFoundException("No invitations were found in DB")))

        Get("/v1/invitation") ~>
          addCredentials(validCredentials) ~> sealedRoute ~> check {

          status shouldEqual StatusCodes.NotFound
        }
      }
    }

    "return 500 Internal Server Error" when {

      "POST request to /invitation is made" in {
        (service.createInvitation _)
          .expects(*, *)
          .returning(Future.failed(InternalServerException("No invitations were found in DB")))

        Post("/v1/invitation", Invitation(invitee, email)) ~> addCredentials(validCredentials) ~> sealedRoute ~> check {
          status shouldEqual StatusCodes.InternalServerError
          contentType shouldBe ContentType(MediaTypes.`application/json`)
          charset shouldBe HttpCharsets.`UTF-8`
        }
      }
    }

    "not handle other paths" in {
      Get("/v1/other") ~> route ~> check {
        handled shouldBe false
      }
    }
  }

  private val validRequest = jsonBody("""{"invitee" : "Adam Dec", "email" : "adec@evojam.com"}""")

  private def jsonBody(content: String) =
    HttpEntity.Strict(ContentTypes.`application/json`, ByteString(content))
}
