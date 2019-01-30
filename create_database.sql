--
-- File generated with SQLiteStudio v3.2.1 on mer. janv. 30 15:19:26 2019
--
-- Text encoding used: UTF-8
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Table: BOOK
CREATE TABLE BOOK (ID STRING PRIMARY KEY, CODE STRING, ID_CONTACT INT REFERENCES BOOK_CONTACT (ID_CONTACT));

-- Table: BOOK_CONTACT
CREATE TABLE BOOK_CONTACT (ID_BOOK STRING REFERENCES BOOK (ID), ID_CONTACT STRING REFERENCES CONTACT (ID), PRIMARY KEY (ID_BOOK, ID_CONTACT));

-- Table: CONTACT
CREATE TABLE CONTACT (ID STRING PRIMARY KEY, NAME STRING, EMAIL STRING, PHONE STRING, ID_TYPE INT REFERENCES TYPE (NUM));

-- Table: TYPE
CREATE TABLE TYPE (NUM INT PRIMARY KEY, VAR STRING);
INSERT INTO TYPE (NUM, VAR) VALUES (1, 'Perso');
INSERT INTO TYPE (NUM, VAR) VALUES (2, 'Pro');

COMMIT TRANSACTION;
PRAGMA foreign_keys = on;