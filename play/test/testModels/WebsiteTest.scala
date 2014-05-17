/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package testModels;

import scala.collection.Set
import org.scalatest._
import org.scalatestplus.play._
import anorm._
import play.api.db.DB
import play.api.Logger
import play.api.Play.current
import org.scalatest.SuiteMixin
import org.scalatest.Suite
import models.Website
import models.Websites

// -----------------------------------------------------------------------------------------------------------
// WEBSITE Domain Class
//
// case class Website(val id: Long, val name: String, val status: Byte, val websiteUrl: String, val agentName: String)
// -----------------------------------------------------------------------------------------------------------

trait TestWebsite extends SuiteMixin { this: Suite =>

  abstract override def withFixture(test: NoArgTest) = {

    // pre test

    try super.withFixture(test) // To be stackable, must call super.withFixture
    finally {

      // post test
      DB.withConnection { implicit c =>
        SQL("DELETE FROM WEBSITES").execute()
      }
    }
  }
}

class WebsiteSpec extends PlaySpec with OneAppPerSuite with TestWebsite {

  def toSet[B](seq: Seq[Option[B]]): Set[B] = (for (Some(b) <- seq) yield b) toSet

  "The Websites object" must {

    "add Website w/o specifying the primary key" in {
      Websites.addWebsite("Smalls Jazz Club", 1, "http://smallsjazzclub.com", "default") match {
        case Some(website) => Websites.getWebsiteById(website.id) mustBe Some(website)
        case None => fail()
      }
    }

    "return all Websites by status via getWebsiteByStatus" in {
      val testA = toSet(Seq(
        Websites.addWebsite("Club 1", 1, "http://club1.com", "default"),
        Websites.addWebsite("Club 2", 1, "http://club2.com", "default"),
        Websites.addWebsite("Club 3", 1, "http://club3.com", "default")))
      testA.size mustBe 3

      val testB = toSet(Seq(
        Websites.addWebsite("Club 4", 0, "http://club4.com", "default"),
        Websites.addWebsite("Club 5", 0, "http://club5.com", "default"),
        Websites.addWebsite("Club 6", 0, "http://club6.com", "default")))
      testB.size mustBe 3

      // test the select
      Websites.getWebsiteByStatus(1).toSet mustBe testA
      Websites.getWebsiteByStatus(0).toSet mustBe testB
    }

    "delete an Website via deleteWebsite" in {
      val optA = Websites.addWebsite("Club 1", 1, "http://club1.com", "default")

      val testA = toSet(Seq(Websites.addWebsite("Club 2", 1, "http://club2.com", "default")))
      testA.size mustBe 1

      // delete OptA
      optA match {
        case Some(website) => Websites.deleteWebsite(website) mustBe true
        case None => fail()
      }

      // read the remaining website
      Websites.getWebsiteByStatus(1).toSet mustBe testA
    }
  }

}
