-- =============================================================
-- ArtConnectPro — Données initiales (seed)
-- À exécuter APRÈS schema.sql
-- =============================================================

USE artconnect_db;

-- -------------------------------------------------------------
-- Disciplines
-- -------------------------------------------------------------
INSERT INTO discipline (name) VALUES
    ('Painting'),
    ('Sculpture'),
    ('Photography'),
    ('Digital Art'),
    ('Music'),
    ('Illustration'),
    ('Ceramics'),
    ('Printmaking')
ON DUPLICATE KEY UPDATE name = name;

-- -------------------------------------------------------------
-- Artists
-- -------------------------------------------------------------
INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, website, is_active) VALUES
    ('Leonardo Vinci',    'Renaissance master, painter, sculptor and polymath.',                        1452, 'leo@vincistudio.it',      '+39 055 0001', 'Florence',      'vincistudio.it',      TRUE),
    ('Claude Monet',      'Founder of French Impressionist painting.',                                  1840, 'claude@monet.fr',         '+33 2 3251 28', 'Giverny',       'monetgarden.fr',      TRUE),
    ('Ansel Adams',       'American landscape photographer and environmentalist.',                      1902, 'ansel@adams.co',          '+1 415 555 01', 'San Francisco', 'anseladams.com',      TRUE),
    ('Frida Kahlo',       'Mexican painter known for her many portraits and self-portraits.',           1907, 'frida@kahlo.mx',          '+52 55 5555 0', 'Mexico City',   'fridakahlo.mx',       TRUE),
    ('Auguste Rodin',     'French sculptor, founder of modern sculpture.',                              1840, 'auguste@rodin.fr',        '+33 1 4354 00', 'Paris',         'musee-rodin.fr',      TRUE),
    ('Yayoi Kusama',      'Japanese artist known for polka dots and infinity mirror rooms.',            1929, 'yayoi@kusama.jp',         '+81 3 3568 00', 'Tokyo',         'yayoikusama.jp',      TRUE),
    ('Banksy',            'Anonymous England-based street artist and activist.',                        1974, 'banksy@art.uk',           NULL,            'Bristol',       'banksy.co.uk',        TRUE),
    ('Georgia O Keeffe',  'American modernist artist known for large-scale flower paintings.',          1887, 'georgia@okeeffe.com',     '+1 505 555 02', 'Santa Fe',      'okeeffemuseum.org',   TRUE),
    ('Jean-Michel Basquiat', 'American artist who rose to success during the 1980s.',                  1960, 'basquiat@neoexp.com',     '+1 212 555 03', 'New York',      'basquiatfoundation.com', TRUE),
    ('Katsushika Hokusai','Japanese ukiyo-e painter and printmaker of the Edo period.',                1760, 'hokusai@wave.jp',         NULL,            'Tokyo',         'hokusai.jp',          TRUE)
ON DUPLICATE KEY UPDATE name = name;

-- -------------------------------------------------------------
-- Artist ↔ Discipline
-- -------------------------------------------------------------
INSERT INTO artist_discipline (artist_id, discipline_id)
SELECT a.id, d.id FROM artist a JOIN discipline d ON
    (a.name = 'Leonardo Vinci'        AND d.name IN ('Painting', 'Sculpture'))
 OR (a.name = 'Claude Monet'          AND d.name = 'Painting')
 OR (a.name = 'Ansel Adams'           AND d.name = 'Photography')
 OR (a.name = 'Frida Kahlo'           AND d.name = 'Painting')
 OR (a.name = 'Auguste Rodin'         AND d.name = 'Sculpture')
 OR (a.name = 'Yayoi Kusama'          AND d.name IN ('Sculpture', 'Painting'))
 OR (a.name = 'Banksy'                AND d.name IN ('Painting', 'Printmaking'))
 OR (a.name = 'Georgia O Keeffe'      AND d.name = 'Painting')
 OR (a.name = 'Jean-Michel Basquiat'  AND d.name IN ('Painting', 'Illustration'))
 OR (a.name = 'Katsushika Hokusai'    AND d.name IN ('Printmaking', 'Illustration'))
ON DUPLICATE KEY UPDATE artist_id = artist_id;

-- -------------------------------------------------------------
-- Artworks
-- -------------------------------------------------------------
INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Mona Lisa', 1503, 'Painting', 'Oil on poplar panel', '77 × 53 cm',
    'Iconic portrait attributed to Leonardo da Vinci.', 850000000.00, 'FOR_SALE', id
