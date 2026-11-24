import axiosClient from './api/axiosClient.js';

// Comprehensive offline fallback vocabulary database
const FALLBACK_WORDS = [
  {
    id: 100,
    kanji: '木漏れ日',
    hiragana: 'こもれび',
    romaji: 'komorebi',
    hanViet: 'MỘC LẬU NHẬT',
    pitchAccent: '[0] 平板 (Heiban)',
    jlptLevel: 'N2',
    partOfSpeech: 'Danh từ [名]',
    primaryMeaning: 'Ánh nắng lọt qua kẽ lá',
    detailedMeaning:
      'Hiện tượng quang học và mỹ học tự nhiên khi ánh sáng mặt trời chiếu xuyên qua vòm tán lá cây rậm rạp tạo thành những vệt sáng lấp lánh đan xen trên mặt đất hoặc bờ tường.',
    senses: [
      {
        senseOrder: 1,
        partOfSpeech: 'Danh từ [名]',
        meaning: 'Ánh nắng lọt qua kẽ lá',
        detailedMeaning:
          'Hiện tượng quang học và mỹ học tự nhiên khi ánh sáng mặt trời chiếu xuyên qua vòm tán lá cây rậm rạp tạo thành những vệt sáng lấp lánh đan xen trên mặt đất hoặc bờ tường.',
        glosses: ['Ánh nắng lọt qua kẽ lá'],
        englishGlosses: ['sunlight filtering through trees'],
      },
    ],
    furiganaSegments: [
      { ruby: '木', rt: 'こ' },
      { ruby: '漏', rt: 'れ' },
      { ruby: '日', rt: 'び' },
    ],
    kanjiBreakdown: [
      {
        character: '木',
        strokeCount: 4,
        jlptLevel: 'Cấp độ N5',
        hanViet: 'MỘC (Cây)',
        onReadings: 'モク, ボク',
        kunReadings: 'き, こ',
        meaning: 'Cây cối, gỗ, chất liệu thực vật.',
      },
      {
        character: '漏',
        strokeCount: 14,
        jlptLevel: 'Cấp độ N1',
        hanViet: 'LẬU (Rò rỉ, lọt)',
        onReadings: 'ロウ',
        kunReadings: 'も-る, も-れる',
        meaning: 'Rò rỉ nước, lọt ánh sáng, sơ hở.',
      },
      {
        character: '日',
        strokeCount: 4,
        jlptLevel: 'Cấp độ N5',
        hanViet: 'NHẬT (Mặt trời)',
        onReadings: 'ニチ, ジツ',
        kunReadings: 'ひ, -び',
        meaning: 'Thái dương, ban ngày, nhật quang.',
      },
    ],
    examples: [
      {
        id: 1,
        japaneseText: '森の中を歩くと、美しい木漏れ日が地面を照らしていた。',
        reading: 'もりのなかをあるくと、うつくしいこもれびがじめんをてらしていた。',
        translation:
          'Đi dạo trong rừng, những tia nắng len qua kẽ lá rọi sáng tuyệt đẹp xuống mặt đất.',
        context: 'Miêu tả văn học / Cảnh sắc',
        level: 'Trung cấp N3',
      },
      {
        id: 2,
        japaneseText: '窓から差し込む木漏れ日を眺めながら、静かに本を読むのが好きだ。',
        reading: 'まどからさしこむこもれびをながめながら、しずかにほんをよむのがすきだ。',
        translation:
          'Tôi thích ngồi đọc sách trong yên ả, ngắm nhìn vạt nắng xuyên qua kẽ lá rọi vào qua khung cửa sổ.',
        context: 'Đời sống thường nhật',
        level: 'N3',
      },
    ],
    synonyms: [
      {
        word: '日光 (にっこう)',
        reading: 'にっこう',
        meaning: 'Ánh sáng mặt trời (Đồng nghĩa rộng)',
        type: 'synonym',
      },
      {
        word: '陽光 (ようこう)',
        reading: 'ようこう',
        meaning: 'Dương quang, nắng ấm',
        type: 'synonym',
      },
      {
        word: '〜ながら (Vừa... vừa)',
        reading: '',
        meaning: 'Ngữ pháp N4 thường dùng kèm',
        type: 'grammar',
      },
    ],
  },
  {
    id: 1,
    kanji: '食べる',
    hiragana: 'たべる',
    romaji: 'taberu',
    hanViet: 'THỰC',
    pitchAccent: '[2] 頭高',
    jlptLevel: 'N5',
    partOfSpeech: 'Động từ nhóm 2 [動]',
    primaryMeaning: 'ăn, dùng bữa',
    detailedMeaning:
      'Hành động nhai và nuốt thức ăn để nạp dinh dưỡng; dùng bữa sáng, trưa hoặc tối.',
    senses: [
      {
        senseOrder: 1,
        partOfSpeech: 'Động từ nhóm 2 [動]',
        meaning: 'ăn, dùng bữa',
        detailedMeaning:
          'Hành động nhai và nuốt thức ăn để nạp dinh dưỡng; dùng bữa sáng, trưa hoặc tối.',
        glosses: ['ăn', 'dùng bữa'],
        englishGlosses: ['to eat'],
      },
      {
        senseOrder: 2,
        partOfSpeech: 'Động từ nhóm 2 [動]',
        meaning: 'kiếm sống, mưu sinh',
        detailedMeaning:
          'Kiếm kế sinh nhai qua một nghề nghiệp hoặc nguồn thu nhập (ví dụ: 音楽で食べていく - sống bằng nghề âm nhạc).',
        glosses: ['kiếm sống', 'mưu sinh'],
        englishGlosses: ['to live on', 'to make a living'],
      },
    ],
    furiganaSegments: [
      { ruby: '食', rt: 'た' },
      { ruby: 'べる', rt: '' },
    ],
    kanjiBreakdown: [
      {
        character: '食',
        strokeCount: 9,
        jlptLevel: 'Cấp độ N5',
        hanViet: 'THỰC (Ăn)',
        onReadings: 'ショク',
        kunReadings: 'た-べる, く-う',
        meaning: 'Ăn uống, thức ăn, thực phẩm.',
      },
    ],
    examples: [
      {
        id: 101,
        japaneseText: '毎朝パンを食べます。',
        reading: 'まいあさパンをたべます。',
        translation: 'Mỗi sáng tôi đều ăn bánh mì.',
        context: 'Sinh hoạt thường nhật',
        level: 'Sơ cấp N5',
      },
      {
        id: 102,
        japaneseText: '一緒に美味しい日本料理を食べましょう。',
        reading: 'いっしょにおいしいにほんりょうりをたべましょう。',
        translation: 'Hãy cùng nhau ăn món ăn Nhật ngon nhé.',
        context: 'Giao tiếp bạn bè',
        level: 'Sơ cấp N5',
      },
    ],
    synonyms: [
      {
        word: '飲む (のむ)',
        reading: 'のむ',
        meaning: 'Uống (thức uống)',
        type: 'synonym',
      },
      {
        word: '食事する (しょくじする)',
        reading: 'しょくじする',
        meaning: 'Dùng bữa, ẩm thực',
        type: 'synonym',
      },
      {
        word: '〜たい (Muốn làm...)',
        reading: '',
        meaning: 'Ngữ pháp N5: 食べたい (Muốn ăn)',
        type: 'grammar',
      },
    ],
  },
  {
    id: 6,
    kanji: '日本語',
    hiragana: 'にほんご',
    romaji: 'nihongo',
    hanViet: 'NHẬT BẢN NGỮ',
    pitchAccent: '[0] 平板 (Heiban)',
    jlptLevel: 'N5',
    partOfSpeech: 'Danh từ [名]',
    primaryMeaning: 'tiếng Nhật, Nhật ngữ',
    detailedMeaning: 'Ngôn ngữ chính thức được sử dụng tại đất nước Nhật Bản.',
    furiganaSegments: [
      { ruby: '日', rt: 'に' },
      { ruby: '本', rt: 'ほん' },
      { ruby: '語', rt: 'ご' },
    ],
    kanjiBreakdown: [
      {
        character: '日',
        strokeCount: 4,
        jlptLevel: 'Cấp độ N5',
        hanViet: 'NHẬT (Mặt trời)',
        onReadings: 'ニチ, ジツ',
        kunReadings: 'ひ, -び',
        meaning: 'Mặt trời, ban ngày, nước Nhật.',
      },
      {
        character: '本',
        strokeCount: 5,
        jlptLevel: 'Cấp độ N5',
        hanViet: 'BỔN (Gốc, sách)',
        onReadings: 'ホン',
        kunReadings: 'もと',
        meaning: 'Gốc rễ, sách vở, nguồn gốc.',
      },
      {
        character: '語',
        strokeCount: 14,
        jlptLevel: 'Cấp độ N5',
        hanViet: 'NGỮ (Ngôn ngữ)',
        onReadings: 'ゴ',
        kunReadings: 'かた-る',
        meaning: 'Lời nói, ngôn ngữ, kể lại.',
      },
    ],
    examples: [
      {
        id: 106,
        japaneseText: '日本語は面白いです。',
        reading: 'にほんごはおもしろいです。',
        translation: 'Tiếng Nhật rất thú vị.',
        context: 'Học tập',
        level: 'Sơ cấp N5',
      },
      {
        id: 107,
        japaneseText: '先生と日本語で楽しく話します。',
        reading: 'せんせいとにほんごでたのしくはなします。',
        translation: 'Nói chuyện vui vẻ với thầy cô bằng tiếng Nhật.',
        context: 'Giao tiếp trường học',
        level: 'Sơ cấp N5',
      },
    ],
    synonyms: [
      {
        word: '国語 (こくご)',
        reading: 'こくご',
        meaning: 'Quốc ngữ (cách gọi tiếng Nhật ở Nhật)',
        type: 'synonym',
      },
      {
        word: '和語 (わご)',
        reading: 'わご',
        meaning: 'Từ thuần Nhật',
        type: 'synonym',
      },
    ],
  },
  {
    id: 5,
    kanji: '本',
    hiragana: 'ほん',
    romaji: 'hon',
    hanViet: 'BỔN, BẢN',
    pitchAccent: '[1] 頭高',
    jlptLevel: 'N5',
    partOfSpeech: 'Danh từ [名]',
    primaryMeaning: 'sách, quyển sách',
    detailedMeaning: 'Tập hợp các trang giấy in chữ hoặc tranh ảnh được đóng lại thành tập.',
    furiganaSegments: [{ ruby: '本', rt: 'ほん' }],
    kanjiBreakdown: [
      {
        character: '本',
        strokeCount: 5,
        jlptLevel: 'Cấp độ N5',
        hanViet: 'BỔN (Gốc, sách)',
        onReadings: 'ホン',
        kunReadings: 'もと',
        meaning: 'Sách vở, gốc rễ, nền tảng.',
      },
    ],
    examples: [
      {
        id: 105,
        japaneseText: '日本語の本を読みます。',
        reading: 'にほんごのほんをよみます。',
        translation: 'Tôi đọc sách tiếng Nhật.',
        context: 'Học tập',
        level: 'Sơ cấp N5',
      },
    ],
    synonyms: [
      {
        word: '書籍 (しょせき)',
        reading: 'しょせき',
        meaning: 'Sách báo, thư tịch',
        type: 'synonym',
      },
    ],
  },
  {
    id: 13,
    kanji: '先生',
    hiragana: 'せんせい',
    romaji: 'sensei',
    hanViet: 'TIÊN SINH',
    pitchAccent: '[3] 中高',
    jlptLevel: 'N5',
    partOfSpeech: 'Danh từ [名]',
    primaryMeaning: 'thầy cô giáo, bác sĩ, tiền bối',
    detailedMeaning:
      'Cách gọi tôn kính dành cho giáo viên, giảng viên, bác sĩ, luật sư hoặc người đi trước.',
    furiganaSegments: [
      { ruby: '先', rt: 'せん' },
      { ruby: '生', rt: 'せい' },
    ],
    kanjiBreakdown: [
      {
        character: '先',
        strokeCount: 6,
        jlptLevel: 'Cấp độ N5',
        hanViet: 'TIÊN (Trước)',
        onReadings: 'セン',
        kunReadings: 'さき',
        meaning: 'Đi trước, thời trước, tiên sinh.',
      },
      {
        character: '生',
        strokeCount: 5,
        jlptLevel: 'Cấp độ N5',
        hanViet: 'SINH (Sống, sinh)',
        onReadings: 'セイ, ショウ',
        kunReadings: 'い-きる, う-まれる',
        meaning: 'Sinh sống, sinh ra, học sinh.',
      },
    ],
    examples: [
      {
        id: 113,
        japaneseText: '山田先生は親切です。',
        reading: 'やまだせんせいはしんせつです。',
        translation: 'Thầy Yamada rất tốt bụng.',
        context: 'Trường học',
        level: 'Sơ cấp N5',
      },
    ],
    synonyms: [
      {
        word: '教師 (きょうし)',
        reading: 'きょうし',
        meaning: 'Giáo viên (nghề nghiệp)',
        type: 'synonym',
      },
    ],
  },
  {
    id: 18,
    kanji: '直す',
    hiragana: 'なおす',
    romaji: 'naosu',
    hanViet: 'TRỰC',
    pitchAccent: '[2] 頭高',
    jlptLevel: 'N4',
    partOfSpeech: 'Động từ nhóm 1 [動]',
    primaryMeaning: 'sửa chữa, chỉnh sửa',
    detailedMeaning:
      'Sửa chữa đồ vật bị hư hỏng hoặc khôi phục về trạng thái hoạt động tốt ban đầu.',
    senses: [
      {
        senseOrder: 1,
        partOfSpeech: 'Động từ nhóm 1 [動]',
        meaning: 'sửa chữa, chỉnh sửa (đồ vật hỏng)',
        detailedMeaning:
          'Sửa chữa đồ vật bị hư hỏng hoặc khôi phục về trạng thái hoạt động tốt ban đầu.',
        glosses: ['sửa chữa', 'chỉnh sửa'],
        englishGlosses: ['to repair', 'to fix'],
      },
      {
        senseOrder: 2,
        partOfSpeech: 'Động từ nhóm 1 [動]',
        meaning: 'đính chính, sửa lỗi sai, chữa trị',
        detailedMeaning:
          'Đính chính lỗi chính tả, sửa phát âm hoặc chữa khỏi thói quen xấu/bệnh tật.',
        glosses: ['đính chính', 'sửa sai', 'chữa trị'],
        englishGlosses: ['to correct', 'to rectify', 'to cure'],
      },
      {
        senseOrder: 3,
        partOfSpeech: 'Động từ nhóm 1 [動]',
        meaning: 'dịch thuật, quy đổi',
        detailedMeaning:
          'Dịch sang một ngôn ngữ khác hoặc chuyển đổi đơn vị đo lường/tiền tệ.',
        glosses: ['dịch sang ngôn ngữ khác', 'quy đổi đơn vị'],
        englishGlosses: ['to translate', 'to convert'],
      },
      {
        senseOrder: 4,
        partOfSpeech: 'Trợ động từ [接尾]',
        meaning: 'làm lại từ đầu (sau V-masu)',
        detailedMeaning:
          'Làm lại một hành động từ đầu để đạt kết quả tốt hơn (VD: 書き直す - viết lại).',
        glosses: ['làm lại từ đầu'],
        englishGlosses: ['to redo', 'to do over again'],
      },
    ],
    furiganaSegments: [
      { ruby: '直', rt: 'なお' },
      { ruby: 'す', rt: '' },
    ],
    kanjiBreakdown: [
      {
        character: '直',
        strokeCount: 8,
        jlptLevel: 'Cấp độ N4',
        hanViet: 'TRỰC (Ngay thẳng, sửa)',
        onReadings: 'チョク, ジキ',
        kunReadings: 'なお-す, なお-る',
        meaning: 'Ngay thẳng, trực tiếp, sửa chữa, đính chính.',
      },
    ],
    examples: [
      {
        id: 118,
        japaneseText: '壊れた時計を自分で直しました。',
        reading: 'こわれたとけいをじぶんでなおしました。',
        translation: 'Tôi đã tự mình sửa chiếc đồng hồ bị hỏng.',
        context: 'Đời sống thường nhật',
        level: 'Sơ cấp N4',
      },
    ],
    synonyms: [
      {
        word: '修理する (しゅうりする)',
        reading: 'しゅうりする',
        meaning: 'Sửa chữa (máy móc, thiết bị kỹ thuật)',
        type: 'synonym',
      },
    ],
  },
  {
    id: 19,
    kanji: '親切',
    hiragana: 'しんせつ',
    romaji: 'shinsetsu',
    hanViet: 'THÂN THIẾT',
    pitchAccent: '[1] 頭高',
    jlptLevel: 'N4',
    partOfSpeech: 'Tính từ đuôi na [形]',
    primaryMeaning: 'thân thiện, tốt bụng, tử tế',
    detailedMeaning: 'Thái độ ân cần, niềm nở và chu đáo khi giúp đỡ hoặc đối xử với người khác.',
    furiganaSegments: [
      { ruby: '親', rt: 'しん' },
      { ruby: '切', rt: 'せつ' },
    ],
    kanjiBreakdown: [
      {
        character: '親',
        strokeCount: 16,
        jlptLevel: 'Cấp độ N4',
        hanViet: 'THÂN (Cha mẹ, thân thiết)',
        onReadings: 'シン',
        kunReadings: 'おや, した-しい',
        meaning: 'Cha mẹ, thân thiết, thân cận.',
      },
      {
        character: '切',
        strokeCount: 4,
        jlptLevel: 'Cấp độ N5',
        hanViet: 'THIẾT (Cắt, cấp thiết)',
        onReadings: 'セツ',
        kunReadings: 'き-る',
        meaning: 'Cắt gọt, tha thiết, cấp thiết.',
      },
    ],
    examples: [
      {
        id: 119,
        japaneseText: '彼は誰にでも親切です。',
        reading: 'かれはだれにでもしんせつです。',
        translation: 'Anh ấy tử tế với tất cả mọi người.',
        context: 'Giao tiếp thường nhật',
        level: 'Sơ cấp N4',
      },
    ],
    synonyms: [
      {
        word: '優しい (やさしい)',
        reading: 'やさしい',
        meaning: 'Hiền từ, dịu dàng, chu đáo',
        type: 'synonym',
      },
    ],
  },
];

