package com.evojam.invitation.routes

import akka.http.scaladsl.model.StatusCodes.{Forbidden, OK, Unauthorized}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.evojam.invitation.auth.{Roles, User, UserAuthenticator}
import com.evojam.invitation.service.InvitationsService
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

class SwaggerSiteRouteSpec extends WordSpec with Matchers with ScalatestRouteTest with MockFactory {

  private val username = "user"
  private val password = "pass"
  private val allRoles = List(Roles.ManageInvitations.roleName, Roles.Swagger.roleName)
  private val onlySwaggerRole = List(Roles.Swagger.roleName)

  private val authenticator = UserAuthenticator(
    List(User(username, password, allRoles),
         User("swagger", "swagger", onlySwaggerRole),
         User("other", "other", List.empty)))
  private val realm = "realm"

  private val invitationRoute = InvitationRoute(mock[InvitationsService], authenticator, realm).route
  private val invitationSealedRoute = Route.seal(invitationRoute)

  private val swaggerRoute = SwaggerSiteRoute(authenticator, realm).route
  private val swaggerSealedRoute = Route.seal(swaggerRoute)

  private val validCredentials = BasicHttpCredentials(username, password)
  private val swaggerCredentials = BasicHttpCredentials("swagger", "swagger")
  private val credentialsWithoutRoles = BasicHttpCredentials("other", "other")

  "SwaggerSiteRoute" should {

    "return 200 OK" when {

      "/swagger was accessed with all roles" in {
        Get("/swagger") ~> addCredentials(validCredentials) ~> swaggerRoute ~> check {
          status shouldEqual OK
        }
      }

      "/swagger was accessed with 'Swagger' role" in {
        Get("/swagger") ~> addCredentials(swaggerCredentials) ~> swaggerRoute ~> check {
          status shouldEqual OK
        }
      }
    }

    "return 401 Unauthorized" when {

      "/swagger was accessed without authentication" in {

        Get("/swagger") ~> swaggerSealedRoute ~> check {
          status shouldEqual Unauthorized
        }
      }
    }

    "return 403 Forbidden" when {

      "/v1/invitation (POST) was accessed without 'Swagger' role" in {

        Post("/v1/invitation") ~> addCredentials(credentialsWithoutRoles) ~> invitationSealedRoute ~> check {
          status shouldEqual Forbidden
        }
      }

      "/v1/invitation (GET) was accessed without 'Swagger' role" in {

        Get("/v1/invitation") ~> addCredentials(credentialsWithoutRoles) ~> invitationSealedRoute ~> check {
          status shouldEqual Forbidden
        }
      }

      "/swagger was accessed without 'Swagger' role" in {

        Get("/swagger") ~> addCredentials(credentialsWithoutRoles) ~> swaggerSealedRoute ~> check {
          status shouldEqual Forbidden
        }
      }
    }
  }
}
