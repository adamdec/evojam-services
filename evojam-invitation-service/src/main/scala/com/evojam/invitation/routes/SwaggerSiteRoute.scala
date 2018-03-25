package com.evojam.invitation.routes

import akka.http.scaladsl.server.Directives
import com.evojam.invitation.auth.{Roles, UserAuthenticator}

case class SwaggerSiteRoute(authenticator: UserAuthenticator, realm: String) extends Directives {

  val SwaggerUIDirectory = "swagger-ui"

  val route = path("swagger") {
    authenticateBasic(realm = realm, authenticator.authenticate) { user =>
      authorize(user.hasRole(Roles.Swagger)) {
        getFromResource(s"$SwaggerUIDirectory/index.html")
      }
    }
  } ~ getFromResourceDirectory(SwaggerUIDirectory)
}
