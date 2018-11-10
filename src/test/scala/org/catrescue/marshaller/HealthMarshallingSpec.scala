package org.catrescue.marshaller

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.scalatest.{GivenWhenThen, MustMatchers, WordSpec}
import org.catrescue.model._
import spray.json._

trait StringHelper {
  implicit class StringHelper(s: String) {
    def trimDoubleQuote = s.replaceAll("\"", "")
  }
}

class HealthMarshallingSpec extends WordSpec
  with MustMatchers
  with SprayJsonSupport
  with GivenWhenThen
  with StringHelper {

  "HealthMarshalling" should {

    import HealthMarshalling._

    "Marshalling Healthy health works" in {

      Given("Prepare Health object with Healthy status")

      val health = Health("MyTestApp", Healthy)

      When("Convert to Json")
      val json = health.toJson
      println(json.prettyPrint)
      val fields = json.asJsObject.fields
      val status = fields("status")
      val statusFields = status.asJsObject.fields

      Then("Conversion must succeed")
      fields("name").toString.trimDoubleQuote must equal("MyTestApp")
      statusFields("reasons").convertTo[List[String]].head.trimDoubleQuote must equal("Healthy")
      statusFields("value").toString.trimDoubleQuote must equal("Healthy")
    }

    "Unmarshalling Healthy health works" in {
      Given("Prepare Health json")
      val healthJson =
        """
          |{
          |  "name": "SpecT",
          |  "status": {
          |    "value": "Healthy",
          |    "reasons": ["Healthy"]
          |  }
          |}
        """.stripMargin

      When("Convert from Json")
      val jsonObj = healthJson.parseJson
      println(jsonObj.prettyPrint)
      val healthObj = jsonObj.convertTo[Health]

      Then("Conversion must succeed")
      healthObj.name must equal("SpecT")
      healthObj.status match {
        case Healthy => succeed
        case _ => fail("Should be Healthy")
      }
    }


    "Marshalling Unhealthy health works" in {

      Given("Prepare Health object with Unhealthy status")

      val health = Health("MyTestApp3", Unhealthy(Some(List("Reason1", "Reason2"))))

      When("Convert to Json")
      val json = health.toJson
      println(json.prettyPrint)
      val fields = json.asJsObject.fields
      val status = fields("status")
      val statusFields = status.asJsObject.fields

      Then("Conversion must succeed")
      fields("name").toString.trimDoubleQuote must equal("MyTestApp3")
      val reasons = statusFields("reasons").convertTo[List[String]]
      reasons(0) must equal("Reason1")
      reasons(1) must equal("Reason2")
      statusFields("value").toString.trimDoubleQuote must equal("Unhealthy")
    }

    "Unmarshalling Unhealthy health works" in {
      Given("Prepare Health with Unhealthy json")
      val healthJson =
        """
          |{
          |  "name": "SpecT",
          |  "status": {
          |    "value": "Unhealthy",
          |    "reasons": ["Failure1", "Failure2", "Failure3"]
          |  }
          |}
        """.stripMargin

      When("Convert from Json")
      val jsonObj = healthJson.parseJson
      println(jsonObj.prettyPrint)
      val healthObj = jsonObj.convertTo[Health]

      Then("Conversion must succeed")
      healthObj.name must equal("SpecT")
      healthObj.status match {
        case Healthy => fail("Should be Unhealthy")
        case Unhealthy(reasons) =>
          reasons match {
            case None => fail("Reasons should exist")
            case Some(reasonsList) =>
              reasonsList(0) must equal("Failure1")
              reasonsList(1) must equal("Failure2")
              reasonsList(2) must equal("Failure3")
          }
      }
    }
  }
}


