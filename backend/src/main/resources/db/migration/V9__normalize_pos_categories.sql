-- ========================================================
-- Migration V9: Normalize pos_category into 1NF table
-- Creates dictionary_sense_pos_categories with CHECK constraint,
-- migrates existing & unclassified senses safely, and drops legacy column.
-- ========================================================

SET NAMES utf8mb4;

-- 1. Create 1NF join table for standardized POS categories
CREATE TABLE dictionary_sense_pos_categories (
    sense_id BIGINT NOT NULL,
    pos_category VARCHAR(30) NOT NULL,
    PRIMARY KEY (sense_id, pos_category),
    INDEX idx_sense_pos_category (pos_category, sense_id),
    CONSTRAINT fk_sense_pos_categories_sense
        FOREIGN KEY (sense_id) REFERENCES dictionary_senses(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_sense_pos_category
        CHECK (pos_category IN ('NOUN', 'VERB', 'ADJECTIVE', 'ADVERB', 'IDIOM', 'OTHER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Migrate existing valid categories from legacy dictionary_senses.pos_category
INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT id, 'NOUN'
FROM dictionary_senses
WHERE FIND_IN_SET('NOUN', REPLACE(UPPER(COALESCE(pos_category, '')), ' ', '')) > 0;

INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT id, 'VERB'
FROM dictionary_senses
WHERE FIND_IN_SET('VERB', REPLACE(UPPER(COALESCE(pos_category, '')), ' ', '')) > 0;

INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT id, 'ADJECTIVE'
FROM dictionary_senses
WHERE FIND_IN_SET('ADJECTIVE', REPLACE(UPPER(COALESCE(pos_category, '')), ' ', '')) > 0;

INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT id, 'ADVERB'
FROM dictionary_senses
WHERE FIND_IN_SET('ADVERB', REPLACE(UPPER(COALESCE(pos_category, '')), ' ', '')) > 0;

INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT id, 'IDIOM'
FROM dictionary_senses
WHERE FIND_IN_SET('IDIOM', REPLACE(UPPER(COALESCE(pos_category, '')), ' ', '')) > 0;

INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT id, 'OTHER'
FROM dictionary_senses
WHERE FIND_IN_SET('OTHER', REPLACE(UPPER(COALESCE(pos_category, '')), ' ', '')) > 0;

-- 3. Backfill senses missing legacy pos_category using raw part_of_speech
INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT ds.id, 'VERB'
FROM dictionary_senses ds
WHERE (ds.pos_category IS NULL OR TRIM(ds.pos_category) = '')
  AND (LOWER(ds.part_of_speech) LIKE 'v1%'
       OR LOWER(ds.part_of_speech) LIKE 'v5%'
       OR LOWER(ds.part_of_speech) LIKE 'vk%'
       OR LOWER(ds.part_of_speech) LIKE 'vz%'
       OR LOWER(ds.part_of_speech) LIKE '%vs%'
       OR LOWER(ds.part_of_speech) LIKE '%aux-v%');

INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT ds.id, 'NOUN'
FROM dictionary_senses ds
WHERE (ds.pos_category IS NULL OR TRIM(ds.pos_category) = '')
  AND (LOWER(ds.part_of_speech) = 'n'
       OR LOWER(ds.part_of_speech) LIKE 'n %'
       OR LOWER(ds.part_of_speech) LIKE 'n,%'
       OR LOWER(ds.part_of_speech) LIKE 'n(%'
       OR LOWER(ds.part_of_speech) LIKE 'n-%'
       OR LOWER(ds.part_of_speech) LIKE 'ctr%'
       OR LOWER(ds.part_of_speech) LIKE 'pref%');

INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT ds.id, 'ADJECTIVE'
FROM dictionary_senses ds
WHERE (ds.pos_category IS NULL OR TRIM(ds.pos_category) = '')
  AND LOWER(ds.part_of_speech) LIKE 'adj-%';

INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT ds.id, 'ADVERB'
FROM dictionary_senses ds
WHERE (ds.pos_category IS NULL OR TRIM(ds.pos_category) = '')
  AND LOWER(ds.part_of_speech) LIKE 'adv%';

INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT ds.id, 'IDIOM'
FROM dictionary_senses ds
WHERE (ds.pos_category IS NULL OR TRIM(ds.pos_category) = '')
  AND (LOWER(ds.part_of_speech) LIKE 'id%'
       OR LOWER(ds.part_of_speech) LIKE 'exp%'
       OR ds.part_of_speech LIKE '%Quán dụng ngữ%');

-- 4. Safe fallback to OTHER for any remaining unclassified senses
INSERT IGNORE INTO dictionary_sense_pos_categories (sense_id, pos_category)
SELECT ds.id, 'OTHER'
FROM dictionary_senses ds
LEFT JOIN dictionary_sense_pos_categories dspc ON dspc.sense_id = ds.id
WHERE dspc.sense_id IS NULL;

-- 5. Drop legacy denormalized column
ALTER TABLE dictionary_senses DROP COLUMN pos_category;

