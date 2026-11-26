-- ========================================================
-- Migration V13: Track Meilisearch incremental synchronization
-- state on dictionary_search_index
-- ========================================================

SET NAMES utf8mb4;

ALTER TABLE dictionary_search_index
    ADD COLUMN meili_synced BOOLEAN NOT NULL DEFAULT FALSE AFTER ascii_synced,
    ADD COLUMN meili_synced_at DATETIME(6) NULL AFTER meili_synced,
    ADD INDEX idx_dsi_meili_synced (meili_synced, entry_id);

