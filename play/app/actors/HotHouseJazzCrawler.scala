/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package actors

//import play.api.Play.current
import utils._
import play.api.Logger
import models.Website
import org.htmlcleaner.TagNode
import models.Source
import models.Artist
import models.Venue
import models.Event
import models.Sources
import org.joda.time.DateTime
import org.joda.time.LocalDate
import scala.util.matching.Regex
import models.Artists
import models.Parties
import models.Venues
import models.Events
import scala.language.postfixOps

trait WebsiteFetcher {

  def fetch(website: Website): TagNode
}

/**
 * Case class for event details parsed from the website
 */
case class EventDetails(val artistName: String, val venueName: String,
  val venueAddress: Option[String], val locationLat: Option[Float], val locationLng: Option[Float],
  val startDate: LocalDate, val endDate: LocalDate, val startTime: Option[Long], val endTime: Option[Long])

/**
 * Trait to be implemented for specific websites
 */
trait EventDetailsParser {

  /**
   * Parse the event unique ID for the site
   *
   * This is to avoid to parse all the event details if we already parsed that event before
   */
  def parseRemoteSiteId(node: TagNode): String

  /**
   * Parse all the event details
   */
  def parseEventDetails(node: TagNode): EventDetails
}

/**
 * Specific EventDetailsParser for hothousejazz.com
 */
object HHJEventDetailsParser extends EventDetailsParser {

  val startTimePattern = """(\d+):(\d\d) +(am|pm)""".r
  val endTimePattern = """- +(\d+):(\d\d) +(am|pm)""".r
  val endDatePattern = """\((\d+)\)""".r

  /**
   * Utility method to parse time from input string
   *
   *  possible format for input string:
   * 		"10:30 pm - 1:30 am (21)" returns (Some(10*60+30+12*60), Some(60+30), Some(21))
   * 		"1:00 pm"				  return (Some(60+12*60), None, None)
   * 		"2:00 pm - 3:00 pm"		  returns (Some(2*60+12*60), Some(3*60), None)
   * 		"Anything goes..."		  returns (None, None, None)
   *
   * @returns (startTime, endTime, endDay)
   */
  def parseEventTime(str: String): (Option[Long], Option[Long], Option[Int]) = {

    def getTime(regex: Regex, str: String): Option[Long] = {
      getTimeDetails(regex, str) match {
        case Some((hh, mm, pm)) => Some(hh * 60 + mm + pm * 60)
        case None => None
      }
    }
    val startTime = getTime(startTimePattern, str)
    val endTime = getTime(endTimePattern, str)
    val endDay = endDatePattern findFirstIn str match {
      case Some(endDatePattern(dd)) => Some(dd toInt)
      case None => None
    }

    (startTime, endTime, endDay)
  }

  /**
   * Utility method to parse the start/end time from the input string
   *
   * @returns (hours, mins, hours for am/pm: am=0, pm=12) or None in all fields if no match is found
   */
  def getTimeDetails(regex: Regex, str: String): Option[(Int, Int, Int)] = {
    regex findFirstIn str match {
      case Some(regex(hh, mm, ampm)) => Some((hh.toInt % 12, mm toInt, if (ampm == "pm") 12 else 0))
      case None => None
    }
  }

  /**
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
   *    start date is 2014-05-31 and times are "10:30 pm - 11:30 pm (10)" then end date is 2014-6-10
   * 	start date is 2014-12-31 and times are "10:30 pm - 1:30 am (10)" then end date is 2015-1-9
   * 	start date is 2014-12-31 and times are "10:30 pm - 11:30 pm (10)" then end date is 2015-1-10
   *
   */
  def getEndDate(startDate: LocalDate, startTime: Option[Long], endTime: Option[Long], endDay: Option[Int]): LocalDate = {

    (startTime, endTime, endDay) match {

      case (Some(st), Some(et), Some(d)) =>

        // cases endDate is same as startDate
        if (startDate.plusDays(1).getDayOfMonth() == d && et < st) startDate
        else {

          // cases we have a different date
          // different month or year
          if (d < startDate.getDayOfMonth()) {

            if (startDate.getMonthOfYear() == 12) {

              val endDate = new LocalDate(startDate.getYear() + 1, 1, d)
              if (et < st) endDate.minusDays(1)
              else endDate

            } else {
              val endDate = new LocalDate(startDate.getYear(), startDate.getMonthOfYear()+1, d)
              if (et < st) endDate.minusDays(1)
              else endDate
            }

          } else {

            // same month
            val endDate = new LocalDate(startDate.getYear(), startDate.getMonthOfYear(), d)
            if (et < st) endDate.minusDays(1)
            else endDate
          }
        }

      case _ => startDate
    }
  }

