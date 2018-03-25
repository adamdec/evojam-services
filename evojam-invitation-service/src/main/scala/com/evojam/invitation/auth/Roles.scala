package com.evojam.invitation.auth

object Roles {

  sealed abstract class Role(val roleName: String)

  case object ManageInvitations extends Role("MANAGE_INVITATIONS")
  case object Swagger extends Role("SWAGGER")
}
