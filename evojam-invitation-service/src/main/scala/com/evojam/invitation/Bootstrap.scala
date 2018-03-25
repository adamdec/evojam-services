package com.evojam.invitation

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.evojam.invitation.auth.{User, UserAuthenticator}
import com.evojam.invitation.config.Configuration
import com.evojam.invitation.config.Configuration.ApiConfig
import com.evojam.invitation.database.repository.InvitationsRepository
import com.evojam.invitation.database.{DbExecutor, DbMigrator}
import com.evojam.invitation.routes.{ApiDocumentationRoutes, HealthCheckRoute, InvitationRoute, SwaggerSiteRoute}
import com.evojam.invitation.service.InvitationsService
import com.evojam.invitation.utils._
import com.evojam.invitationservice.config.BuildInfo
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.JdbcProfile

import scala.concurrent.Future.fromTry
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

// $COVERAGE-OFF$

object Bootstrap extends LazyLogging {

  val ServiceName = "evojam-invitation-service"
  private implicit val system: ActorSystem = ActorSystem(ServiceName)
  private implicit val executionContext: ExecutionContext = system.dispatcher

  private val config = Configuration()
  private val authenticator = UserAuthenticator(config.api.users.map(User.apply))
  private val jdbcProfile: JdbcProfile = SlickProfile.getProfile(config.db.profile)
  private val dbMigrator = DbMigrator(config.db)
  private val dbExecutor = DbExecutor(jdbcProfile)

  private val routes: Route =
    handleRejections(CustomRejectionHandler()) {
      handleExceptions(CustomExceptionHandler()) {
        SwaggerSiteRoute(authenticator, config.api.realm).route ~
          HealthCheckRoute(dbExecutor, dbMigrator).route ~
          InvitationRoute(
            InvitationsService(InvitationsRepository(jdbcProfile, config.db.schema), dbExecutor),
            authenticator,
            config.api.realm
          ).route ~
          ApiDocumentationRoutes(config.api).routes
      }
    }

  def main(args: Array[String]): Unit = {
    (for {
      _ <- fromTry(dbMigrator.migrate())
      _ <- bind(routes, config.api)
      _ <- system.whenTerminated
    } yield ()).recover {
      case NonFatal(ex) =>
        logger.error("Error during application startup", ex)
        system.terminate()
    }
    ()
  }

  def bind(route: Route, config: ApiConfig)(implicit system: ActorSystem): Future[Unit] = {
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    HttpsContext(config.ssl).enableConditionally

    val bindingFuture = Http().bindAndHandle(Logging.logRequests(route), config.host, config.port)

    bindingFuture.onComplete {
      case Success(_) =>
        logger.debug(
          s"$ServiceName [${BuildInfo.version}] started at ${config.scheme}://${config.host}:${config.port}/")
      case Failure(ex) =>
        logger.error(s"Failed to bind to ${config.scheme}://${config.host}:${config.port}/", ex)
        system.terminate()
    }

    bindingFuture.map(_ => ())
  }
}

// $COVERAGE-ON$
