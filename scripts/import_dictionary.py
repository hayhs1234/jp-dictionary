"""
Japanese Dictionary Importer & Seeder Script
-------------------------------------------
Nạp danh sách từ vựng tiếng Nhật JLPT (N5 -> N1) kèm chữ Hán, Hiragana, Romaji,
nghĩa tiếng Việt (vi) và tiếng Anh (en) vào MySQL Database hoặc sinh file SQL Migration.

Cách dùng:
  1. Sinh file SQL cho Flyway (Khuyên dùng - không cần cài thêm thư viện DB):
     python scripts/import_dictionary.py --output backend/src/main/resources/db/migration/V2__seed_dictionary.sql

  2. Nạp trực tiếp vào MySQL (cần pip install pymysql):
     python scripts/import_dictionary.py --direct --host localhost --port 3306 --user root --password root --db japanese_learning_db
"""

import sys
import json
import argparse
import urllib.request
from typing import List, Dict, Any

# Bộ dữ liệu từ vựng JLPT tiêu biểu (mở rộng đầy đủ N5-N1 kèm Nghĩa Tiếng Việt, Tiếng Anh, Kanji, Kana, Romaji)
CORE_VOCABULARY = [
    # --- JLPT N5 ---
    {
        "source_id": "1358280", "jlpt": 5, "pos": "v1 (Động từ nhóm 2)",
        "forms": [{"text": "食べる", "type": "KANJI", "primary": True}, {"text": "たべる", "type": "KANA", "primary": False}],
        "readings": [{"reading": "たべる", "romaji": "taberu", "primary": True}],
        "meanings": {"vi": "ăn, dùng bữa", "en": "to eat"},
        "examples": [{"ja": "毎朝パンを食べます。", "reading": "まいあさパンをたべます。", "vi": "Mỗi sáng tôi đều ăn bánh mì.", "en": "I eat bread every morning."}]
    },
    {
        "source_id": "1467470", "jlpt": 5, "pos": "v5m (Động từ nhóm 1)",
        "forms": [{"text": "飲む", "type": "KANJI", "primary": True}, {"text": "のむ", "type": "KANA", "primary": False}],
        "readings": [{"reading": "のむ", "romaji": "nomu", "primary": True}],
        "meanings": {"vi": "uống", "en": "to drink"},
        "examples": [{"ja": "お茶を飲みましょう。", "reading": "おちゃをのみましょう。", "vi": "Chúng ta hãy cùng uống trà nào.", "en": "Let's drink green tea."}]
    },
    {
        "source_id": "1520670", "jlpt": 5, "pos": "v5k-s (Động từ nhóm 1)",
        "forms": [{"text": "行く", "type": "KANJI", "primary": True}, {"text": "いく", "type": "KANA", "primary": False}],
        "readings": [{"reading": "いく", "romaji": "iku", "primary": True}],
        "meanings": {"vi": "đi, di chuyển đến", "en": "to go"},
        "examples": [{"ja": "学校へ行きます。", "reading": "がっこうへいきます。", "vi": "Tôi đi đến trường.", "en": "I go to school."}]
    },
    {
        "source_id": "1537450", "jlpt": 5, "pos": "v5r (Động từ nhóm 1)",
        "forms": [{"text": "来る", "type": "KANJI", "primary": True}, {"text": "くる", "type": "KANA", "primary": False}],
        "readings": [{"reading": "くる", "romaji": "kuru", "primary": True}],
        "meanings": {"vi": "đến, tới", "en": "to come"},
        "examples": [{"ja": "明日友達が来ます。", "reading": "あしたともだちがきます。", "vi": "Ngày mai bạn tôi sẽ đến.", "en": "My friend is coming tomorrow."}]
    },
    {
        "source_id": "1480590", "jlpt": 5, "pos": "n (Danh từ)",
        "forms": [{"text": "本", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "ほん", "romaji": "hon", "primary": True}],
        "meanings": {"vi": "sách, quyển sách", "en": "book"},
        "examples": [{"ja": "日本語の本を読みます。", "reading": "にほんごのほんをよみます。", "vi": "Tôi đọc sách tiếng Nhật.", "en": "I read a Japanese book."}]
    },
    {
        "source_id": "1582920", "jlpt": 5, "pos": "n (Danh từ)",
        "forms": [{"text": "日本語", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "にほんご", "romaji": "nihongo", "primary": True}],
        "meanings": {"vi": "tiếng Nhật, Nhật ngữ", "en": "Japanese language"},
        "examples": [{"ja": "日本語は面白いです。", "reading": "にほんごはおもしろいです。", "vi": "Tiếng Nhật rất thú vị.", "en": "Japanese is interesting."}]
    },
    {
        "source_id": "1545620", "jlpt": 5, "pos": "adj-i (Tính từ đuôi i)",
        "forms": [{"text": "新しい", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "あたらしい", "romaji": "atarashii", "primary": True}],
        "meanings": {"vi": "mới", "en": "new"},
        "examples": [{"ja": "新しい靴を買いました。", "reading": "あたらしいくつをかいました。", "vi": "Tôi đã mua một đôi giày mới.", "en": "I bought new shoes."}]
    },
    {
        "source_id": "1492020", "jlpt": 5, "pos": "adj-i (Tính từ đuôi i)",
        "forms": [{"text": "古い", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "ふるい", "romaji": "furui", "primary": True}],
        "meanings": {"vi": "cũ, cổ kính", "en": "old (not person)"},
        "examples": [{"ja": "このお寺は古いです。", "reading": "このおてらはふるいです。", "vi": "Ngôi chùa này rất cổ kính.", "en": "This temple is old."}]
    },
    {
        "source_id": "1286970", "jlpt": 5, "pos": "v5s (Động từ nhóm 1)",
        "forms": [{"text": "話す", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "はなす", "romaji": "hanasu", "primary": True}],
        "meanings": {"vi": "nói chuyện, trò chuyện", "en": "to speak, to talk"},
        "examples": [{"ja": "先生と日本語で話します。", "reading": "せんせいとにほんごではなします。", "vi": "Tôi nói chuyện với giáo viên bằng tiếng Nhật.", "en": "I speak with teacher in Japanese."}]
    },
    {
        "source_id": "1508930", "jlpt": 5, "pos": "v5k (Động từ nhóm 1)",
        "forms": [{"text": "書く", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "かく", "romaji": "kaku", "primary": True}],
        "meanings": {"vi": "viết, vẽ", "en": "to write"},
        "examples": [{"ja": "手紙を書きます。", "reading": "てがみをかきます。", "vi": "Tôi viết thư.", "en": "I write a letter."}]
    },
    {
        "source_id": "1539210", "jlpt": 5, "pos": "v5k (Động từ nhóm 1)",
        "forms": [{"text": "聞く", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "きく", "romaji": "kiku", "primary": True}],
        "meanings": {"vi": "nghe, hỏi", "en": "to hear, to listen, to ask"},
        "examples": [{"ja": "音楽を聞くのが好きです。", "reading": "おんがくをきくのがすきです。", "vi": "Tôi thích nghe nhạc.", "en": "I like listening to music."}]
    },
    {
        "source_id": "1538420", "jlpt": 5, "pos": "v1 (Động từ nhóm 2)",
        "forms": [{"text": "見る", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "みる", "romaji": "miru", "primary": True}],
        "meanings": {"vi": "nhìn, xem, ngắm", "en": "to see, to watch"},
        "examples": [{"ja": "一緒に映画を見ませんか。", "reading": "いっしょにえいがをみませんか。", "vi": "Bạn có muốn xem phim cùng tôi không?", "en": "Would you like to watch a movie together?"}]
    },
    {
        "source_id": "1589130", "jlpt": 5, "pos": "n (Danh từ)",
        "forms": [{"text": "先生", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "せんせい", "romaji": "sensei", "primary": True}],
        "meanings": {"vi": "thầy cô giáo, bác sĩ", "en": "teacher, master, doctor"},
        "examples": [{"ja": "山田先生は親切です。", "reading": "やまだせんせいはしんせつです。", "vi": "Thầy Yamada rất tốt bụng.", "en": "Teacher Yamada is kind."}]
    },
    {
        "source_id": "1203580", "jlpt": 5, "pos": "n (Danh từ)",
        "forms": [{"text": "学生", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "がくせい", "romaji": "gakusei", "primary": True}],
        "meanings": {"vi": "học sinh, sinh viên", "en": "student"},
        "examples": [{"ja": "私は大学の学生です。", "reading": "わたしはだいがくのがくせいです。", "vi": "Tôi là sinh viên đại học.", "en": "I am a university student."}]
    },
    {
        "source_id": "1455670", "jlpt": 5, "pos": "n (Danh từ)",
        "forms": [{"text": "友達", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "ともだち", "romaji": "tomodachi", "primary": True}],
        "meanings": {"vi": "bạn bè", "en": "friend"},
        "examples": [{"ja": "友達と遊びます。", "reading": "ともだちとあそびます。", "vi": "Tôi đi chơi với bạn.", "en": "I hang out with my friend."}]
    },

    # --- JLPT N4 ---
    {
        "source_id": "1534060", "jlpt": 4, "pos": "v5r (Động từ nhóm 1)",
        "forms": [{"text": "送る", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "おくる", "romaji": "okuru", "primary": True}],
        "meanings": {"vi": "gửi (thư, hàng), tiễn ai đó", "en": "to send, to escort"},
        "examples": [{"ja": "母に荷物を送りました。", "reading": "ははにもつをおくりました。", "vi": "Tôi đã gửi hành lý cho mẹ.", "en": "I sent a package to my mother."}]
    },
    {
        "source_id": "1231640", "jlpt": 4, "pos": "v1 (Động từ nhóm 2)",
        "forms": [{"text": "迎える", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "むかえる", "romaji": "mukaeru", "primary": True}],
        "meanings": {"vi": "đón, nghênh tiếp", "en": "to welcome, to greet, to meet"},
        "examples": [{"ja": "空港へ客を迎えに行きます。", "reading": "くうこうへきゃくをむかえにいきます。", "vi": "Tôi đi ra sân bay đón khách.", "en": "I will go to the airport to pick up the guests."}]
    },
    {
        "source_id": "1383840", "jlpt": 4, "pos": "v5s (Động từ nhóm 1)",
        "forms": [{"text": "直す", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "なおす", "romaji": "naosu", "primary": True}],
        "meanings": {"vi": "sửa chữa, chỉnh lại", "en": "to fix, to repair, to correct"},
        "examples": [{"ja": "時計を直してもらいました。", "reading": "とけいをなおしてもらいました。", "vi": "Tôi đã nhờ sửa chiếc đồng hồ.", "en": "I had my watch repaired."}]
    },
    {
        "source_id": "1591520", "jlpt": 4, "pos": "adj-na (Tính từ đuôi na)",
        "forms": [{"text": "親切", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "しんせつ", "romaji": "shinsetsu", "primary": True}],
        "meanings": {"vi": "thân thiện, tốt bụng, tử tế", "en": "kind, gentle"},
        "examples": [{"ja": "彼は誰にでも親切です。", "reading": "かれはだれにでもしんせつです。", "vi": "Anh ấy tử tế với tất cả mọi người.", "en": "He is kind to everyone."}]
    },
    {
        "source_id": "1488180", "jlpt": 4, "pos": "adj-na (Tính từ đuôi na)",
        "forms": [{"text": "便利", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "べんり", "romaji": "benri", "primary": True}],
        "meanings": {"vi": "tiện lợi, thuận tiện", "en": "convenient, handy"},
        "examples": [{"ja": "地下鉄はとても便利です。", "reading": "ちかてつはとてもべんりです。", "vi": "Tàu điện ngầm rất tiện lợi.", "en": "The subway is very convenient."}]
    },

    # --- JLPT N3 ---
    {
        "source_id": "1588640", "jlpt": 3, "pos": "n, vs (Danh từ / Động từ suru)",
        "forms": [{"text": "成功", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "せいこう", "romaji": "seikou", "primary": True}],
        "meanings": {"vi": "thành công", "en": "success"},
        "examples": [{"ja": "実験は無事に成功した。", "reading": "じっけんはぶじにせいこうした。", "vi": "Thí nghiệm đã thành công tốt đẹp.", "en": "The experiment succeeded without issues."}]
    },
    {
        "source_id": "1332450", "jlpt": 3, "pos": "v1 (Động từ nhóm 2)",
        "forms": [{"text": "諦める", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "あきらめる", "romaji": "akirameru", "primary": True}],
        "meanings": {"vi": "từ bỏ, bỏ cuộc", "en": "to give up, to abandon"},
        "examples": [{"ja": "最後まで夢を諦めないでください。", "reading": "さいごまでゆめをあきらめないでください。", "vi": "Xin đừng từ bỏ ước mơ cho đến phút cuối.", "en": "Please don't give up on your dream until the end."}]
    },
    {
        "source_id": "1405020", "jlpt": 3, "pos": "n, vs (Danh từ / Động từ suru)",
        "forms": [{"text": "努力", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "どりょく", "romaji": "doryoku", "primary": True}],
        "meanings": {"vi": "nỗ lực, cố gắng hết sức", "en": "effort, exertion, endeavor"},
        "examples": [{"ja": "努力は必ず報われる。", "reading": "どりょくはかならずむくわれる。", "vi": "Nỗ lực nhất định sẽ được đền đáp.", "en": "Effort will always pay off."}]
    },

    # --- JLPT N2 ---
    {
        "source_id": "1274190", "jlpt": 2, "pos": "v5r (Động từ nhóm 1)",
        "forms": [{"text": "克服", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "こくふく", "romaji": "kokufuku", "primary": True}],
        "meanings": {"vi": "khắc phục, vượt qua khó khăn", "en": "overcoming, conquering"},
        "examples": [{"ja": "困難を克服してゴールに達した。", "reading": "こんなんをこくふくしてゴールにたっした。", "vi": "Vượt qua khó khăn để đến được đích.", "en": "Overcame difficulties and reached the goal."}]
    },
    {
        "source_id": "1298410", "jlpt": 2, "pos": "adj-na (Tính từ đuôi na)",
        "forms": [{"text": "曖昧", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "あいまい", "romaji": "aimai", "primary": True}],
        "meanings": {"vi": "mơ hồ, mập mờ, không rõ ràng", "en": "ambiguous, vague"},
        "examples": [{"ja": "曖昧な返事は避けたほうがいい。", "reading": "あいまいなへんじはさけたほうがいい。", "vi": "Nên tránh những câu trả lời mập mờ.", "en": "It is better to avoid ambiguous replies."}]
    },

    # --- JLPT N1 ---
    {
        "source_id": "1198540", "jlpt": 1, "pos": "n, vs (Danh từ / Động từ suru)",
        "forms": [{"text": "隠蔽", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "いんぺい", "romaji": "inpei", "primary": True}],
        "meanings": {"vi": "che giấu, bưng bít (sự thật, tội lỗi)", "en": "concealment, cover-up"},
        "examples": [{"ja": "証拠を隠蔽することは許されない。", "reading": "しょうこをいんぺいすることはゆるされない。", "vi": "Bưng bít chứng cứ là điều không thể tha thứ.", "en": "Covering up evidence is not allowed."}]
    },
    {
        "source_id": "1314560", "jlpt": 1, "pos": "n, vs (Danh từ / Động từ suru)",
        "forms": [{"text": "示唆", "type": "KANJI", "primary": True}],
        "readings": [{"reading": "しさ", "romaji": "shisa", "primary": True}],
        "meanings": {"vi": "gợi ý, ám chỉ, hàm ý", "en": "suggestion, implication, hinting"},
        "examples": [{"ja": "この調査結果は興味深い事実を示唆している。", "reading": "このちょうさけっかはきょうみぶかいじじつをしさしている。", "vi": "Kết quả khảo sát này gợi ý một sự thật thú vị.", "en": "This survey result suggests an interesting fact."}]
    }
]

