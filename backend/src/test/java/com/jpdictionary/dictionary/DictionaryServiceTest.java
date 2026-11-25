package com.jpdictionary.dictionary;

import com.jpdictionary.dictionary.dto.KanjiBreakdownDto;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.dto.SynonymDto;
import com.jpdictionary.dictionary.dto.WordDetailDto;
import com.jpdictionary.dictionary.entity.DictionaryEntry;
import com.jpdictionary.dictionary.entity.DictionaryForm;
import com.jpdictionary.dictionary.entity.DictionaryReading;
import com.jpdictionary.dictionary.entity.DictionarySense;
import com.jpdictionary.dictionary.entity.DictionarySynonym;
import com.jpdictionary.dictionary.entity.KanjiEntry;
import com.jpdictionary.dictionary.entity.SenseGloss;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.KanjiEntryRepository;
import com.jpdictionary.dictionary.service.DictionaryService;
import com.jpdictionary.dictionary.service.deinflect.JapaneseDeinflector;
import com.jpdictionary.dictionary.service.impl.DictionaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private DictionaryEntryRepository entryRepository;

    @Mock
    private KanjiEntryRepository kanjiEntryRepository;

    @Mock
    private com.jpdictionary.dictionary.service.external.KanjiExternalApiService kanjiExternalApiService;

    private DictionaryService dictionaryService;

    @BeforeEach
    void setUp() {
        dictionaryService = new DictionaryServiceImpl(
                entryRepository,
                kanjiEntryRepository,
                new JapaneseDeinflector(),
                kanjiExternalApiService
        );
    }

    @Test
    @DisplayName("Should return empty list when search term and filters are empty")
    void testSearchEmptyReturnsEmptyList() {
        List<WordDetailDto> results = dictionaryService.search("", null, null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return mapped WordDetailDto on exact match")
    void testSearchExactMatch() {
        DictionaryEntry entry = createMockWord(1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn, dùng bữa", (byte) 5);

        when(entryRepository.findExactMatches("食べる", null, null)).thenReturn(List.of(entry));

        List<WordDetailDto> results = dictionaryService.search("食べる", null, null);
        assertNotNull(results);
        assertEquals(1, results.size());
        WordDetailDto word = results.get(0);
        assertEquals("食べる", word.getKanji());
        assertEquals("たべる", word.getHiragana());
        assertEquals("taberu", word.getRomaji());
        assertEquals("ăn, dùng bữa", word.getPrimaryMeaning());
        assertEquals("N5", word.getJlptLevel());
        assertNotNull(word.getSenses());
        assertEquals(1, word.getSenses().size());
        assertEquals("ăn, dùng bữa", word.getSenses().get(0).getMeaning());
    }

    @Test
    @DisplayName("Should return all senses when word has multiple senses (Polysemy)")
    void testSearchMultipleSensesReturnsAllSenses() {
        DictionaryEntry entry = createMockWord(18L, "直す", "なおす", "naosu", "v5s (Động từ nhóm 1)", "sửa chữa", (byte) 4);

        // Add second sense: Đính chính, sửa lỗi
        DictionarySense sense2 = DictionarySense.builder()
                .id(100L)
                .entry(entry)
                .senseOrder(2)
                .partOfSpeech("v5s (Động từ nhóm 1)")
                .notes("Đính chính, sửa lỗi sai")
                .build();
        sense2.getGlosses().add(SenseGloss.builder().id(100L).sense(sense2).languageCode("vi").meaning("đính chính, sửa sai").build());
        sense2.getGlosses().add(SenseGloss.builder().id(101L).sense(sense2).languageCode("en").meaning("to correct").build());
        entry.getSenses().add(sense2);

        // Add third sense: Dịch thuật, quy đổi
        DictionarySense sense3 = DictionarySense.builder()
                .id(102L)
                .entry(entry)
                .senseOrder(3)
                .partOfSpeech("v5s (Động từ nhóm 1)")
                .notes("Dịch thuật sang ngôn ngữ khác")
                .build();
        sense3.getGlosses().add(SenseGloss.builder().id(103L).sense(sense3).languageCode("vi").meaning("dịch sang ngôn ngữ khác").build());
        entry.getSenses().add(sense3);

        when(entryRepository.findExactMatches("直す", null, null)).thenReturn(List.of(entry));

        List<WordDetailDto> results = dictionaryService.search("直す", null, null);
        assertNotNull(results);
        assertEquals(1, results.size());
        WordDetailDto word = results.get(0);

        assertEquals("直す", word.getKanji());
        assertEquals("sửa chữa", word.getPrimaryMeaning());
        assertNotNull(word.getSenses());
        assertEquals(3, word.getSenses().size());

        // Verify sense 1
        assertEquals("sửa chữa", word.getSenses().get(0).getMeaning());
        assertEquals("v5s (Động từ nhóm 1)", word.getSenses().get(0).getPartOfSpeech());

        // Verify sense 2
        assertEquals("đính chính, sửa sai", word.getSenses().get(1).getMeaning());
        assertEquals("Đính chính, sửa lỗi sai", word.getSenses().get(1).getDetailedMeaning());
        assertTrue(word.getSenses().get(1).getEnglishGlosses().contains("to correct"));

        // Verify sense 3
        assertEquals("dịch sang ngôn ngữ khác", word.getSenses().get(2).getMeaning());
    }

    @Test
    @DisplayName("Should find root word when searching conjugated verb form (De-inflection)")
    void testSearchConjugatedFormDeinflection() {
        DictionaryEntry entry = createMockWord(1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn, dùng bữa", (byte) 5);

        // Exact match for '食べた' yields empty
        when(entryRepository.findExactMatches("食べた", null, null)).thenReturn(Collections.emptyList());
        // De-inflector generates '食べる', and findExactMatches returns entry
        when(entryRepository.findExactMatches("食べる", null, null)).thenReturn(List.of(entry));

        List<WordDetailDto> results = dictionaryService.search("食べた", null, null);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("食べる", results.get(0).getKanji());
        assertEquals("たべる", results.get(0).getHiragana());
    }

    @Test
    @DisplayName("Should return empty list when word does not exist in DB")
    void testSearchNotFoundReturnsEmptyList() {
        when(entryRepository.findExactMatches("nonexistentword", null, null))
                .thenReturn(Collections.emptyList());
        when(entryRepository.searchEntries(eq("nonexistentword"), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<WordDetailDto> results = dictionaryService.search("nonexistentword", null, null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return kanji breakdown from KanjiEntryRepository")
    void testGetKanjiDetail() {
        KanjiEntry kanji = KanjiEntry.builder()
                .id(1L)
                .characterText("食")
                .strokeCount(9)
                .jlptLevel("N5")
                .hanViet("THỰC (Ăn)")
                .onReadings("ショク")
                .kunReadings("た-べる, く-う")
                .meaning("Ăn uống, thực phẩm")
                .build();

        when(kanjiEntryRepository.findByCharacterText("食")).thenReturn(Optional.of(kanji));

        KanjiBreakdownDto dto = dictionaryService.getKanjiDetail("食");
        assertNotNull(dto);
        assertEquals("食", dto.getCharacter());
        assertEquals(9, dto.getStrokeCount());
        assertEquals("THỰC (Ăn)", dto.getHanViet());
        assertEquals("Cấp độ N5", dto.getJlptLevel());
    }

    @Test
    @DisplayName("Should fetch and cache kanji from external API when not in local DB (Hybrid Model)")
    void testHybridKanjiFallback() {
        when(kanjiEntryRepository.findByCharacterText("蛍")).thenReturn(Optional.empty());

        KanjiEntry externalKanji = KanjiEntry.builder()
                .id(999L)
                .characterText("蛍")
                .strokeCount(11)
                .jlptLevel("N2")
                .hanViet("HÁN TỰ (蛍)")
                .onReadings("ケイ")
                .kunReadings("ほたる")
                .meaning("lightning-bug, firefly")
                .build();

        when(kanjiExternalApiService.fetchAndCacheKanji("蛍")).thenReturn(Optional.of(externalKanji));

        KanjiBreakdownDto dto = dictionaryService.getKanjiDetail("蛍");
        assertNotNull(dto);
        assertEquals("蛍", dto.getCharacter());
        assertEquals(11, dto.getStrokeCount());
        assertEquals("ほたる", dto.getKunReadings());
        assertEquals("Cấp độ N2", dto.getJlptLevel());
    }

    @Test
    @DisplayName("Should return suggestions list from repository")
    void testGetSuggestions() {
        Object[] row = new Object[]{"木漏れ日", "こもれび", "komorebi", "Ánh nắng lọt qua kẽ lá", 28L};
        List<Object[]> rawList = new java.util.ArrayList<>();
        rawList.add(row);

        when(entryRepository.findSuggestionsRaw(eq("komorebi"), any(Pageable.class)))
                .thenReturn(rawList);

        List<SuggestionDto> suggestions = dictionaryService.getSuggestions("komorebi");
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        assertEquals("木漏れ日", suggestions.get(0).getTerm());
    }

    @Test
    @DisplayName("Should pass NOUN category to repository when filtering by noun")
    void testSearchFilterByNoun() {
        DictionaryEntry nounEntry = createMockWord(5L, "本", "ほん", "hon", "n (Danh từ)", "sách", (byte) 5);
        when(entryRepository.findExactMatches("本", null, "NOUN")).thenReturn(List.of(nounEntry));

        List<WordDetailDto> results = dictionaryService.search("本", null, "noun");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("本", results.get(0).getKanji());
        assertEquals("n (Danh từ)", results.get(0).getPartOfSpeech());
    }

    @Test
    @DisplayName("Should pass VERB category to repository when filtering by verb")
    void testSearchFilterByVerb() {
        DictionaryEntry verbEntry = createMockWord(1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn", (byte) 5);
        when(entryRepository.findExactMatches("食べる", null, "VERB")).thenReturn(List.of(verbEntry));

        List<WordDetailDto> results = dictionaryService.search("食べる", null, "verb");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("食べる", results.get(0).getKanji());
    }

    @Test
    @DisplayName("Should pass IDIOM category to repository when filtering by idiom")
    void testSearchFilterByIdiom() {
        DictionaryEntry idiomEntry = createMockWord(99L, "一期一会", "いちごいちえ", "ichigoichie", "id (Quán dụng ngữ)", "nhất kỳ nhất hội", (byte) 1);
        when(entryRepository.findExactMatches("一期一会", null, "IDIOM")).thenReturn(List.of(idiomEntry));

        List<WordDetailDto> results = dictionaryService.search("一期一会", null, "idiom");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("一期一会", results.get(0).getKanji());
    }

    @Test
    @DisplayName("Should prioritize matching sense POS when filtering multi-sense word")
    void testSearchFilterPolysemyPrefersMatchingSense() {
        DictionaryEntry entry = createMockWord(18L, "直す", "なおす", "naosu", "n (Danh từ)", "việc sửa", (byte) 4);
        DictionarySense sense2 = DictionarySense.builder()
                .id(100L)
                .entry(entry)
                .senseOrder(2)
                .partOfSpeech("v5s (Động từ nhóm 1)")
                .posCategory("VERB")
                .notes("sửa chữa")
                .build();
        sense2.getGlosses().add(SenseGloss.builder().id(100L).sense(sense2).languageCode("vi").meaning("sửa chữa").build());
        entry.getSenses().add(sense2);

        when(entryRepository.findExactMatches("直す", null, "VERB")).thenReturn(List.of(entry));

        List<WordDetailDto> results = dictionaryService.search("直す", null, "verb");
        assertNotNull(results);
        assertEquals(1, results.size());
        // Primary POS of DTO should be the matching sense (Verb), not the first sense (Noun)
        assertEquals("v5s (Động từ nhóm 1)", results.get(0).getPartOfSpeech());
    }

    @Test
    @DisplayName("Should return null pitchAccent when reading has no pitch accent data")
    void testSearchReturnsNullPitchAccentWhenMissing() {
        DictionaryEntry entry = createMockWord(1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn", (byte) 5);
        when(entryRepository.findExactMatches("食べる", null, null)).thenReturn(List.of(entry));

        List<WordDetailDto> results = dictionaryService.search("食べる", null, null);
        assertEquals(1, results.size());
        assertNull(results.get(0).getPitchAccent());
    }

    @Test
    @DisplayName("Should format pitchAccent from primary reading when present in DB")
    void testSearchFormatsPitchAccentFromReading() {
        DictionaryEntry entry = createMockWord(1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn", (byte) 5);
        entry.getReadings().get(0).setPitchAccent("2");
        when(entryRepository.findExactMatches("食べる", null, null)).thenReturn(List.of(entry));

        List<WordDetailDto> results = dictionaryService.search("食べる", null, null);
        assertEquals(1, results.size());
        assertEquals("[2] 中高 (Nakadaka)", results.get(0).getPitchAccent());
    }

    @Test
    @DisplayName("Should map synonyms and grammar patterns from DB entry for any word")
    void testSearchMapsSynonymsFromDatabase() {
        DictionaryEntry entry = createMockWord(1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn", (byte) 5);
        entry.getSynonyms().add(DictionarySynonym.builder()
                .id(1L)
                .entry(entry)
                .word("食事する (しょくじする)")
                .reading("しょくじする")
                .meaning("Dùng bữa, ăn uống")
                .relationType("synonym")
                .displayOrder(1)
                .build());
        entry.getSynonyms().add(DictionarySynonym.builder()
                .id(2L)
                .entry(entry)
                .word("〜てみる")
                .reading(null)
                .meaning("Thử làm gì đó")
                .relationType("grammar")
                .displayOrder(2)
                .build());

        when(entryRepository.findExactMatches("食べる", null, null)).thenReturn(List.of(entry));

        List<WordDetailDto> results = dictionaryService.search("食べる", null, null);
        assertEquals(1, results.size());
        List<SynonymDto> synonyms = results.get(0).getSynonyms();
        assertNotNull(synonyms);
        assertEquals(2, synonyms.size());
        assertEquals("食事する (しょくじする)", synonyms.get(0).getWord());
        assertEquals("しょくじする", synonyms.get(0).getReading());
        assertEquals("Dùng bữa, ăn uống", synonyms.get(0).getMeaning());
        assertEquals("synonym", synonyms.get(0).getType());

        assertEquals("〜てみる", synonyms.get(1).getWord());
        assertEquals("", synonyms.get(1).getReading());
        assertEquals("grammar", synonyms.get(1).getType());
    }

    @Test
    @DisplayName("Should return empty synonyms list when DB entry has no synonyms")
    void testSearchReturnsEmptySynonymsWhenMissing() {
        DictionaryEntry entry = createMockWord(1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn", (byte) 5);
        entry.setSynonyms(null);
        when(entryRepository.findExactMatches("食べる", null, null)).thenReturn(List.of(entry));

        List<WordDetailDto> results = dictionaryService.search("食べる", null, null);
        assertEquals(1, results.size());
        assertNotNull(results.get(0).getSynonyms());
        assertTrue(results.get(0).getSynonyms().isEmpty());
    }

    private DictionaryEntry createMockWord(Long id, String kanji, String hiragana, String romaji, String pos, String meaning, Byte jlpt) {
        DictionaryEntry entry = DictionaryEntry.builder()
                .id(id)
                .jlptLevel(jlpt)
                .build();

        DictionaryForm form = DictionaryForm.builder()
                .id(id)
                .entry(entry)
                .writtenForm(kanji)
                .formType("KANJI")
                .isPrimary(true)
                .build();
        entry.getForms().add(form);

        DictionaryReading reading = DictionaryReading.builder()
                .id(id)
                .entry(entry)
                .reading(hiragana)
                .romaji(romaji)
                .isPrimary(true)
                .build();
        entry.getReadings().add(reading);

        DictionarySense sense = DictionarySense.builder()
                .id(id)
                .entry(entry)
                .partOfSpeech(pos)
                .posCategory(PosCategory.inferCategories(pos))
                .build();

        SenseGloss gloss = SenseGloss.builder()
                .id(id)
                .sense(sense)
                .languageCode("vi")
                .meaning(meaning)
                .build();
        sense.getGlosses().add(gloss);
        entry.getSenses().add(sense);

        return entry;
    }
}
