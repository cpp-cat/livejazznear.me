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
// SOURCE Domain Class
// -----------------------------------------------------------------------------------------------------------

/**
 * Case class for Source
 *
 * This indicates where we got the Artist, Venue, or Event or any other items scouted from the web
 */
case class Source(val id: Long, val websiteId: Long, val createTime: DateTime, val remoteSiteId: Option[String])

object Sources {

  /**
   * Get Sources by websiteId and remoteSiteId
   */
  def getSourceByWebsiteId(websiteId: Long, remoteSiteId: Option[String]): List[Source] = DB.withConnection { implicit c =>

    def ops = {
      remoteSiteId match {
        case Some(text) => "="
        case None => "IS"
      }
    }

    SQL(s"SELECT s.ID, s.CREATE_TIME FROM SOURCES s WHERE s.WEBSITE_ID = {websiteId} AND s.REMOTE_SITE_ID $ops {remoteSiteId}")
      .on('websiteId -> websiteId, 'remoteSiteId -> remoteSiteId)
      .as(get[Long]("ID") ~ get[DateTime]("CREATE_TIME")
        map { case id ~ create_time => Source(id, websiteId, create_time, remoteSiteId) } *)

  }

  /**
   * Add Source
   */
  def addSource(websiteId: Long, createTime: DateTime, remoteSiteId: Option[String]): Option[Source] = DB.withConnection { implicit c =>
    val oId = SQL("""INSERT INTO SOURCES(WEBSITE_ID, CREATE_TIME, REMOTE_SITE_ID) 
    		VALUES ({websiteId}, {createTime}, {remoteSiteId})""")
      .on('websiteId -> websiteId, 'createTime -> createTime, 'remoteSiteId -> remoteSiteId).executeInsert()

    oId match {
      case Some(id) => Some(Source(id, websiteId, createTime, remoteSiteId))
      case _ => None
    }
  }

  /**
   * Get Source by primary key
   */
  def getSourceById(id: Long): Option[Source] = DB.withConnection { implicit c =>
    SQL("SELECT s.WEBSITE_ID, s.CREATE_TIME, s.REMOTE_SITE_ID FROM SOURCES s WHERE s.ID = {id}")
      .on('id -> id)
      .as((get[Long]("WEBSITE_ID") ~ get[DateTime]("CREATE_TIME") ~ get[Option[String]]("REMOTE_SITE_ID")).singleOpt)
      .map { case websiteId ~ createTime ~ remoteSiteId => Source(id, websiteId, createTime, remoteSiteId) }
  }

  /**
   * Delete Source by primary key
   */
  def deleteSource(source: Source): Boolean = DB.withConnection { implicit c =>
    SQL("DELETE FROM SOURCES s WHERE s.ID = {id}").on('id -> source.id).executeUpdate() == 1
  }

}					
