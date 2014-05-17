/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import play.api.Logger
import AnormExtension._
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

// -----------------------------------------------------------------------------------------------------------
// EVENT Domain Class
// -----------------------------------------------------------------------------------------------------------

/**
 * Case class for Event
 *
 * Instances are created via the repository layer
 * startTime and endTime are in min since the start of the day
 * i.e. startTime = hr*60 + min
 * If endTime < startTime that it's the next day
 */
case class Event(val id: Long, val status: Byte, val sourceId: Long,
  val artistId: Long, val venueId: Long,
  val startDate: LocalDate, val endDate: LocalDate,
  val startTime: Option[Long], val endTime: Option[Long])

/**
 * The repository interface for Event
 *
 */
object Events {

  val PENDING: Byte = 0
  val ACTIVE: Byte = 1

  /**
   * Get all Events by status
   * @TODO Add pagination
   */
  def getAllEvents(status: Byte = ACTIVE): List[Event] = DB.withConnection { implicit c =>
    SQL("""SELECT e.ID, e.STATUS, e.SOURCE_ID, e.ARTIST_ID, e.VENUE_ID, e.START_DATE, e.END_DATE, e.START_TIME, e.END_TIME
		       FROM EVENTS e WHERE e.STATUS = {status}
		       ORDER BY e.START_DATE""")
      .on('status -> status)
      .as(get[Long]("ID") ~ get[Byte]("STATUS") ~ get[Long]("SOURCE_ID") ~ get[Long]("ARTIST_ID") ~ get[Long]("VENUE_ID")
        ~ get[LocalDate]("START_DATE") ~ get[LocalDate]("END_DATE") ~ get[Option[Long]]("START_TIME") ~ get[Option[Long]]("END_TIME")
        map { case id ~ status ~ source_id ~ artist_id ~ event_id ~ start_date ~ end_date ~ start_time ~ end_time => Event(id, status, source_id, artist_id, event_id, start_date, end_date, start_time, end_time) } *)
  }

  /**
   * Add Event while specifying primary key id
   */
  def addEvent(id: Long, status: Byte, sourceId: Long, artistId: Long, venueId: Long, startDate: LocalDate, endDate: LocalDate, startTime: Option[Long], endTime: Option[Long]): Option[Event] = DB.withConnection { implicit c =>
    val result = SQL("""INSERT INTO EVENTS(ID, STATUS, SOURCE_ID, ARTIST_ID, VENUE_ID, START_DATE, END_DATE, START_TIME, END_TIME) 
    			VALUES ({id}, {status}, {sourceId}, {artistId}, {venueId}, {startDate}, {endDate}, {startTime}, {endTime})""")
      .on('id -> id, 'status -> status, 'sourceId -> sourceId, 'artistId -> artistId, 'venueId -> venueId, 'startDate -> startDate, 'endDate -> endDate, 'startTime -> startTime, 'endTime -> endTime).executeUpdate()

    if (result == 1) Some(Event(id, status, sourceId, artistId, venueId, startDate, endDate, startTime, endTime))
    else None
  }

  /**
   * Add Event, primary key specified by the auto increment
   */
  def addEvent(status: Byte, sourceId: Long, artistId: Long, venueId: Long, startDate: LocalDate, endDate: LocalDate, startTime: Option[Long], endTime: Option[Long]): Option[Event] = DB.withConnection { implicit c =>
    val oId = SQL("""INSERT INTO EVENTS(STATUS, SOURCE_ID, ARTIST_ID, VENUE_ID, START_DATE, END_DATE, START_TIME, END_TIME) 
    		VALUES ({status}, {sourceId}, {artistId}, {venueId}, {startDate}, {endDate}, {startTime}, {endTime})""")
      .on('status -> status, 'sourceId -> sourceId, 'artistId -> artistId, 'venueId -> venueId, 'startDate -> startDate, 'endDate -> endDate, 'startTime -> startTime, 'endTime -> endTime).executeInsert()

    oId match {
      case Some(id) => Some(Event(id, status, sourceId, artistId, venueId, startDate, endDate, startTime, endTime))
      case _ => None
    }
  }

  /**
   * Get Event by primary key
   */
  def getEventById(id: Long): Option[Event] = DB.withConnection { implicit c =>
    SQL("SELECT e.STATUS, e.SOURCE_ID, e.ARTIST_ID, e.VENUE_ID, e.START_DATE, e.END_DATE, e.START_TIME, e.END_TIME FROM EVENTS e WHERE e.ID = {id}")
      .on('id -> id)
      .as((get[Byte]("STATUS") ~ get[Long]("SOURCE_ID") ~ get[Long]("ARTIST_ID") ~ get[Long]("VENUE_ID")
        ~ get[LocalDate]("START_DATE") ~ get[LocalDate]("END_DATE") ~ get[Option[Long]]("START_TIME") ~ get[Option[Long]]("END_TIME")).singleOpt)
      .map { case status ~ source_id ~ artist_id ~ event_id ~ start_date ~ end_date ~ start_time ~ end_time => Event(id, status, source_id, artist_id, event_id, start_date, end_date, start_time, end_time) }
  }

  /**
   * Delete Event by primary key
   *
   * @TODO Cascade delete the artist and/or venue if they are not used by any other events
   */
  def deleteEvent(event: Event): Boolean = DB.withConnection { implicit c =>
    SQL("DELETE FROM EVENTS e WHERE e.ID = {id}").on('id -> event.id).executeUpdate() == 1
  }

}
