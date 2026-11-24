-- ========================================================
-- Migration V5: Add rich polysemous senses (multiple senses)
-- for common multi-sense Japanese words
-- ========================================================

SET NAMES utf8mb4;

-- 1. Entry 18: 直す (なおす) - Sửa chữa / Đính chính / Dịch đổi / Làm lại
INSERT INTO dictionary_senses (id, entry_id, sense_order, part_of_speech, notes) VALUES
(100, 18, 2, 'v5s (Động từ nhóm 1)', 'Đính chính, sửa lỗi sai, chữa bệnh, sửa tật xấu'),
(101, 18, 3, 'v5s (Động từ nhóm 1)', 'Dịch thuật sang ngôn ngữ khác, quy đổi tiền tệ hoặc đơn vị đo lường'),
(102, 18, 4, 'v5s, aux-v (Động từ / Trợ động từ)', 'Làm lại một hành động từ đầu (sau thể liên dụng V-masu)');

INSERT INTO sense_glosses (sense_id, language_code, meaning, display_order) VALUES
(100, 'vi', 'đính chính, sửa sai, chữa trị (tật, bệnh)', 1),
(100, 'en', 'to correct, to rectify, to cure', 2),
(101, 'vi', 'dịch sang ngôn ngữ khác, quy đổi đơn vị/tiền tệ', 1),
(101, 'en', 'to translate, to convert', 2),
(102, 'vi', 'làm lại từ đầu (VD: 書き直す - viết lại)', 1),
(102, 'en', 'to redo, to do over again', 2);

-- 2. Entry 5: 本 (ほん) - Sách / Nguồn gốc, căn bản / Lượng từ đếm vật dài
INSERT INTO dictionary_senses (id, entry_id, sense_order, part_of_speech, notes) VALUES
(103, 5, 2, 'n, pref (Danh từ / Tiền tố)', 'Nguồn cội, gốc rễ, bản chất, chính yếu'),
(104, 5, 3, 'ctr (Lượng từ)', 'Trợ từ / Lượng từ đếm các vật thon dài như bút, chai lọ, que, cây cối');

INSERT INTO sense_glosses (sense_id, language_code, meaning, display_order) VALUES
(103, 'vi', 'nguồn gốc, gốc rễ, căn bản, chính yếu', 1),
(103, 'en', 'origin, source, main, principal', 2),
(104, 'vi', 'lượng từ đếm vật thon dài (cây, que, chai, bình)', 1),
(104, 'en', 'counter for long cylindrical things', 2);

-- 3. Entry 11: 聞く (きく) - Nghe / Hỏi thăm
INSERT INTO dictionary_senses (id, entry_id, sense_order, part_of_speech, notes) VALUES
(105, 11, 2, 'v5k (Động từ nhóm 1)', 'Hỏi han, tìm hiểu thông tin, hỏi đường, xin ý kiến');

INSERT INTO sense_glosses (sense_id, language_code, meaning, display_order) VALUES
(105, 'vi', 'hỏi, hỏi thăm, nghe ngóng thông tin', 1),
(105, 'en', 'to ask, to enquire, to question', 2);

-- 4. Entry 12: 見る (みる) - Nhìn xem / Khám bệnh / Thử làm
INSERT INTO dictionary_senses (id, entry_id, sense_order, part_of_speech, notes) VALUES
(106, 12, 2, 'v1 (Động từ nhóm 2)', 'Khám bệnh, chẩn đoán sức khỏe (bác sĩ khám)'),
(107, 12, 3, 'v1, aux-v (Trợ động từ)', 'Làm thử một việc gì đó xem sao (sau thể V-te)');

INSERT INTO sense_glosses (sense_id, language_code, meaning, display_order) VALUES
(106, 'vi', 'khám bệnh, chẩn đoán, xem mạch', 1),
(106, 'en', 'to examine (medically), to diagnose', 2),
(107, 'vi', 'thử làm (sau thể -te: 食べてみる, やってみる)', 1),
(107, 'en', 'to try doing, to see after doing', 2);

-- 5. Entry 3: 行く (いく) - Đi / Tiến triển thuận lợi
INSERT INTO dictionary_senses (id, entry_id, sense_order, part_of_speech, notes) VALUES
(108, 3, 2, 'v5k-s (Động từ nhóm 1)', 'Tiến triển, diễn biến sự việc (ví dụ: うまくいく - diễn ra suôn sẻ)');

INSERT INTO sense_glosses (sense_id, language_code, meaning, display_order) VALUES
(108, 'vi', 'diễn ra, tiến triển (suôn sẻ, trôi chảy)', 1),
(108, 'en', 'to proceed, to turn out, to go well', 2);

