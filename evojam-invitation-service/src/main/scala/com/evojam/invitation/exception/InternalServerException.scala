package com.evojam.invitation.exception

import akka.http.scaladsl.model.StatusCodes

case class InternalServerException(message: String, cause: Option[Throwable] = None)
    extends InvitationException(StatusCodes.InternalServerError, message, cause)
