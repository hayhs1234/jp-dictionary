package com.jpdictionary.dictionary;

import com.jpdictionary.auth.repository.UserRepository;
import com.jpdictionary.config.CacheConfig;
import com.jpdictionary.dictionary.dto.KanjiBreakdownDto;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.dto.WordDetailDto;
import com.jpdictionary.dictionary.dto.WordSummaryDto;
import com.jpdictionary.dictionary.entity.DictionaryEntry;
import com.jpdictionary.dictionary.entity.DictionaryForm;
import com.jpdictionary.dictionary.entity.DictionaryReading;
import com.jpdictionary.dictionary.entity.DictionarySense;
import com.jpdictionary.dictionary.entity.KanjiEntry;
import com.jpdictionary.dictionary.entity.SenseGloss;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.DictionaryFormRepository;
import com.jpdictionary.dictionary.repository.DictionarySearchIndexRepository;
import com.jpdictionary.dictionary.repository.KanjiEntryRepository;
import com.jpdictionary.dictionary.service.DictionarySearchIndexService;
import com.jpdictionary.dictionary.service.DictionaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
                "spring.cache.type=caffeine",
                "app.search-index.sync-on-startup=false"
})
@ActiveProfiles("test")
class DictionaryCachingIntegrationTest {

        @Autowired
        private DictionaryService dictionaryService;

        @Autowired
        private DictionarySearchIndexService searchIndexService;

        @Autowired
        private CacheManager cacheManager;

        @MockitoBean
        private UserRepository userRepository;

        @MockitoBean
        private DictionaryEntryRepository entryRepository;

        @MockitoBean
        private DictionaryFormRepository dictionaryFormRepository;

        @MockitoBean
        private DictionarySearchIndexRepository searchIndexRepository;

        @MockitoBean
        private KanjiEntryRepository kanjiEntryRepository;

        @BeforeEach
        void clearAllCaches() {
                for (String name : cacheManager.getCacheNames()) {
                        Cache cache = cacheManager.getCache(name);
                        if (cache != null) {
                                cache.clear();
                        }
                }
        }

        @Test
        @DisplayName("Should serve repeated search, suggest, detail, and kanji calls from Caffeine cache")
        void testCaffeineCachesRepeatedReadsAndEvictsOnReindex() {
                DictionaryEntry entry = createSampleEntry(1L, "食べる", "たべる", "taberu", "ăn", (byte) 5);
                when(entryRepository.findExactMatchIdsByTerms(eq(List.of("食べる")), isNull(), isNull(), anyInt()))
                                .thenReturn(List.of(1L));
                when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(entry));
                when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));

                KanjiEntry shoku = KanjiEntry.builder()
                                .characterText("食").strokeCount(9).jlptLevel("N5").hanViet("THỰC")
                                .onReadings("ショク").kunReadings("た-べる").meaning("Ăn").build();
                when(kanjiEntryRepository.findByCharacterText("食")).thenReturn(Optional.of(shoku));
                when(kanjiEntryRepository.findByCharacterTextIn(anyCollection())).thenReturn(List.of(shoku));

                Object[] row = new Object[] { 1L, "食べる", "たべる", "taberu", "ăn", (byte) 5 };
                when(searchIndexRepository.findSuggestionRowsPrefix("tabe%", 8))
                                .thenReturn(Collections.singletonList(row));

                // 1. Search cache hit + eviction on reindex
                List<WordSummaryDto> s1 = dictionaryService.search("食べる", null, null);
                List<WordSummaryDto> s2 = dictionaryService.search(" 食べる ", null, null);
                assertEquals(1, s1.size());
                assertEquals(s1, s2);
                verify(entryRepository, times(1))
                                .findExactMatchIdsByTerms(eq(List.of("食べる")), isNull(), isNull(), anyInt());

                // Trigger reindexAll -> should evict wordSearch & wordSuggest caches
                when(entryRepository.findAll(any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(entry)));
                searchIndexService.reindexAll();

                dictionaryService.search("食べる", null, null);
                verify(entryRepository, times(2))
                                .findExactMatchIdsByTerms(eq(List.of("食べる")), isNull(), isNull(), anyInt());

                // 2. Suggest cache hit
                List<SuggestionDto> sug1 = dictionaryService.getSuggestions("tabe");
                List<SuggestionDto> sug2 = dictionaryService.getSuggestions("TABE");
                assertEquals(1, sug1.size());
                assertEquals(sug1, sug2);
                verify(searchIndexRepository, times(1)).findSuggestionRowsPrefix("tabe%", 8);

                // 3. Word detail cache hit
                WordDetailDto d1 = dictionaryService.getWordDetail(1L);
                WordDetailDto d2 = dictionaryService.getWordDetail(1L);
                assertEquals(d1, d2);
                verify(entryRepository, times(1)).findById(1L);

                // 4. Kanji detail cache hit
                KanjiBreakdownDto k1 = dictionaryService.getKanjiDetail("食");
                KanjiBreakdownDto k2 = dictionaryService.getKanjiDetail("食べる");
                assertEquals(k1, k2);
                verify(kanjiEntryRepository, times(1)).findByCharacterText("食");

                // Verify expected cache regions exist
                assertEquals(
                                true,
                                cacheManager.getCacheNames().containsAll(List.of(
                                                CacheConfig.WORD_SEARCH,
                                                CacheConfig.WORD_SUGGEST,
                                                CacheConfig.WORD_DETAIL,
                                                CacheConfig.KANJI_DETAIL,
                                                CacheConfig.TRENDING_KANJI)));
        }

        private DictionaryEntry createSampleEntry(
                        Long id, String kanji, String hiragana, String romaji, String meaning, Byte jlpt) {
                DictionaryEntry entry = DictionaryEntry.builder().id(id).jlptLevel(jlpt).build();
                entry.getForms().add(DictionaryForm.builder()
                                .id(id).entry(entry).writtenForm(kanji).formType("KANJI").isPrimary(true).build());
                entry.getReadings().add(DictionaryReading.builder()
                                .id(id).entry(entry).reading(hiragana).romaji(romaji).isPrimary(true).build());
                DictionarySense sense = DictionarySense.builder()
                                .id(id).entry(entry).partOfSpeech("v1")
                                .posCategories(new LinkedHashSet<>(PosCategory.inferCategorySet("v1"))).build();
                sense.getGlosses().add(SenseGloss.builder()
                                .id(id).sense(sense).languageCode("vi").meaning(meaning).build());
                entry.getSenses().add(sense);
                return entry;
        }
}
