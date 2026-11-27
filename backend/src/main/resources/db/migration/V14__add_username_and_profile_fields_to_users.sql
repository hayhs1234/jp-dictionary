-- ========================================================
-- Migration V14: Add username login identifier and profile
-- fields to users table (preserving USER / ADMIN roles)
-- ========================================================

SET NAMES utf8mb4;

ALTER TABLE users
    ADD COLUMN username VARCHAR(50) NULL AFTER id,
    ADD COLUMN avatar_url VARCHAR(500) NULL AFTER display_name,
    ADD COLUMN jlpt_target VARCHAR(10) NOT NULL DEFAULT 'N5' AFTER avatar_url,
    ADD COLUMN bio VARCHAR(255) NULL AFTER jlpt_target;

UPDATE users
SET username = CONCAT('user_', id)
WHERE username IS NULL OR TRIM(username) = '';

ALTER TABLE users
    MODIFY COLUMN username VARCHAR(50) NOT NULL,
    ADD CONSTRAINT uk_users_username UNIQUE (username);

