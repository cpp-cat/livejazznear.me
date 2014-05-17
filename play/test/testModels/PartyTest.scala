/*
 * Copyright (c) 2014 Dufresne Management Consulting LLC.
 */
package testModels;

import scala.collection.Set
import org.scalatest._
import org.scalatestplus.play._
import anorm._
import play.api.db.DB
import play.api.Logger
import play.api.Play.current
import org.scalatest.SuiteMixin
import org.scalatest.Suite
import models.Artists
import models.Artist
import models.Artist
import models.Venues
import models.Venue

trait TestParty extends SuiteMixin { this: Suite =>

  abstract override def withFixture(test: NoArgTest) = {

    // pre test

    try super.withFixture(test) // To be stackable, must call super.withFixture
    finally {

      // post test
      DB.withConnection { implicit c =>
        SQL("DELETE FROM PARTIES").execute()
      }
    }
  }
}

class PartySpec extends PlaySpec with OneAppPerSuite with TestParty {

  def toSet[B](seq: Seq[Option[B]]): Set[B] = (for (Some(b) <- seq) yield b) toSet

  "The Artists object" must {

    "add Artist by specifying the primary key" in {
      Artists.addArtist(1, 1, 1, "Ella", Some("http://ellafitzerald.com")) mustBe Some(Artist(1, 1, 1, "Ella", Some("http://ellafitzerald.com")))
    }

    "add Artist w/o specifying the primary key" in {
      Artists.addArtist(1, 1, "Louis", Some("http://ellafitzerald.com")) match {
        case Some(artist) => Artists.getArtistById(artist.id) mustBe Some(artist)
        case None => fail()
      }
    }

    "return all Artist via getAllArtist" in {
      val testA = toSet(Seq(Artists.addArtist(1, 1, "Louis", Some("http://louisarmstrong.com")),
        Artists.addArtist(1, 1, "Ella", Some("http://ellafitzerald.com"))))

      val result = Artists.getAllArtists.toSet

      result mustBe testA
    }

    "find Artists by name via getArtistsByName" in {
      val testA = toSet(Seq(
        Artists.addArtist(1, 1, "Louis and Ella", Some("http://louisarmstrong.com")),
        Artists.addArtist(1, 1, "The Ella Fitzerald", Some("http://ellafitzerald.com")),
        Artists.addArtist(1, 1, "Ella", Some("http://ellafitzerald.com"))))
      testA.size mustBe 3

      // Some non matching cases
      Artists.addArtist(1, 1, "Johnny O'Neal Trio", Some("http://johnnyoneal.com"))
      Artists.addArtist(1, 1, "Spike and Friends", Some("http://smallsjazzclub.com"))
      Artists.addArtist(0, 1, "Ella la Douce", Some("http://ellafitzerald.com"))

      Artists.getArtistsByName("Ella").toSet mustBe testA
    }

    "delete an Artist via deleteArtist" in {
      val optA = Artists.addArtist(1, 1, "Louis", Some("http://louisarmstrong.com"))
      val testA = toSet(Seq(Artists.addArtist(1, 1, "Ella", Some("http://ellafitzerald.com"))))
      testA.size mustBe 1

      // delete OptA
      optA match {
        case Some(artist) => Artists.deleteArtist(artist) mustBe true
        case None => Some("failed insert") mustBe Some("None")
      }

      // read all remaining artists (which is Ella)
      val result = Artists.getAllArtists.toSet

      result mustBe testA
    }
  }

  "The Venues object" must {

    "add Venue by specifying the primary key" in {
      Venues.addVenue(1, 1, 1, "Smalls Jazz Club", Some("NYC"), None, Some("http://smallsjazzclub.com")) mustBe Some(Venue(1, 1, 1, "Smalls Jazz Club", Some("NYC"), None, Some("http://smallsjazzclub.com")))
    }

    "add Venue w/o specifying the primary key" in {
      Venues.addVenue(1, 1, "Blue Notes", Some("some address"), Some("212-555-1212"), Some("http://bluenotesnyc.com")) match {
        case Some(venue) => Venues.getVenueById(venue.id) mustBe Some(venue)
        case None => fail()
      }
    }

    "return all Venues via getAllVenues" in {
      val testA = toSet(Seq(
        Venues.addVenue(1, 1, "Club 1", None, None, Some("http://club1.com")),
        Venues.addVenue(1, 1, "Club 2", None, None, Some("http://club2.com"))))
      testA.size mustBe 2

      val result = Venues.getAllVenues.toSet

      result mustBe testA
    }

    "find Venues by name via getVenuesByName" in {
      val testA = toSet(Seq(
        Venues.addVenue(1, 1, "Club 1", None, None, Some("http://club1.com")),
        Venues.addVenue(1, 1, "The Cotton Club", None, None, Some("http://club2.com")),
        Venues.addVenue(1, 1, "Smalls Club of Jazz", None, None, Some("http://club3.com")),
        Venues.addVenue(1, 1, "Smalls Club's of Jazz", None, None, Some("http://club3.com"))))
      testA.size mustBe 4

      // Some non matching cases
      Venues.addVenue(1, 1, "The Cotton Band", None, None, Some("http://club2.com"))
      Venues.addVenue(0, 1, "The Cotton Club2", None, None, Some("http://club2.com"))

      Venues.getVenuesByName("Club").toSet mustBe testA
    }

    "delete a Venue via deleteVenue" in {
      val optA = Venues.addVenue(1, 1, "Club 1", None, None, Some("http://club1.com"))
      val testA = toSet(Seq(Venues.addVenue(1, 1, "Club 2", None, None, Some("http://club2.com"))))
      testA.size mustBe 1

      // delete OptA
      optA match {
        case Some(venue) => Venues.deleteVenue(venue) mustBe true
        case None => fail()
      }

      // read all remaining artists (which is Club 2)
      val result = Venues.getAllVenues.toSet

      result mustBe testA
    }
  }

}