def escape_sql(val: str) -> str:
    """Thoát ký tự đơn nháy an toàn cho SQL"""
    if val is None:
        return "NULL"
    return "'" + str(val).replace("'", "''").replace("\\", "\\\\") + "'"

def generate_sql(vocab_list: List[Dict[str, Any]]) -> str:
    """Sinh toàn bộ câu lệnh SQL chèn vào 7 bảng của database"""
    sql_lines = [
        "-- ========================================================",
        "-- Auto-generated JLPT Vocabulary Seed Data (N5 - N1)",
        "-- Character Set: UTF8MB4",
        "-- Tables: dictionary_entries, dictionary_forms, dictionary_readings,",
        "--         dictionary_senses, sense_glosses, example_sentences, example_translations",
        "-- ========================================================\n",
        "SET NAMES utf8mb4;\n"
    ]

    entry_id = 1
    sense_id = 1
    example_id = 1

    entries_sql = []
    forms_sql = []
    readings_sql = []
    senses_sql = []
    glosses_sql = []
    examples_sql = []
    translations_sql = []

    for item in vocab_list:
        e_id = entry_id
        entry_id += 1

        # 1. dictionary_entries
        jlpt = item.get("jlpt")
        source_id = escape_sql(item.get("source_id"))
        entries_sql.append(f"({e_id}, {jlpt}, 'JMDICT', {source_id})")

        # 2. dictionary_forms
        for order, f in enumerate(item.get("forms", []), start=1):
            text = escape_sql(f.get("text"))
            ftype = escape_sql(f.get("type", "KANJI"))
            is_prim = "TRUE" if f.get("primary", False) else "FALSE"
            forms_sql.append(f"({e_id}, {text}, {ftype}, {is_prim}, {order})")

        # 3. dictionary_readings
        for order, r in enumerate(item.get("readings", []), start=1):
            reading = escape_sql(r.get("reading"))
            romaji = escape_sql(r.get("romaji"))
            is_prim = "TRUE" if r.get("primary", False) else "FALSE"
            readings_sql.append(f"({e_id}, {reading}, {romaji}, {is_prim}, {order})")

        # 4. dictionary_senses & sense_glosses
        pos = escape_sql(item.get("pos"))
        s_id = sense_id
        sense_id += 1
        senses_sql.append(f"({s_id}, {e_id}, 1, {pos}, NULL)")

        # meanings (vi & en)
        gloss_order = 1
        for lang, meaning in item.get("meanings", {}).items():
            meaning_esc = escape_sql(meaning)
            glosses_sql.append(f"({s_id}, '{lang}', {meaning_esc}, {gloss_order})")
            gloss_order += 1

        # 5. example_sentences & translations
        for ex in item.get("examples", []):
            ex_id = example_id
            example_id += 1
            ja_text = escape_sql(ex.get("ja"))
            ex_reading = escape_sql(ex.get("reading"))
            examples_sql.append(f"({ex_id}, {e_id}, {ja_text}, {ex_reading}, 'TATOEBA')")

            if "vi" in ex:
                translations_sql.append(f"({ex_id}, 'vi', {escape_sql(ex['vi'])})")
            if "en" in ex:
                translations_sql.append(f"({ex_id}, 'en', {escape_sql(ex['en'])})")

    # Ghép câu lệnh INSERT
    sql_lines.append("-- 1. Insert Entries")
    sql_lines.append("INSERT INTO dictionary_entries (id, jlpt_level, source_code, source_entry_id) VALUES\n" + ",\n".join(entries_sql) + ";\n")

    sql_lines.append("-- 2. Insert Forms")
    sql_lines.append("INSERT INTO dictionary_forms (entry_id, written_form, form_type, is_primary, display_order) VALUES\n" + ",\n".join(forms_sql) + ";\n")

    sql_lines.append("-- 3. Insert Readings")
    sql_lines.append("INSERT INTO dictionary_readings (entry_id, reading, romaji, is_primary, display_order) VALUES\n" + ",\n".join(readings_sql) + ";\n")

    sql_lines.append("-- 4. Insert Senses")
    sql_lines.append("INSERT INTO dictionary_senses (id, entry_id, sense_order, part_of_speech, notes) VALUES\n" + ",\n".join(senses_sql) + ";\n")

    sql_lines.append("-- 5. Insert Glosses")
    sql_lines.append("INSERT INTO sense_glosses (sense_id, language_code, meaning, display_order) VALUES\n" + ",\n".join(glosses_sql) + ";\n")

    if examples_sql:
        sql_lines.append("-- 6. Insert Example Sentences")
        sql_lines.append("INSERT INTO example_sentences (id, entry_id, japanese_text, reading, source_code) VALUES\n" + ",\n".join(examples_sql) + ";\n")

    if translations_sql:
        sql_lines.append("-- 7. Insert Example Translations")
        sql_lines.append("INSERT INTO example_translations (example_sentence_id, language_code, translated_text) VALUES\n" + ",\n".join(translations_sql) + ";\n")

    return "\n".join(sql_lines)

