package com.evojam.invitation.utils

import slick.jdbc.JdbcProfile

object SlickProfile {

  def getProfile(profileClass: String): JdbcProfile =
    Class.forName(profileClass + "$").getField("MODULE$").get(null).asInstanceOf[JdbcProfile]
}
