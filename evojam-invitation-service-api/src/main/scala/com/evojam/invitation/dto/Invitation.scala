package com.evojam.invitation.dto

import io.circe.generic.JsonCodec
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.EmailValidator

@JsonCodec
case class Invitation(invitee: String, email: String) {
  require(StringUtils.isNotEmpty(invitee), "Invitee name must not be empty")
  require(StringUtils.isNotEmpty(email), "Invitee email must not be empty")
  require(EmailValidator.getInstance().isValid(email), "Invitee email must be valid")
}
