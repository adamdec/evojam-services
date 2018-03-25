package com.evojam.invitation.auth

import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.directives.Credentials
import org.scalatest.{Matchers, WordSpec}

class UserAuthenticatorSpec extends WordSpec with Matchers {

  private val user1 =
    User("user1", "pass1", List("read_resource", "write_resource"))
  private val user2 = User("user2", "pass2", List.empty)

  private val authenticator = UserAuthenticator(List(user1, user2))

  "UserAuthenticator" should {

    "authenticate user" when {

      "credentials are valid" in {
        authenticator.authenticate(credentials(user1.username, user1.password)) shouldBe Some(user1)
        authenticator.authenticate(credentials(user2.username, user2.password)) shouldBe Some(user2)
      }
    }

    "not authenticate user" when {

      "credentials are missing" in {
        authenticator.authenticate(Credentials.Missing) shouldBe None
      }

      "username is invalid" in {
        authenticator.authenticate(credentials("invalid", user1.password)) shouldBe None
      }

      "password is invalid" in {
        authenticator.authenticate(credentials(user1.username, "invalid")) shouldBe None
      }
    }
  }

  private def credentials(username: String, password: String) =
    Credentials(Some(BasicHttpCredentials(username, password)))
}
