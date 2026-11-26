package com.jpdictionary.dictionary.service.impl;

import com.jpdictionary.common.exception.ResourceNotFoundException;
import com.jpdictionary.config.CacheConfig;
import com.jpdictionary.dictionary.dto.ExampleSentenceDto;
import com.jpdictionary.dictionary.dto.FuriganaSegmentDto;
import com.jpdictionary.dictionary.dto.KanjiBreakdownDto;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.dto.SynonymDto;
import com.jpdictionary.dictionary.dto.WordDetailDto;
import com.jpdictionary.dictionary.dto.WordSenseDto;
import com.jpdictionary.dictionary.dto.WordSummaryDto;
import com.jpdictionary.dictionary.entity.DictionaryEntry;
import com.jpdictionary.dictionary.entity.DictionaryForm;
import com.jpdictionary.dictionary.entity.DictionaryReading;
import com.jpdictionary.dictionary.entity.DictionarySense;
import com.jpdictionary.dictionary.entity.DictionarySynonym;
import com.jpdictionary.dictionary.entity.ExampleSentence;
import com.jpdictionary.dictionary.entity.ExampleTranslation;
import com.jpdictionary.dictionary.entity.KanjiEntry;
import com.jpdictionary.dictionary.entity.SenseGloss;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.KanjiEntryRepository;
import com.jpdictionary.dictionary.service.DictionaryService;
import com.jpdictionary.dictionary.service.external.KanjiExternalApiService;
import com.jpdictionary.dictionary.service.search.DictionarySearchBackend;
import com.jpdictionary.dictionary.service.util.PitchAccentFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private static final int DEFAULT_SEARCH_LIMIT = 20;
    private static final int DEFAULT_SUGGESTION_LIMIT = 8;
    private static final Map<String, Byte> JLPT_LEVEL_MAP = Map.of(
            "N5", (byte) 5,
            "N4", (byte) 4,
            "N3", (byte) 3,
            "N2", (byte) 2,
            "N1", (byte) 1
    );

    private final DictionaryEntryRepository entryRepository;
    private final DictionarySearchBackend searchBackend;
    private final KanjiEntryRepository kanjiEntryRepository;
    private final KanjiExternalApiService kanjiExternalApiService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.WORD_SEARCH,
            key = "(#query == null ? '' : #query.trim().toLowerCase()) + '|' "
                    + "+ (#jlpt == null ? 'all' : #jlpt.trim().toUpperCase()) + '|' "
                    + "+ (#type == null ? 'all' : #type.trim().toLowerCase())",
            condition = "(#query != null && !#query.trim().isEmpty()) "
                    + "|| (#jlpt != null && !#jlpt.trim().isEmpty() && !#jlpt.equalsIgnoreCase('all')) "
                    + "|| (#type != null && !#type.trim().isEmpty() && !#type.equalsIgnoreCase('all'))"
    )
    public List<WordSummaryDto> search(String query, String jlpt, String type) {
        String cleanQuery = (query != null) ? query.trim() : "";
        Byte jlptLevel = parseJlptLevel(jlpt);
        PosCategory category = PosCategory.fromCode(type);

        if (cleanQuery.isEmpty() && jlptLevel == null && category == PosCategory.ALL) {
            return Collections.emptyList();
        }

        List<Long> orderedIds = searchBackend.searchEntryIds(cleanQuery, jlptLevel, category, DEFAULT_SEARCH_LIMIT);
        if (orderedIds == null || orderedIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<DictionaryEntry> entries = fetchEntriesInOrder(orderedIds);
        Map<String, KanjiEntry> kanjiMap = loadKanjiMap(entries);
        List<WordSummaryDto> summaries = new ArrayList<>(entries.size());
        for (DictionaryEntry entry : entries) {
            summaries.add(mapToWordSummary(entry, category, kanjiMap));
        }
        return summaries;
    }

    private List<DictionaryEntry> fetchEntriesInOrder(List<Long> orderedIds) {
        if (orderedIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<DictionaryEntry> fetched = entryRepository.findAllByIdIn(orderedIds);
        if (fetched == null || fetched.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, DictionaryEntry> byId = fetched.stream()
                .collect(Collectors.toMap(DictionaryEntry::getId, e -> e, (first, second) -> first));

        List<DictionaryEntry> ordered = new ArrayList<>(orderedIds.size());
        for (Long id : orderedIds) {
            DictionaryEntry entry = byId.get(id);
            if (entry != null) {
                ordered.add(entry);
            }
        }
        return ordered;
    }

    private Map<String, KanjiEntry> loadKanjiMap(Collection<DictionaryEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> allKanjiChars = new LinkedHashSet<>();
        for (DictionaryEntry entry : entries) {
            allKanjiChars.addAll(extractKanjiSet(extractPrimaryKanji(entry)));
        }
        if (allKanjiChars.isEmpty()) {
            return Collections.emptyMap();
        }
        List<KanjiEntry> kanjiEntries = kanjiEntryRepository.findByCharacterTextIn(allKanjiChars);
        if (kanjiEntries == null || kanjiEntries.isEmpty()) {
            return Collections.emptyMap();
        }
        return kanjiEntries.stream()
                .collect(Collectors.toMap(KanjiEntry::getCharacterText, k -> k, (k1, k2) -> k1));
    }

    @Override
    @Transactional
    @Cacheable(
            cacheNames = CacheConfig.WORD_DETAIL,
            key = "#id",
            condition = "#id != null && #id > 0"
    )
    public WordDetailDto getWordDetail(Long id) {
        if (id == null || id <= 0) {
            throw new ResourceNotFoundException("Invalid dictionary entry ID: " + id);
        }
        DictionaryEntry entry = entryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dictionary entry not found: " + id));
        Map<String, KanjiEntry> kanjiMap = loadKanjiMap(List.of(entry));
        return mapToWordDetail(entry, PosCategory.ALL, kanjiMap);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.WORD_SUGGEST,
            key = "#query.trim().toLowerCase()",
            condition = "#query != null && !#query.trim().isEmpty()"
    )
    public List<SuggestionDto> getSuggestions(String query) {
        String cleanQuery = (query != null) ? query.trim() : "";
        if (cleanQuery.isEmpty()) {
            return Collections.emptyList();
        }
        return searchBackend.getSuggestions(cleanQuery, DEFAULT_SUGGESTION_LIMIT);
    }

    @Override
    @Transactional
    @Cacheable(
            cacheNames = CacheConfig.KANJI_DETAIL,
            key = "#character.substring(0, 1)",
            condition = "#character != null && !#character.isEmpty()",
            unless = "#result == null || #result.strokeCount == null"
    )
    public KanjiBreakdownDto getKanjiDetail(String character) {
        return resolveKanjiDetail(character);
    }

    private KanjiBreakdownDto resolveKanjiDetail(String character) {
        if (character == null || character.isEmpty()) {
            return null;
        }
        String ch = character.substring(0, 1);

        Optional<KanjiEntry> opt = kanjiEntryRepository.findByCharacterText(ch);
        if (opt.isPresent()) {
            return mapKanjiToDto(opt.get());
        }

        return fetchExternalOrDefaultKanji(ch);
    }

    private KanjiBreakdownDto fetchExternalOrDefaultKanji(String ch) {
        if (kanjiExternalApiService != null) {
            Optional<KanjiEntry> externalOpt = kanjiExternalApiService.fetchAndCacheKanji(ch);
            if (externalOpt.isPresent()) {
                return mapKanjiToDto(externalOpt.get());
            }
        }

        return KanjiBreakdownDto.builder()
                .character(ch)
                .strokeCount(null)
                .jlptLevel("Hán tự")
                .hanViet("")
                .onReadings("-")
                .kunReadings("-")
                .meaning("Chữ Hán trong hệ thống chữ viết tiếng Nhật.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.TRENDING_KANJI, key = "'default'")
    public List<KanjiBreakdownDto> getTrendingKanji() {
        List<KanjiEntry> trending = kanjiEntryRepository.findTopTrendingKanji(PageRequest.of(0, 4));
        if (trending.isEmpty()) {
            return new ArrayList<>(List.of(
                    resolveKanjiDetail("桜"),
                    resolveKanjiDetail("雨"),
                    resolveKanjiDetail("木"),
                    resolveKanjiDetail("日")));
        }
        return trending.stream().map(this::mapKanjiToDto).collect(Collectors.toCollection(ArrayList::new));
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

    private WordSummaryDto mapToWordSummary(
            DictionaryEntry entry,
            PosCategory targetCategory,
            Map<String, KanjiEntry> kanjiMap
    ) {
        String kanji = extractPrimaryKanji(entry);
        DictionaryReading primaryReading = extractPrimaryReading(entry);
        String hiragana = primaryReading != null ? primaryReading.getReading() : "";
        String romaji = primaryReading != null ? primaryReading.getRomaji() : "";

        List<WordSenseDto> senseDtos = mapSenses(entry.getSenses());
        String primaryMeaning = resolvePrimaryMeaning(senseDtos);
        String pos = resolvePrimaryPos(senseDtos, targetCategory);
        String jlptStr = entry.getJlptLevel() != null ? "N" + entry.getJlptLevel() : "N/A";

        return WordSummaryDto.builder()
                .id(entry.getId())
                .kanji(kanji)
                .hiragana(hiragana)
                .romaji(romaji)
                .hanViet(buildHanVietString(kanji, kanjiMap))
                .pitchAccent(PitchAccentFormatter.format(primaryReading))
                .jlptLevel(jlptStr)
                .partOfSpeech(pos)
                .primaryMeaning(primaryMeaning)
                .senseCount(senseDtos.size())
                .furiganaSegments(buildFuriganaSegments(kanji, hiragana))
                .build();
    }

    private WordDetailDto mapToWordDetail(
            DictionaryEntry entry,
            PosCategory targetCategory,
            Map<String, KanjiEntry> kanjiMap
    ) {
        String kanji = extractPrimaryKanji(entry);
        DictionaryReading primaryReading = extractPrimaryReading(entry);
        String hiragana = primaryReading != null ? primaryReading.getReading() : "";
        String romaji = primaryReading != null ? primaryReading.getRomaji() : "";

        List<WordSenseDto> senseDtos = mapSenses(entry.getSenses());
        String primaryMeaning = resolvePrimaryMeaning(senseDtos);
        String detailedMeaning = resolveDetailedMeaning(senseDtos, primaryMeaning);
        String pos = resolvePrimaryPos(senseDtos, targetCategory);

        String jlptStr = entry.getJlptLevel() != null ? "N" + entry.getJlptLevel() : "N/A";
        String hanViet = buildHanVietString(kanji, kanjiMap);

        List<KanjiBreakdownDto> kanjiList = extractKanjiBreakdown(kanji, kanjiMap);
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
        List<String> posCategories = sense.getPosCategories() != null
                ? sense.getPosCategories().stream()
                        .filter(PosCategory::isStorable)
                        .map(Enum::name)
                        .collect(Collectors.toCollection(ArrayList::new))
                : Collections.emptyList();
        List<String> viGlosses = extractGlossesByLanguage(sense, "vi");
        List<String> enGlosses = extractGlossesByLanguage(sense, "en");

        String meaning = resolveSenseMeaning(viGlosses, enGlosses, sense);
        String notes = sense.getNotes() != null ? sense.getNotes() : "";
        int order = sense.getSenseOrder() != null ? sense.getSenseOrder() : defaultOrder;

        return WordSenseDto.builder()
                .senseOrder(order)
                .partOfSpeech(pos)
                .posCategories(posCategories)
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
                .collect(Collectors.toCollection(ArrayList::new));
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
                if (matchesSenseCategory(s, targetCategory)) {
                    return s.getPartOfSpeech();
                }
            }
        }
        return senses.get(0).getPartOfSpeech();
    }

    private boolean matchesSenseCategory(WordSenseDto sense, PosCategory targetCategory) {
        List<String> categories = sense.getPosCategories();
        if (categories != null && !categories.isEmpty()) {
            return categories.contains(targetCategory.name());
        }
        return targetCategory.matches(sense.getPartOfSpeech());
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

    private List<KanjiBreakdownDto> extractKanjiBreakdown(String text, Map<String, KanjiEntry> kanjiMap) {
        Set<String> kanjiChars = extractKanjiSet(text);
        if (kanjiChars.isEmpty()) {
            return Collections.emptyList();
        }

        List<KanjiBreakdownDto> list = new ArrayList<>(kanjiChars.size());
        for (String ch : kanjiChars) {
            KanjiEntry entry = kanjiMap.get(ch);
            if (entry != null) {
                list.add(mapKanjiToDto(entry));
            } else {
                list.add(fetchExternalOrDefaultKanji(ch));
            }
        }
        return list;
    }

    private boolean isKanji(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A;
    }

    private Set<String> extractKanjiSet(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptySet();
        }
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
        if (text == null || text.isEmpty() || map.isEmpty()) {
            return "";
        }
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
        return JLPT_LEVEL_MAP.get(jlpt.toUpperCase(Locale.ROOT));
    }
}
