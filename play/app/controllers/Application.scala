/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package controllers

import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.concurrent.Akka
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.Props
import actors.HHJEventCrawlerActor
import models.Website
import scala.language.postfixOps

/**
 * Application Controller
 *
 */
object Application extends Controller {

  val eventCrawlerActor = Akka.system.actorOf(Props[HHJEventCrawlerActor], name = "hhjEventCrawlerActor")
  implicit val timeout = Timeout(10 seconds)

  // Need to redo the view and put a login page
  def index = Action.async {
    val website = Website(0, "Hot House Jazz Tester", 1, "data/hhj-short-test.html", "hhjEventCrawlerActor")
    val futureInt = eventCrawlerActor ? website
    futureInt map {n_events =>  Ok(views.html.index(s"Got $n_events new events!"))}
  }

}
