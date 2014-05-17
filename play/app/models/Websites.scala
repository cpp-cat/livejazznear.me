/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import play.api.Logger

// -----------------------------------------------------------------------------------------------------------
// WEBSITE Domain Class
// -----------------------------------------------------------------------------------------------------------

/**
 * Case class for Website
 *
 * Domain class representing a website that we crawl to get Events and Artists 
 */
case class Website(val id: Long, val name: String, val status: Byte, val websiteUrl: String, val agentName: String)

object Websites {

  val PENDING: Byte = 0
  val ACTIVE: Byte = 1

  /**
   * Get Website by primary key
   */
  def getWebsiteById(id: Long): Option[Website] = DB.withConnection { implicit c =>
    SQL("SELECT w.NAME, w.STATUS, w.WEBSITE_URL, w.CRAWLER_AGENT_NAME FROM WEBSITES w WHERE w.ID = {id}")
      .on('id -> id)
      .as((get[String]("NAME") ~ get[Byte]("STATUS") ~ get[String]("WEBSITE_URL") ~ get[String]("CRAWLER_AGENT_NAME")).singleOpt)
      .map { case name ~ status ~ websiteUrl ~ agentName => Website(id, name, status, websiteUrl, agentName) }
  }

  /**
   * Get Website by status
   */
  def getWebsiteByStatus(status: Byte=ACTIVE): List[Website] = DB.withConnection { implicit c =>

    SQL(s"SELECT w.ID, w.NAME, w.WEBSITE_URL, w.CRAWLER_AGENT_NAME FROM WEBSITES w WHERE w.STATUS = {status}")
      .on('status -> status)
      .as(get[Long]("ID") ~ get[String]("NAME") ~ get[String]("WEBSITE_URL") ~ get[String]("CRAWLER_AGENT_NAME")
        map { case id ~ name ~ websiteUrl ~ agentName => Website(id, name, status, websiteUrl, agentName) } *)
  }

  /**
   * Get Website by URL
   */
  def getWebsiteByUrl(websiteUrl: String): List[Website] = DB.withConnection { implicit c =>

    SQL(s"SELECT w.ID, w.NAME, w.STATUS, w.WEBSITE_URL, w.CRAWLER_AGENT_NAME FROM WEBSITES w WHERE w.WEBSITE_URL LIKE {websiteUrl}")
      .on('websiteUrl -> ('%'+websiteUrl+'%'))
      .as(get[Long]("ID") ~ get[String]("NAME") ~ get[Byte]("STATUS") ~ get[String]("WEBSITE_URL") ~ get[String]("CRAWLER_AGENT_NAME")
        map { case id ~ name ~ status ~ websiteUrl ~ agentName => Website(id, name, status, websiteUrl, agentName) } *)
  }

  /**
   * Add Website
   */
  def addWebsite(name: String, status: Byte, websiteUrl: String, agentName: String): Option[Website] = DB.withConnection { implicit c =>
    val oId = SQL("""INSERT INTO WEBSITES(NAME, STATUS, WEBSITE_URL, CRAWLER_AGENT_NAME) 
    		VALUES ({name}, {status}, {websiteUrl}, {agentName})""")
      .on('name -> name, 'status -> status, 'websiteUrl -> websiteUrl, 'agentName -> agentName).executeInsert()

    oId match {
      case Some(id) => Some(Website(id, name, status, websiteUrl, agentName))
      case _ => None
    }
  }

  /**
   * Delete Website by primary key
   */
  def deleteWebsite(website: Website): Boolean = DB.withConnection { implicit c =>
    SQL("DELETE FROM WEBSITES w WHERE w.ID = {id}").on('id -> website.id).executeUpdate() == 1
  }

}					