// Fallback search algorithm
function localSearch(params = {}) {
  const query = (params.q || '').trim().toLowerCase();
  const jlpt = params.jlpt;
  const type = params.type;

  let results = FALLBACK_WORDS;

  if (query) {
    results = results.filter((w) => {
      const matchKanji = w.kanji.toLowerCase().includes(query);
      const matchHiragana = w.hiragana.toLowerCase().includes(query);
      const matchRomaji = w.romaji.toLowerCase().includes(query);
      const matchMeaning = w.primaryMeaning.toLowerCase().includes(query);
      const matchHanViet = w.hanViet.toLowerCase().includes(query);
      return matchKanji || matchHiragana || matchRomaji || matchMeaning || matchHanViet;
    });
  }

  if (jlpt && jlpt !== 'all') {
    results = results.filter((w) => w.jlptLevel === jlpt);
  }

  if (type && type !== 'all') {
    const typeMap = {
      noun: 'Danh từ',
      verb: 'Động từ',
      adj: 'Tính từ',
      idiom: 'Quán dụng ngữ',
    };
    const targetLabel = typeMap[type];
    if (targetLabel) {
      results = results.filter((w) => w.partOfSpeech.includes(targetLabel));
    }
  }

  return results;
}

function localSuggestions(query = '') {
  const clean = query.trim().toLowerCase();
  if (!clean) return [];

  return FALLBACK_WORDS.filter((w) => {
    return (
      (w.kanji && w.kanji.toLowerCase().includes(clean)) ||
      (w.hiragana && w.hiragana.toLowerCase().includes(clean)) ||
      (w.romaji && w.romaji.toLowerCase().includes(clean)) ||
      (w.primaryMeaning && w.primaryMeaning.toLowerCase().includes(clean))
    );
  }).map((w) => ({
    id: w.id,
    term: w.kanji,
    reading: w.hiragana,
    romaji: w.romaji,
    meaning: w.primaryMeaning,
    jlpt: w.jlptLevel,
  }));
}