FROM artist WHERE name = 'Leonardo Vinci' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'The Last Supper', 1498, 'Painting', 'Tempera on gesso', '460 × 880 cm',
    'Famous mural painting in Milan by Leonardo da Vinci.', 450000000.00, 'EXHIBITED', id
FROM artist WHERE name = 'Leonardo Vinci' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Water Lilies', 1919, 'Painting', 'Oil on canvas', '100 × 300 cm',
    'Series of approximately 250 oil paintings by Claude Monet.', 40000000.00, 'FOR_SALE', id
FROM artist WHERE name = 'Claude Monet' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Impression Sunrise', 1872, 'Painting', 'Oil on canvas', '48 × 63 cm',
    'The painting that gave Impressionism its name.', 35000000.00, 'EXHIBITED', id
FROM artist WHERE name = 'Claude Monet' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'The Two Fridas', 1939, 'Painting', 'Oil on canvas', '173 × 173 cm',
    'Double self-portrait by Frida Kahlo, showing two versions of the artist.', 5000000.00, 'EXHIBITED', id
FROM artist WHERE name = 'Frida Kahlo' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Self-Portrait with Thorn Necklace', 1940, 'Painting', 'Oil on canvas', '47 × 38 cm',
    'One of Frida Kahlo''s most celebrated self-portraits.', 3000000.00, 'FOR_SALE', id
FROM artist WHERE name = 'Frida Kahlo' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Monolith, The Face of Half Dome', 1927, 'Photography', 'Silver gelatin print', '40 × 50 cm',
    'Iconic photograph by Ansel Adams of Yosemite''s Half Dome.', 100000.00, 'FOR_SALE', id
FROM artist WHERE name = 'Ansel Adams' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Moonrise Hernandez', 1941, 'Photography', 'Silver gelatin print', '50 × 60 cm',
    'One of the most reproduced photographs in the history of the medium.', 609600.00, 'SOLD', id
FROM artist WHERE name = 'Ansel Adams' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'The Thinker', 1904, 'Sculpture', 'Bronze', '186 cm height',
    'Bronze sculpture by Auguste Rodin depicting a man in contemplation.', 15000000.00, 'EXHIBITED', id
FROM artist WHERE name = 'Auguste Rodin' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'The Kiss', 1889, 'Sculpture', 'Marble', '182 cm height',
    'Marble sculpture by Auguste Rodin depicting a couple embracing.', 12000000.00, 'EXHIBITED', id
FROM artist WHERE name = 'Auguste Rodin' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Infinity Mirror Room', 2013, 'Sculpture', 'Mixed media / mirrors', 'Variable',
    'Immersive installation by Yayoi Kusama with polka dots and mirrors.', 2000000.00, 'EXHIBITED', id
FROM artist WHERE name = 'Yayoi Kusama' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Pumpkin', 1994, 'Sculpture', 'Fiberglass and urethane', '250 cm height',
    'Yellow dotted pumpkin sculpture, iconic work by Kusama.', 1500000.00, 'FOR_SALE', id
FROM artist WHERE name = 'Yayoi Kusama' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Girl with Balloon', 2002, 'Painting', 'Stencil spray paint on wall', '35 × 35 cm',
    'Iconic Banksy street art showing a girl reaching for a heart-shaped balloon.', 1000000.00, 'FOR_SALE', id
FROM artist WHERE name = 'Banksy' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Black Iris', 1926, 'Painting', 'Oil on canvas', '91 × 76 cm',
    'Large-scale close-up of a black iris by Georgia O''Keeffe.', 3500000.00, 'FOR_SALE', id
FROM artist WHERE name = 'Georgia O Keeffe' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'Untitled (Skull)', 1981, 'Painting', 'Acrylic and oil on canvas', '182 × 213 cm',
    'Neo-expressionist skull by Jean-Michel Basquiat.', 110000000.00, 'SOLD', id
FROM artist WHERE name = 'Jean-Michel Basquiat' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT 'The Great Wave', 1831, 'Printmaking', 'Woodblock print', '25 × 37 cm',
    'Famous woodblock print from Hokusai''s series Thirty-six Views of Mount Fuji.', 2600000.00, 'EXHIBITED', id