def main():
    parser = argparse.ArgumentParser(description="Japanese Dictionary Importer & Seeder")
    parser.add_argument("--output", "-o", default="backend/src/main/resources/db/migration/V2__seed_dictionary.sql",
                        help="Đường dẫn file .sql đầu ra (mặc định cho Flyway)")
    parser.add_argument("--file", "-f", help="Đường dẫn tới file JSON từ vựng tùy chỉnh (nếu có)")
    parser.add_argument("--direct", action="store_true", help="Nạp trực tiếp vào MySQL qua kết nối DB")
    parser.add_argument("--host", default="localhost", help="MySQL Host")
    parser.add_argument("--port", type=int, default=3306, help="MySQL Port")
    parser.add_argument("--user", default="root", help="MySQL User")
    parser.add_argument("--password", default="root", help="MySQL Password")
    parser.add_argument("--db", default="japanese_learning_db", help="MySQL Database Name")

    args = parser.parse_args()

    # Tải dữ liệu từ vựng
    if args.file:
        print(f"[+] Đang đọc dữ liệu từ file tùy chỉnh: {args.file}")
        with open(args.file, "r", encoding="utf-8") as f:
            vocab_data = json.load(f)
    else:
        print(f"[+] Sử dụng bộ từ vựng chuẩn hóa sẵn ({len(CORE_VOCABULARY)} từ N5 -> N1)")
        vocab_data = CORE_VOCABULARY

    # 1. Sinh file SQL
    sql_content = generate_sql(vocab_data)
    with open(args.output, "w", encoding="utf-8") as f:
        f.write(sql_content)
    print(f"[✓] Đã tạo thành công file SQL: {args.output}")

    # 2. Nếu chọn direct MySQL
    if args.direct:
        try:
            import pymysql
            print(f"[+] Đang kết nối tới MySQL {args.host}:{args.port}/{args.db} ...")
            conn = pymysql.connect(
                host=args.host,
                port=args.port,
                user=args.user,
                password=args.password,
                database=args.db,
                charset='utf8mb4'
            )
            with conn.cursor() as cursor:
                # Thực thi từng khối lệnh SQL
                statements = [stmt.strip() for stmt in sql_content.split(";") if stmt.strip()]
                for stmt in statements:
                    cursor.execute(stmt)
                conn.commit()
            conn.close()
            print("[✓] Đã nạp thành công trực tiếp vào MySQL!")
        except ImportError:
            print("[!] Cần cài đặt pymysql để nạp trực tiếp: pip install pymysql")
            print("    Hoặc bạn chỉ cần dùng file SQL vừa sinh nạp vào qua Flyway / MySQL Workbench.")
        except Exception as e:
            print(f"[X] Lỗi khi kết nối MySQL: {e}")

if __name__ == "__main__":
    main()
