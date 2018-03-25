package com.evojam.invitation.auth

import com.evojam.invitation.config.Configuration.UserConfig
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}

class UserSpec extends WordSpec with Matchers {

  private val username = "username"
  private val password = "password"
  private val allRoles = List(Roles.ManageInvitations.roleName, Roles.Swagger.roleName)
  private val oneRole = List(Roles.ManageInvitations.roleName)

  "User" should {

    "be created from config" in {
      val config =
        s"""
          | {
          |   username = "$username"
          |   password = "$password"
          |   roles = [ "${allRoles.head}", "${allRoles.last}" ]
          | }
        """.stripMargin

      val user = User(UserConfig(ConfigFactory.parseString(config)))

      user.username shouldBe username
      user.password shouldBe password
      user.roles shouldBe allRoles
    }

    "check roles" in {
      val user = User(username, password, oneRole)

      user.hasRole(Roles.ManageInvitations) shouldBe true
      user.hasRole(Roles.Swagger) shouldBe false
    }
  }
}
