package com.evojam.invitation.utils

import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.server.Directives.{extractRequestContext, mapRouteResult}
import akka.http.scaladsl.server.{Directive0, RouteResult}
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import com.typesafe.scalalogging.LazyLogging

// $COVERAGE-OFF$

object Logging extends LazyLogging {

  val logRequests: Directive0 =
    extractRequestContext.flatMap { ctx =>
      mapRouteResult { result =>
        logger.debug(s"${ctx.request.method.value} ${ctx.request.uri.path} - ${logMessage(ctx.request.entity, result)}")
        result
      }
    }

  private def logMessage(requestEntity: RequestEntity, result: RouteResult): String = result match {
    case Complete(response) =>
      s"${response.status}\n" +
        s"  Request:  $requestEntity\n" +
        s"  Response: ${response.entity}"
    case Rejected(_) =>
      s"Rejected\n" +
        s"  Request:  $requestEntity"
  }
}

// $COVERAGE-ON$
