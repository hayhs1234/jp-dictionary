-- ========================================================
-- Migration V6: Standardize part of speech categories
-- Adds pos_category column to dictionary_senses and populates existing data
-- ========================================================

SET NAMES utf8mb4;

-- 1. Add pos_category column
ALTER TABLE dictionary_senses ADD COLUMN pos_category VARCHAR(50) NULL AFTER part_of_speech;

-- 2. Populate verbs (Ichidan, Godan, Kuru, etc.)
UPDATE dictionary_senses
SET pos_category = 'VERB'
WHERE part_of_speech LIKE 'v1%'
   OR part_of_speech LIKE 'v5%'
   OR part_of_speech LIKE 'vk%'
   OR part_of_speech LIKE 'vz%';

-- 3. Populate nouns (including counters and prefixes)
UPDATE dictionary_senses
SET pos_category = 'NOUN'
WHERE part_of_speech LIKE 'n %'
   OR part_of_speech LIKE 'n (%'
   OR part_of_speech = 'n'
   OR part_of_speech LIKE 'ctr%'
   OR part_of_speech LIKE 'pref%';

-- 4. Populate adjectives (i-adj, na-adj, etc.)
UPDATE dictionary_senses
SET pos_category = 'ADJECTIVE'
WHERE part_of_speech LIKE 'adj-%';

-- 5. Populate adverbs
UPDATE dictionary_senses
SET pos_category = 'ADVERB'
WHERE part_of_speech LIKE 'adv%';

-- 6. Populate multi-category senses (Noun + Suru verb)
UPDATE dictionary_senses
SET pos_category = 'NOUN,VERB'
WHERE part_of_speech LIKE '%vs%';

-- 7. Populate idioms / expressions
UPDATE dictionary_senses
SET pos_category = 'IDIOM'
WHERE part_of_speech LIKE 'id%'
   OR part_of_speech LIKE 'exp%'
   OR part_of_speech LIKE '%Quán dụng ngữ%';

-- 8. Default fallback for any remaining records
UPDATE dictionary_senses
SET pos_category = 'NOUN'
WHERE pos_category IS NULL;

