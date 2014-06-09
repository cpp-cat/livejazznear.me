/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package testActors;

import anorm._
import play.api.db.DB
import play.api.Play.current
import org.scalatest._
import org.scalatestplus.play._
import org.htmlcleaner.HtmlCleaner
import org.apache.commons.lang3.StringEscapeUtils
import org.htmlcleaner.TagNode
import java.io.File
import actors.HHJEventDetailsParser
import org.joda.time.LocalDate
import play.api.Logger
import actors.EventDetails
import actors.WebsiteFetcher
import models.Website
import actors.CrawlerHelper
import models.Websites
import models.Sources
import models.Artists
import models.Venues
import models.Source
import models.Artist
import models.Venue
import models.Event

// -----------------------------------------------------------------------------------------------------------
// HotHouseJazzSpec Class: Testing parsing HHJ site
// -----------------------------------------------------------------------------------------------------------

class HotHouseJazzSpec extends PlaySpec with OneAppPerSuite {

  def fixture =
    new {
      val cleaner = new HtmlCleaner
      val props = cleaner.getProperties
      val rootNode = cleaner.clean(new File("data/hhj-event1-test.html")).evaluateXPath("//div[@class='eventon_list_event']") map { case ev: TagNode => ev } head
    }

  "The HHJEventDetailsParser object" must {

    "extract the remoteSiteId from the rootNode" in {
      val f = fixture
      HHJEventDetailsParser.parseRemoteSiteId(f.rootNode) mustBe "2022"
    }

    "parse the Event start and end times" in {
      HHJEventDetailsParser.parseEventTime("10:30 pm - 1:30 am (21)") mustBe (Some(10 * 60 + 30 + 12 * 60), Some(60 + 30), Some(21))
      HHJEventDetailsParser.parseEventTime("1:00 pm") mustBe (Some(1 * 60 + 0 + 12 * 60), None, None)
      HHJEventDetailsParser.parseEventTime("2:00 pm - 3:00 pm") mustBe (Some(2 * 60 + 0 + 12 * 60), Some(3 * 60 + 0 + 12 * 60), None)
      HHJEventDetailsParser.parseEventTime("Anything goes...") mustBe (None, None, None)
      HHJEventDetailsParser.parseEventTime("2:00 pm - 3:00 am") mustBe (Some(2 * 60 + 0 + 12 * 60), Some(3 * 60 + 0 + 0 * 60), None)
      HHJEventDetailsParser.parseEventTime("2:00 pm - 10:45 am (1)") mustBe (Some(2 * 60 + 0 + 12 * 60), Some(10 * 60 + 45 + 0 * 60), Some(1))
      HHJEventDetailsParser.parseEventTime("10:00 am") mustBe (Some(10 * 60 + 0 + 0 * 60), None, None)
      HHJEventDetailsParser.parseEventTime("12:00 am") mustBe (Some(0 * 60 + 0 + 0 * 60), None, None)
      HHJEventDetailsParser.parseEventTime("12:00 pm") mustBe (Some(0 * 60 + 0 + 12 * 60), None, None)
      HHJEventDetailsParser.parseEventTime("2:00 pm - 12:00 am") mustBe (Some(2 * 60 + 0 + 12 * 60), Some(0 * 60 + 0 + 0 * 60), None)
      HHJEventDetailsParser.parseEventTime("2:00 pm - 12:00 pm") mustBe (Some(2 * 60 + 0 + 12 * 60), Some(0 * 60 + 0 + 12 * 60), None)
    }

    "compute the Event end date based on start date, start time, end time and end day" in {
      /*
	   * Compute the Event end date based on start date, start time, end time and end day.
	   * 
	   * Examples:
	   * 	Cases where end date is same as start date
	   *    ------------------------------------------
	   * 	start date is 2014-05-20 and times are "10:30 pm - 1:30 am (21)" then end date is 2014-05-20
	   * 	start date is 2014-05-20 and times are "10:30 pm" then end date is 2014-05-20
	   * 	start date is 2014-05-20 and times are "10:30 pm - 11:55 pm" then end date is 2014-05-20
	   * 	start date is 2014-05-31 and times are "10:30 pm - 1:30 am (1)" then end date is 2014-05-31
	   * 	start date is 2014-12-31 and times are "10:30 pm - 1:30 am (1)" then end date is 2014-12-31
	   *  
	   *  	Cases where end date is not same as start date (recurring event)
	   *    ----------------------------------------------------------------
	   * 	start date is 2014-05-20 and times are "10:30 pm - 11:55 pm (21)" then end date is 2014-05-21
	   * 	start date is 2014-05-20 and times are "10:30 pm - 1:30 am (25)" then end date is 2014-05-24
	   * 	start date is 2014-05-31 and times are "10:30 pm - 1:30 am (30)" then end date is 2014-06-29
	   * 	start date is 2014-05-31 and times are "10:30 pm - 11:30 pm (10)" then end date is 2014-6-10
	   * 	start date is 2014-12-31 and times are "10:30 pm - 1:30 am (10)" then end date is 2015-1-9
	   * 	start date is 2014-12-31 and times are "10:30 pm - 11:30 pm (10)" then end date is 2015-1-10
	   * 
	   */

      import HHJEventDetailsParser._
      def test(sd: String, et: String, ed: String) = {
        val startDate = new LocalDate(sd)
        val (startTime, endTime, endDay) = parseEventTime(et)
        getEndDate(startDate, startTime, endTime, endDay) mustBe new LocalDate(ed)
      }

      test("2014-3-20", "10:30 pm - 11:30 pm", "2014-3-20")
      test("2014-2-20", "10:30 pm", "2014-2-20")
      test("2014-1-20", "10:30 pm - 1:30 am (21)", "2014-1-20")
      test("2014-5-31", "10:30 pm - 1:30 am (1)", "2014-5-31")
      test("2014-12-31", "10:30 pm - 1:30 am (1)", "2014-12-31")

      test("2014-6-20", "10:30 pm - 11:55 pm (21)", "2014-6-21")
      test("2014-7-20", "10:30 pm - 1:30 am (25)", "2014-7-24")
      test("2014-8-31", "10:30 pm - 1:30 am (30)", "2014-9-29")
      test("2014-10-31", "10:30 pm - 11:30 pm (10)", "2014-11-10")
      test("2024-12-31", "10:30 pm - 1:30 am (10)", "2025-1-9")
      test("2034-12-31", "10:30 pm - 11:30 pm (10)", "2035-1-10")

      test("2016-5-15", "12:30 pm - 2:30 pm", "2016-5-15")
      test("2017-5-15", "12:30 pm - 2:30 pm (17)", "2017-5-17")
    }

    "parse all the event details" in {
      def loadEvent(fname: String): TagNode = {
        val cleaner = new HtmlCleaner
        cleaner.clean(new File(fname)).evaluateXPath("//div[@class='eventon_list_event']") map { case ev: TagNode => ev } head
      }

      val details1 = HHJEventDetailsParser.parseEventDetails(loadEvent("data/hhj-event1-test.html"))
      details1 mustBe EventDetails("Chris Gillespie", "Bemelmans at Carlyle Hotel", Some("35 East 76th St, New York, NY"), Some(40.77474f), Some(-73.96343f), new LocalDate("2014-05-20"), new LocalDate("2014-05-24"), Some(1050), Some(1230))

      val details2 = HHJEventDetailsParser.parseEventDetails(loadEvent("data/hhj-event2-test.html"))
      details2 mustBe EventDetails("Sue Maskaleris", "Bryant Park", Some("6th Ave, between 41st & 42nd Streets, New York, NY"), None, None, new LocalDate("2014-05-19"), new LocalDate("2014-05-23"), Some(750), Some(870))

      val details3 = HHJEventDetailsParser.parseEventDetails(loadEvent("data/hhj-event3-test.html"))
      details3 mustBe EventDetails("Guided Tours", "Louis Armstrong House Museum", Some("34-56 107th Street, Corona, NY 11368"), Some(40.75458f), Some(-73.861595f), new LocalDate("2014-05-20"), new LocalDate("2014-05-23"), Some(600), Some(1020))

      val details4 = HHJEventDetailsParser.parseEventDetails(loadEvent("data/hhj-event4-test.html"))
      details4 mustBe EventDetails("Susie Ibarra", "The Stone", Some("2nd Street, New York, NY 10003"), Some(40.724895f), Some(-73.99035f), new LocalDate("2014-05-20"), new LocalDate("2014-05-25"), Some(1200), Some(1410))

      val details5 = HHJEventDetailsParser.parseEventDetails(loadEvent("data/hhj-event5-test.html"))
      details5 mustBe EventDetails("Michelle Walker Qrt", "Jazz at Kitano at Kitano Hotel", Some("66 Park Avenue, New York, NY 10016"), Some(40.74968f), Some(-73.97996f), new LocalDate("2014-05-21"), new LocalDate("2014-05-21"), Some(1200), Some(1395))
    }

  }
}

