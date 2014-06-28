# LiveJazzNear.me Application

> This web application is based on Play Framework and Akka using Scala language.

# Product backlog

## Mobile Features
- Create single page mobile app using PhoneGap, Backbone, Handlebar and jQuery Mobile
- Develop media files for backgrounds and logo
- Put in place a left menu and right pane for dialog with left/right page navigation
- Simple login using email address
- Loging using OAuth with Google and Facebook
- Calendar display page
- Calendar search events by dates, venues and artists
- Calendar search events by geo-location (here and now)
- Search result page without recommendations
- Search result page with recommendations on top
- Search result page with recommendations and features events
- User profile page
- Add past activity to user profile page
- Artist profile page with user discussion with star rating
- Venue profile page with user discussion and star rating
- Personal calendar for events of interest
- A 2-questions survey
- Event submission page

## Font End Features
- Create an index.html with a login
- Create a configuration page to setup sites to crawl, agent to use, and crawling schedule
- Create a dashboard page to monitor crawling activities
- Make a user profile page
- Make an Event submission page
- Make a master record quality workstation (Artist and Event)
- Create an Artist profile page with user discussion with star rating
- Create a Venue profile page with user discussion and star rating

## Data Management Features
- Master Artist, Venue and Event to avoid duplicates, link Artist playing in different bands
- Crawl web to find Artist and Venue official website
- Use google search to find official website of Artist and Venue
- Master Artist and Venue - link same Artist playing in different bands
- Investigate if need Akka Circuit Breaker pattern or supervision to DB agent
- Create a security model with a user login using email and OAuth 
- Create a Calendar of all events.
- Create a user profile and personalized calendar - past events and future events
- Collect user feedback on Artist and Venue of attended events 
- Collect user comments and star rating on Artists and Venues

## Recommender Engine
- Make recommendations using Collaborative Filtering
- Make recommendations based on content-based search criteria.
- Make recommendations based on what's on nearby


# Sprint 4: Adding Spark 1.0.0

- Version: 0.1.4
- Start Date: 6/21/2014
- Target Date: 6/28/2014
- Actual Date: 6/28/2014

## Product Features:
- Adding Spark with a simple test

## User Stories Sprint Backlog

## Completed User Stories
- Spark 1.0.0 uses Akka 2.2.3 with is compatible with Play 2.2.1. See the sample project in
  Play 2.2.1 workspace.
- Forked Spark 1.1.0-SNAPSHOT to upgrade Akka to Akka 2.3.3 and protobuf to version 2.5.0
  (see https://groups.google.com/forum/#!topic/akka-user/cI4CEKEJvfs)
- Installed Maven locally to build Spark into local repository, added local repository
  to build.sbt of this project.
- Added simple route /spark to test spark. Does not work properly, Spark needs more work
  in order to upgrade Akka.

# Sprint 3: Upgrading to Play 2.3.0, Akka 2.3.3, Activator 1.2.2, Scala 2.10.4

- Version: 0.1.3
- Start Date: 6/15/2014
- Target Date: 6/28/2014
- Actual Date: 6/21/2014

## Product Features:
- Upgrading PLay & Akka to latest version
- Implementing a WebService call using WS with json on ajax
- Adding Typesafe Slick to the project and implementing a demonstration of it's use

## User Stories Sprint Backlog

## Completed User Stories
- Upgrading to latest Ractive Platform using Typesafe Activator 1.2.2
- Removing helper AnormExtension (columnToFloat and floatToStatement) since it is now part of anorm in Play 2.3
- Getting Events information using WebService with Play WS API (upgraded in Play 2.3)
- Implementing a WebService call using WS with json on ajax 
- Adding route /ws and implementing in controllers.Application.ws using web services from http://www.geonames.org/
- Adding Typesafe Slick to the project and implementing a demonstration of it's use
- Ading route /slick and implementing demonstration in controllers.Application.slick, following http://slick.typesafe.com/doc/2.0.2/gettingstarted.html


# Sprint 2: Crawling the Web for Artists, Venues and Events

- Version: 0.1.2
- Start Date: 5/17/2014
- Target Date: 6/13/2014
- Actual Date: 6/15/2014

## Product Features:
- Create a site crawler to fetch Events (Artist, Venue, and Event) from configured sites.
- Master Artist, Venue and Event to avoid duplicates (Master by Name)

## User Stories Sprint Backlog

## Completed User Stories
- Use Agent to crawl and insert Artist, Venue, and Event in database (load website page from file)
- Find a crawler utility (used http://htmlcleaner.sourceforge.net/index.php)
- Create unit test for crawling sample events
- Save page sample locally to crawl for development purpose
- Added longitude and latitude to Party corresponding to address location.
- Implement HHJ crawler to find Events, Artists, and Venue
- Implement an Event crawler that add Events, Artists, and Venues into database
- Artists and Venues are mastered by exact name match (not case sensitive)


# Sprint 1: Artist, Venue and Event Domain Model

- Version: 0.1.1
- Start Date: 4/20/2014
- Target Date: 5/15/2014
- Actual Date: 5/16/2014

## Product Features:
- Create a database of Artist, Venue and Events using Anorm database access layer.

## User Stories
- Configure H2 database for development
- Enabling Anorm database access
- Enabling evolution database schema tracking
- Instaling ScalaTest for unit testing
- Define logical model for Artist, Venue and Event
- Create an agent to insert / read Artist, Venue and Event from database
- Also added Source and Website domain model.
- Added unit test cases for each domain class.

