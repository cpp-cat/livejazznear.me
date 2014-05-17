#-- livejazznear.me.sql
#--
#-- Copyright (c) 2014 Dufresne Management Consulting LLC.
#-- -------------------------------------------------------------------------------------

# --- !Ups

CREATE TABLE PARTIES (
    ID BIGINT IDENTITY NOT NULL PRIMARY KEY,
    STATUS TINYINT DEFAULT 1,                   
    SOURCE_ID BIGINT NOT NULL,                  
    TYPE_ID BIGINT NOT NULL,                    
    NAME VARCHAR(255) NOT NULL,                 
    STREET_ADDRESS VARCHAR(255),                
    PHONE_NBR VARCHAR(25),                      
    WEBSITE_URL VARCHAR(255)                    
);


CREATE INDEX parties_key_idx ON PARTIES (ID, TYPE_ID);
CREATE INDEX parties_status_idx ON PARTIES (TYPE_ID, STATUS);
CREATE INDEX parties_name_idx ON PARTIES (NAME);


CREATE TABLE EVENTS (
    ID BIGINT IDENTITY NOT NULL PRIMARY KEY,
    STATUS TINYINT DEFAULT 1,                   
    SOURCE_ID BIGINT NOT NULL,                  
    ARTIST_ID BIGINT NOT NULL,                  
    VENUE_ID BIGINT NOT NULL,                   
    START_DATE DATE NOT NULL,                   
    END_DATE DATE NOT NULL,                     
    START_TIME BIGINT,                            
    END_TIME BIGINT                               
);


CREATE INDEX events_dates_idx ON EVENTS (START_DATE, END_DATE);
CREATE INDEX events_date_idx ON EVENTS (START_DATE);


CREATE TABLE SOURCES (
    ID BIGINT IDENTITY NOT NULL PRIMARY KEY,
    WEBSITE_ID BIGINT NOT NULL,                 
    CREATE_TIME TIMESTAMP NOT NULL,             
    REMOTE_SITE_ID VARCHAR(100)                 
);

CREATE INDEX sources_website_rid ON SOURCES (WEBSITE_ID, REMOTE_SITE_ID);

CREATE TABLE WEBSITES (
    ID BIGINT IDENTITY NOT NULL PRIMARY KEY,
    NAME VARCHAR(255) DEFAULT '',               
    STATUS TINYINT DEFAULT 1,                   
    WEBSITE_URL VARCHAR (255) NOT NULL,                  
    CRAWLER_AGENT_NAME VARCHAR(100) NOT NULL          
);

CREATE INDEX Websites_name ON WEBSITES (NAME);
CREATE INDEX Websites_status ON WEBSITES (STATUS);
CREATE INDEX Websites_url ON WEBSITES (WEBSITE_URL);


CREATE TABLE TYPES (
    ID BIGINT IDENTITY NOT NULL PRIMARY KEY,
    CODE VARCHAR_IGNORECASE(80) NOT NULL UNIQUE    
);

CREATE TABLE ALIASES (
    PARTY1_ID BIGINT NOT NULL,                  
    PARTY2_ID BIGINT NOT NULL,                  
    CONSTRAINT aliases_pkey PRIMARY KEY (PARTY1_ID, PARTY2_ID),
    CONSTRAINT aliases_order_chk CHECK (PARTY1_ID < PARTY2_ID)
);


INSERT INTO TYPES VALUES (1, 'Artist');
INSERT INTO TYPES VALUES (2, 'Venue');


# --- !Downs

DROP TABLE PARTIES IF EXISTS;
DROP TABLE EVENTS IF EXISTS;
DROP TABLE SOURCES IF EXISTS;
DROP TABLE WEBSITES IF EXISTS;
DROP TABLE TYPES IF EXISTS;
DROP TABLE ALIASES IF EXISTS;

