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
import models.Events
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import models.Event
import scala.language.postfixOps

// -----------------------------------------------------------------------------------------------------------
// EVENT Domain Class
// -----------------------------------------------------------------------------------------------------------

trait TestEvent extends SuiteMixin { this: Suite =>

  abstract override def withFixture(test: NoArgTest) = {

    // pre test

    try super.withFixture(test) // To be stackable, must call super.withFixture
    finally {

      // post test
      DB.withConnection { implicit c =>
        SQL("DELETE FROM EVENTS").execute()
      }
    }
  }
}

class EventSpec extends PlaySpec with OneAppPerSuite with TestEvent {

  def toSet[B](seq: Seq[Option[B]]): Set[B] = (for (Some(b) <- seq) yield b) toSet

  "The Events object" must {

    "add Event by specifying the primary key" in {
      val startDate = new LocalDate(2014, 5, 9)
      val endDate = new LocalDate(2014, 5, 30)
      val startTime: Long = 21 * 60
      val endTime: Long = 1 * 60
      Events.addEvent(1, 1, 1, 2, 3, startDate, endDate, Some(startTime), Some(endTime)) mustBe Some(Event(1, 1, 1, 2, 3, startDate, endDate, Some(startTime), Some(endTime)))
    }

    "add Event w/o specifying the primary key" in {
      val startDate = new LocalDate(2014, 5, 9)
      val endDate = new LocalDate(2014, 5, 30)
      Events.addEvent(1, 1, 2, 3, startDate, endDate, Some(21 * 60), Some(1 * 60)) match {
        case Some(event) => Events.getEventById(event.id) mustBe Some(event)
        case None => Some("failed insert") mustBe Some("None")
      }
    }

    "return all Events via getAllEvents" in {
      val testA = toSet(Seq(
        Events.addEvent(1, 1, 2, 3, new LocalDate(2014, 5, 9), new LocalDate(2014, 5, 30), None, None),
        Events.addEvent(1, 1, 2, 3, new LocalDate(2014, 5, 11), new LocalDate(2014, 6, 9), None, None)))
      testA.size mustBe 2

      val result = Events.getAllEvents().toSet

      result mustBe testA
    }

    "delete an Event via deleteEvent" in {
      val optA = Events.addEvent(1, 1, 2, 3, new LocalDate(2014, 5, 9), new LocalDate(2014, 5, 30), None, None)
      val testA = toSet(Seq(
        Events.addEvent(1, 1, 2, 3, new LocalDate(2014, 5, 11), new LocalDate(2014, 6, 9), None, None)))
      testA.size mustBe 1

      // delete OptA
      optA match {
        case Some(event) => Events.deleteEvent(event) mustBe true
        case None => Some("failed insert") mustBe Some("None")
      }

      // read all remaining events
      val result = Events.getAllEvents().toSet

      result mustBe testA
    }
  }

}
