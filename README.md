# LiveJazz.me Application

> This web application is based on Play Framework and Akka using Scala language.

# Product backlog

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
- Master Artist, Venue and Event to avoid duplicates- Create a security model with a user login using email and facebook(?) 
- Create a database of Artist, Venue and Events managed by an agent.
- Create a site crawler to fetch Events (Artist, Venue, and Event) from configured sites.
- Create a site crawler to fetch Artist and Venue information from configured sites (wikipedia?)
- Create a Calendar of all events.
- Create a user profile and personalized calendar - past events and future events
- Collect user feedback on Artist and Venue of attended events 
- Collect user comments and star rating on Artists and Venues

## Recommender Engine
- Make recommendations using Collaborative Filtering
- Make recommendations based on content-based search criteria.
- Make recommendations based on what's on nearby

# Sprint 1: Artist, Venue and Event

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

