-- livejazznear.me.sql
--
-- Copyright (c) 2014 Dufresne Management Consulting LLC.
-- -------------------------------------------------------------------------------------

-- Testing Data
-- --------------------------------------------------------------------------------------
INSERT INTO WEBSITES VALUES (1, 'Hot House Jazz', 1, 'http://hothousejazz.com/calendar', 'hotHouseAgent');

INSERT INTO SOURCES VALUES (1, 1, '2014-04-25 00:25:31', 'ev123456789');

INSERT INTO PARTIES VALUES (1, 1, 1, 1, 'Satchmo and Ella', NULL, NULL, 'http://louisarmstrong.com/');
INSERT INTO PARTIES VALUES (2, 1, 1, 1, 'Ella Fitzgerald', NULL, NULL, 'http://louisarmstrong.com/');
INSERT INTO PARTIES VALUES (3, 1, 1, 2, 'Smalls Jazz Club', '183 West 10th Street, New York, NY 10014', '212-252-5091', 'http://www.smallsjazzclub.com/');
INSERT INTO PARTIES VALUES (4, 1, 1, 1, 'Ella and Dizzy', NULL, NULL, NULL);
INSERT INTO PARTIES VALUES (5, 1, 1, 2, 'Blue Notes', NULL, NULL, NULL);

INSERT INTO EVENTS  VALUES (1, 1, 1, 1, 3, '2014-04-25', '2015-04-25', '21:00:00', '00:30:00');
INSERT INTO EVENTS  VALUES (2, 1, 1, 2, 3, '2014-04-25', '2015-04-27', '19:00:00', '21:30:00');
INSERT INTO EVENTS  VALUES (3, 1, 1, 4, 5, '2014-04-26', '2015-04-26', '19:00:00', '21:30:00');

INSERT INTO ALIASES VALUES (1, 2);
INSERT INTO ALIASES VALUES (2, 4);


-- Sample queries
-- --------------------------------------------------------------------------------------

-- Select all active Artists
SELECT a.ID, a.NAME, a.WEBSITE_URL
       FROM PARTIES a WHERE a.TYPE_ID = 1 AND a.STATUS = 1
       ORDER BY a.NAME

-- Select event details for today
SELECT a.ID, a.NAME, a.WEBSITE_URL, 
       v.ID, v.NAME, v.STREET_ADDRESS, v.PHONE_NBR, v.WEBSITE_URL, 
       e.ID, e.START_DATE, e.END_DATE, e.START_TIME, e.END_TIME 
       FROM (EVENTS e INNER JOIN PARTIES a ON e.ARTIST_ID = a.ID) INNER JOIN PARTIES v ON e.VENUE_ID = v.ID
       WHERE e.START_DATE <= CURRENT_DATE() AND e.END_DATE >= CURRENT_DATE()
       ORDER BY a.NAME

-- Select all aliases of Artist 'Ella Fitzgerald' (ID = 2), including Ella (ID = 2)
SELECT a.ID, a.NAME, a.WEBSITE_URL
       FROM PARTIES a WHERE a.ID IN
       (SELECT a1.ID FROM (PARTIES a1 INNER JOIN ALIASES l ON a1.ID = l.PARTY1_ID) WHERE l.PARTY2_ID = 2
        UNION
        SELECT a2.ID FROM (PARTIES a2 INNER JOIN ALIASES l ON a2.ID = l.PARTY2_ID) WHERE l.PARTY1_ID = 2
        UNION SELECT ID FROM PARTIES WHERE ID = 2)
       ORDER BY a.NAME
       
-- Select all events of Artist 'Ella Fitzgerald' (ID = 2) including events for alias of Ella
SELECT a.ID, a.NAME, a.WEBSITE_URL, 
       v.ID, v.NAME, v.STREET_ADDRESS, v.PHONE_NBR, v.WEBSITE_URL, 
       e.ID, e.START_DATE, e.END_DATE, e.START_TIME, e.END_TIME 
       FROM (EVENTS e INNER JOIN PARTIES a ON e.ARTIST_ID = a.ID) INNER JOIN PARTIES v ON e.VENUE_ID = v.ID
       WHERE a.ID IN 
       (SELECT a1.ID FROM (PARTIES a1 INNER JOIN ALIASES l ON a1.ID = l.PARTY1_ID) WHERE l.PARTY2_ID = 2
        UNION
        SELECT a2.ID FROM (PARTIES a2 INNER JOIN ALIASES l ON a2.ID = l.PARTY2_ID) WHERE l.PARTY1_ID = 2
        UNION SELECT ID FROM PARTIES WHERE ID = 2)
       ORDER BY e.START_DATE, v.NAME


