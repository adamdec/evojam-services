package com.evojam.invitation.dto

import io.circe.generic.JsonCodec

@JsonCodec
case class Invitations(invitations: List[Invitation])

// $COVERAGE-OFF$

object Invitations {

  def apply(invitations: Invitation*): Invitations = Invitations(invitations.toList)
}

// $COVERAGE-ON$
