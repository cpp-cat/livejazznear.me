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
import org.joda.time.DateTime
import models.Source
import models.Sources

// -----------------------------------------------------------------------------------------------------------
// SOURCE Domain Class
// -----------------------------------------------------------------------------------------------------------

trait TestSource extends SuiteMixin { this: Suite =>

  abstract override def withFixture(test: NoArgTest) = {

    // pre test

    try super.withFixture(test) // To be stackable, must call super.withFixture
    finally {

      // post test
      DB.withConnection { implicit c =>
        SQL("DELETE FROM SOURCES").execute()
      }
    }
  }
}

class SourceSpec extends PlaySpec with OneAppPerSuite with TestSource {

  def toSet[B](seq: Seq[Option[B]]): Set[B] = (for (Some(b) <- seq) yield b) toSet

  "The Sources object" must {

    "add Source w/o specifying the primary key" in {
      val createDate = new DateTime(2014, 5, 12, 23, 59)
      Sources.addSource(1, createDate, Some("remote ID")) match {
        case Some(source) => Sources.getSourceById(source.id) mustBe Some(source)
        case None => fail()
      }
    }

    "return all Sources via getSourceByWebsiteId" in {
      val createDate = new DateTime(2014, 5, 12, 23, 59)
      val testA = toSet(Seq(
    		  Sources.addSource(1, createDate, Some("123456")),
    		  Sources.addSource(1, createDate.plusDays(1), Some("123456")),
    		  Sources.addSource(1, createDate.plusDays(2), Some("123456"))))
      testA.size mustBe 3

      val testB = toSet(Seq(
    		  Sources.addSource(2, createDate, None),
    		  Sources.addSource(2, createDate.plusDays(1), None),
    		  Sources.addSource(2, createDate.plusDays(2), None)))
      testB.size mustBe 3
 
      // test the select
      Sources.getSourceByWebsiteId(1, Some("123456")).toSet mustBe testA
      Sources.getSourceByWebsiteId(2, None).toSet mustBe testB
    }

    "delete an Source via deleteSource" in {
      val createDate = new DateTime(2014, 5, 12, 23, 59)
      val optA = Sources.addSource(2, createDate, None)
      val testA = toSet(Seq(Sources.addSource(2, createDate.plusDays(1), None)))
      testA.size mustBe 1

      // delete OptA
      optA match {
        case Some(source) => Sources.deleteSource(source) mustBe true
        case None => fail()
      }

      // read the remaining source
      Sources.getSourceByWebsiteId(2, None).toSet mustBe testA
    }
  }

}
