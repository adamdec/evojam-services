package com.evojam.invitation.config

import org.scalatest.{Matchers, WordSpec}

class ConfigurationSpec extends WordSpec with Matchers {

  private val configuration = Configuration()

  "Configuration" should {

    "be properly read" in {
      configuration.api shouldNot be(null)
      configuration.api.host shouldBe "0.0.0.0"
      configuration.api.port shouldBe 8080

      configuration.api.ssl shouldNot be(null)
      configuration.api.ssl.enabled shouldBe false
      configuration.api.ssl.certificate shouldBe ""
      configuration.api.ssl.password shouldBe ""

      configuration.api.realm shouldBe "Invitation Service TEST"

      configuration.db shouldNot be(null)

      configuration.db.username shouldBe None
      configuration.db.password shouldBe None
      configuration.db.schema shouldBe None
      configuration.db.driver shouldBe Some("org.h2.Driver")

      configuration.api.users shouldNot be(null)
      configuration.api.users.length shouldBe 3
    }
  }
}
