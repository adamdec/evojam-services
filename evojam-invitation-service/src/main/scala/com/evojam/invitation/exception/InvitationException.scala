package com.evojam.invitation.exception

import akka.http.scaladsl.model.StatusCode
import com.evojam.invitation.dto.ExceptionResponse

abstract class InvitationException(val code: StatusCode, message: String, cause: Option[Throwable]) extends Exception {

  def toResponse(uri: String): ExceptionResponse =
    ExceptionResponse(message, uri, cause.map(_.getMessage))
}
