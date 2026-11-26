package com.jpdictionary.dictionary;

import com.jpdictionary.dictionary.dto.KanjiBreakdownDto;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.dto.SynonymDto;
import com.jpdictionary.dictionary.dto.WordDetailDto;
import com.jpdictionary.dictionary.dto.WordSummaryDto;
import com.jpdictionary.dictionary.entity.DictionaryEntry;
import com.jpdictionary.dictionary.entity.DictionaryForm;
import com.jpdictionary.dictionary.entity.DictionaryReading;
import com.jpdictionary.dictionary.entity.DictionarySense;
import com.jpdictionary.dictionary.entity.DictionarySynonym;
import com.jpdictionary.dictionary.entity.KanjiEntry;
import com.jpdictionary.dictionary.entity.SenseGloss;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.DictionarySearchIndexRepository;
import com.jpdictionary.dictionary.repository.KanjiEntryRepository;
import com.jpdictionary.dictionary.service.DictionaryService;
import com.jpdictionary.dictionary.service.deinflect.JapaneseDeinflector;
import com.jpdictionary.dictionary.service.external.KanjiExternalApiService;
import com.jpdictionary.dictionary.service.impl.DictionaryServiceImpl;
import com.jpdictionary.dictionary.service.search.MySqlDictionarySearchBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private DictionaryEntryRepository entryRepository;

    @Mock
    private DictionarySearchIndexRepository searchIndexRepository;

    @Mock
    private KanjiEntryRepository kanjiEntryRepository;

    @Mock
    private KanjiExternalApiService kanjiExternalApiService;

    private DictionaryService dictionaryService;

    @BeforeEach
    void setUp() {
        MySqlDictionarySearchBackend mySqlBackend = new MySqlDictionarySearchBackend(
                entryRepository,
                searchIndexRepository,
                new JapaneseDeinflector()
        );
        dictionaryService = new DictionaryServiceImpl(
                entryRepository,
                mySqlBackend,
                kanjiEntryRepository,
                kanjiExternalApiService
        );
    }

    @Test
    @DisplayName("Should return empty list when search term and filters are empty")
    void testSearchEmptyReturnsEmptyList() {
        List<WordSummaryDto> results = dictionaryService.search("", null, null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return mapped WordSummaryDto on exact match")
    void testSearchExactMatch() {
        DictionaryEntry entry = createMockWord(
                1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn, dùng bữa", (byte) 5);

        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("食べる")), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(1L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(entry));

        List<WordSummaryDto> results = dictionaryService.search("食べる", null, null);
        assertNotNull(results);
        assertEquals(1, results.size());
        WordSummaryDto word = results.get(0);
        assertEquals(1L, word.getId());
        assertEquals("食べる", word.getKanji());
        assertEquals("たべる", word.getHiragana());
        assertEquals("taberu", word.getRomaji());
        assertEquals("ăn, dùng bữa", word.getPrimaryMeaning());
        assertEquals("N5", word.getJlptLevel());
        assertEquals(1, word.getSenseCount());
    }

    @Test
    @DisplayName("Should return senseCount in search summary and all senses in getWordDetail (Polysemy)")
    void testPolysemySummaryAndDetail() {
        DictionaryEntry entry = createMockWord(
                18L, "直す", "なおす", "naosu", "v5s (Động từ nhóm 1)", "sửa chữa", (byte) 4);

        DictionarySense sense2 = DictionarySense.builder()
                .id(100L)
                .entry(entry)
                .senseOrder(2)
                .partOfSpeech("v5s (Động từ nhóm 1)")
                .notes("Đính chính, sửa lỗi sai")
                .build();
        sense2.getGlosses().add(
                SenseGloss.builder().id(100L).sense(sense2).languageCode("vi").meaning("đính chính, sửa sai").build());
        sense2.getGlosses().add(
                SenseGloss.builder().id(101L).sense(sense2).languageCode("en").meaning("to correct").build());
        entry.getSenses().add(sense2);

        DictionarySense sense3 = DictionarySense.builder()
                .id(102L)
                .entry(entry)
                .senseOrder(3)
                .partOfSpeech("v5s (Động từ nhóm 1)")
                .notes("Dịch thuật sang ngôn ngữ khác")
                .build();
        sense3.getGlosses().add(
                SenseGloss.builder().id(103L).sense(sense3).languageCode("vi")
                        .meaning("dịch sang ngôn ngữ khác").build());
        entry.getSenses().add(sense3);

        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("直す")), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(18L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(entry));

        List<WordSummaryDto> summaryResults = dictionaryService.search("直す", null, null);
        assertEquals(1, summaryResults.size());
        assertEquals(3, summaryResults.get(0).getSenseCount());
        assertEquals("sửa chữa", summaryResults.get(0).getPrimaryMeaning());

        when(entryRepository.findById(18L)).thenReturn(Optional.of(entry));
        WordDetailDto word = dictionaryService.getWordDetail(18L);
        assertEquals("直す", word.getKanji());
        assertEquals("sửa chữa", word.getPrimaryMeaning());
        assertNotNull(word.getSenses());
        assertEquals(3, word.getSenses().size());
        assertEquals("sửa chữa", word.getSenses().get(0).getMeaning());
        assertEquals("đính chính, sửa sai", word.getSenses().get(1).getMeaning());
        assertEquals("Đính chính, sửa lỗi sai", word.getSenses().get(1).getDetailedMeaning());
        assertTrue(word.getSenses().get(1).getEnglishGlosses().contains("to correct"));
        assertEquals("dịch sang ngôn ngữ khác", word.getSenses().get(2).getMeaning());
    }

    @Test
    @DisplayName("Should batch de-inflected root lookup into a single IN query")
    void testSearchConjugatedFormDeinflectionBatched() {
        DictionaryEntry entry = createMockWord(
                1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn, dùng bữa", (byte) 5);

        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("食べた")), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(entryRepository.findExactMatchIdsByTerms(
                org.mockito.ArgumentMatchers.<Collection<String>>argThat(
                        terms -> terms != null && terms.contains("食べる")),
                isNull(), isNull(), anyInt()))
                .thenReturn(List.of(1L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(entry));

        List<WordSummaryDto> results = dictionaryService.search("食べた", null, null);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("食べる", results.get(0).getKanji());
        assertEquals("たべる", results.get(0).getHiragana());

        verify(entryRepository, times(2))
                .findExactMatchIdsByTerms(anyCollection(), isNull(), isNull(), anyInt());
    }

    @Test
    @DisplayName("Should preserve ranking across Exact, Prefix, and FULLTEXT ngram tiers")
    void testSearchPreservesRankingOrderAcrossSearchIndexTiers() {
        DictionaryEntry exactEntry = createMockWord(
                30L, "行く", "いく", "iku", "v5k", "đi", (byte) 5);
        DictionaryEntry prefixEntry = createMockWord(
                10L, "行き先", "いきさき", "ikisaki", "n", "điểm đến", (byte) 3);
        DictionaryEntry fulltextEntry = createMockWord(
                20L, "旅行", "りょこう", "ryokou", "n, vs", "du lịch", (byte) 4);

        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("行き")), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(30L));
        when(searchIndexRepository.findPrefixMatchIds(eq("行き%"), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(10L));
        when(searchIndexRepository.findFulltextMatchIds(eq("+行き"), eq("+行き"), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(20L));
        when(entryRepository.findAllByIdIn(anyCollection()))
                .thenReturn(List.of(prefixEntry, fulltextEntry, exactEntry));

        List<WordSummaryDto> results = dictionaryService.search("行き", null, null);
        assertEquals(List.of(30L, 10L, 20L), results.stream().map(WordSummaryDto::getId).toList());
    }

    @Test
    @DisplayName("Should use single-character LIKE fallback on search index when query length is 1")
    void testSearchSingleCharacterUsesLikeFallbackInsteadOfNgramFulltext() {
        DictionaryEntry entry = createMockWord(
                50L, "日本", "にほん", "nihon", "n", "Nhật Bản", (byte) 5);

        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("本")), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(searchIndexRepository.findPrefixMatchIds(eq("本%"), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(searchIndexRepository.findSingleCharSubstringIds(eq("%本%"), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(50L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(entry));

        List<WordSummaryDto> results = dictionaryService.search("本", null, null);
        assertEquals(1, results.size());
        assertEquals(50L, results.get(0).getId());
        verify(searchIndexRepository, never())
                .findFulltextMatchIds(anyString(), anyString(), isNull(), isNull(), anyInt());
    }

    @Test
    @DisplayName("Should normalize Vietnamese query without diacritics for FULLTEXT ngram search")
    void testSearchVietnameseAsciiFallbackUsesFulltext() {
        DictionaryEntry entry = createMockWord(
                1L, "食べる", "たべる", "taberu", "v1", "ăn uống", (byte) 5);

        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("ăn uống")), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(searchIndexRepository.findPrefixMatchIds(eq("ăn uống%"), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(searchIndexRepository.findFulltextMatchIds(
                eq("+ăn +uống"), eq("+an +uong"), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(1L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(entry));

        List<WordSummaryDto> results = dictionaryService.search("ăn uống", null, null);
        assertEquals(1, results.size());
        verify(searchIndexRepository).findFulltextMatchIds("+ăn +uống", "+an +uong", null, null, 20);
    }

    @Test
    @DisplayName("Should query dictionary_search_index with bitmask for filter-only searches")
    void testSearchFilterOnlyUsesSearchIndexWithBitmask() {
        DictionaryEntry e1 = createMockWord(1L, "食べる", "たべる", "taberu", "v1", "ăn", (byte) 5);
        DictionaryEntry e2 = createMockWord(2L, "先生", "せんせい", "sensei", "n", "giáo viên", (byte) 5);

        when(searchIndexRepository.findIdsByFilters(eq((byte) 5), isNull(), anyInt()))
                .thenReturn(List.of(1L, 2L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(e1, e2));

        KanjiEntry shoku = KanjiEntry.builder().characterText("食").hanViet("THỰC (Ăn)").build();
        KanjiEntry sen = KanjiEntry.builder().characterText("先").hanViet("TIÊN (Trước)").build();
        KanjiEntry sei = KanjiEntry.builder().characterText("生").hanViet("SINH (Sống)").build();
        when(kanjiEntryRepository.findByCharacterTextIn(anyCollection()))
                .thenReturn(List.of(shoku, sen, sei));

        List<WordSummaryDto> results = dictionaryService.search("", "N5", null);
        assertEquals(2, results.size());
        assertEquals("THỰC", results.get(0).getHanViet());
        assertEquals("TIÊN SINH", results.get(1).getHanViet());

        verify(kanjiEntryRepository, times(1)).findByCharacterTextIn(Set.of("食", "先", "生"));
    }

    @Test
    @DisplayName("Should not load examples or synonyms during search summary mapping")
    void testSearchDoesNotTouchHeavyLazyCollections() {
        DictionaryEntry entry = spy(createMockWord(
                1L, "食べる", "たべる", "taberu", "v1", "ăn", (byte) 5));

        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("食べる")), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(1L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(entry));

        List<WordSummaryDto> results = dictionaryService.search("食べる", null, null);
        assertEquals(1, results.size());
        verify(entry, never()).getExamples();
        verify(entry, never()).getSynonyms();
    }

    @Test
    @DisplayName("Should return empty list when word does not exist in DB")
    void testSearchNotFoundReturnsEmptyList() {
        when(entryRepository.findExactMatchIdsByTerms(anyCollection(), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(searchIndexRepository.findPrefixMatchIds(eq("nonexistentword%"), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(searchIndexRepository.findFulltextMatchIds(
                eq("+nonexistentword"), eq("+nonexistentword"), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());

        List<WordSummaryDto> results = dictionaryService.search("nonexistentword", null, null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should escape LIKE wildcards in prefix patterns")
    void testSearchEscapesLikeWildcards() {
        when(entryRepository.findExactMatchIdsByTerms(anyCollection(), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(searchIndexRepository.findPrefixMatchIds(eq("100!%!_test%"), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(searchIndexRepository.findFulltextMatchIds(
                eq("+100%_test"), eq("+100%_test"), isNull(), isNull(), anyInt()))
                .thenReturn(Collections.emptyList());

        List<WordSummaryDto> results = dictionaryService.search("100%_test", null, null);
        assertTrue(results.isEmpty());
        verify(searchIndexRepository).findPrefixMatchIds("100!%!_test%", null, null, 20);
    }

    @Test
    @DisplayName("Should batch Kanji lookup once for both hanViet and kanjiBreakdown in getWordDetail")
    void testGetWordDetailBatchesKanjiLookup() {
        DictionaryEntry entry = createMockWord(13L, "先生", "せんせい", "sensei", "n", "giáo viên", (byte) 5);
        KanjiEntry sen = KanjiEntry.builder()
                .characterText("先").strokeCount(6).jlptLevel("N5").hanViet("TIÊN (Trước)")
                .onReadings("セン").kunReadings("さき").meaning("Trước").build();
        KanjiEntry sei = KanjiEntry.builder()
                .characterText("生").strokeCount(5).jlptLevel("N5").hanViet("SINH (Sống)")
                .onReadings("セイ").kunReadings("い-きる").meaning("Sống").build();

        when(entryRepository.findById(13L)).thenReturn(Optional.of(entry));
        when(kanjiEntryRepository.findByCharacterTextIn(Set.of("先", "生"))).thenReturn(List.of(sen, sei));

        WordDetailDto detail = dictionaryService.getWordDetail(13L);
        assertEquals("TIÊN SINH", detail.getHanViet());
        assertEquals(2, detail.getKanjiBreakdown().size());
        verify(kanjiEntryRepository, times(1)).findByCharacterTextIn(Set.of("先", "生"));
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
    @DisplayName("Should return ordered suggestions from dictionary_search_index with JLPT badge")
    void testGetSuggestions() {
        Object[] row = new Object[]{28L, "木漏れ日", "こもれび", "komorebi", "Ánh nắng lọt qua kẽ lá", (byte) 2};
        List<Object[]> prefixRows = new java.util.ArrayList<>();
        prefixRows.add(row);
        when(searchIndexRepository.findSuggestionRowsPrefix("komorebi%", 8)).thenReturn(prefixRows);
        when(searchIndexRepository.findSuggestionRowsFulltext("+komorebi", "+komorebi", 8))
                .thenReturn(Collections.emptyList());

        List<SuggestionDto> suggestions = dictionaryService.getSuggestions("komorebi");
        assertNotNull(suggestions);
        assertEquals(1, suggestions.size());
        assertEquals(28L, suggestions.get(0).getId());
        assertEquals("木漏れ日", suggestions.get(0).getTerm());
        assertEquals("こもれび", suggestions.get(0).getReading());
        assertEquals("komorebi", suggestions.get(0).getRomaji());
        assertEquals("N2", suggestions.get(0).getJlpt());
    }

    @Test
    @DisplayName("Should pass NOUN category and bitmask when filtering by noun")
    void testSearchFilterByNoun() {
        DictionaryEntry nounEntry = createMockWord(5L, "本", "ほん", "hon", "n (Danh từ)", "sách", (byte) 5);
        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("本")), isNull(), eq("NOUN"), anyInt()))
                .thenReturn(List.of(5L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(nounEntry));

        List<WordSummaryDto> results = dictionaryService.search("本", null, "noun");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("本", results.get(0).getKanji());
        assertEquals("n (Danh từ)", results.get(0).getPartOfSpeech());
    }

    @Test
    @DisplayName("Should pass VERB category and bitmask when filtering by verb")
    void testSearchFilterByVerb() {
        DictionaryEntry verbEntry = createMockWord(
                1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn", (byte) 5);
        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("食べる")), isNull(), eq("VERB"), anyInt()))
                .thenReturn(List.of(1L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(verbEntry));

        List<WordSummaryDto> results = dictionaryService.search("食べる", null, "verb");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("食べる", results.get(0).getKanji());
    }

    @Test
    @DisplayName("Should pass IDIOM category when filtering by idiom")
    void testSearchFilterByIdiom() {
        DictionaryEntry idiomEntry = createMockWord(
                99L, "一期一会", "いちごいちえ", "ichigoichie", "id (Quán dụng ngữ)", "nhất kỳ nhất hội", (byte) 1);
        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("一期一会")), isNull(), eq("IDIOM"), anyInt()))
                .thenReturn(List.of(99L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(idiomEntry));

        List<WordSummaryDto> results = dictionaryService.search("一期一会", null, "idiom");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("一期一会", results.get(0).getKanji());
    }

    @Test
    @DisplayName("Should pass ADVERB and OTHER categories distinctly without VERB collision")
    void testSearchFilterByAdverbAndOther() {
        DictionaryEntry advEntry = createMockWord(
                101L, "とても", "とても", "totemo", "adv (Phó từ)", "rất", (byte) 5);
        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("とても")), isNull(), eq("ADVERB"), anyInt()))
                .thenReturn(List.of(101L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(advEntry));

        List<WordSummaryDto> advResults = dictionaryService.search("とても", null, "adverb");
        assertEquals(1, advResults.size());
        assertEquals("adv (Phó từ)", advResults.get(0).getPartOfSpeech());

        DictionaryEntry prtEntry = createMockWord(
                102L, "これ", "これ", "kore", "pn (Đại từ)", "cái này", (byte) 5);
        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("これ")), isNull(), eq("OTHER"), anyInt()))
                .thenReturn(List.of(102L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(prtEntry));

        List<WordSummaryDto> otherResults = dictionaryService.search("これ", null, "other");
        assertEquals(1, otherResults.size());
        assertEquals("pn (Đại từ)", otherResults.get(0).getPartOfSpeech());

        verify(entryRepository).findExactMatchIdsByTerms(eq(List.of("とても")), isNull(), eq("ADVERB"), anyInt());
        verify(entryRepository).findExactMatchIdsByTerms(eq(List.of("これ")), isNull(), eq("OTHER"), anyInt());
    }

    @Test
    @DisplayName("Should prioritize matching sense POS using stored posCategories on polysemy filter")
    void testSearchFilterPolysemyPrefersMatchingSense() {
        DictionaryEntry entry = createMockWord(18L, "直す", "なおす", "naosu", "n (Danh từ)", "việc sửa", (byte) 4);
        DictionarySense sense2 = DictionarySense.builder()
                .id(100L)
                .entry(entry)
                .senseOrder(2)
                .partOfSpeech("Nhãn tùy chỉnh")
                .posCategories(Set.of(PosCategory.VERB))
                .notes("sửa chữa")
                .build();
        sense2.getGlosses().add(
                SenseGloss.builder().id(100L).sense(sense2).languageCode("vi").meaning("sửa chữa").build());
        entry.getSenses().add(sense2);

        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("直す")), isNull(), eq("VERB"), anyInt()))
                .thenReturn(List.of(18L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(entry));

        List<WordSummaryDto> results = dictionaryService.search("直す", null, "verb");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Nhãn tùy chỉnh", results.get(0).getPartOfSpeech());
    }

    @Test
    @DisplayName("Should map normalized posCategories onto WordSenseDto for multi-category senses")
    void testMapSingleSensePopulatesPosCategories() {
        DictionaryEntry entry = createMockWord(
                21L, "成功", "せいこう", "seikou", "n, vs (Danh từ / Động từ suru)", "thành công", (byte) 3);
        when(entryRepository.findById(21L)).thenReturn(Optional.of(entry));

        WordDetailDto detail = dictionaryService.getWordDetail(21L);
        List<String> categories = detail.getSenses().get(0).getPosCategories();
        assertTrue(categories.contains("NOUN"));
        assertTrue(categories.contains("VERB"));
    }

    @Test
    @DisplayName("Should return null pitchAccent when reading has no pitch accent data")
    void testSearchReturnsNullPitchAccentWhenMissing() {
        DictionaryEntry entry = createMockWord(1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn", (byte) 5);
        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("食べる")), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(1L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(entry));

        List<WordSummaryDto> results = dictionaryService.search("食べる", null, null);
        assertEquals(1, results.size());
        assertNull(results.get(0).getPitchAccent());
    }

    @Test
    @DisplayName("Should format pitchAccent from primary reading when present in DB")
    void testSearchFormatsPitchAccentFromReading() {
        DictionaryEntry entry = createMockWord(1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn", (byte) 5);
        entry.getReadings().get(0).setPitchAccent("2");
        when(entryRepository.findExactMatchIdsByTerms(eq(List.of("食べる")), isNull(), isNull(), anyInt()))
                .thenReturn(List.of(1L));
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(entry));

        List<WordSummaryDto> results = dictionaryService.search("食べる", null, null);
        assertEquals(1, results.size());
        assertEquals("[2] 中高 (Nakadaka)", results.get(0).getPitchAccent());
    }

    @Test
    @DisplayName("Should map synonyms and grammar patterns from DB entry in getWordDetail")
    void testGetWordDetailMapsSynonymsFromDatabase() {
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

        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));

        WordDetailDto detail = dictionaryService.getWordDetail(1L);
        List<SynonymDto> synonyms = detail.getSynonyms();
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
    void testGetWordDetailReturnsEmptySynonymsWhenMissing() {
        DictionaryEntry entry = createMockWord(1L, "食べる", "たべる", "taberu", "v1 (Động từ nhóm 2)", "ăn", (byte) 5);
        entry.setSynonyms(null);
        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));

        WordDetailDto detail = dictionaryService.getWordDetail(1L);
        assertNotNull(detail.getSynonyms());
        assertTrue(detail.getSynonyms().isEmpty());
    }

    private DictionaryEntry createMockWord(
            Long id, String kanji, String hiragana, String romaji, String pos, String meaning, Byte jlpt) {
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
                .posCategories(new LinkedHashSet<>(PosCategory.inferCategorySet(pos)))
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
