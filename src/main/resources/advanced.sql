-- =============================================================
-- ArtConnectPro — Fonctionnalités BD avancées
-- Exécuter APRÈS schema.sql et data.sql
-- =============================================================

USE artconnect_db;

-- -------------------------------------------------------------
-- TABLE D'AUDIT (pour le trigger de statut des œuvres)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS artwork_audit (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    artwork_id    BIGINT,
    artwork_title VARCHAR(300),
    old_status    VARCHAR(20),
    new_status    VARCHAR(20),
    changed_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by    VARCHAR(100) NOT NULL DEFAULT 'system',
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------
-- INDEX
-- Objectif : accélérer les filtres et tris les plus fréquents
-- -------------------------------------------------------------

-- idx_artist_city : accélère la recherche d'artistes par ville
CREATE INDEX idx_artist_city       ON artist(city);

-- idx_artwork_status : accélère le filtre par statut (FOR_SALE, SOLD, EXHIBITED)
CREATE INDEX idx_artwork_status    ON artwork(status);

-- idx_workshop_datetime : accélère le tri/filtre des ateliers par date
CREATE INDEX idx_workshop_datetime ON workshop(date_time);

-- idx_booking_member : accélère la récupération des réservations d'un membre
CREATE INDEX idx_booking_member    ON booking(member_id);

-- idx_review_artwork : accélère le calcul des notes moyennes par œuvre
CREATE INDEX idx_review_artwork    ON review(artwork_id);

-- -------------------------------------------------------------
-- VUES
-- -------------------------------------------------------------

-- Vue 1 : artistes actifs avec leurs disciplines (sécurité : masque bio et coordonnées privées)
CREATE OR REPLACE VIEW v_active_artists AS
SELECT
    a.id,
    a.name,
    a.city,
    a.contact_email,
    a.birth_year,
    GROUP_CONCAT(d.name ORDER BY d.name SEPARATOR ', ') AS disciplines
FROM artist a
LEFT JOIN artist_discipline ad ON a.id = ad.artist_id
LEFT JOIN discipline d         ON ad.discipline_id = d.id
WHERE a.is_active = TRUE
GROUP BY a.id, a.name, a.city, a.contact_email, a.birth_year;

-- Vue 2 : expositions actuellement en cours
CREATE OR REPLACE VIEW v_current_exhibitions AS
SELECT
    e.id,
    e.title,
    e.start_date,
    e.end_date,
    e.theme,
    e.curator_name,
    g.name    AS gallery_name,
    g.address AS gallery_address,
    COUNT(ea.artwork_id) AS artwork_count
FROM exhibition e
JOIN gallery g                ON e.gallery_id = g.id
LEFT JOIN exhibition_artwork ea ON e.id = ea.exhibition_id
WHERE CURDATE() BETWEEN e.start_date AND e.end_date
GROUP BY e.id, e.title, e.start_date, e.end_date, e.theme, e.curator_name,
         g.name, g.address;

-- Vue 3 : disponibilité des ateliers (places restantes)
CREATE OR REPLACE VIEW v_workshop_availability AS
SELECT
    w.id,
    w.title,
    w.date_time,
    w.price,
    w.level,
    w.location,
    a.name                                     AS instructor_name,
    w.max_participants,
    COUNT(b.id)                                AS booked_count,
    (w.max_participants - COUNT(b.id))         AS available_spots
FROM workshop w
JOIN artist a      ON w.instructor_id = a.id
LEFT JOIN booking b ON w.id = b.workshop_id
GROUP BY w.id, w.title, w.date_time, w.price, w.level,
         w.location, a.name, w.max_participants;

-- Vue 4 : résumé des œuvres avec note moyenne et nombre de reviews
CREATE OR REPLACE VIEW v_artwork_summary AS
SELECT
    aw.id,
    aw.title,
    aw.type,
    aw.price,
    aw.status,
    aw.creation_year,
    ar.name                  AS artist_name,
    COUNT(r.id)              AS review_count,
    ROUND(AVG(r.rating), 1) AS avg_rating
