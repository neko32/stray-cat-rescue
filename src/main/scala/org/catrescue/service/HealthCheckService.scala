package org.catrescue.service

import org.catrescue.model._

import scala.concurrent.Future
import scala.util.Random


trait HealthCheckService {

  def check: Future[Health]
  def deepCheck: Future[Health]
}

class SimpleHealthCheckService extends HealthCheckService {

  override def check = {
    if(Random.nextBoolean) {
      Future.successful(Health("App", Healthy))
    } else {
      Future.successful(Health("App", Unhealthy(Some(List("[DynamoDB] failed to connect", "[SQS] Reached max message num in the queue")))))
    }
  }

  override def deepCheck = check

}
