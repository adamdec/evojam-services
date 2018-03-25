package com.evojam.invitation.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.evojam.invitation.auth.{Roles, User, UserAuthenticator}
import com.evojam.invitation.dto.{ExceptionResponse, Invitation, Invitations}
import com.evojam.invitation.service.InvitationsService
import io.swagger.annotations._
import javax.ws.rs.Path

import scala.concurrent.ExecutionContext

@Api(value = "/v1", protocols = "http, https", tags = Array("invitations"))
@Path("/v1")
case class InvitationRoute(
    invitationsService: InvitationsService,
    authenticator: UserAuthenticator,
    realm: String
)(implicit ec: ExecutionContext) {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  val route: Route = pathPrefix("v1") {
    authenticateBasic(realm = realm, authenticator.authenticate) { user =>
      getInvitations(user) ~ createInvitation(user)
    }
  }

  @Path("/invitation")
  @ApiOperation(
    value = "Get all invitations (sorted by 'invitee' field).",
    httpMethod = "GET",
    nickname = "getInvitations",
    produces = "application/json;charset=utf-8"
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 200,
                      message = "Returns a list of invitations (sorted by 'invitee' field).",
                      response = classOf[Invitations]),
      new ApiResponse(code = 404, message = "No invitations have been found", response = classOf[ExceptionResponse]),
      new ApiResponse(code = 401, message = "Could not access resource due to security restrictions"),
      new ApiResponse(code = 403, message = "Could not access resource due to security restrictions")
    ))
  def getInvitations: User => Route =
    (user: User) =>
      path("invitation") {
        authorize(user.hasRole(Roles.ManageInvitations)) {
          get {
            onSuccess(invitationsService.getInvitations) { response =>
              complete((StatusCodes.OK, response))
            }
          }
        }
    }

  @Path("/invitation")
  @ApiOperation(
    value = "Create an invitation.",
    httpMethod = "POST",
    nickname = "createInvitation",
    consumes = "application/json;charset=utf-8",
    produces = "text/plain"
  )
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        name = "body",
        value = "Invitation to create",
        required = true,
        dataTypeClass = classOf[Invitation],
        paramType = "body"
      )
    )
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 201, message = "Returns list of invitations", response = classOf[Invitations]),
      new ApiResponse(
        code = 400,
        message =
          "Could not create invitation due to missing or malformed body or input parameters did not pass the validation"),
      new ApiResponse(code = 401, message = "Could not access resource due to security restrictions"),
      new ApiResponse(code = 403, message = "Could not access resource due to security restrictions"),
      new ApiResponse(code = 500, message = "Could not create invitation due to internal server error"),
    ))
  def createInvitation: User => Route =
    (user: User) =>
      path("invitation") {
        authorize(user.hasRole(Roles.ManageInvitations)) {
          post {
            entity(as[Invitation]) { invitation =>
              onSuccess(invitationsService.createInvitation(invitation.invitee, invitation.email)) { _ =>
                complete(StatusCodes.Created)
              }
            }
          }
        }
    }
}