FROM artist WHERE name = 'Katsushika Hokusai' ON DUPLICATE KEY UPDATE title = title;

-- -------------------------------------------------------------
-- Artwork Tags
-- -------------------------------------------------------------
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Renaissance'   FROM artwork WHERE title = 'Mona Lisa'                        ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Portrait'      FROM artwork WHERE title = 'Mona Lisa'                        ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Iconic'        FROM artwork WHERE title = 'Mona Lisa'                        ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Religious'     FROM artwork WHERE title = 'The Last Supper'                  ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Mural'         FROM artwork WHERE title = 'The Last Supper'                  ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Impressionism' FROM artwork WHERE title = 'Water Lilies'                     ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Nature'        FROM artwork WHERE title = 'Water Lilies'                     ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Impressionism' FROM artwork WHERE title = 'Impression Sunrise'               ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Self-Portrait' FROM artwork WHERE title = 'The Two Fridas'                   ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Surrealism'    FROM artwork WHERE title = 'The Two Fridas'                   ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Landscape'     FROM artwork WHERE title = 'Monolith, The Face of Half Dome'  ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Black & White' FROM artwork WHERE title = 'Monolith, The Face of Half Dome'  ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Bronze'        FROM artwork WHERE title = 'The Thinker'                      ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Modern'        FROM artwork WHERE title = 'The Thinker'                      ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Street Art'    FROM artwork WHERE title = 'Girl with Balloon'                ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Political'     FROM artwork WHERE title = 'Girl with Balloon'                ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Installation'  FROM artwork WHERE title = 'Infinity Mirror Room'             ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Immersive'     FROM artwork WHERE title = 'Infinity Mirror Room'             ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Neo-Expressionism' FROM artwork WHERE title = 'Untitled (Skull)'             ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Japan'         FROM artwork WHERE title = 'The Great Wave'                   ON DUPLICATE KEY UPDATE name = name;
INSERT INTO artwork_tag (artwork_id, name)
SELECT id, 'Woodblock'     FROM artwork WHERE title = 'The Great Wave'                   ON DUPLICATE KEY UPDATE name = name;

-- -------------------------------------------------------------
-- Galleries
-- -------------------------------------------------------------
INSERT INTO gallery (name, address, owner_name, opening_hours, contact_phone, rating, website) VALUES
    ('Louvre Art House',    'Rue de Rivoli, Paris',           'Jean Dupont',    'Tue-Sun 9h-18h',  '+33 1 4020 50', 4.9, 'louvreartho.fr'),
    ('The British Gallery', 'Great Russell St, London',       'Emma Clarke',    'Daily 10h-17h',   '+44 20 7323 8', 4.7, 'britishgallery.co.uk'),
    ('Metropolitan Hub',    '1000 5th Ave, New York',         'James Harper',   'Wed-Sun 10h-17h', '+1 212 535 77', 4.8, 'metmuseum.org'),
    ('Galerie Rivoli',      '15 Rue de Rivoli, Paris',        'Sophie Martin',  'Mon-Sat 11h-19h', '+33 1 4277 12', 4.5, 'galerierivoli.fr'),
    ('Tokyo Modern',        '2-4 Minami-Aoyama, Tokyo',       'Kenji Yamada',   'Tue-Sun 10h-18h', '+81 3 5777 87', 4.6, 'tokyomodern.jp')
ON DUPLICATE KEY UPDATE name = name;

-- -------------------------------------------------------------
-- Exhibitions
-- -------------------------------------------------------------
INSERT INTO exhibition (title, start_date, end_date, gallery_id, curator_name, theme)
SELECT 'Renaissance Revival',
    DATE_SUB(CURDATE(), INTERVAL 1 MONTH),
    DATE_ADD(CURDATE(), INTERVAL 2 MONTH),
    id, 'Dr. Elena Rossi', 'Classic Renaissance'
FROM gallery WHERE name = 'Louvre Art House' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO exhibition (title, start_date, end_date, gallery_id, curator_name, theme)
SELECT 'Sculpting the Soul',
    DATE_SUB(CURDATE(), INTERVAL 15 DAY),
    DATE_ADD(CURDATE(), INTERVAL 1 MONTH),
    id, 'Marcus Thorne', 'Modern & Classical Sculpture'
