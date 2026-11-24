package com.jpdictionary.dictionary.service.impl;

import com.jpdictionary.common.exception.ResourceNotFoundException;
import com.jpdictionary.dictionary.dto.ExampleSentenceDto;
import com.jpdictionary.dictionary.dto.FuriganaSegmentDto;
import com.jpdictionary.dictionary.dto.KanjiBreakdownDto;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.dto.SynonymDto;
import com.jpdictionary.dictionary.dto.WordDetailDto;
import com.jpdictionary.dictionary.entity.DictionaryEntry;
import com.jpdictionary.dictionary.entity.DictionaryForm;
import com.jpdictionary.dictionary.entity.DictionaryReading;
import com.jpdictionary.dictionary.entity.DictionarySense;
import com.jpdictionary.dictionary.entity.ExampleSentence;
import com.jpdictionary.dictionary.entity.ExampleTranslation;
import com.jpdictionary.dictionary.entity.SenseGloss;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryEntryRepository entryRepository;

    private static final Map<String, KanjiBreakdownDto> KANJI_DATABASE = new HashMap<>();

    static {
        registerKanji("木", 4, "Cấp độ N5", "MỘC (Cây)", "モク, ボク", "き, こ",
                "Cây cối, gỗ, chất liệu thực vật.");
        registerKanji("漏", 14, "Cấp độ N1", "LẬU (Rò rỉ, lọt)", "ロウ", "も-る, も-れる",
                "Rò rỉ nước, lọt ánh sáng, sơ hở.");
        registerKanji("日", 4, "Cấp độ N5", "NHẬT (Mặt trời)", "ニチ, ジツ", "ひ, -び",
                "Thái dương, ban ngày, nhật quang.");
        registerKanji("食", 9, "Cấp độ N5", "THỰC (Ăn)", "ショク", "た-べる, く-う",
                "Ăn uống, thức ăn, thực phẩm.");
        registerKanji("飲", 12, "Cấp độ N5", "ẨM (Uống)", "イン", "の-む",
                "Uống chất lỏng, ẩm thực.");
        registerKanji("行", 6, "Cấp độ N5", "HÀNH (Đi)", "コウ, ギョウ", "い-く, ゆ-く",
                "Đi lại, tiến hành, thực hành.");
        registerKanji("来", 7, "Cấp độ N5", "LAI (Đến)", "ライ", "く-る, きた-る",
                "Đến, tới, tương lai.");
        registerKanji("本", 5, "Cấp độ N5", "BỔN (Gốc, sách)", "ホン", "もと",
                "Sách vở, gốc rễ, nền tảng.");
        registerKanji("語", 14, "Cấp độ N5", "NGỮ (Ngôn ngữ)", "ゴ", "かた-る",
                "Lời nói, tiếng nói, ngôn ngữ.");
        registerKanji("新", 13, "Cấp độ N5", "TÂN (Mới)", "シン", "あたら-しい",
                "Mới mẻ, tân tiến, tươi mới.");
        registerKanji("古", 5, "Cấp độ N5", "CỔ (Cũ)", "コ", "ふる-i",
                "Cổ xưa, cũ kỹ, lâu đời.");
        registerKanji("話", 13, "Cấp độ N5", "THOẠI (Nói)", "ワ", "はな-す, はなし",
                "Trò chuyện, câu chuyện, đàm thoại.");
        registerKanji("書", 10, "Cấp độ N5", "THƯ (Viết, sách)", "ショ", "か-く",
                "Viết lách, sách vở, tài liệu.");
        registerKanji("聞", 14, "Cấp độ N5", "VĂN (Nghe)", "ブン, モン", "き-く",
                "Nghe thấy, thính giác, hỏi han.");
        registerKanji("見", 7, "Cấp độ N5", "KIẾN (Nhìn)", "ケン", "み-る",
                "Nhìn thấy, quan sát, kiến thức.");
        registerKanji("先", 6, "Cấp độ N5", "TIÊN (Trước)", "セン", "さき",
                "Đi trước, thời trước, tiên sinh.");
        registerKanji("生", 5, "Cấp độ N5", "SINH (Sống, sinh)", "セイ, ショウ", "い-きる, う-まれる",
                "Sinh sống, sinh ra, học sinh.");
        registerKanji("学", 8, "Cấp độ N5", "HỌC (Học tập)", "ガク", "まな-ぶ",
                "Học hành, trường học, học giả.");
        registerKanji("友", 4, "Cấp độ N5", "HỮU (Bạn bè)", "ユウ", "とも",
                "Bạn hữu, bè bạn, thân thiện.");
        registerKanji("達", 12, "Cấp độ N5", "ĐẠT (Đạt đến)", "タツ", "たち",
                "Đạt được, thông suốt, số nhiều.");
        registerKanji("桜", 10, "Cấp độ N3", "ANH (Hoa anh đào)", "オウ", "さくら",
                "Cây hoa anh đào, biểu tượng nước Nhật.");
        registerKanji("雨", 8, "Cấp độ N5", "VŨ (Cơn mưa)", "ウ", "あめ",
                "Nước mưa, thời tiết mưa.");
    }

    private static void registerKanji(String ch, int strokes, String jlpt, String hv,
            String on, String kun, String meaning) {
        KANJI_DATABASE.put(ch, KanjiBreakdownDto.builder()
                .character(ch)
                .strokeCount(strokes)
                .jlptLevel(jlpt)
                .hanViet(hv)
                .onReadings(on)
                .kunReadings(kun)
                .meaning(meaning)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WordDetailDto> search(String query, String jlpt, String type) {
        String cleanQuery = (query != null) ? query.trim() : "";
        Byte jlptLevel = parseJlptLevel(jlpt);
        String posPattern = parsePosPattern(type);

        if (cleanQuery.isEmpty() && jlptLevel == null && posPattern == null) {
            return Collections.emptyList();
        }

        List<DictionaryEntry> results = collectEntries(cleanQuery, jlptLevel, posPattern);
        return mapEntriesToDtos(results);
    }

    private List<DictionaryEntry> collectEntries(String query, Byte jlpt, String pos) {
        List<DictionaryEntry> results = new ArrayList<>();
        if (!query.isEmpty()) {
            results.addAll(entryRepository.findExactMatches(query, jlpt, pos));
        }
        if (results.size() < 20) {
            List<DictionaryEntry> fuzzy = entryRepository.searchEntries(
                    query, jlpt, pos, PageRequest.of(0, 20 - results.size()));
            for (DictionaryEntry entry : fuzzy) {
                if (results.stream().noneMatch(e -> e.getId().equals(entry.getId()))) {
                    results.add(entry);
                }
            }
        }
        return results;
    }

    private List<WordDetailDto> mapEntriesToDtos(List<DictionaryEntry> entries) {
        List<WordDetailDto> dtos = new ArrayList<>();
        for (DictionaryEntry entry : entries) {
            dtos.add(mapToWordDetail(entry));
        }
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public WordDetailDto getWordDetail(Long id) {
        if (id == null || id <= 0) {
            throw new ResourceNotFoundException("Invalid dictionary entry ID: " + id);
        }
        DictionaryEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dictionary entry not found: " + id));
        return mapToWordDetail(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuggestionDto> getSuggestions(String query) {
        String cleanQuery = (query != null) ? query.trim() : "";
        if (cleanQuery.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object[]> rawList = entryRepository.findSuggestionsRaw(cleanQuery, PageRequest.of(0, 8));
        Map<String, SuggestionDto> map = new LinkedHashMap<>();

        for (Object[] row : rawList) {
            String form = (String) row[0];
            String reading = (String) row[1];
            String romaji = (String) row[2];
            String meaning = (String) row[3];
            Long id = (Long) row[4];

            if (!map.containsKey(form)) {
                map.put(form, SuggestionDto.builder()
                        .id(id)
                        .term(form)
                        .reading(reading)
                        .romaji(romaji)
                        .meaning(meaning)
                        .build());
            }
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public KanjiBreakdownDto getKanjiDetail(String character) {
        if (character == null || character.isEmpty()) {
            return null;
        }
        String ch = character.substring(0, 1);
        KanjiBreakdownDto dto = KANJI_DATABASE.get(ch);
        if (dto != null) {
            return dto;
        }
        return KanjiBreakdownDto.builder()
                .character(ch)
                .strokeCount(null)
                .jlptLevel("Hán tự")
                .hanViet(deriveSingleHanViet(ch))
                .onReadings("-")
                .kunReadings("-")
                .meaning("Chữ Hán trong hệ thống chữ viết tiếng Nhật.")
                .build();
    }

    @Override
    public List<KanjiBreakdownDto> getTrendingKanji() {
        return List.of(
                getKanjiDetail("桜"),
                getKanjiDetail("雨"),
                getKanjiDetail("木"),
                getKanjiDetail("日"));
    }

    private WordDetailDto mapToWordDetail(DictionaryEntry entry) {
        String kanji = entry.getForms().stream()
                .filter(DictionaryForm::getIsPrimary)
                .map(DictionaryForm::getWrittenForm)
                .findFirst()
                .orElse(entry.getForms().isEmpty() ? "" : entry.getForms().get(0).getWrittenForm());

        DictionaryReading primaryReading = entry.getReadings().stream()
                .filter(DictionaryReading::getIsPrimary)
                .findFirst()
                .orElse(entry.getReadings().isEmpty() ? null : entry.getReadings().get(0));

        String hiragana = primaryReading != null ? primaryReading.getReading() : "";
        String romaji = primaryReading != null ? primaryReading.getRomaji() : "";

        DictionarySense primarySense = entry.getSenses().stream().findFirst().orElse(null);
        String pos = primarySense != null ? primarySense.getPartOfSpeech() : "";

        String primaryMeaning = "";
        String detailedMeaning = "";
        if (primarySense != null && !primarySense.getGlosses().isEmpty()) {
            Optional<SenseGloss> viGloss = primarySense.getGlosses().stream()
                    .filter(g -> "vi".equalsIgnoreCase(g.getLanguageCode()))
                    .findFirst();
            primaryMeaning = viGloss.map(SenseGloss::getMeaning)
                    .orElse(primarySense.getGlosses().get(0).getMeaning());
            detailedMeaning = primarySense.getNotes() != null
                    ? primarySense.getNotes()
                    : primaryMeaning;
        }

        String jlptStr = entry.getJlptLevel() != null ? "N" + entry.getJlptLevel() : "N/A";
        String hanViet = deriveHanViet(kanji);

        List<KanjiBreakdownDto> kanjiList = extractKanjiBreakdown(kanji);
        List<ExampleSentenceDto> exampleDtos = mapExamples(entry.getExamples());
        List<FuriganaSegmentDto> segments = buildFuriganaSegments(kanji, hiragana);

        return WordDetailDto.builder()
                .id(entry.getId())
                .kanji(kanji)
                .hiragana(hiragana)
                .romaji(romaji)
                .hanViet(hanViet)
                .pitchAccent(derivePitchAccent(pos))
                .jlptLevel(jlptStr)
                .partOfSpeech(pos)
                .primaryMeaning(primaryMeaning)
                .detailedMeaning(detailedMeaning)
                .furiganaSegments(segments)
                .kanjiBreakdown(kanjiList)
                .examples(exampleDtos)
                .synonyms(buildDefaultSynonyms(kanji))
                .build();
    }

    private String derivePitchAccent(String pos) {
        return "[0] 平板 (Heiban)";
    }

    private List<ExampleSentenceDto> mapExamples(List<ExampleSentence> examples) {
        List<ExampleSentenceDto> dtos = new ArrayList<>();
        if (examples == null) {
            return dtos;
        }
        for (ExampleSentence ex : examples) {
            String trans = ex.getTranslations().stream()
                    .filter(t -> "vi".equalsIgnoreCase(t.getLanguageCode()))
                    .map(ExampleTranslation::getTranslatedText)
                    .findFirst()
                    .orElse(ex.getTranslations().isEmpty() ? "" : ex.getTranslations().get(0).getTranslatedText());

            dtos.add(ExampleSentenceDto.builder()
                    .id(ex.getId())
                    .japaneseText(ex.getJapaneseText())
                    .reading(ex.getReading())
                    .translation(trans)
                    .context("Đời sống thường nhật")
                    .level("Chuẩn JLPT")
                    .build());
        }
        return dtos;
    }

    private List<KanjiBreakdownDto> extractKanjiBreakdown(String text) {
        List<KanjiBreakdownDto> list = new ArrayList<>();
        if (text == null) {
            return list;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isKanji(c)) {
                String ch = String.valueOf(c);
                if (list.stream().noneMatch(k -> k.getCharacter().equals(ch))) {
                    list.add(getKanjiDetail(ch));
                }
            }
        }
        return list;
    }

    private boolean isKanji(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A;
    }

    private String deriveSingleHanViet(String ch) {
        KanjiBreakdownDto dto = KANJI_DATABASE.get(ch);
        if (dto != null && dto.getHanViet() != null) {
            return dto.getHanViet().split(" ")[0];
        }
        return "";
    }

    private String deriveHanViet(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isKanji(c)) {
                String hv = deriveSingleHanViet(String.valueOf(c));
                if (!hv.isEmpty()) {
                    if (!sb.isEmpty()) {
                        sb.append(" ");
                    }
                    sb.append(hv);
                }
            }
        }
        return sb.toString();
    }

    private boolean requiresFurigana(String kanji, String hiragana) {
        return hiragana != null && !hiragana.isEmpty() && !kanji.equals(hiragana);
    }

    private int calculateCommonSuffixLength(String kanji, String hiragana) {
        int kLen = kanji.length();
        int hLen = hiragana.length();
        int suffixLen = 0;
        while (suffixLen < kLen && suffixLen < hLen
                && kanji.charAt(kLen - 1 - suffixLen) == hiragana.charAt(hLen - 1 - suffixLen)) {
            suffixLen++;
        }
        return suffixLen;
    }

    private List<FuriganaSegmentDto> buildFuriganaSegments(String kanji, String hiragana) {
        if (kanji == null || kanji.isEmpty()) {
            return Collections.emptyList();
        }
        if (!requiresFurigana(kanji, hiragana)) {
            return List.of(new FuriganaSegmentDto(kanji, ""));
        }

        int suffixLen = calculateCommonSuffixLength(kanji, hiragana);
        int kLen = kanji.length();
        int hLen = hiragana.length();
        if (suffixLen > 0 && suffixLen < kLen && suffixLen < hLen) {
            String kanjiStem = kanji.substring(0, kLen - suffixLen);
            String hiraganaStem = hiragana.substring(0, hLen - suffixLen);
            String okurigana = kanji.substring(kLen - suffixLen);
            return List.of(
                    new FuriganaSegmentDto(kanjiStem, hiraganaStem),
                    new FuriganaSegmentDto(okurigana, ""));
        }

        return List.of(new FuriganaSegmentDto(kanji, hiragana));
    }

    private List<SynonymDto> buildDefaultSynonyms(String kanji) {
        if ("木漏れ日".equals(kanji)) {
            return List.of(
                    new SynonymDto("日光 (にっこう)", "にっこう", "Ánh sáng mặt trời (Đồng nghĩa rộng)", "synonym"),
                    new SynonymDto("陽光 (ようこう)", "ようこう", "Dương quang, nắng ấm", "synonym"),
                    new SynonymDto("〜ながら", "", "Vừa... vừa - Ngữ pháp N4 thường dùng kèm", "grammar"));
        }
        return Collections.emptyList();
    }

    private Byte parseJlptLevel(String jlpt) {
        if (jlpt == null || jlpt.equalsIgnoreCase("all")) {
            return null;
        }
        if (jlpt.equalsIgnoreCase("N5")) {
            return (byte) 5;
        }
        if (jlpt.equalsIgnoreCase("N4")) {
            return (byte) 4;
        }
        if (jlpt.equalsIgnoreCase("N3")) {
            return (byte) 3;
        }
        if (jlpt.equalsIgnoreCase("N2")) {
            return (byte) 2;
        }
        if (jlpt.equalsIgnoreCase("N1")) {
            return (byte) 1;
        }
        return null;
    }

    private String parsePosPattern(String type) {
        if (type == null || type.equalsIgnoreCase("all")) {
            return null;
        }
        if (type.equalsIgnoreCase("noun")) {
            return "n";
        }
        if (type.equalsIgnoreCase("verb")) {
            return "v";
        }
        if (type.equalsIgnoreCase("adj")) {
            return "adj";
        }
        return null;
    }
}
