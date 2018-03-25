package com.evojam.invitation.dto

import org.scalatest.{Matchers, WordSpec}

class ValidationSpec extends WordSpec with Matchers {

  "Validation" should {

    "pass" when {

      "Invitation is valid" in {
        Invitation("Adam Dec", "adam.dec@evojam.com")
      }
    }

    "fail" when {

      "Invitation name is null" in {
        an[IllegalArgumentException] should be thrownBy {
          Invitation(null, "adam.dec@evojam.com")
        }
      }

      "Invitation name is empty" in {
        an[IllegalArgumentException] should be thrownBy {
          Invitation("", "adam.dec@evojam.com")
        }
      }

      "Invitation email is null" in {
        an[IllegalArgumentException] should be thrownBy {
          Invitation("Adam Dec", null)
        }
      }

      "Invitation email is empty" in {
        an[IllegalArgumentException] should be thrownBy {
          Invitation("Adam Dec", "")
        }
      }

      "Invitation email is invalid" in {
        an[IllegalArgumentException] should be thrownBy {
          Invitation("Adam Dec", "adam.dec")
        }
      }
    }
  }
}
