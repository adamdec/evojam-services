package com.evojam.invitation.exception

import akka.http.scaladsl.model.StatusCodes

case class NotFoundException(message: String, cause: Option[Throwable] = None)
    extends InvitationException(StatusCodes.NotFound, message, cause)