// -----------------------------------------------------------------------------------------------------------
// CrawlerHelperSpec Class: Testing crawling HHJ site and adding events in database
// -----------------------------------------------------------------------------------------------------------

class CrawlerHelperSpec extends PlaySpec with OneAppPerSuite {

  override def withFixture(test: NoArgTest) = {

    // pre test

    try super.withFixture(test) // To be stackable, must call super.withFixture
    finally {

      // post test
      DB.withConnection { implicit c =>
        SQL("DELETE FROM EVENTS").execute()
        SQL("DELETE FROM PARTIES").execute()
        SQL("DELETE FROM SOURCES").execute()
        SQL("DELETE FROM WEBSITES").execute()
      }
    }
  }

  "The CrawlerHelper class" must {

    "master the Artist by name in the Events" in {
      val fetcher = new WebsiteFetcher {
        def fetch(website: Website): TagNode = { (new HtmlCleaner).clean(new File("data/hhj-master-name-test.html")) }
      }
      val crawler = new CrawlerHelper(fetcher, HHJEventDetailsParser)
      val website = Websites.addWebsite("Hout House Jazz Tester", Websites.ACTIVE, "http://hothousejazz.com/calendar", "agentName") match {
        case Some(w) => w
        case None => fail()
      }
      val events = crawler.parseEvents(website)
      events.size mustBe 2

      // Get the Artist, should be same for both events
      val artist = Artists.getArtistById(events.head.artistId) match {
        case Some(a) => a
        case None => fail()
      }

      // check the attributes for each event, in particular must be same Artist (by artist.id)
      events foreach (event => {

        val source = Sources.getSourceById(event.sourceId) match {
          case Some(s) => s
          case None => fail()
        }
        val venue = Venues.getVenueById(event.venueId) match {
          case Some(v) => v
          case None => fail()
        }

        // check the data
        source.remoteSiteId match {
          case Some("4057") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("4057"))
            artist mustBe Artist(artist.id, 1, source.id, "Susie Ibarra", None)
            venue mustBe Venue(venue.id, 1, source.id, "The Stone", Some("2nd Street, New York, NY 10003"), Some(40.724895f), Some(-73.99035f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-20"), new LocalDate("2014-05-25"), Some(1200), Some(1410))

          case Some("4073") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("4073"))
            artist mustBe Artist(artist.id, 1, artist.sourceId, "Susie Ibarra", None)
            venue mustBe Venue(venue.id, 1, source.id, "Village Vanguard", Some("178 7th Avenue South, New York, NY 10014"), Some(40.73604f), Some(-74.001724f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-20"), new LocalDate("2014-05-25"), Some(1230), Some(1435))

          case _ =>
            Logger.info("---------------------------")
            Logger.info(source.toString)
            Logger.info(artist.toString)
            Logger.info(venue.toString)
            Logger.info(event.toString)
        }
      })
    }

    "create new Events in crawler.parseEvents(website)" in {
      val fetcher = new WebsiteFetcher {
        def fetch(website: Website): TagNode = { (new HtmlCleaner).clean(new File("data/hhj-short-test.html")) }
      }
      val crawler = new CrawlerHelper(fetcher, HHJEventDetailsParser)
      val website = Websites.addWebsite("Hout House Jazz Tester", Websites.ACTIVE, "http://hothousejazz.com/calendar", "agentName") match {
        case Some(w) => w
        case None => fail()
      }
      val events = crawler.parseEvents(website)
      events.size mustBe 12

      events foreach (event => {
        val source = Sources.getSourceById(event.sourceId) match {
          case Some(s) => s
          case None => fail()
        }
        val artist = Artists.getArtistById(event.artistId) match {
          case Some(a) => a
          case None => fail()
        }
        val venue = Venues.getVenueById(event.venueId) match {
          case Some(v) => v
          case None => fail()
        }

        // check the data
        source.remoteSiteId match {
          case Some("4057") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("4057"))
            artist mustBe Artist(artist.id, 1, source.id, "Susie Ibarra", None)
            venue mustBe Venue(venue.id, 1, source.id, "The Stone", Some("2nd Street, New York, NY 10003"), Some(40.724895f), Some(-73.99035f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-20"), new LocalDate("2014-05-25"), Some(1200), Some(1410))

          case Some("4073") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("4073"))
            artist mustBe Artist(artist.id, 1, source.id, "Steve Wilson & Wilsonian’s Grain", None)
            venue mustBe Venue(venue.id, 1, source.id, "Village Vanguard", Some("178 7th Avenue South, New York, NY 10014"), Some(40.73604f), Some(-74.001724f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-20"), new LocalDate("2014-05-25"), Some(1230), Some(1435))

          case Some("3634") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("3634"))
            artist mustBe Artist(artist.id, 1, source.id, "Alberto Pibiri", None)
            venue mustBe Venue(venue.id, 1, source.id, "Measure at Langham Place Hotel", Some("400 Fifth Avenue, New York, NY 10018"), Some(40.75021f), Some(-73.98383f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-21"), new LocalDate("2014-05-25"), Some(1200), Some(1380))

          case Some("4559") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("4559"))
            artist mustBe Artist(artist.id, 1, source.id, "Ben Wolfe Qnt feat Nicholas Payton", None)
            venue mustBe Venue(venue.id, 1, source.id, "Dizzy's Club Coca Cola at Jazz At Lincoln Center", Some("10 Columbus Circle, 5th Floor, New York, NY 10019"), Some(40.768414f), Some(-73.982704f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-22"), new LocalDate("2014-05-25"), Some(1170), Some(1410))

          case Some("4735") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("4735"))
            artist mustBe Artist(artist.id, 1, source.id, "Randy Weston African Rhythms Qnt", None)
            venue mustBe Venue(venue.id, 1, source.id, "Jazz Standard", Some("116 East 27th Street, New York, NY 10016"), Some(40.742157f), Some(-73.983826f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-23"), new LocalDate("2014-05-24"), Some(1170), Some(60))

          case Some("4657") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("4657"))
            artist mustBe Artist(artist.id, 1, source.id, "Benny Golson Qrt", None)
            venue mustBe Venue(venue.id, 1, source.id, "Blue Note", Some("131 West 3rd Street, NY 10012"), Some(40.73091f), Some(-74.00064f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-23"), new LocalDate("2014-05-25"), Some(1200), Some(1435))

          case Some("201") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("201"))
            artist mustBe Artist(artist.id, 1, source.id, "Bill Saxton Bebop Band", None)
            venue mustBe Venue(venue.id, 1, source.id, "Bill's Place", Some("148 West 133rd Street, New York, NY 10030"), Some(40.813374f), Some(-73.94364f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-23"), new LocalDate("2014-05-24"), Some(1260), Some(60))

          case Some("3951") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("3951"))
            artist mustBe Artist(artist.id, 1, source.id, "Ronny Whyte Trio", None)
            venue mustBe Venue(venue.id, 1, source.id, "Knickerbocker Bar & Grill", Some("33 University Place, New York, NY 10003"), Some(40.732014f), Some(-73.99443f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-23"), new LocalDate("2014-05-24"), Some(1305), Some(120))

          case Some("1469") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("1469"))
            artist mustBe Artist(artist.id, 1, source.id, "Eric Lemon & BJ Ensemble", None)
            venue mustBe Venue(venue.id, 1, source.id, "Brownstone Jazz", Some("107 Macon Street, Brooklyn, NY 11216"), Some(40.681587f), Some(-73.94703f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-23"), new LocalDate("2014-05-24"), Some(1380), Some(60))

          case Some("285") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("285"))
            artist mustBe Artist(artist.id, 1, source.id, "Jazz brunch", None)
            venue mustBe Venue(venue.id, 1, source.id, "Harlem Tavern", Some("2153 Frederick Douglass, New York, NY 10026"), Some(40.80475f), Some(-73.95548f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-24"), new LocalDate("2014-05-25"), Some(660), Some(960))

          case Some("1633") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("1633"))
            artist mustBe Artist(artist.id, 1, source.id, "Guided Tours", None)
            venue mustBe Venue(venue.id, 1, source.id, "Louis Armstrong House Museum", Some("34-56 107th Street, Corona, NY 11368"), Some(40.75458f), Some(-73.861595f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-24"), new LocalDate("2014-05-25"), Some(720), Some(1020))

          case Some("4639") =>
            source mustBe Source(source.id, website.id, source.createTime, Some("4639"))
            artist mustBe Artist(artist.id, 1, source.id, "Aleksi Glick", None)
            venue mustBe Venue(venue.id, 1, source.id, "Bar Next Door", Some("129 McDougal Street, New York, NY 10012"), Some(40.73071f), Some(-74.000145f), None, None)
            event mustBe Event(event.id, 1, source.id, artist.id, venue.id, new LocalDate("2014-05-24"), new LocalDate("2014-05-24"), Some(1170), Some(90))

          case _ =>
            Logger.info("---------------------------")
            Logger.info(source.toString)
            Logger.info(artist.toString)
            Logger.info(venue.toString)
            Logger.info(event.toString)
        }
      })
    }

  }
}
