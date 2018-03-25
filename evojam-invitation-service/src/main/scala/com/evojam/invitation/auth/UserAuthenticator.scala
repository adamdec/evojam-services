package com.evojam.invitation.auth

import akka.http.scaladsl.server.directives.Credentials

case class UserAuthenticator(users: List[User]) {

  def authenticate(credentials: Credentials): Option[User] =
    credentials match {
      case providedCredentials @ Credentials.Provided(username) =>
        users.find(_.username == username).flatMap { user =>
          if (providedCredentials.verify(user.password)) {
            Some(user)
          } else {
            None
          }
        }
      case _ => None
    }
}
