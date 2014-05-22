-- livejazznear.me.sql
--
-- Copyright (c) 2014 Dufresne Management Consulting LLC.
-- -------------------------------------------------------------------------------------

-- Drop all tables
-- --------------------------------------------------------------------------------------
DROP TABLE PARTIES IF EXISTS;
DROP TABLE EVENTS IF EXISTS;
DROP TABLE SOURCES IF EXISTS;
DROP TABLE WEBSITES IF EXISTS;
DROP TABLE TYPES IF EXISTS;
DROP TABLE ALIASES IF EXISTS;


-- Tables Definition
-- --------------------------------------------------------------------------------------

-- PARTIES table definition
-- Used for both Artists and Venues
CREATE TABLE PARTIES (
    ID BIGINT IDENTITY NOT NULL PRIMARY KEY,
    STATUS TINYINT DEFAULT 1,                   -- Status of this entry (0: pending, 1: active)
    SOURCE_ID BIGINT NOT NULL,                  -- Identifying where we got this info - FK to SOURCES
    TYPE_ID BIGINT NOT NULL,                    -- Party type (ARTIST, VENUE)
    --
    NAME VARCHAR(255) NOT NULL,                 -- Party's name
    STREET_ADDRESS VARCHAR(255),                -- Party's address, single string for now. . .
    LOCATION_LAT REAL,                          -- Party's address latitude
    LOCATION_LNG REAL,                          -- Party's address longitude
    PHONE_NBR VARCHAR(25),                      -- Party's phone nbr
    WEBSITE_URL VARCHAR(255)                    -- Official Party's website
);


-- Index on Parties
CREATE INDEX parties_key_idx ON PARTIES (ID, TYPE_ID);
CREATE INDEX parties_status_idx ON PARTIES (TYPE_ID, STATUS);
CREATE INDEX parties_location_idx ON PARTIES (LOCATION_LAT, LOCATION_LNG);


-- EVENTS table definition
CREATE TABLE EVENTS (
    ID BIGINT IDENTITY NOT NULL PRIMARY KEY,
    STATUS TINYINT DEFAULT 1,                   -- Status of this entry (0: pending, 1: active)
    SOURCE_ID BIGINT NOT NULL,                  -- Identifying where we got this info - FK to SOURCES
    --
    ARTIST_ID BIGINT NOT NULL,                  -- FK TO PARTIES (Artist)
    VENUE_ID BIGINT NOT NULL,                   -- FK TO PARTIES (Venue)
    START_DATE DATE NOT NULL,                   -- Start date of event
    END_DATE DATE NOT NULL,                     -- End date of event, same as start date for single day event
    START_TIME BIGINT,                          -- Time the event is starting (nbr of min since start of day)
    END_TIME BIGINT                             -- Time the envent is ending, would be next day if less than start time
);


-- Index on Event dates
CREATE INDEX events_dates_idx ON EVENTS (START_DATE, END_DATE);
CREATE INDEX events_date_idx ON EVENTS (START_DATE);


-- SOURCES table definition
-- This indicates where we got the Artist, Venue, or Event or any other
-- items scouted from the web
CREATE TABLE SOURCES (
    ID BIGINT IDENTITY NOT NULL PRIMARY KEY,
    WEBSITE_ID BIGINT NOT NULL,                 -- Website where the item came from, FK to WEBSITES
    CREATE_TIME TIMESTAMP NOT NULL,             -- Time when the item was scouted from the remote site
    REMOTE_SITE_ID VARCHAR(100)                 -- Item ID on the remote site, to avoid duplicates
);


-- Index on Sources
CREATE INDEX sources_website_rid ON SOURCES (WEBSITE_ID, REMOTE_SITE_ID);


-- WEBSITES table definition
-- This table holds all the website we scout Events from
CREATE TABLE WEBSITES (
    ID BIGINT IDENTITY NOT NULL PRIMARY KEY,
    NAME VARCHAR(255) DEFAULT '',               -- Display name for website
    STATUS TINYINT DEFAULT 1,                   -- Status of this website (0: pending/hold, 1: active/scan)
    WEBSITE_URL VARCHAR (255) NOT NULL,         -- URL of the start page to crawl from
    CRAWLER_AGENT_NAME VARCHAR(100) NOT NULL    -- Akka Agent name crawling the website
);

-- Index on Websites
CREATE INDEX Websites_name ON WEBSITES (NAME);
CREATE INDEX Websites_status ON WEBSITES (STATUS);
CREATE INDEX Websites_url ON WEBSITES (WEBSITE_URL);


-- TYPES table definition
-- This table indicates various types used
CREATE TABLE TYPES (
    ID BIGINT IDENTITY NOT NULL PRIMARY KEY,
    CODE VARCHAR_IGNORECASE(80) NOT NULL UNIQUE    -- Type code e.g., 'ARTIST', 'VENUE', etc.
);

-- ALIASES table definition
-- This table defines aliases between parties
-- Keys are ordered (PARTY1_ID < PARTY2_ID) so se stored half the number of rows and when we apply
-- ratings, we roll up to lowest PARTY_ID
CREATE TABLE ALIASES (
    PARTY1_ID BIGINT NOT NULL,                  -- Party 1 of the alias
    PARTY2_ID BIGINT NOT NULL,                  -- Party 2 of the alias
    CONSTRAINT aliases_pkey PRIMARY KEY (PARTY1_ID, PARTY2_ID),
    CONSTRAINT aliases_order_chk CHECK (PARTY1_ID < PARTY2_ID)
);


-- Create configuration and metadata
-- --------------------------------------------------------------------------------------
INSERT INTO TYPES VALUES (1, 'Artist');
INSERT INTO TYPES VALUES (2, 'Venue');

