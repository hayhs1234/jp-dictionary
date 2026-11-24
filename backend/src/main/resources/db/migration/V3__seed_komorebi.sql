-- V3: Insert genuine entry for 木漏れ日 (Komorebi)
SET NAMES utf8mb4;

INSERT INTO dictionary_entries (id, jlpt_level, source_code, source_entry_id) VALUES
(28, 2, 'JMDICT', '1599990');

INSERT INTO dictionary_forms (entry_id, written_form, form_type, is_primary, display_order) VALUES
(28, '木漏れ日', 'KANJI', TRUE, 1),
(28, '木もれ日', 'KANJI', FALSE, 2),
(28, 'こもれび', 'KANA', FALSE, 3);

INSERT INTO dictionary_readings (entry_id, reading, romaji, is_primary, display_order) VALUES
(28, 'こもれび', 'komorebi', TRUE, 1);

INSERT INTO dictionary_senses (id, entry_id, sense_order, part_of_speech, notes) VALUES
(28, 28, 1, 'n (Danh từ)', 'Hiện tượng quang học khi ánh nắng xuyên qua vòm lá');

INSERT INTO sense_glosses (sense_id, language_code, meaning, display_order) VALUES
(28, 'vi', 'ánh nắng lọt qua kẽ lá', 1),
(28, 'en', 'sunlight filtering through trees', 2);

INSERT INTO example_sentences (id, entry_id, japanese_text, reading, source_code) VALUES
(28, 28, '森の中を歩くと、美しい木漏れ日が地面を照らしていた。', 'もりのなかをあるくと、うつくしいこもれびがじめんをてらしていた。', 'TATOEBA');

INSERT INTO example_translations (example_sentence_id, language_code, translated_text) VALUES
(28, 'vi', 'Đi dạo trong rừng, những tia nắng len qua kẽ lá rọi sáng tuyệt đẹp xuống mặt đất.'),
(28, 'en', 'Walking in the forest, beautiful sunlight filtered through trees illuminated the ground.');

