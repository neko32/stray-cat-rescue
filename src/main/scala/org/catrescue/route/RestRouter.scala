package org.catrescue.route

import akka.http.scaladsl.server.Directives.{get, onSuccess, pathPrefix}
import akka.http.scaladsl.server.Route
import org.catrescue.marshaller.HealthMarshalling
import org.catrescue.model.{Healthy, Unhealthy}
import org.catrescue.service.HealthCheckService
import org.catrescue.util.JsonResponseHandling
import spray.json._

class RestRouter(
  healthCheckService: HealthCheckService
) extends HealthMarshalling
with JsonResponseHandling {


  import akka.http.scaladsl.model.StatusCodes._

  // when new handler is added, enhance like below
  // def routes: Route = healthCheckHandler ~ newHandler ~ newHandler2
  def routes: Route = healthCheckHandler

  def healthCheckHandler = {
    pathPrefix("health") {
      get {
        onSuccess(healthCheckService.check) { result =>
          result.status match {
            case Healthy => asJsonResponse(OK, result.toJson.prettyPrint)
            case Unhealthy(_) => asJsonResponse(InternalServerError, result.toJson.prettyPrint)
          }
        }
      }
    }
  }

}