export const dictionaryService = {
  /**
   * Search for vocabulary words matching query and filters
   */
  async search(params = {}) {
    try {
      const res = await axiosClient.get('/dictionary/search', { params });
      const data = res?.data !== undefined ? res.data : res;
      if (Array.isArray(data)) {
        return data;
      }
      return [];
    } catch {
      return localSearch(params);
    }
  },

  /**
   * Get suggestions for search autocomplete
   */
  async getSuggestions(query = '') {
    if (!query.trim()) return [];
    try {
      const res = await axiosClient.get('/dictionary/suggest', { params: { q: query } });
      const data = res?.data !== undefined ? res.data : res;
      if (Array.isArray(data)) {
        return data;
      }
      return [];
    } catch {
      return localSuggestions(query);
    }
  },

  /**
   * Get detail of a specific word by ID
   */
  async getWordDetail(id) {
    try {
      const res = await axiosClient.get(`/dictionary/words/${id}`);
      const data = res?.data !== undefined ? res.data : res;
      if (data && data.kanji) {
        return data;
      }
      return FALLBACK_WORDS.find((w) => w.id === Number(id)) || null;
    } catch {
      return FALLBACK_WORDS.find((w) => w.id === Number(id)) || null;
    }
  },

  /**
   * Get kanji breakdown details for a single character
   */
  async getKanji(char) {
    try {
      const res = await axiosClient.get(`/dictionary/kanji/${char}`);
      const data = res?.data !== undefined ? res.data : res;
      if (data && data.character) {
        return data;
      }
      return null;
    } catch {
      return null;
    }
  },

  /**
   * Get trending kanji of the day
   */
  async getTrendingKanji() {
    try {
      const res = await axiosClient.get('/dictionary/trending');
      const data = res?.data !== undefined ? res.data : res;
      if (Array.isArray(data) && data.length > 0) {
        return data;
      }
    } catch {
      // ignore
    }
    return [
      {
        character: '桜',
        strokeCount: 10,
        jlptLevel: 'N3',
        hanViet: 'ANH (Hoa anh đào)',
        kunReadings: 'さくら',
        romaji: 'sakura',
      },
      {
        character: '雨',
        strokeCount: 8,
        jlptLevel: 'N5',
        hanViet: 'VŨ (Cơn mưa)',
        kunReadings: 'あめ',
        romaji: 'ame',
      },
    ];
  },
};

export default dictionaryService;
