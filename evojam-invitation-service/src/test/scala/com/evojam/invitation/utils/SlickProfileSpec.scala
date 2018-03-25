package com.evojam.invitation.utils

import org.scalatest.{Matchers, WordSpec}
import slick.jdbc._

class SlickProfileSpec extends WordSpec with Matchers {

  "SlickProfile" should {

    "return profile for H2 database" in {
      SlickProfile.getProfile("slick.jdbc.H2Profile") shouldBe a[H2Profile]
    }

    "return profile for Oracle database" in {
      SlickProfile.getProfile("slick.jdbc.OracleProfile") shouldBe a[OracleProfile]
    }
  }
}