FROM artwork aw
JOIN artist ar     ON aw.artist_id = ar.id
LEFT JOIN review r ON aw.id = r.artwork_id
GROUP BY aw.id, aw.title, aw.type, aw.price, aw.status, aw.creation_year, ar.name;

-- -------------------------------------------------------------
-- TRIGGERS
-- -------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_check_exhibition_dates;
DROP TRIGGER IF EXISTS trg_check_booking_capacity;
DROP TRIGGER IF EXISTS trg_audit_artwork_status;

DELIMITER //

-- Trigger 1 : cohérence des dates d'exposition (BEFORE INSERT)
-- Empêche l'insertion d'une exposition dont la date de fin précède la date de début
CREATE TRIGGER trg_check_exhibition_dates
BEFORE INSERT ON exhibition
FOR EACH ROW
BEGIN
    IF NEW.end_date IS NOT NULL AND NEW.start_date IS NOT NULL
       AND NEW.end_date <= NEW.start_date THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La date de fin doit être postérieure à la date de début';
    END IF;
END//

-- Trigger 2 : capacité maximale des ateliers (BEFORE INSERT sur booking)
-- Bloque l'inscription si l'atelier est déjà complet
CREATE TRIGGER trg_check_booking_capacity
BEFORE INSERT ON booking
FOR EACH ROW
BEGIN
    DECLARE v_booked INT;
    DECLARE v_max    INT;
    SELECT COUNT(*)         INTO v_booked FROM booking  WHERE workshop_id = NEW.workshop_id;
    SELECT max_participants INTO v_max    FROM workshop WHERE id          = NEW.workshop_id;
    IF v_booked >= v_max THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cet atelier est complet';
    END IF;
END//

-- Trigger 3 : audit des changements de statut des œuvres (AFTER UPDATE)
-- Enregistre chaque changement de statut dans artwork_audit
CREATE TRIGGER trg_audit_artwork_status
AFTER UPDATE ON artwork
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO artwork_audit (artwork_id, artwork_title, old_status, new_status)
        VALUES (OLD.id, OLD.title, OLD.status, NEW.status);
    END IF;
END//

DELIMITER ;

-- -------------------------------------------------------------
-- FONCTIONS ET PROCÉDURES STOCKÉES
-- -------------------------------------------------------------
DROP FUNCTION  IF EXISTS fn_workshop_participant_count;
DROP FUNCTION  IF EXISTS fn_artist_total_value;
DROP PROCEDURE IF EXISTS sp_book_workshop;
DROP PROCEDURE IF EXISTS sp_create_exhibition;
DROP PROCEDURE IF EXISTS sp_member_full_report;

DELIMITER //

-- Fonction 1 : nombre de participants inscrits à un atelier
CREATE FUNCTION fn_workshop_participant_count(p_workshop_id BIGINT)
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE cnt INT;
    SELECT COUNT(*) INTO cnt FROM booking WHERE workshop_id = p_workshop_id;
    RETURN cnt;
END//

