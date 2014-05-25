package actors

import models.Website
import org.htmlcleaner.TagNode
import org.htmlcleaner.HtmlCleaner
import java.io.File
import models.Website

object eventParsing {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(212); 
  println("Welcome to the Scala worksheet");$skip(238); 

  val fetcher = new WebsiteFetcher {

    def fetch(website: Website): TagNode = {
      val cleaner = new HtmlCleaner
      cleaner.clean(new File("/home/michel/projects/github/livejazznear.me/play/data/hhj-short-test.html"))
    }
  };System.out.println("""fetcher  : actors.WebsiteFetcher = """ + $show(fetcher ));$skip(69); 
  
  val crawler = new CrawlerHelper(fetcher, HHJEventDetailsParser);System.out.println("""crawler  : actors.CrawlerHelper = """ + $show(crawler ));$skip(117); 
  // bogus since we mockup the WebsiteFetcher above
  val website = Website(0, "name", 1, "websiteUrl", "agentName");System.out.println("""website  : models.Website = """ + $show(website ));$skip(47); 
  
  val events = crawler.parseEvents(website);System.out.println("""events  : List[models.Event] = """ + $show(events ))}
  
}
