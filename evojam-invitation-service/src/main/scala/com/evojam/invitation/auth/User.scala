package com.evojam.invitation.auth

import com.evojam.invitation.auth.Roles.Role
import com.evojam.invitation.config.Configuration.UserConfig

case class User(username: String, password: String, roles: List[String]) {

  def hasRole(role: Role): Boolean = roles.contains(role.roleName)
}

object User {

  def apply(userConfig: UserConfig): User =
    User(userConfig.username, userConfig.password, userConfig.roles)
}