  def parseRemoteSiteId(node: TagNode): String = {
    node.getAttributeByName("event_id")
  }

  def parseEventDetails(node: TagNode): EventDetails = {

    // get the start date - should always exist
    val startDateStr = node.evaluateXPath("//time") map { case d: TagNode => d.getAttributeByName("datetime") } head
    val startDate: LocalDate = LocalDate.parse(startDateStr)

    // that should always exist
    val p = node.evaluateXPath("//p[@data-location_name]") map { case d: TagNode => d } head

    val venueName = p.getAttributeByName("data-location_name")
    val artistName = (p.evaluateXPath("/span[@itemprop='name']") map { case d: TagNode => d }).head.getText.toString
    val address = (p.evaluateXPath("//em[@add_str]") map { case d: TagNode => d }).head.getAttributeByName("add_str")

    // get the elm w/ latlng -- this may not exist in the html
    val latlngStr = p.getAttributeByName("latlng")
    val latlng: Array[Option[Float]] = latlngStr match {
      case null => Array(None, None)
      case ss => ss.split(',') map { s => Some(s.toFloat) }
    }

    // Get the element with time information, if exist
    val eventTimeElm: Array[TagNode] = p.evaluateXPath("//em[@class='evcal_time']") map { case d: TagNode => d }

    val (startTime, endTime, endDay) = eventTimeElm match {
      case Array() => (None, None, None)
      case _ => parseEventTime(eventTimeElm.head.getText.toString)

    }

    // determine end date
    val endDate = getEndDate(startDate, startTime, endTime, endDay)

    EventDetails(artistName, venueName, Some(address), latlng(0), latlng(1), startDate, endDate, startTime, endTime)
  }
}

/**
 * Framework class for parsing Events from website using the appropriate EventDetailsParser
 */
class CrawlerHelper(websiteFetcher: WebsiteFetcher, eventDetailsParser: EventDetailsParser) {

  /**
   * Create the Artist domain object in database.
   * 
   * If the Artist already exist by name match, return the existing one.
   * 
   * @TODO Find aliases based on artist name
   * @TODO Find artist's official website via google search api
   * @returns Some(Artist) or None if something when wrong while creating the Artist in database
   */
  def parseArtist(eventDetails: EventDetails, source: Source): Option[Artist] = {
    
    // Check if the Artist already exist by exact name match (not case sensitive)
    val artistNameLower = eventDetails.artistName.toLowerCase()
    val result = for {
      artist <- Artists.getArtistsByName(eventDetails.artistName) if artistNameLower == artist.name.toLowerCase()
    } yield artist
    
    // return the match found, if non found (empty list) then create in database
    result match {
      case List() => Artists.addArtist(Parties.ACTIVE, source.id, eventDetails.artistName, None)
      case _ => Some(result.head)
    }
  }

  /**
   * Create the Venue domain object in database
   * 
   * If the Venue already exist by name match, return the existing one.
   * 
   * @TODO Find venue's official website via google search api
   * @TODO Add Venue phone number
   * @returns Some(Venue) or None if something when wrong while creating the Venue in database
   */
  def parseVenue(eventDetails: EventDetails, source: Source): Option[Venue] = {
    
    // Check if the Venue already exist by exact name match (not case sensitive)
    val venueNameLower = eventDetails.venueName.toLowerCase()
    val result = for {
      venue <- Venues.getVenuesByName(eventDetails.venueName) if venueNameLower == venue.name.toLowerCase()
    } yield venue
    
    // return the match found, if non found (empty list) then create in database
    result match {
      case List() => Venues.addVenue(Parties.ACTIVE, source.id, eventDetails.venueName, eventDetails.venueAddress, eventDetails.locationLat, eventDetails.locationLng, None, None)
      case _ => Some(result.head)
    }
  }

