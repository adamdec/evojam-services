package com.evojam.invitation.utils

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.ExceptionHandler
import com.evojam.invitation.dto.ExceptionResponse
import com.evojam.invitation.exception.InvitationException

import scala.util.control.NonFatal

object CustomExceptionHandler {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  def apply(): ExceptionHandler = ExceptionHandler {
    case e: InvitationException =>
      extractUri { uri =>
        complete((e.code, e.toResponse(uri.toString)))
      }
    case NonFatal(e) =>
      extractUri { uri =>
        complete(
          (StatusCodes.InternalServerError,
           ExceptionResponse(e.toString, uri.toString, Option(e.getCause).map(_.getMessage))))
      }
  }
}