-- Fonction 2 : valeur totale du catalogue d'un artiste (somme des prix des œuvres)
CREATE FUNCTION fn_artist_total_value(p_artist_name VARCHAR(200))
RETURNS DECIMAL(20, 2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE total DECIMAL(20, 2);
    SELECT COALESCE(SUM(aw.price), 0) INTO total
    FROM artwork aw
    JOIN artist ar ON aw.artist_id = ar.id
    WHERE ar.name = p_artist_name;
    RETURN total;
END//

-- Procédure 1 : réserver un atelier de manière transactionnelle
-- Vérifie la disponibilité, la non-duplication et insère la réservation atomiquement
CREATE PROCEDURE sp_book_workshop(
    IN  p_workshop_id BIGINT,
    IN  p_member_id   BIGINT,
    OUT p_status      VARCHAR(100)
)
BEGIN
    DECLARE v_booked  INT;
    DECLARE v_max     INT;
    DECLARE v_already INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status = 'ERREUR : échec de la transaction';
    END;

    START TRANSACTION;

    SELECT COUNT(*) INTO v_already FROM booking
    WHERE workshop_id = p_workshop_id AND member_id = p_member_id;

    IF v_already > 0 THEN
        ROLLBACK;
        SET p_status = 'ERREUR : membre déjà inscrit';
    ELSE
        SELECT COUNT(*)         INTO v_booked FROM booking  WHERE workshop_id = p_workshop_id;
        SELECT max_participants INTO v_max    FROM workshop WHERE id          = p_workshop_id;

        IF v_booked >= v_max THEN
            ROLLBACK;
            SET p_status = 'ERREUR : atelier complet';
        ELSE
            INSERT INTO booking (workshop_id, member_id, booking_date, payment_status)
            VALUES (p_workshop_id, p_member_id, NOW(), 'PENDING');
            COMMIT;
            SET p_status = 'SUCCÈS';
        END IF;
    END IF;
END//

-- Procédure 2 : créer une nouvelle exposition associée à une galerie existante
CREATE PROCEDURE sp_create_exhibition(
    IN p_title        VARCHAR(300),
    IN p_start_date   DATE,
    IN p_end_date     DATE,
    IN p_gallery_name VARCHAR(200),
    IN p_curator      VARCHAR(200),
    IN p_theme        VARCHAR(200)
)
BEGIN
    DECLARE v_gallery_id BIGINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT id INTO v_gallery_id FROM gallery WHERE name = p_gallery_name LIMIT 1;

    IF v_gallery_id IS NULL THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Galerie introuvable';
    ELSE
        INSERT INTO exhibition (title, start_date, end_date, gallery_id, curator_name, theme)
        VALUES (p_title, p_start_date, p_end_date, v_gallery_id, p_curator, p_theme);
        COMMIT;
    END IF;
END//

-- Procédure 3 : rapport complet d'un membre (ateliers réservés + avis laissés)
CREATE PROCEDURE sp_member_full_report(IN p_member_name VARCHAR(200))
BEGIN
    -- Informations du membre
    SELECT cm.id, cm.name, cm.email, cm.city, cm.membership_type
    FROM community_member cm
    WHERE cm.name = p_member_name;

    -- Ateliers réservés
    SELECT w.title AS atelier, w.date_time, b.payment_status
    FROM booking b
    JOIN community_member cm ON b.member_id   = cm.id
    JOIN workshop w          ON b.workshop_id = w.id
    WHERE cm.name = p_member_name
    ORDER BY w.date_time;

    -- Avis laissés sur des œuvres
    SELECT aw.title AS oeuvre, r.rating, r.comment, r.review_date
    FROM review r
    JOIN community_member cm ON r.member_id  = cm.id
    JOIN artwork aw          ON r.artwork_id = aw.id
    WHERE cm.name = p_member_name
    ORDER BY r.review_date DESC;
END//

DELIMITER ;

-- -------------------------------------------------------------
-- SCÉNARIO TRANSACTIONNEL DE TEST
-- Inscrire un membre à plusieurs ateliers de façon atomique.
-- Si une inscription échoue, toutes les suivantes sont abandonnées.
-- -------------------------------------------------------------
/*
-- Décommenter pour tester :
SET @s1 = '', @s2 = '', @s3 = '';
CALL sp_book_workshop(1, 2, @s1);
CALL sp_book_workshop(2, 2, @s2);
CALL sp_book_workshop(3, 2, @s3);
SELECT @s1 AS inscription_1, @s2 AS inscription_2, @s3 AS inscription_3;

-- Vérifier l'audit après un changement de statut :
UPDATE artwork SET status = 'SOLD' WHERE title = 'Mona Lisa';
SELECT * FROM artwork_audit;

-- Utiliser les fonctions :
SELECT fn_workshop_participant_count(1) AS participants_atelier_1;
SELECT fn_artist_total_value('Leonardo Vinci') AS valeur_catalogue_leo;

-- Utiliser les vues :
SELECT * FROM v_active_artists;
SELECT * FROM v_current_exhibitions;
SELECT * FROM v_workshop_availability;
SELECT * FROM v_artwork_summary;

-- Rapport d'un membre :
CALL sp_member_full_report('Alice Wonderland');
*/