  /**
   * Parse the Event from the input node if the event does not already exist in database
   *
   * @returns None if the Event already exist based on remoteSiteId attribute otherwise Some(Event)
   */
  def parseEvent(website: Website, node: TagNode): Option[Event] = {

    // create the Source object
    val remoteSiteId = eventDetailsParser.parseRemoteSiteId(node)
    val websiteName = website.name

    try {
      if (remoteSiteId.length() == 0) {
        Logger.error(s"CrawlerHelper: HTML has no remoteSiteId from $websiteName!!")
        throw new NickelException("CrawlerHelper: Cannot create Source object -- HTML has no remoteSiteId!")
      }

      // Check if the Event already exist
      if (Sources.getSourceByWebsiteId(website.id, Some(remoteSiteId)).isEmpty) {

        // Create the Source object to track the Event, Artist and Venue creation
        val source = Sources.addSource(website.id, DateTime.now(), Some(remoteSiteId)) match {
          case Some(s) => s
          case None =>
            Logger.error(s"CrawlerHelper: Cannot create Source object for $remoteSiteId from $websiteName!")
            throw new NickelException("CrawlerHelper: Cannot create Source object!")
        }

        val eventDetails = eventDetailsParser.parseEventDetails(node)

        // create the artist
        val artist = parseArtist(eventDetails, source) match {
          case Some(a) => a
          case None => 
            Logger.error(s"CrawlerHelper: Cannot create/find Artist object for $remoteSiteId from $websiteName!")
            throw new NickelException("CrawlerHelper: Cannot create Artist object!")
        }

        // create the venue
        val venue = parseVenue(eventDetails, source) match {
          case Some(v) => v
          case None => 
            Logger.error(s"CrawlerHelper: Cannot create/find Venue object for $remoteSiteId from $websiteName!")
            throw new NickelException("CrawlerHelper: Cannot create Venue object!")
        }

        // create the Event itself
        Events.addEvent(Events.ACTIVE, source.id, artist.id, venue.id, eventDetails.startDate, eventDetails.endDate, eventDetails.startTime, eventDetails.endTime) match {
          case Some(e) => Some(e)
          case None =>
            Logger.error(s"CrawlerHelper: Cannot create Event object for $remoteSiteId from $websiteName!")
            throw new NickelException("CrawlerHelper: Cannot create Event object!")
        }

      } else {

        // The event already exist
        None
      }

    } catch {
      case ne: NickelException =>
        Logger.error(s"CrawlerHelper: Error while creating Event $remoteSiteId from $websiteName, skipping...")
        None
      case e: Exception =>
        val msg = e.getMessage()
        Logger.error(s"CrawlerHelper: Exception while creating Event $remoteSiteId from $websiteName, message: $msg, skipping this event...")
        None
    }
  }

  /**
   * Parse all events from website, return all newly created Events
   *
   * @returns List of all newly create Events, Events that already existed in DB are skipped
   */
  def parseEvents(website: Website): List[Event] = {

    def parseEventsFromParentNode(events: TagNode): List[Event] = {

      val eventNodes: List[TagNode] = events.evaluateXPath("//div[@class='eventon_list_event']") map { case ev: TagNode => ev } toList

      for (
        eNode <- eventNodes; event <- parseEvent(website, eNode)
      ) yield event

    }

    //* get the root node of the page, may need to pass parameters
    val rootNode = websiteFetcher.fetch(website)

    // Get all events
    rootNode.evaluateXPath("//div[@id='evcal_list']").head match {
      case events: TagNode => parseEventsFromParentNode(events)
      case _ => List.empty
    }

  }

}
