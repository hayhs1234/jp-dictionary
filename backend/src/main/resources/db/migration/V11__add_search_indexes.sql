-- ========================================================
-- Migration V11: Add B-Tree indexes for dictionary search & suggestions
-- Targets: dictionary_entries.jlpt_level, dictionary_readings.romaji,
--          dictionary_forms(is_primary, written_form),
--          dictionary_readings(is_primary, reading)
-- ========================================================

SET NAMES utf8mb4;

CREATE INDEX idx_entries_jlpt
    ON dictionary_entries (jlpt_level);

CREATE INDEX idx_readings_romaji
    ON dictionary_readings (romaji);

CREATE INDEX idx_forms_primary_written
    ON dictionary_forms (is_primary, written_form);

CREATE INDEX idx_readings_primary_reading
    ON dictionary_readings (is_primary, reading);

