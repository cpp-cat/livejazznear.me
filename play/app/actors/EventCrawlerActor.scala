/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import models.Website
import org.htmlcleaner.TagNode
import org.htmlcleaner.HtmlCleaner
import java.io.File

/**
 * Agent to crawl HotHouseJazz.com for Events, Artists, and Venues
 */
class HHJEventCrawlerActor() extends Actor with ActorLogging {

  //TODO Read from file temporarily for now
  val fetcher = new WebsiteFetcher {
    def fetch(website: Website): TagNode = { (new HtmlCleaner).clean(new File(website.websiteUrl)) }
  }

  def receive = {

    // Crawl the website for Events...
    case website: Website =>
      log.info(s"HHJEventCrawlerActor: Received website message, with $website")
      val crawler = new CrawlerHelper(fetcher, HHJEventDetailsParser)
      val events = crawler.parseEvents(website)
      log.info("HHJEventCrawlerActor: Parsed {} new events", events.size)
      sender ! events.size
//      context.stop(self)
  }

}