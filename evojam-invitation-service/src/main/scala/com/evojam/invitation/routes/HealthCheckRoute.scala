package com.evojam.invitation.routes

import java.lang.management.ManagementFactory
import java.time.Instant.ofEpochMilli
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime.ofInstant
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME

import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.evojam.invitation.database.{DbExecutor, DbMigrator}
import com.evojam.invitationservice.config.BuildInfo
import io.swagger.annotations._
import javax.ws.rs.Path
import org.flywaydb.core.api.FlywayException
import slick.jdbc.OracleProfile

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

@Api(protocols = "http, https", tags = Array("healthcheck"))
case class HealthCheckRoute(dbExecutor: DbExecutor, dbMigrator: DbMigrator)(implicit ec: ExecutionContext) {

  import dbExecutor._
  import dbExecutor.driver.api._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  private val buildInfo = BuildInfo.toMap.mapValues(value => Option(value).map(_.toString).orNull)

  val route: Route = healthCheckRoute ~ simpleHealthCheckRoute

  @Path("/healthcheck")
  @ApiOperation(value = "Check the application and DB health",
                httpMethod = "GET",
                produces = "application/json;charset=utf-8")
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Application is up & running"),
      new ApiResponse(code = 404, message = "Application not found under requested URI"),
      new ApiResponse(code = 500, message = "Application is not running properly")
    ))
  def healthCheckRoute: Route =
    path("healthcheck") {
      get {
        onSuccess(healthCheck) { (code, response) =>
          complete((code, response))
        }
      }
    }

  @Path("/")
  @ApiOperation(value = "Quick check of the application health",
                httpMethod = "GET",
                produces = "application/json;charset=utf-8")
  @ApiResponses(
    Array(
      new ApiResponse(code = 200, message = "Application is up & running"),
      new ApiResponse(code = 404, message = "Application not found under requested URI"),
      new ApiResponse(code = 500, message = "Application is not running properly")
    ))
  def simpleHealthCheckRoute: Route =
    path("") {
      get {
        complete(buildInfo)
      }
    }

  private def healthCheck = {
    val connectivityCheck = healthCheckQuery.as[Int].headOption.run.map(_ => "OK")
    val version = dbMigrator.version().flatMap {
      case Some(v) => Success(v)
      case None    => Failure(new FlywayException("No migrations were applied"))
    }

    connectivityCheck.transform { connectivity =>
      val info = buildInfoMap(connectivity, version)
      val code = (connectivity, version) match {
        case (_: Success[String], _: Success[String]) => OK
        case _                                        => InternalServerError
      }
      Success((code, info))
    }
  }

  private def buildInfoMap(dbConnectivityMessage: Try[String], dbVersion: Try[String]) =
    Map(
      "build" -> buildInfo,
      "database" -> Map(
        "connectivity" -> dbConnectivityMessage.recover {
          case NonFatal(e) => s"Cannot connect to DB: $e"
        }.get,
        "version" -> dbVersion.recover {
          case NonFatal(e) => s"Cannot get DB schema version: $e"
        }.get
      ),
      "metrics" -> Map(
        "uptime" -> uptime.getOrElse("NOT_DEFINED")
      )
    )

  private[routes] def uptime: Option[String] =
    Try(ISO_LOCAL_TIME.format(ofInstant(ofEpochMilli(ManagementFactory.getRuntimeMXBean.getUptime), UTC))).toOption

  private def healthCheckQuery = dbExecutor.driver match {
    case OracleProfile =>
      sql"select 1 from dual"
    case _ =>
      sql"select 1"
  }
}