FROM gallery WHERE name = 'The British Gallery' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO exhibition (title, start_date, end_date, gallery_id, curator_name, theme)
SELECT 'Impressionist Dreams',
    DATE_SUB(CURDATE(), INTERVAL 2 MONTH),
    DATE_ADD(CURDATE(), INTERVAL 3 MONTH),
    id, 'Sarah Jenkins', 'Light and Color'
FROM gallery WHERE name = 'Metropolitan Hub' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO exhibition (title, start_date, end_date, gallery_id, curator_name, theme)
SELECT 'Street Art & Beyond',
    DATE_SUB(CURDATE(), INTERVAL 10 DAY),
    DATE_ADD(CURDATE(), INTERVAL 2 MONTH),
    id, 'Lena Dubois', 'Urban Expression'
FROM gallery WHERE name = 'Galerie Rivoli' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO exhibition (title, start_date, end_date, gallery_id, curator_name, theme)
SELECT 'Infinity Worlds',
    DATE_SUB(CURDATE(), INTERVAL 5 DAY),
    DATE_ADD(CURDATE(), INTERVAL 4 MONTH),
    id, 'Hana Tanaka', 'Contemporary Japanese Art'
FROM gallery WHERE name = 'Tokyo Modern' ON DUPLICATE KEY UPDATE title = title;

-- -------------------------------------------------------------
-- Exhibition ↔ Artwork
-- -------------------------------------------------------------
INSERT INTO exhibition_artwork (exhibition_id, artwork_id)
SELECT e.id, a.id FROM exhibition e JOIN artwork a ON
    (e.title = 'Renaissance Revival'  AND a.title IN ('Mona Lisa', 'The Last Supper'))
 OR (e.title = 'Sculpting the Soul'   AND a.title IN ('The Thinker', 'The Kiss'))
 OR (e.title = 'Impressionist Dreams' AND a.title IN ('Water Lilies', 'Impression Sunrise'))
 OR (e.title = 'Street Art & Beyond'  AND a.title = 'Girl with Balloon')
 OR (e.title = 'Infinity Worlds'      AND a.title IN ('Infinity Mirror Room', 'Pumpkin', 'The Great Wave'))
ON DUPLICATE KEY UPDATE exhibition_id = exhibition_id;

-- -------------------------------------------------------------
-- Workshops
-- -------------------------------------------------------------
INSERT INTO workshop (title, date_time, duration_minutes, max_participants, price, instructor_id, location, description, level)
SELECT 'Mastering Oil Painting',
    DATE_ADD(NOW(), INTERVAL 5 DAY),
    180, 10, 150.00, id, 'Florence Studio',
    'Learn the fundamentals of oil painting with Renaissance techniques.', 'Intermediate'
FROM artist WHERE name = 'Leonardo Vinci' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO workshop (title, date_time, duration_minutes, max_participants, price, instructor_id, location, description, level)
SELECT 'Impressionist Landscapes',
    DATE_ADD(NOW(), INTERVAL 10 DAY),
    180, 12, 120.00, id, 'Giverny Gardens',
    'Capture the beauty of nature with Impressionist brushwork.', 'Beginner'
FROM artist WHERE name = 'Claude Monet' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO workshop (title, date_time, duration_minutes, max_participants, price, instructor_id, location, description, level)
SELECT 'Sculpting Modernity',
    DATE_ADD(NOW(), INTERVAL 15 DAY),
    240, 8, 200.00, id, 'Paris Workshop',
    'Introduction to modern sculpture techniques using clay and plaster.', 'Advanced'
FROM artist WHERE name = 'Auguste Rodin' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO workshop (title, date_time, duration_minutes, max_participants, price, instructor_id, location, description, level)
SELECT 'Nature Photography Masterclass',
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    240, 15, 180.00, id, 'Yosemite Valley',
    'Learn to capture stunning landscapes with natural light.', 'Intermediate'
FROM artist WHERE name = 'Ansel Adams' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO workshop (title, date_time, duration_minutes, max_participants, price, instructor_id, location, description, level)
SELECT 'Dots & Infinity: Abstract Art',
    DATE_ADD(NOW(), INTERVAL 20 DAY),
    120, 20, 90.00, id, 'Tokyo Modern Studio',
    'Explore repetitive patterns and infinity concepts in contemporary art.', 'Beginner'
FROM artist WHERE name = 'Yayoi Kusama' ON DUPLICATE KEY UPDATE title = title;

