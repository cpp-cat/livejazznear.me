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
import play.api.libs.ws._
import scala.util.{ Success, Failure }
import scala.concurrent.Future
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import utils.NickelException

/**
 * Application Controller
 *
 */
object Application extends Controller {

  val eventCrawlerActor = Akka.system.actorOf(Props[HHJEventCrawlerActor], name = "hhjEventCrawlerActor")
  implicit val timeout = Timeout(10 seconds)

  // Need to redo the view and put a login page
  //Example using the Actor to parse the events from sample file, usinf anorm database access
  def index = Action.async {
    val website = Website(0, "Hot House Jazz Tester", 1, "data/hhj-short-test.html", "hhjEventCrawlerActor")
    val futureInt = eventCrawlerActor ? website
    futureInt map { n_events => Ok(views.html.index(s"Got $n_events new events!")) }
  }

  // Example (standalone) using Web Service call
  def ws = Action.async {
    val holder: WSRequestHolder = WS.url("http://api.geonames.org/wikipediaSearchJSON")
    val complexHolder: WSRequestHolder = holder.withHeaders("Accept" -> "application/json").withRequestTimeout(10000)
      .withQueryString("q" -> "london")
      .withQueryString("maxRows" -> "10")
      .withQueryString("username" -> "regency901")

    case class Entry(
      lang: String, title: String, summary: String,
      feature: String, countryCode: String,
      elevation: Int, lat: Double, lng: Double,
      wikipediaUrl: String, thumbnailImg: String, rank: Int)

    implicit val entryReads: Reads[Entry] = (
      (JsPath \ "lang").read[String] and
      (JsPath \ "title").read[String] and
      (JsPath \ "summary").read[String] and
      (JsPath \ "feature").read[String] and
      (JsPath \ "countryCode").read[String] and
      (JsPath \ "elevation").read[Int] and
      (JsPath \ "lat").read[Double] and
      (JsPath \ "lng").read[Double] and
      (JsPath \ "wikipediaUrl").read[String] and
      (JsPath \ "thumbnailImg").read[String] and
      (JsPath \ "rank").read[Int])(Entry.apply _)

    //val futureResponse: Future[WSResponse] = complexHolder.get()
    val futureEntries: Future[Seq[Entry]] = complexHolder.get() map { response =>
      (response.json \ "geonames").validate[Seq[Entry]] match {
        case s: JsSuccess[Seq[Entry]] => s.get
        case e: JsError => throw new NickelException("Application.ws: Cannot create seq of Entry!")
      }
    }

    futureEntries map { result => Ok(result.foldLeft[String]("")((s: String, e: Entry) => s + e.toString() + "\n\n")) }
  }

  // Example (standalone) using H2 in memory database with Slick database access
  // see http://slick.typesafe.com/doc/2.0.2/gettingstarted.html
  // see also http://blog.lunatech.com/2013/08/29/play-slick-evolutions to use evolution
  def slick = Action {
    import scala.slick.driver.H2Driver.simple._
    Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver") withSession {
      implicit session =>

        // SCHEMA DEFINITION
        // --------------------------------------------------------------------------------
        // Definition of the SUPPLIERS table
        class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
          def id = column[Int]("SUP_ID", O.PrimaryKey) // This is the primary key column
          def name = column[String]("SUP_NAME")
          def street = column[String]("STREET")
          def city = column[String]("CITY")
          def state = column[String]("STATE")
          def zip = column[String]("ZIP")
          // Every table needs a * projection with the same type as the table's type parameter
          def * = (id, name, street, city, state, zip)
        }
        val suppliers = TableQuery[Suppliers]

        // Definition of the COFFEES table
        class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
          def name = column[String]("COF_NAME", O.PrimaryKey)
          def supID = column[Int]("SUP_ID")
          def price = column[Double]("PRICE")
          def sales = column[Int]("SALES")
          def total = column[Int]("TOTAL")
          def * = (name, supID, price, sales, total)
          // A reified foreign key relation that can be navigated to create a join
          def supplier = foreignKey("SUP_FK", supID, suppliers)(_.id)
        }
        val coffees = TableQuery[Coffees]

        // POPULATING THE DATABASE
        // --------------------------------------------------------------------------------
        // Create the tables, including primary and foreign keys
        (suppliers.ddl ++ coffees.ddl).create

        // Insert some suppliers
        suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199")
        suppliers += (49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460")
        suppliers += (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966")

        // Insert some coffees (using JDBC's batch insert feature, if supported by the DB)
        coffees ++= Seq(
          ("Colombian", 101, 7.99, 0, 0),
          ("French_Roast", 49, 8.99, 0, 0),
          ("Espresso", 150, 9.99, 0, 0),
          ("Colombian_Decaf", 101, 8.99, 0, 0),
          ("French_Roast_Decaf", 49, 9.99, 0, 0))

        // QUERYING THE DATABASE
        // --------------------------------------------------------------------------------
        // Iterate through all coffees and output them
        val s1 = "Iterate through all coffees and output them:\n"
        // note, the tuple is (name, supID, price, sales, total) as per the select * define above
        val s2: String = coffees.foldLeft[String](s1)((s: String, c) => s + c._1 + ", " + c._2 + ", " + c._3 + ", " + c._4 + ", " + c._5 + "\n")

        //        coffees foreach {
        //          case (name, supID, price, sales, total) =>
        //            println("  " + name + "\t" + supID + "\t" + price + "\t" + sales + "\t" + total)
        //        }
        // Perform a join to retrieve coffee names and supplier names for
        // all coffees costing less than $9.00
        val q2 = for {
          c <- coffees if c.price < 9.0
          s <- suppliers if s.id === c.supID
        } yield (c.name, s.name, c.price)

        val s3 = s2 + "\n Perform a join to retrieve coffee names, supplier names and price for all coffees costing less than $9.00:\n"
        val s4: String = q2.foldLeft[String](s3)((s: String, c) => s + c._1 + ", " + c._2 + ", " + c._3 + "\n")

        // Another way to perform the join by using the foreign key relationship
        val q3 = for {
          c <- coffees if c.price < 9.0
          s <- c.supplier
        } yield (c.name, s.name, c.price)

        val s5 = s4 + "\n Another way to peform the join by using the foreign key relationship:\n"
        val s6: String = q2.foldLeft[String](s5)((s: String, c) => s + c._1 + ", " + c._2 + ", " + c._3 + "\n")
        Ok(s6)
    }
  }
}
