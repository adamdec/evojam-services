package com.evojam.invitation.routes

import akka.http.scaladsl.model.{ContentType, HttpCharsets, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.evojam.invitation.config.Configuration
import com.evojam.invitation.database.{DbExecutor, DbMigrator}
import com.evojam.invitation.utils.SlickProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class HealthCheckRouteSpec extends WordSpec with Matchers with ScalatestRouteTest with MockFactory {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds)

  private val config = Configuration()
  private val jdbcProfile: JdbcProfile = SlickProfile.getProfile(config.db.profile)
  private val dbMigrator = mock[MockableDbMigrator]
  private val healtCheckRoute = HealthCheckRoute(DbExecutor(jdbcProfile), dbMigrator)
  private val route = healtCheckRoute.route

  "HealthCheckRoute" should {

    "return formatted elaped time" in {
      val formattedelapsedTime = healtCheckRoute.uptime.get

      formattedelapsedTime should fullyMatch regex """\d{2}:\d{2}:\d{2}.\d{1,}"""
    }

    "return 200 OK" when {

      "/healthcheck GET is invoked" in {
        val version = "1.2.3"
        (dbMigrator.version _)
          .expects()
          .returning(Success(Some(version)))

        Get("/healthcheck") ~> route ~> check {
          status shouldBe StatusCodes.OK
          contentType shouldBe ContentType(MediaTypes.`application/json`)
          charset shouldBe HttpCharsets.`UTF-8`

          val data = responseAs[Map[String, Map[String, String]]]
          data("build") shouldNot be(empty)
          data("database") shouldNot be(empty)
          data("metrics") shouldNot be(empty)

          data("build")("version") shouldNot be(empty)
          data("build")("name") shouldNot be(empty)
          data("database")("connectivity") shouldBe "OK"
          data("database")("version") shouldBe version
          data("metrics")("uptime") shouldNot be(empty)
        }
      }

      "/ GET is invoked" in {
        Get("/") ~> route ~> check {
          status shouldBe StatusCodes.OK
          contentType shouldBe ContentType(MediaTypes.`application/json`)
          charset shouldBe HttpCharsets.`UTF-8`

          val data = responseAs[Map[String, String]]
          data shouldNot be(empty)

          data("version") shouldNot be(empty)
          data("name") shouldNot be(empty)
        }
      }
    }

    "return 500 Internal Server Error for GET request" when {

      "database has not been migrated yet" in {
        (dbMigrator.version _)
          .expects()
          .returning(Success(None))

        Get("/healthcheck") ~> route ~> check {
          status shouldBe StatusCodes.InternalServerError
          contentType shouldBe ContentType(MediaTypes.`application/json`)
          charset shouldBe HttpCharsets.`UTF-8`

          val data = responseAs[Map[String, Map[String, String]]]
          data("build") shouldNot be(empty)
          data("database") shouldNot be(empty)
          data("metrics") shouldNot be(empty)

          data("build")("version") shouldNot be(empty)
          data("build")("name") shouldNot be(empty)
          data("database")("connectivity") shouldBe "OK"
          data("database")("version") should include("No migrations were applied")
          data("metrics")("uptime") shouldNot be(empty)
        }
      }

      "schema version cannot be retrieved" in {
        (dbMigrator.version _)
          .expects()
          .returning(Failure(new Exception("reason")))

        Get("/healthcheck") ~> route ~> check {
          status shouldBe StatusCodes.InternalServerError
          contentType shouldBe ContentType(MediaTypes.`application/json`)
          charset shouldBe HttpCharsets.`UTF-8`

          val data = responseAs[Map[String, Map[String, String]]]
          data("build") shouldNot be(empty)
          data("database") shouldNot be(empty)
          data("metrics") shouldNot be(empty)

          data("build")("version") shouldNot be(empty)
          data("build")("name") shouldNot be(empty)
          data("database")("connectivity") shouldBe "OK"
          data("database")("version") should (include("Cannot get DB schema version") and include("reason"))
          data("metrics")("uptime") shouldNot be(empty)
        }
      }
    }

    "not handle other paths" in {
      Get("/other") ~> route ~> check {
        handled shouldBe false
      }
    }

    "not handle requests other than GET" in {
      Post("/healthcheck") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
      }
      Put("/healthcheck") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
      }
      Delete("/healthcheck") ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.MethodNotAllowed
      }
    }
  }

  private class MockableDbMigrator extends DbMigrator(config.db)
}
