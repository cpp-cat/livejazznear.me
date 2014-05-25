/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package testActors;

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
      details3 mustBe EventDetails("Guided Tours", "Louis Armstrong House Museum", Some("34-56 107th Street, Corona, NY 11368"),Some(40.75458f),Some(-73.861595f), new LocalDate("2014-05-20"), new LocalDate("2014-05-23"), Some(600), Some(1020))

      val details4 = HHJEventDetailsParser.parseEventDetails(loadEvent("data/hhj-event4-test.html"))
      details4 mustBe EventDetails("Susie Ibarra", "The Stone", Some("2nd Street, New York, NY 10003"), Some(40.724895f), Some(-73.99035f), new LocalDate("2014-05-20"), new LocalDate("2014-05-25"), Some(1200), Some(1410))

      val details5 = HHJEventDetailsParser.parseEventDetails(loadEvent("data/hhj-event5-test.html"))
      details5 mustBe EventDetails("Michelle Walker Qrt", "Jazz at Kitano at Kitano Hotel", Some("66 Park Avenue, New York, NY 10016"), Some(40.74968f), Some(-73.97996f), new LocalDate("2014-05-21"), new LocalDate("2014-05-21"), Some(1200), Some(1395))
    }

  }
}
