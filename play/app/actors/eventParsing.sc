package actors

import models.Website
import org.htmlcleaner.TagNode
import org.htmlcleaner.HtmlCleaner
import java.io.File
import models.Website

object eventParsing {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet

  val fetcher = new WebsiteFetcher {

    def fetch(website: Website): TagNode = {
      val cleaner = new HtmlCleaner
      cleaner.clean(new File("/home/michel/projects/github/livejazznear.me/play/data/hhj-short-test.html"))
    }
  }                                               //> fetcher  : actors.WebsiteFetcher = actors.eventParsing$$anonfun$main$1$$anon
                                                  //| $1@369f0df0
  
  val crawler = new CrawlerHelper(fetcher, HHJEventDetailsParser)
                                                  //> crawler  : actors.CrawlerHelper = actors.CrawlerHelper@3e557638
  // bogus since we mockup the WebsiteFetcher above
  val website = Website(0, "name", 1, "websiteUrl", "agentName")
                                                  //> website  : models.Website = Website(0,name,1,websiteUrl,agentName)
  
  val events = crawler.parseEvents(website)       //> 13:22:05.231 [main] ERROR application - CrawlerHelper: Exception while creat
                                                  //| ing Event 4057 from name, message: There is no started application, skipping
                                                  //|  this event...
                                                  //| 13:22:05.235 [main] ERROR application - CrawlerHelper: Exception while creat
                                                  //| ing Event 4073 from name, message: There is no started application, skipping
                                                  //|  this event...
                                                  //| 13:22:05.236 [main] ERROR application - CrawlerHelper: Exception while creat
                                                  //| ing Event 3634 from name, message: There is no started application, skipping
                                                  //|  this event...
                                                  //| 13:22:05.236 [main] ERROR application - CrawlerHelper: Exception while creat
                                                  //| ing Event 4559 from name, message: There is no started application, skipping
                                                  //|  this event...
                                                  //| 13:22:05.237 [main] ERROR application - CrawlerHelper: Exception while creat
                                                  //| ing Event 4735 from name, message: There is no started application, skipping
                                                  //|  this event...
                                                  //| 13:22:05.237 [main] ERROR application - CrawlerHelper: Exception while creat
                                                  //| ing Event 
                                                  //| Output exceeds cutoff limit.
  
}