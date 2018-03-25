package com.evojam.invitation.routes

import com.evojam.invitation.config.Configuration.ApiConfig
import com.evojam.invitationservice.config.BuildInfo
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.{Contact, Info, License}
import io.swagger.models.auth.BasicAuthDefinition
import io.swagger.models.{ExternalDocs, Scheme}

// $COVERAGE-OFF$

case class ApiDocumentationRoutes(apiConfig: ApiConfig) extends SwaggerHttpService {

  override val apiClasses = Set(classOf[HealthCheckRoute], classOf[InvitationRoute])

  override val schemes: List[Scheme] = List(Scheme.valueOf(apiConfig.scheme.toUpperCase()))

  override val info = Info(
    version = s"${BuildInfo.version}, ${BuildInfo.scalaVersion}, ${BuildInfo.sbtVersion}",
    title = BuildInfo.name,
    contact = Some(Contact(name = BuildInfo.team, url = BuildInfo.teamPage, email = BuildInfo.teamEmail)),
    license = BuildInfo.licenses.headOption.map {
      case (name, url) => License(name, url.toString)
    }
  )

  override val externalDocs = Some(new ExternalDocs("Project Core Documentation", BuildInfo.projectUrl))

  override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
  override val unwantedDefinitions: Seq[String] = Seq.empty
}
// $COVERAGE-ON$
