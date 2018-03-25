package com.evojam.invitation.utils

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.{RejectionHandler, ValidationRejection}
import com.evojam.invitation.dto.ExceptionResponse

object CustomRejectionHandler {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  def apply(): RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case e: ValidationRejection =>
          extractUri { uri =>
            complete((StatusCodes.BadRequest, ExceptionResponse(e.message, uri.toString, e.cause.map(_.getMessage))))
          }
      }
      .result()
}
