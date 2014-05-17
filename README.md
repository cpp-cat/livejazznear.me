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


# Sprint 2: Crawling the Web for Artists, Venues and Events

- Version: 0.1.2
- Start Date: 5/17/2014
- Target Date: 6/13/2014
- Actual Date: 6/XX/2014

## Product Features:
- Create a site crawler to fetch Events (Artist, Venue, and Event) from configured sites.
- Search web to find Artist and Venue official website
- Master Artist, Venue and Event to avoid duplicates, link Artist playing in different bands

## User Stories
- Find a crawler utility
- Create unit test for crawling sample events
- Save page to crawl from locally for development purpose
- Use Agents with supervision to craw and insert Artist, Venue, and Event in database
- Use google search to find official website of Artist and Venue
- Master Artist and Venue - find duplicates
- Master Artist and Venue - link same Artist playing in different bands


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

