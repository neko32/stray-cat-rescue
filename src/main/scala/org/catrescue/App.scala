package org.catrescue

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import org.catrescue.route.RestRouter
import org.catrescue.service.{HealthCheckService, SimpleHealthCheckService}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object App extends Config {


  implicit val cfg = appConfig

  def main(args: Array[String]):Unit = {

    implicit val sys = ActorSystem(cfg.getString("app.akka.systemName"))
    implicit val ec = sys.dispatcher
    implicit val mat = ActorMaterializer()

    // configs
    val appName = cfg.getString("app.akka.systemName")
    val (host, port) = (cfg.getString("app.host"), cfg.getInt("app.port"))

    // prepare services
    val healthCheckService: HealthCheckService = new SimpleHealthCheckService()
    // DI - injecting services prepared
    val api = new RestRouter(healthCheckService)

    val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(api.routes, host, port)
    val log = Logging(sys.eventStream, appName)
    bindingFuture.map { serverBinding =>
      log.info(s"REST API is bound to ${serverBinding.localAddress}")
    }.onComplete {
      case Success(_) => log.info(s"Success to bind")
      case Failure(ex) =>
        log.error(ex, "failed to bind")
        sys.terminate()
    }
  }

}
