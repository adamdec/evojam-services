package com.evojam.invitation.dto

import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.scalatest.{Matchers, WordSpec}

class JsonSerializationSpec extends WordSpec with Matchers {

  val invitee = "Adam Dec"
  val email = "adec@evojam.com"

  "Json Serialization" should {

    "marshall ExceptionResponse" in {
      check(ExceptionResponse("message", "uri", Option("couse")))
    }

    "marshall Invitation" in {
      check(Invitation(invitee, email))
    }

    "create a valid Invitation Json message" in {
      val expected =
        """
          |{
          |  "invitee" : "Adam Dec",
          |  "email" : "adec@evojam.com"
          |}
      """.stripMargin.trim
      val json = Invitation(invitee, email).asJson.toString().trim

      json shouldBe expected
    }

    "marshall Invitations" in {
      check(Invitations(List(Invitation(invitee, email))))
    }

    "create a valid Invitations Json message" in {
      val expected =
        """
          |{
          |  "invitations" : [
          |    {
          |      "invitee" : "Adam Dec",
          |      "email" : "adec@evojam.com"
          |    }
          |  ]
          |}
        """.stripMargin.trim
      val json = Invitations(List(Invitation(invitee, email))).asJson.toString().trim

      json shouldBe expected
    }
  }

  private def check[T](dto: T)(implicit encoder: Encoder[T], decoder: Decoder[T]) =
    encoder(dto).as[T] shouldBe Right(dto)
}
