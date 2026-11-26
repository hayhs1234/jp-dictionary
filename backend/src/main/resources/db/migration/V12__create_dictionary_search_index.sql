-- ========================================================
-- Migration V12: Add frequency_rank to dictionary_entries and
-- create flattened dictionary_search_index table with FULLTEXT (ngram)
-- ========================================================

SET NAMES utf8mb4;

ALTER TABLE dictionary_entries
    ADD COLUMN frequency_rank INT NULL AFTER jlpt_level;

CREATE TABLE dictionary_search_index (
    entry_id BIGINT NOT NULL,
    jlpt_level TINYINT NULL,
    pos_mask INT NOT NULL DEFAULT 0
        COMMENT 'Bitmask: 1=NOUN, 2=VERB, 4=ADJECTIVE, 8=ADVERB, 16=IDIOM, 32=OTHER',
    frequency_rank INT NULL,
    primary_form VARCHAR(255) NOT NULL DEFAULT '',
    primary_reading VARCHAR(255) NOT NULL DEFAULT '',
    primary_romaji VARCHAR(255) NOT NULL DEFAULT '',
    primary_meaning VARCHAR(500) NOT NULL DEFAULT '',
    forms_text TEXT NOT NULL,
    readings_text TEXT NOT NULL,
    romaji_text TEXT NOT NULL,
    meanings_text TEXT NOT NULL,
    meanings_ascii TEXT NOT NULL,
    ascii_synced BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (entry_id),
    INDEX idx_dsi_jlpt_freq (jlpt_level, frequency_rank, entry_id),
    INDEX idx_dsi_freq (frequency_rank, entry_id),
    INDEX idx_dsi_primary_form (primary_form),
    INDEX idx_dsi_primary_reading (primary_reading),
    INDEX idx_dsi_primary_romaji (primary_romaji),
    INDEX idx_dsi_ascii_synced (ascii_synced),
    FULLTEXT INDEX ft_dsi_jp_ngram (forms_text, readings_text) WITH PARSER ngram,
    FULLTEXT INDEX ft_dsi_romaji_ngram (romaji_text) WITH PARSER ngram,
    FULLTEXT INDEX ft_dsi_meaning_ngram (meanings_text, meanings_ascii) WITH PARSER ngram,
    CONSTRAINT fk_dsi_entry
        FOREIGN KEY (entry_id) REFERENCES dictionary_entries(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET SESSION group_concat_max_len = 65535;

INSERT INTO dictionary_search_index (
    entry_id,
    jlpt_level,
    pos_mask,
    frequency_rank,
    primary_form,
    primary_reading,
    primary_romaji,
    primary_meaning,
    forms_text,
    readings_text,
    romaji_text,
    meanings_text,
    meanings_ascii,
    ascii_synced
)
SELECT
    e.id,
    e.jlpt_level,
    COALESCE((
        SELECT BIT_OR(
            CASE pc.pos_category
                WHEN 'NOUN' THEN 1
                WHEN 'VERB' THEN 2
                WHEN 'ADJECTIVE' THEN 4
                WHEN 'ADVERB' THEN 8
                WHEN 'IDIOM' THEN 16
                WHEN 'OTHER' THEN 32
                ELSE 0
            END
        )
        FROM dictionary_senses s
        INNER JOIN dictionary_sense_pos_categories pc ON pc.sense_id = s.id
        WHERE s.entry_id = e.id
    ), 0) AS pos_mask,
    e.frequency_rank,
    COALESCE((
        SELECT f.written_form FROM dictionary_forms f
        WHERE f.entry_id = e.id
        ORDER BY f.is_primary DESC, f.display_order ASC, f.id ASC
        LIMIT 1
    ), '') AS primary_form,
    COALESCE((
        SELECT r.reading FROM dictionary_readings r
        WHERE r.entry_id = e.id
        ORDER BY r.is_primary DESC, r.display_order ASC, r.id ASC
        LIMIT 1
    ), '') AS primary_reading,
    COALESCE((
        SELECT r.romaji FROM dictionary_readings r
        WHERE r.entry_id = e.id AND r.romaji IS NOT NULL AND r.romaji <> ''
        ORDER BY r.is_primary DESC, r.display_order ASC, r.id ASC
        LIMIT 1
    ), '') AS primary_romaji,
    COALESCE((
        SELECT LEFT(g.meaning, 500)
        FROM dictionary_senses s
        INNER JOIN sense_glosses g ON g.sense_id = s.id
        WHERE s.entry_id = e.id
        ORDER BY s.sense_order ASC,
                 CASE WHEN g.language_code = 'vi' THEN 0 WHEN g.language_code = 'en' THEN 1 ELSE 2 END,
                 g.display_order ASC,
                 g.id ASC
        LIMIT 1
    ), '') AS primary_meaning,
    COALESCE((
        SELECT GROUP_CONCAT(f.written_form ORDER BY f.is_primary DESC, f.display_order ASC SEPARATOR ' ')
        FROM dictionary_forms f
        WHERE f.entry_id = e.id
    ), '') AS forms_text,
    COALESCE((
        SELECT GROUP_CONCAT(r.reading ORDER BY r.is_primary DESC, r.display_order ASC SEPARATOR ' ')
        FROM dictionary_readings r
        WHERE r.entry_id = e.id
    ), '') AS readings_text,
    COALESCE((
        SELECT GROUP_CONCAT(r.romaji ORDER BY r.is_primary DESC, r.display_order ASC SEPARATOR ' ')
        FROM dictionary_readings r
        WHERE r.entry_id = e.id AND r.romaji IS NOT NULL AND r.romaji <> ''
    ), '') AS romaji_text,
    COALESCE((
        SELECT GROUP_CONCAT(g.meaning ORDER BY s.sense_order ASC, g.display_order ASC SEPARATOR '; ')
        FROM dictionary_senses s
        INNER JOIN sense_glosses g ON g.sense_id = s.id
        WHERE s.entry_id = e.id
    ), '') AS meanings_text,
    REPLACE(REPLACE(COALESCE((
        SELECT GROUP_CONCAT(g.meaning ORDER BY s.sense_order ASC, g.display_order ASC SEPARATOR '; ')
        FROM dictionary_senses s
        INNER JOIN sense_glosses g ON g.sense_id = s.id
        WHERE s.entry_id = e.id
    ), ''), 'đ', 'd'), 'Đ', 'D') AS meanings_ascii,
    FALSE
FROM dictionary_entries e;
