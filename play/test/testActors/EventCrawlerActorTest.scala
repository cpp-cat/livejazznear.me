/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package testActors;

import anorm._
import play.api.db.DB
import play.api.Play.current
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import actors.HHJEventCrawlerActor
import models.Website
import play.api.test.WithApplication

class EventCrawlerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  val eventCrawlerActor = system.actorOf(Props[HHJEventCrawlerActor])
  val website = Website(0, "Hot House Jazz Tester", 1, "data/hhj-short-test.html", "hhjEventCrawlerActor")

  def this() = this(ActorSystem("EventCrawlerSpec"))

  override def afterAll {
    // post test
    new WithApplication {
      DB.withConnection { implicit c =>
        SQL("DELETE FROM EVENTS").execute()
        SQL("DELETE FROM PARTIES").execute()
        SQL("DELETE FROM SOURCES").execute()
        SQL("DELETE FROM WEBSITES").execute()
      }
    }
    TestKit.shutdownActorSystem(system)
  }

  "An HHJEventCrawlerActor actor" must {

    "load Events in DB" in new WithApplication {
      eventCrawlerActor ! website
      expectMsg(12)
    }

    "Should not load duplicate Events in DB" in new WithApplication {
      eventCrawlerActor ! website
      expectMsg(0)
    }

  }
}