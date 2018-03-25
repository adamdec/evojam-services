package com.evojam.invitation.dto

import io.circe.generic.JsonCodec

@JsonCodec
case class ExceptionResponse(message: String, uri: String, cause: Option[String])
