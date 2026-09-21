-- =============================================================
-- ArtConnectPro — Schéma MySQL
-- Exécuter en tant que : mysql -u root -p < schema.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS artconnect_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE artconnect_db;

-- -------------------------------------------------------------
-- discipline
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS discipline (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------
-- artist
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS artist (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(200) NOT NULL UNIQUE,
    bio           TEXT,
    birth_year    INT,
    contact_email VARCHAR(200),
    phone         VARCHAR(50),
    city          VARCHAR(100),
    website       VARCHAR(300),
    social_media  VARCHAR(300),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------
-- artist_discipline  (many-to-many)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS artist_discipline (
    artist_id     BIGINT NOT NULL,
    discipline_id BIGINT NOT NULL,
    PRIMARY KEY (artist_id, discipline_id),
    FOREIGN KEY (artist_id)     REFERENCES artist(id)     ON DELETE CASCADE,
    FOREIGN KEY (discipline_id) REFERENCES discipline(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- artwork
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS artwork (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    title         VARCHAR(300)   NOT NULL UNIQUE,
    creation_year INT,
    type          VARCHAR(100),
    medium        VARCHAR(200),
    dimensions    VARCHAR(200),
    description   TEXT,
    price         DECIMAL(15, 2) NOT NULL DEFAULT 0,
    status        ENUM('FOR_SALE','SOLD','EXHIBITED') NOT NULL DEFAULT 'FOR_SALE',
    artist_id     BIGINT         NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- artwork_tag
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS artwork_tag (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    artwork_id BIGINT       NOT NULL,
    name       VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (artwork_id) REFERENCES artwork(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- gallery
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS gallery (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    name          VARCHAR(200)   NOT NULL UNIQUE,
    address       VARCHAR(400),
    owner_name    VARCHAR(200),
    opening_hours VARCHAR(200),
    contact_phone VARCHAR(50),
    rating        DECIMAL(3, 1)  NOT NULL DEFAULT 0,
    website       VARCHAR(300),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------
-- exhibition
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exhibition (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(300) NOT NULL UNIQUE,
    start_date   DATE,
    end_date     DATE,
    description  TEXT,
    gallery_id   BIGINT       NOT NULL,
    curator_name VARCHAR(200),
    theme        VARCHAR(200),
    PRIMARY KEY (id),
    FOREIGN KEY (gallery_id) REFERENCES gallery(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- exhibition_artwork  (many-to-many)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exhibition_artwork (
    exhibition_id BIGINT NOT NULL,
    artwork_id    BIGINT NOT NULL,
    PRIMARY KEY (exhibition_id, artwork_id),
    FOREIGN KEY (exhibition_id) REFERENCES exhibition(id) ON DELETE CASCADE,
    FOREIGN KEY (artwork_id)    REFERENCES artwork(id)    ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- workshop
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS workshop (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    title            VARCHAR(300)   NOT NULL UNIQUE,
    date_time        DATETIME       NOT NULL,
    duration_minutes INT            NOT NULL DEFAULT 60,
    max_participants INT            NOT NULL DEFAULT 10,
    price            DECIMAL(10, 2) NOT NULL DEFAULT 0,
    instructor_id    BIGINT         NOT NULL,
    location         VARCHAR(300),
    description      TEXT,
    level            VARCHAR(50),
    PRIMARY KEY (id),
    FOREIGN KEY (instructor_id) REFERENCES artist(id) ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- community_member
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS community_member (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(200) NOT NULL UNIQUE,
    email           VARCHAR(200) NOT NULL UNIQUE,
    birth_year      INT,
    phone           VARCHAR(50),
    city            VARCHAR(100),
    membership_type VARCHAR(50)  NOT NULL DEFAULT 'free',
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------
-- member_discipline  (many-to-many : favorite disciplines)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS member_discipline (
    member_id     BIGINT NOT NULL,
    discipline_id BIGINT NOT NULL,
    PRIMARY KEY (member_id, discipline_id),
    FOREIGN KEY (member_id)     REFERENCES community_member(id) ON DELETE CASCADE,
    FOREIGN KEY (discipline_id) REFERENCES discipline(id)       ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- booking
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS booking (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    workshop_id    BIGINT      NOT NULL,
    member_id      BIGINT      NOT NULL,
    booking_date   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    PRIMARY KEY (id),
    UNIQUE KEY uq_booking (workshop_id, member_id),
    FOREIGN KEY (workshop_id) REFERENCES workshop(id)          ON DELETE CASCADE,
    FOREIGN KEY (member_id)   REFERENCES community_member(id)  ON DELETE CASCADE
);

-- -------------------------------------------------------------
-- review
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS review (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    member_id   BIGINT NOT NULL,
    artwork_id  BIGINT NOT NULL,
    rating      INT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    review_date DATE   NOT NULL DEFAULT (CURRENT_DATE),
    PRIMARY KEY (id),
    UNIQUE KEY uq_review (member_id, artwork_id),
    FOREIGN KEY (member_id)  REFERENCES community_member(id) ON DELETE CASCADE,
    FOREIGN KEY (artwork_id) REFERENCES artwork(id)          ON DELETE CASCADE
);