INSERT INTO workshop (title, date_time, duration_minutes, max_participants, price, instructor_id, location, description, level)
SELECT 'Urban Stencil Art',
    DATE_ADD(NOW(), INTERVAL 3 DAY),
    180, 10, 75.00, id, 'Bristol Art Space',
    'Learn stencil cutting and spray painting techniques.', 'Beginner'
FROM artist WHERE name = 'Banksy' ON DUPLICATE KEY UPDATE title = title;

-- -------------------------------------------------------------
-- Community Members
-- -------------------------------------------------------------
INSERT INTO community_member (name, email, birth_year, phone, city, membership_type) VALUES
    ('Alice Wonderland', 'alice@art.com',         1992, '+33 6 1234 56', 'Paris',         'Premium'),
    ('Bob Ross',         'bob@happytrees.com',     1942, '+1 904 555 01', 'London',        'Premium'),
    ('Charlie Brown',    'charlie@peanuts.com',    1995, '+1 212 555 02', 'New York',      'Premium'),
    ('Diana Prince',     'diana@artlover.com',     1988, '+1 310 555 03', 'Los Angeles',   'free'),
    ('Ethan Hunt',       'ethan@gallery.com',      1975, '+1 617 555 04', 'Boston',        'Premium'),
    ('Fiona Green',      'fiona@artcollect.com',   2000, '+44 20 5555 0', 'London',        'free'),
    ('Gabriel Torres',   'gabriel@artmx.com',      1983, '+52 55 5555 1', 'Mexico City',   'Premium'),
    ('Hana Sato',        'hana@tokyo-art.jp',      1997, '+81 3 5555 87', 'Tokyo',         'free')
ON DUPLICATE KEY UPDATE name = name;

-- -------------------------------------------------------------
-- Member ↔ Discipline (disciplines favorites)
-- -------------------------------------------------------------
INSERT INTO member_discipline (member_id, discipline_id)
SELECT m.id, d.id FROM community_member m JOIN discipline d ON
    (m.name = 'Alice Wonderland' AND d.name IN ('Painting', 'Photography'))
 OR (m.name = 'Bob Ross'         AND d.name IN ('Painting', 'Illustration'))
 OR (m.name = 'Charlie Brown'    AND d.name = 'Sculpture')
 OR (m.name = 'Diana Prince'     AND d.name IN ('Digital Art', 'Photography'))
 OR (m.name = 'Ethan Hunt'       AND d.name IN ('Photography', 'Painting'))
 OR (m.name = 'Fiona Green'      AND d.name = 'Ceramics')
 OR (m.name = 'Gabriel Torres'   AND d.name IN ('Painting', 'Printmaking'))
 OR (m.name = 'Hana Sato'        AND d.name IN ('Printmaking', 'Digital Art'))
ON DUPLICATE KEY UPDATE member_id = member_id;

-- -------------------------------------------------------------
-- Bookings (inscriptions aux ateliers)
-- -------------------------------------------------------------
INSERT INTO booking (workshop_id, member_id, booking_date, payment_status)
SELECT w.id, m.id, DATE_SUB(NOW(), INTERVAL 2 DAY), 'PAID'
FROM workshop w JOIN community_member m ON w.title = 'Mastering Oil Painting' AND m.name = 'Alice Wonderland'
ON DUPLICATE KEY UPDATE payment_status = payment_status;

INSERT INTO booking (workshop_id, member_id, booking_date, payment_status)
SELECT w.id, m.id, DATE_SUB(NOW(), INTERVAL 2 DAY), 'PAID'
FROM workshop w JOIN community_member m ON w.title = 'Mastering Oil Painting' AND m.name = 'Bob Ross'
ON DUPLICATE KEY UPDATE payment_status = payment_status;

INSERT INTO booking (workshop_id, member_id, booking_date, payment_status)
SELECT w.id, m.id, DATE_SUB(NOW(), INTERVAL 1 DAY), 'PENDING'
FROM workshop w JOIN community_member m ON w.title = 'Impressionist Landscapes' AND m.name = 'Charlie Brown'
ON DUPLICATE KEY UPDATE payment_status = payment_status;

INSERT INTO booking (workshop_id, member_id, booking_date, payment_status)
SELECT w.id, m.id, DATE_SUB(NOW(), INTERVAL 1 DAY), 'PAID'
FROM workshop w JOIN community_member m ON w.title = 'Impressionist Landscapes' AND m.name = 'Diana Prince'
ON DUPLICATE KEY UPDATE payment_status = payment_status;

