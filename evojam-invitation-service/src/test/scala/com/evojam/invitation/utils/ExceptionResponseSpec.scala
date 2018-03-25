package com.evojam.invitation.utils

import akka.http.scaladsl.model.StatusCodes
import com.evojam.invitation.dto.ExceptionResponse
import com.evojam.invitation.exception.{InternalServerException, NotFoundException}
import org.scalatest.{Matchers, WordSpec}

class ExceptionResponseSpec extends WordSpec with Matchers {

  private val cause = new Exception("cause")

  "ExceptionResponse" should {

    "convert InternalServerException" in {
      val e = InternalServerException("message")

      e.code shouldBe StatusCodes.InternalServerError
      e.toResponse("uri") shouldBe ExceptionResponse("message", "uri", None)
    }

    "convert InternalServerException with cause" in {
      val e = InternalServerException("message", Some(cause))

      e.code shouldBe StatusCodes.InternalServerError
      e.toResponse("uri") shouldBe ExceptionResponse("message", "uri", Some("cause"))
    }

    "convert NotFoundException" in {
      val e = NotFoundException("message")

      e.code shouldBe StatusCodes.NotFound
      e.toResponse("uri") shouldBe ExceptionResponse("message", "uri", None)
    }

    "convert NotFoundException with cause" in {
      val e = NotFoundException("message", Some(cause))

      e.code shouldBe StatusCodes.NotFound
      e.toResponse("uri") shouldBe ExceptionResponse("message", "uri", Some("cause"))
    }
  }
}
