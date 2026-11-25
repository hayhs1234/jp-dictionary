-- ========================================================
-- Migration V7: Add pitch_accent column to dictionary_readings
-- Stores Tokyo pitch accent kernel number(s), e.g. '0', '1', '2', or '0,3'.
-- NULL means pitch accent data is not yet available for the reading.
-- ========================================================

SET NAMES utf8mb4;

ALTER TABLE dictionary_readings
    ADD COLUMN pitch_accent VARCHAR(50) NULL AFTER romaji;

-- Populate standard Tokyo pitch accent kernel numbers for seeded entries (1..28)
UPDATE dictionary_readings SET pitch_accent = '2' WHERE entry_id = 1  AND reading = 'たべる';
UPDATE dictionary_readings SET pitch_accent = '1' WHERE entry_id = 2  AND reading = 'のむ';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 3  AND reading = 'いく';
UPDATE dictionary_readings SET pitch_accent = '1' WHERE entry_id = 4  AND reading = 'くる';
UPDATE dictionary_readings SET pitch_accent = '1' WHERE entry_id = 5  AND reading = 'ほん';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 6  AND reading = 'にほんご';
UPDATE dictionary_readings SET pitch_accent = '4' WHERE entry_id = 7  AND reading = 'あたらしい';
UPDATE dictionary_readings SET pitch_accent = '2' WHERE entry_id = 8  AND reading = 'ふるい';
UPDATE dictionary_readings SET pitch_accent = '2' WHERE entry_id = 9  AND reading = 'はなす';
UPDATE dictionary_readings SET pitch_accent = '1' WHERE entry_id = 10 AND reading = 'かく';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 11 AND reading = 'きく';
UPDATE dictionary_readings SET pitch_accent = '1' WHERE entry_id = 12 AND reading = 'みる';
UPDATE dictionary_readings SET pitch_accent = '3' WHERE entry_id = 13 AND reading = 'せんせい';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 14 AND reading = 'がくせい';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 15 AND reading = 'ともだち';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 16 AND reading = 'おくる';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 17 AND reading = 'むかえる';
UPDATE dictionary_readings SET pitch_accent = '2' WHERE entry_id = 18 AND reading = 'なおす';
UPDATE dictionary_readings SET pitch_accent = '1' WHERE entry_id = 19 AND reading = 'しんせつ';
UPDATE dictionary_readings SET pitch_accent = '1' WHERE entry_id = 20 AND reading = 'べんり';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 21 AND reading = 'せいこう';
UPDATE dictionary_readings SET pitch_accent = '4' WHERE entry_id = 22 AND reading = 'あきらめる';
UPDATE dictionary_readings SET pitch_accent = '1' WHERE entry_id = 23 AND reading = 'どりょく';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 24 AND reading = 'こくふく';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 25 AND reading = 'あいまい';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 26 AND reading = 'いんぺい';
UPDATE dictionary_readings SET pitch_accent = '1' WHERE entry_id = 27 AND reading = 'しさ';
UPDATE dictionary_readings SET pitch_accent = '0' WHERE entry_id = 28 AND reading = 'こもれび';

