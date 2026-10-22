-- Japanese Learning MVP
-- MySQL 8.0+
-- Flyway Migration V1: Initial database schema (dictionary + user accounts + flashcard learning)

-- 1. User accounts
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Dictionary headwords
CREATE TABLE dictionary_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    jlpt_level TINYINT NULL COMMENT '5=N5, 4=N4, 3=N3, 2=N2, 1=N1; NULL=unknown',
    source_code VARCHAR(50) NULL,
    source_entry_id VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_dictionary_source UNIQUE (source_code, source_entry_id),
    CONSTRAINT chk_dictionary_jlpt
        CHECK (jlpt_level IS NULL OR jlpt_level BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Written forms: kanji, kana, or other forms
CREATE TABLE dictionary_forms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    entry_id BIGINT NOT NULL,
    written_form VARCHAR(255) NOT NULL,
    form_type VARCHAR(20) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_forms_written_form (written_form),
    INDEX idx_forms_entry (entry_id),
    CONSTRAINT fk_forms_entry
        FOREIGN KEY (entry_id) REFERENCES dictionary_entries(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_forms_type
        CHECK (form_type IN ('KANJI', 'KANA', 'OTHER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. Readings in kana and optional romaji
CREATE TABLE dictionary_readings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    entry_id BIGINT NOT NULL,
    reading VARCHAR(255) NOT NULL,
    romaji VARCHAR(255) NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_readings_reading (reading),
    INDEX idx_readings_entry (entry_id),
    CONSTRAINT fk_readings_entry
        FOREIGN KEY (entry_id) REFERENCES dictionary_entries(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 5. Senses: one headword may have multiple senses
CREATE TABLE dictionary_senses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    entry_id BIGINT NOT NULL,
    sense_order INT NOT NULL DEFAULT 0,
    part_of_speech VARCHAR(50) NULL,
    notes TEXT NULL,
    PRIMARY KEY (id),
    INDEX idx_senses_entry (entry_id),
    CONSTRAINT fk_senses_entry
        FOREIGN KEY (entry_id) REFERENCES dictionary_entries(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 6. Meaning/gloss per language
CREATE TABLE sense_glosses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sense_id BIGINT NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    meaning TEXT NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_glosses_sense_language (sense_id, language_code),
    CONSTRAINT fk_glosses_sense
        FOREIGN KEY (sense_id) REFERENCES dictionary_senses(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 7. Japanese example sentences
CREATE TABLE example_sentences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    entry_id BIGINT NOT NULL,
    japanese_text TEXT NOT NULL,
    reading TEXT NULL,
    source_code VARCHAR(50) NULL,
    PRIMARY KEY (id),
    INDEX idx_examples_entry (entry_id),
    CONSTRAINT fk_examples_entry
        FOREIGN KEY (entry_id) REFERENCES dictionary_entries(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 8. Example translations
CREATE TABLE example_translations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    example_sentence_id BIGINT NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    translated_text TEXT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_example_translations_sentence (example_sentence_id),
    CONSTRAINT fk_example_translations_sentence
        FOREIGN KEY (example_sentence_id) REFERENCES example_sentences(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 9. User-created vocabulary decks
CREATE TABLE decks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_decks_user (user_id),
    CONSTRAINT fk_decks_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 10. A flashcard is a dictionary entry saved in a particular deck.
-- Review state belongs to this deck-specific card.
CREATE TABLE flashcards (
    id BIGINT NOT NULL AUTO_INCREMENT,
    deck_id BIGINT NOT NULL,
    dictionary_entry_id BIGINT NOT NULL,
    next_review_at DATETIME(6) NULL,
    interval_days INT NOT NULL DEFAULT 0,
    ease_factor DECIMAL(5,2) NOT NULL DEFAULT 2.50,
    repetitions INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_flashcard_deck_entry UNIQUE (deck_id, dictionary_entry_id),
    INDEX idx_flashcards_due (deck_id, next_review_at),
    INDEX idx_flashcards_entry (dictionary_entry_id),
    CONSTRAINT fk_flashcards_deck
        FOREIGN KEY (deck_id) REFERENCES decks(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_flashcards_entry
        FOREIGN KEY (dictionary_entry_id) REFERENCES dictionary_entries(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_flashcards_interval CHECK (interval_days >= 0),
    CONSTRAINT chk_flashcards_repetitions CHECK (repetitions >= 0),
    CONSTRAINT chk_flashcards_ease CHECK (ease_factor >= 1.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 11. Immutable review history for progress/statistics
CREATE TABLE review_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    flashcard_id BIGINT NOT NULL,
    reviewed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    rating VARCHAR(20) NOT NULL,
    previous_interval_days INT NOT NULL DEFAULT 0,
    next_interval_days INT NOT NULL DEFAULT 0,
    response_time_ms INT NULL,
    PRIMARY KEY (id),
    INDEX idx_review_logs_card_time (flashcard_id, reviewed_at),
    CONSTRAINT fk_review_logs_flashcard
        FOREIGN KEY (flashcard_id) REFERENCES flashcards(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_review_rating
        CHECK (rating IN ('AGAIN', 'HARD', 'GOOD', 'EASY')),
    CONSTRAINT chk_review_previous_interval CHECK (previous_interval_days >= 0),
    CONSTRAINT chk_review_next_interval CHECK (next_interval_days >= 0),
    CONSTRAINT chk_review_response_time
        CHECK (response_time_ms IS NULL OR response_time_ms >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
