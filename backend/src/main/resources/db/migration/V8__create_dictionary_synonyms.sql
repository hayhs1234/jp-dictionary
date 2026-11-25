-- ========================================================
-- Migration V8: Create dictionary_synonyms table and seed
-- synonyms & related grammar patterns for seeded entries (1..28)
-- ========================================================

SET NAMES utf8mb4;

CREATE TABLE dictionary_synonyms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    entry_id BIGINT NOT NULL,
    word VARCHAR(255) NOT NULL,
    reading VARCHAR(255) NULL,
    meaning TEXT NOT NULL,
    relation_type VARCHAR(20) NOT NULL DEFAULT 'synonym',
    display_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_synonyms_entry (entry_id),
    CONSTRAINT fk_synonyms_entry
        FOREIGN KEY (entry_id) REFERENCES dictionary_entries(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_synonyms_type
        CHECK (relation_type IN ('synonym', 'grammar'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO dictionary_synonyms (entry_id, word, reading, meaning, relation_type, display_order) VALUES
-- 1. 食べる (たべる)
(1, '食事する (しょくじする)', 'しょくじする', 'Dùng bữa, ăn uống (trang trọng hơn)', 'synonym', 1),
(1, '召し上がる (めしあがる)', 'めしあがる', 'Dùng bữa (tôn kính ngữ)', 'synonym', 2),
(1, '〜てみる', '', 'Thử làm... (VD: 食べてみる - ăn thử) - Ngữ pháp N4', 'grammar', 3),

-- 2. 飲む (のむ)
(2, '味わう (あじわう)', 'あじわう', 'Thưởng thức, nếm hương vị', 'synonym', 1),
(2, '〜てみる', '', 'Thử uống (VD: 飲んでみる) - Ngữ pháp N4', 'grammar', 2),

-- 3. 行く (いく)
(3, '出かける (でかける)', 'でかける', 'Ra ngoài, đi ra ngoài', 'synonym', 1),
(3, '参る (まいる)', 'まいる', 'Đi, đến (khiêm nhường ngữ)', 'synonym', 2),
(3, '〜ていく', '', 'Tiếp tục / đi dần về phía trước - Ngữ pháp N4', 'grammar', 3),

-- 4. 来る (くる)
(4, '訪れる (おとずれる)', 'おとずれる', 'Ghé thăm, tìm đến', 'synonym', 1),
(4, 'いらっしゃる', 'いらっしゃる', 'Đến (tôn kính ngữ)', 'synonym', 2),
(4, '〜てくる', '', 'Bắt đầu / trở nên / đi rồi quay lại - Ngữ pháp N4', 'grammar', 3),

-- 5. 本 (ほん)
(5, '書籍 (しょせき)', 'しょせき', 'Sách báo, thư tịch (trang trọng)', 'synonym', 1),
(5, '図書 (としょ)', 'としょ', 'Sách vở, tài liệu thư viện', 'synonym', 2),
(5, '〜冊 (〜さつ)', 'さつ', 'Lượng từ đếm quyển sách, tập vở', 'grammar', 3),

-- 6. 日本語 (にほんご)
(6, '国語 (こくご)', 'こくご', 'Quốc ngữ (cách gọi tiếng Nhật tại Nhật)', 'synonym', 1),
(6, '和語 (わご)', 'わご', 'Từ thuần Nhật', 'synonym', 2),

-- 7. 新しい (あたらしい)
(7, '新鮮 (しんせん)', 'しんせん', 'Tươi mới, mới mẻ (tính từ đuôi na)', 'synonym', 1),
(7, '最新 (さいしん)', 'さいしん', 'Mới nhất, tối tân', 'synonym', 2),
(7, '〜くなる', '', 'Trở nên mới hơn (新しくなる) - Ngữ pháp N5', 'grammar', 3),

-- 8. 古い (ふるい)
(8, '昔 (むかし)', 'むかし', 'Ngày xưa, thời xưa', 'synonym', 1),
(8, '時代遅れ (じだいおくれ)', 'じだいおくれ', 'Lỗi thời, lạc hậu', 'synonym', 2),

-- 9. 話す (はなす)
(9, '語る (かたる)', 'かたる', 'Kể lại, tâm sự, bàn luận', 'synonym', 1),
(9, '喋る (しゃべる)', 'しゃべる', 'Tán gẫu, nói chuyện thân mật', 'synonym', 2),
(9, '〜について話す', '', 'Nói về chủ đề... - Mẫu câu thường dùng', 'grammar', 3),

-- 10. 書く (かく)
(10, '記す (しるす)', 'しるす', 'Ghi chép, ghi nhận lại', 'synonym', 1),
(10, '記入する (きにゅうする)', 'きにゅうする', 'Điền vào, ghi vào (biểu mẫu)', 'synonym', 2),

-- 11. 聞く (きく)
(11, '尋ねる (たずねる)', 'たずねる', 'Hỏi thăm, dò hỏi', 'synonym', 1),
(11, '聴く (きく)', 'きく', 'Lắng nghe (âm nhạc, bài giảng)', 'synonym', 2),

-- 12. 見る (みる)
(12, '眺める (ながめる)', 'ながめる', 'Ngắm nhìn (phong cảnh)', 'synonym', 1),
(12, '観察する (かんさつする)', 'かんさつする', 'Quan sát, theo dõi', 'synonym', 2),
(12, '〜てみる', '', 'Thử làm gì đó (sau V-て) - Ngữ pháp N4', 'grammar', 3),

-- 13. 先生 (せんせい)
(13, '教師 (きょうし)', 'きょうし', 'Giáo viên (chỉ nghề nghiệp)', 'synonym', 1),
(13, '講師 (こうし)', 'こうし', 'Giảng viên, diễn giả', 'synonym', 2),

-- 14. 学生 (がくせい)
(14, '生徒 (せいと)', 'せいと', 'Học sinh (trung học)', 'synonym', 1),
(14, '児童 (じどう)', 'じどう', 'Học sinh tiểu học, nhi đồng', 'synonym', 2),

-- 15. 友達 (ともだち)
(15, '友人 (ゆうじん)', 'ゆうじん', 'Bạn bè, bằng hữu (trang trọng hơn)', 'synonym', 1),
(15, '仲間 (なかま)', 'なかま', 'Đồng đội, bạn đồng hành', 'synonym', 2),

-- 16. 送る (おくる)
(16, '届ける (とどける)', 'とどける', 'Giao đến, chuyển phát tận nơi', 'synonym', 1),
(16, '見送る (みおくる)', 'みおくる', 'Tiễn đưa (người thân, khách)', 'synonym', 2),

-- 17. 迎える (むかえる)
(17, '招く (まねく)', 'まねく', 'Mời, đón tiếp (khách)', 'synonym', 1),
(17, '迎えに行く (むかえにいく)', 'むかえにいく', 'Đi đón - Cụm động từ thường dùng', 'grammar', 2),

-- 18. 直す (なおす)
(18, '修理する (しゅうりする)', 'しゅうりする', 'Sửa chữa (máy móc, thiết bị kỹ thuật)', 'synonym', 1),
(18, '訂正する (ていせいする)', 'ていせいする', 'Đính chính, hiệu đính (lỗi sai)', 'synonym', 2),
(18, '〜直す (〜なおす)', 'なおす', 'Làm lại từ đầu (VD: 書き直す - viết lại)', 'grammar', 3),

-- 19. 親切 (しんせつ)
(19, '優しい (やさしい)', 'やさしい', 'Hiền từ, dịu dàng, chu đáo', 'synonym', 1),
(19, '丁寧 (ていねい)', 'ていねい', 'Lịch sự, ân cần, cẩn thận', 'synonym', 2),

-- 20. 便利 (べんり)
(20, '快適 (かいてき)', 'かいてき', 'Thoải mái, dễ chịu, tiện nghi', 'synonym', 1),
(20, '重宝 (ちょうほう)', 'ちょうほう', 'Hữu ích, tiện dụng', 'synonym', 2),

-- 21. 成功 (せいこう)
(21, '達成 (たっせい)', 'たっせい', 'Đạt được (mục tiêu, thành tựu)', 'synonym', 1),
(21, '勝利 (しょうり)', 'しょうり', 'Chiến thắng, thắng lợi', 'synonym', 2),

-- 22. 諦める (あきらめる)
(22, '断念する (だんねんする)', 'だんねんする', 'Từ bỏ ý định, bỏ cuộc (trang trọng)', 'synonym', 1),
(22, '投げ出す (なげだす)', 'なげだす', 'Buông xuôi, bỏ dở giữa chừng', 'synonym', 2),

-- 23. 努力 (どりょく)
(23, '頑張る (がんばる)', 'がんばる', 'Cố gắng, phấn đấu (khẩu ngữ)', 'synonym', 1),
(23, '精進 (しょうじん)', 'しょうじん', 'Tinh tấn, chuyên tâm rèn luyện', 'synonym', 2),

-- 24. 克服 (こくふく)
(24, '乗り越える (のりこえる)', 'のりこえる', 'Vượt qua (khó khăn, thử thách)', 'synonym', 1),
(24, '打破する (だはする)', 'だはする', 'Phá vỡ, vượt thoát (bế tắc)', 'synonym', 2),

-- 25. 曖昧 (あいまい)
(25, '不明確 (ふめいかく)', 'ふめいかく', 'Không rõ ràng, thiếu minh xác', 'synonym', 1),
(25, '漠然 (ばくぜん)', 'ばくぜん', 'Mơ hồ, mông lung', 'synonym', 2),

-- 26. 隠蔽 (いんぺい)
(26, '隠す (かくす)', 'かくす', 'Giấu giếm, che đậy (thông dụng)', 'synonym', 1),
(26, '秘密にする (ひみつにする)', 'ひみつにする', 'Giữ bí mật, giấu kín', 'synonym', 2),

-- 27. 示唆 (しさ)
(27, '暗示 (あんじ)', 'あんじ', 'Ám chỉ, gợi mở', 'synonym', 1),
(27, '提案 (ていあん)', 'ていあん', 'Đề xuất, kiến nghị', 'synonym', 2),

-- 28. 木漏れ日 (こもれび)
(28, '日光 (にっこう)', 'にっこう', 'Ánh sáng mặt trời (Đồng nghĩa rộng)', 'synonym', 1),
(28, '陽光 (ようこう)', 'ようこう', 'Dương quang, nắng ấm', 'synonym', 2),
(28, '〜ながら', '', 'Vừa... vừa - Ngữ pháp N4 thường dùng kèm', 'grammar', 3);

