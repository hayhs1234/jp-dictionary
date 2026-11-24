package com.jpdictionary.dictionary;

import com.jpdictionary.dictionary.dto.KanjiBreakdownDto;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.dto.WordDetailDto;
import com.jpdictionary.dictionary.entity.DictionaryEntry;
import com.jpdictionary.dictionary.entity.DictionaryForm;
import com.jpdictionary.dictionary.entity.DictionaryReading;
import com.jpdictionary.dictionary.entity.DictionarySense;
import com.jpdictionary.dictionary.entity.SenseGloss;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.service.DictionaryService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceTest {

    @Mock
    private DictionaryEntryRepository entryRepository;

    private DictionaryService dictionaryService;

    @BeforeEach
    void setUp() {
        dictionaryService = new DictionaryServiceImpl(entryRepository);
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
        DictionaryEntry entry = DictionaryEntry.builder()
                .id(1L)
                .jlptLevel((byte) 5)
                .build();

        DictionaryForm form = DictionaryForm.builder()
                .id(1L)
                .entry(entry)
                .writtenForm("食べる")
                .formType("KANJI")
                .isPrimary(true)
                .build();
        entry.getForms().add(form);

        DictionaryReading reading = DictionaryReading.builder()
                .id(1L)
                .entry(entry)
                .reading("たべる")
                .romaji("taberu")
                .isPrimary(true)
                .build();
        entry.getReadings().add(reading);

        DictionarySense sense = DictionarySense.builder()
                .id(1L)
                .entry(entry)
                .partOfSpeech("v1 (Động từ nhóm 2)")
                .build();

        SenseGloss gloss = SenseGloss.builder()
                .id(1L)
                .sense(sense)
                .languageCode("vi")
                .meaning("ăn, dùng bữa")
                .build();
        sense.getGlosses().add(gloss);
        entry.getSenses().add(sense);

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
    @DisplayName("Should return kanji breakdown for single kanji")
    void testGetKanjiDetail() {
        KanjiBreakdownDto kanji = dictionaryService.getKanjiDetail("食");
        assertNotNull(kanji);
        assertEquals("食", kanji.getCharacter());
        assertEquals(9, kanji.getStrokeCount());
        assertEquals("THỰC (Ăn)", kanji.getHanViet());
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
}