INSERT INTO booking (workshop_id, member_id, booking_date, payment_status)
SELECT w.id, m.id, NOW(), 'PENDING'
FROM workshop w JOIN community_member m ON w.title = 'Urban Stencil Art' AND m.name = 'Fiona Green'
ON DUPLICATE KEY UPDATE payment_status = payment_status;

INSERT INTO booking (workshop_id, member_id, booking_date, payment_status)
SELECT w.id, m.id, NOW(), 'PAID'
FROM workshop w JOIN community_member m ON w.title = 'Dots & Infinity: Abstract Art' AND m.name = 'Hana Sato'
ON DUPLICATE KEY UPDATE payment_status = payment_status;

INSERT INTO booking (workshop_id, member_id, booking_date, payment_status)
SELECT w.id, m.id, NOW(), 'PAID'
FROM workshop w JOIN community_member m ON w.title = 'Nature Photography Masterclass' AND m.name = 'Ethan Hunt'
ON DUPLICATE KEY UPDATE payment_status = payment_status;

INSERT INTO booking (workshop_id, member_id, booking_date, payment_status)
SELECT w.id, m.id, NOW(), 'PENDING'
FROM workshop w JOIN community_member m ON w.title = 'Nature Photography Masterclass' AND m.name = 'Gabriel Torres'
ON DUPLICATE KEY UPDATE payment_status = payment_status;

-- -------------------------------------------------------------
-- Reviews
-- -------------------------------------------------------------
INSERT INTO review (member_id, artwork_id, rating, comment, review_date)
SELECT m.id, a.id, 5, 'Unbelievable detail, a true masterpiece!', CURDATE()
FROM community_member m JOIN artwork a ON m.name = 'Alice Wonderland' AND a.title = 'Mona Lisa'
ON DUPLICATE KEY UPDATE rating = rating;

INSERT INTO review (member_id, artwork_id, rating, comment, review_date)
SELECT m.id, a.id, 4, 'The colors are absolutely stunning.', CURDATE()
FROM community_member m JOIN artwork a ON m.name = 'Bob Ross' AND a.title = 'Water Lilies'
ON DUPLICATE KEY UPDATE rating = rating;

INSERT INTO review (member_id, artwork_id, rating, comment, review_date)
SELECT m.id, a.id, 5, 'Deeply moving and timeless.', CURDATE()
FROM community_member m JOIN artwork a ON m.name = 'Charlie Brown' AND a.title = 'The Thinker'
ON DUPLICATE KEY UPDATE rating = rating;

INSERT INTO review (member_id, artwork_id, rating, comment, review_date)
SELECT m.id, a.id, 5, 'The sheer scale is breathtaking.', CURDATE()
FROM community_member m JOIN artwork a ON m.name = 'Diana Prince' AND a.title = 'The Last Supper'
ON DUPLICATE KEY UPDATE rating = rating;

INSERT INTO review (member_id, artwork_id, rating, comment, review_date)
SELECT m.id, a.id, 4, 'A powerful street art statement.', CURDATE()
FROM community_member m JOIN artwork a ON m.name = 'Ethan Hunt' AND a.title = 'Girl with Balloon'
ON DUPLICATE KEY UPDATE rating = rating;

INSERT INTO review (member_id, artwork_id, rating, comment, review_date)
SELECT m.id, a.id, 5, 'The immersive experience is unlike anything else.', CURDATE()
FROM community_member m JOIN artwork a ON m.name = 'Hana Sato' AND a.title = 'Infinity Mirror Room'
ON DUPLICATE KEY UPDATE rating = rating;

INSERT INTO review (member_id, artwork_id, rating, comment, review_date)
SELECT m.id, a.id, 5, 'A raw and powerful neo-expressionist statement.', CURDATE()
FROM community_member m JOIN artwork a ON m.name = 'Gabriel Torres' AND a.title = 'Untitled (Skull)'
ON DUPLICATE KEY UPDATE rating = rating;

INSERT INTO review (member_id, artwork_id, rating, comment, review_date)
SELECT m.id, a.id, 4, 'Iconic and instantly recognizable.', CURDATE()
FROM community_member m JOIN artwork a ON m.name = 'Fiona Green' AND a.title = 'The Great Wave'
ON DUPLICATE KEY UPDATE rating = rating;
