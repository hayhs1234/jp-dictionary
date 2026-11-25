package com.jpdictionary.dictionary.service.impl;

import com.jpdictionary.common.exception.ResourceNotFoundException;
import com.jpdictionary.dictionary.dto.ExampleSentenceDto;
import com.jpdictionary.dictionary.dto.FuriganaSegmentDto;
import com.jpdictionary.dictionary.dto.KanjiBreakdownDto;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.dto.SynonymDto;
import com.jpdictionary.dictionary.dto.WordDetailDto;
import com.jpdictionary.dictionary.dto.WordSenseDto;
import com.jpdictionary.dictionary.entity.DictionaryEntry;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.entity.DictionaryForm;
import com.jpdictionary.dictionary.entity.DictionaryReading;
import com.jpdictionary.dictionary.entity.DictionarySense;
import com.jpdictionary.dictionary.entity.DictionarySynonym;
import com.jpdictionary.dictionary.entity.ExampleSentence;
import com.jpdictionary.dictionary.entity.ExampleTranslation;
import com.jpdictionary.dictionary.entity.KanjiEntry;
import com.jpdictionary.dictionary.entity.SenseGloss;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.KanjiEntryRepository;
import com.jpdictionary.dictionary.service.DictionaryService;
import com.jpdictionary.dictionary.service.deinflect.JapaneseDeinflector;
import com.jpdictionary.dictionary.service.external.KanjiExternalApiService;
import com.jpdictionary.dictionary.service.util.PitchAccentFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private final DictionaryEntryRepository entryRepository;
    private final KanjiEntryRepository kanjiEntryRepository;
    private final JapaneseDeinflector deinflector;
    private final KanjiExternalApiService kanjiExternalApiService;

    @Override
    @Transactional(readOnly = true)
    public List<WordDetailDto> search(String query, String jlpt, String type) {
        String cleanQuery = (query != null) ? query.trim() : "";
        Byte jlptLevel = parseJlptLevel(jlpt);
        PosCategory category = PosCategory.fromCode(type);
        String posCategoryParam = (category != PosCategory.ALL) ? category.name() : null;

        if (cleanQuery.isEmpty() && jlptLevel == null && posCategoryParam == null) {
            return Collections.emptyList();
        }

        List<DictionaryEntry> results = collectEntries(cleanQuery, jlptLevel, posCategoryParam);
        return mapEntriesToDtos(results, category);
    }

    private List<DictionaryEntry> collectEntries(String query, Byte jlpt, String pos) {
        List<DictionaryEntry> results = new ArrayList<>();
        if (!query.isEmpty()) {
            results.addAll(entryRepository.findExactMatches(query, jlpt, pos));
        }

        // De-inflection: detect conjugated forms (e.g., 食べた -> 食べる, いかない -> いく)
        if (!query.isEmpty() && deinflector != null) {
            List<String> deinflectedRoots = deinflector.deinflect(query);
            for (String root : deinflectedRoots) {
                List<DictionaryEntry> rootMatches = entryRepository.findExactMatches(root, jlpt, pos);
                for (DictionaryEntry entry : rootMatches) {
                    if (results.stream().noneMatch(e -> e.getId().equals(entry.getId()))) {
                        results.add(entry);
                    }
                }
            }
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

    private List<WordDetailDto> mapEntriesToDtos(List<DictionaryEntry> entries, PosCategory targetCategory) {
        List<WordDetailDto> dtos = new ArrayList<>();
        for (DictionaryEntry entry : entries) {
            dtos.add(mapToWordDetail(entry, targetCategory));
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
    @Transactional
    public KanjiBreakdownDto getKanjiDetail(String character) {
        if (character == null || character.isEmpty()) {
            return null;
        }
        String ch = character.substring(0, 1);

        // 1. Check local database
        Optional<KanjiEntry> opt = kanjiEntryRepository.findByCharacterText(ch);
        if (opt.isPresent()) {
            return mapKanjiToDto(opt.get());
        }

        // 2. Hybrid fallback: Fetch from external API and auto-cache to local DB
        if (kanjiExternalApiService != null) {
            Optional<KanjiEntry> externalOpt = kanjiExternalApiService.fetchAndCacheKanji(ch);
            if (externalOpt.isPresent()) {
                return mapKanjiToDto(externalOpt.get());
            }
        }

        // 3. Fallback for completely unknown symbols or offline
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
    @Transactional(readOnly = true)
    public List<KanjiBreakdownDto> getTrendingKanji() {
        List<KanjiEntry> trending = kanjiEntryRepository.findTopTrendingKanji(PageRequest.of(0, 4));
        if (trending.isEmpty()) {
            return List.of(
                    getKanjiDetail("桜"),
                    getKanjiDetail("雨"),
                    getKanjiDetail("木"),
                    getKanjiDetail("日"));
        }
        return trending.stream().map(this::mapKanjiToDto).collect(Collectors.toList());
    }

    private KanjiBreakdownDto mapKanjiToDto(KanjiEntry k) {
        return KanjiBreakdownDto.builder()
                .character(k.getCharacterText())
                .strokeCount(k.getStrokeCount())
                .jlptLevel(k.getJlptLevel() != null ? "Cấp độ " + k.getJlptLevel() : "Thường dùng")
                .hanViet(k.getHanViet())
                .onReadings(k.getOnReadings() != null ? k.getOnReadings() : "-")
                .kunReadings(k.getKunReadings() != null ? k.getKunReadings() : "-")
                .meaning(k.getMeaning())
                .build();
    }

    private WordDetailDto mapToWordDetail(DictionaryEntry entry) {
        return mapToWordDetail(entry, PosCategory.ALL);
    }

    private WordDetailDto mapToWordDetail(DictionaryEntry entry, PosCategory targetCategory) {
        String kanji = extractPrimaryKanji(entry);
        DictionaryReading primaryReading = extractPrimaryReading(entry);
        String hiragana = primaryReading != null ? primaryReading.getReading() : "";
        String romaji = primaryReading != null ? primaryReading.getRomaji() : "";

        List<WordSenseDto> senseDtos = mapSenses(entry.getSenses());
        String primaryMeaning = resolvePrimaryMeaning(senseDtos);
        String detailedMeaning = resolveDetailedMeaning(senseDtos, primaryMeaning);
        String pos = resolvePrimaryPos(senseDtos, targetCategory);

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
                .pitchAccent(PitchAccentFormatter.format(primaryReading))
                .jlptLevel(jlptStr)
                .partOfSpeech(pos)
                .primaryMeaning(primaryMeaning)
                .detailedMeaning(detailedMeaning)
                .senses(senseDtos)
                .furiganaSegments(segments)
                .kanjiBreakdown(kanjiList)
                .examples(exampleDtos)
                .synonyms(mapSynonyms(entry.getSynonyms()))
                .build();
    }

    private String extractPrimaryKanji(DictionaryEntry entry) {
        return entry.getForms().stream()
                .filter(DictionaryForm::getIsPrimary)
                .map(DictionaryForm::getWrittenForm)
                .findFirst()
                .orElse(entry.getForms().isEmpty() ? "" : entry.getForms().get(0).getWrittenForm());
    }

    private DictionaryReading extractPrimaryReading(DictionaryEntry entry) {
        return entry.getReadings().stream()
                .filter(DictionaryReading::getIsPrimary)
                .findFirst()
                .orElse(entry.getReadings().isEmpty() ? null : entry.getReadings().get(0));
    }

    private List<WordSenseDto> mapSenses(List<DictionarySense> senses) {
        if (senses == null) {
            return Collections.emptyList();
        }
        List<WordSenseDto> dtos = new ArrayList<>();
        for (int i = 0; i < senses.size(); i++) {
            dtos.add(mapSingleSense(senses.get(i), i + 1));
        }
        return dtos;
    }

    private WordSenseDto mapSingleSense(DictionarySense sense, int defaultOrder) {
        String pos = sense.getPartOfSpeech() != null ? sense.getPartOfSpeech() : "";
        List<String> viGlosses = extractGlossesByLanguage(sense, "vi");
        List<String> enGlosses = extractGlossesByLanguage(sense, "en");

        String meaning = resolveSenseMeaning(viGlosses, enGlosses, sense);
        String notes = sense.getNotes() != null ? sense.getNotes() : "";
        int order = sense.getSenseOrder() != null ? sense.getSenseOrder() : defaultOrder;

        return WordSenseDto.builder()
                .senseOrder(order)
                .partOfSpeech(pos)
                .meaning(meaning)
                .detailedMeaning(notes)
                .glosses(viGlosses)
                .englishGlosses(enGlosses)
                .build();
    }

    private List<String> extractGlossesByLanguage(DictionarySense sense, String langCode) {
        return sense.getGlosses().stream()
                .filter(g -> langCode.equalsIgnoreCase(g.getLanguageCode()))
                .map(SenseGloss::getMeaning)
                .toList();
    }

    private String resolveSenseMeaning(List<String> viGlosses,
                                       List<String> enGlosses,
                                       DictionarySense sense) {
        if (!viGlosses.isEmpty()) {
            return String.join("; ", viGlosses);
        }
        if (!enGlosses.isEmpty()) {
            return String.join("; ", enGlosses);
        }
        if (!sense.getGlosses().isEmpty()) {
            return sense.getGlosses().get(0).getMeaning();
        }
        return "";
    }

    private String resolvePrimaryMeaning(List<WordSenseDto> senses) {
        return senses.isEmpty() ? "" : senses.get(0).getMeaning();
    }

    private String resolveDetailedMeaning(List<WordSenseDto> senses, String defaultMeaning) {
        if (senses.isEmpty()) {
            return defaultMeaning;
        }
        String detailed = senses.get(0).getDetailedMeaning();
        return (detailed != null && !detailed.isEmpty()) ? detailed : defaultMeaning;
    }

    private String resolvePrimaryPos(List<WordSenseDto> senses, PosCategory targetCategory) {
        if (senses.isEmpty()) {
            return "";
        }
        if (targetCategory != null && targetCategory != PosCategory.ALL) {
            for (WordSenseDto s : senses) {
                if (targetCategory.matches(s.getPartOfSpeech())) {
                    return s.getPartOfSpeech();
                }
            }
        }
        return senses.get(0).getPartOfSpeech();
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
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> kanjiChars = new LinkedHashSet<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isKanji(c)) {
                kanjiChars.add(String.valueOf(c));
            }
        }
        if (kanjiChars.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, KanjiEntry> map = kanjiEntryRepository.findByCharacterTextIn(kanjiChars).stream()
                .collect(Collectors.toMap(KanjiEntry::getCharacterText, k -> k, (k1, k2) -> k1));

        List<KanjiBreakdownDto> list = new ArrayList<>();
        for (String ch : kanjiChars) {
            KanjiEntry entry = map.get(ch);
            if (entry != null) {
                list.add(mapKanjiToDto(entry));
            } else {
                list.add(getKanjiDetail(ch));
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
        Optional<KanjiEntry> opt = kanjiEntryRepository.findByCharacterText(ch);
        if (opt.isPresent()) {
            return opt.get().getHanViet().split(" ")[0];
        }
        return "";
    }

    private String deriveHanViet(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        Set<String> kanjiChars = extractKanjiSet(text);
        if (kanjiChars.isEmpty()) {
            return "";
        }

        Map<String, KanjiEntry> map = kanjiEntryRepository.findByCharacterTextIn(kanjiChars).stream()
                .collect(Collectors.toMap(KanjiEntry::getCharacterText, k -> k, (k1, k2) -> k1));

        return buildHanVietString(text, map);
    }

    private Set<String> extractKanjiSet(String text) {
        Set<String> kanjiChars = new LinkedHashSet<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isKanji(c)) {
                kanjiChars.add(String.valueOf(c));
            }
        }
        return kanjiChars;
    }

    private String buildHanVietString(String text, Map<String, KanjiEntry> map) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isKanji(c)) {
                KanjiEntry entry = map.get(String.valueOf(c));
                if (entry != null && entry.getHanViet() != null) {
                    String hv = entry.getHanViet().split(" ")[0];
                    if (!sb.isEmpty()) {
                        sb.append(" ");
                    }
                    sb.append(hv);
                }
            }
        }
        return sb.toString();
    }

    private List<FuriganaSegmentDto> buildFuriganaSegments(String kanji, String hiragana) {
        List<FuriganaSegmentDto> list = new ArrayList<>();
        if (kanji == null || kanji.isEmpty()) {
            return list;
        }
        if (hiragana == null || hiragana.isEmpty() || kanji.equals(hiragana)) {
            list.add(new FuriganaSegmentDto(kanji, ""));
            return list;
        }

        // Dynamic okurigana matching: find common trailing kana
        int kLen = kanji.length();
        int hLen = hiragana.length();
        int suffixLen = calculateCommonSuffixLength(kanji, hiragana, kLen, hLen);

        if (suffixLen > 0 && suffixLen < kLen && suffixLen < hLen) {
            String kanjiStem = kanji.substring(0, kLen - suffixLen);
            String hiraganaStem = hiragana.substring(0, hLen - suffixLen);
            String okurigana = kanji.substring(kLen - suffixLen);
            list.add(new FuriganaSegmentDto(kanjiStem, hiraganaStem));
            list.add(new FuriganaSegmentDto(okurigana, ""));
            return list;
        }

        list.add(new FuriganaSegmentDto(kanji, hiragana));
        return list;
    }

    private int calculateCommonSuffixLength(String kanji, String hiragana, int kLen, int hLen) {
        int suffixLen = 0;
        while (suffixLen < kLen && suffixLen < hLen
                && kanji.charAt(kLen - 1 - suffixLen) == hiragana.charAt(hLen - 1 - suffixLen)) {
            suffixLen++;
        }
        return suffixLen;
    }

    private List<SynonymDto> mapSynonyms(List<DictionarySynonym> synonyms) {
        if (synonyms == null || synonyms.isEmpty()) {
            return Collections.emptyList();
        }
        List<SynonymDto> dtos = new ArrayList<>();
        for (DictionarySynonym syn : synonyms) {
            dtos.add(SynonymDto.builder()
                    .word(syn.getWord())
                    .reading(syn.getReading() != null ? syn.getReading() : "")
                    .meaning(syn.getMeaning())
                    .type(syn.getRelationType() != null ? syn.getRelationType() : "synonym")
                    .build());
        }
        return dtos;
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

    private String parsePosCategory(String type) {
        PosCategory category = PosCategory.fromCode(type);
        return (category != PosCategory.ALL) ? category.name() : null;
    }
}
