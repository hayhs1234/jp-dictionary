package com.jpdictionary.dictionary.service.impl;

import com.jpdictionary.dictionary.entity.DictionaryEntry;
import com.jpdictionary.dictionary.entity.DictionaryForm;
import com.jpdictionary.dictionary.entity.DictionaryReading;
import com.jpdictionary.dictionary.entity.DictionarySearchIndex;
import com.jpdictionary.dictionary.entity.DictionarySense;
import com.jpdictionary.dictionary.entity.SenseGloss;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.DictionarySearchIndexRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DictionarySearchIndexServiceTest {

    @Mock
    private DictionaryEntryRepository entryRepository;

    @Mock
    private DictionarySearchIndexRepository searchIndexRepository;

    private DictionarySearchIndexServiceImpl indexService;

    @BeforeEach
    void setUp() {
        indexService = new DictionarySearchIndexServiceImpl(entryRepository, searchIndexRepository);
    }

    @Test
    @DisplayName("Should build flattened index row with Vietnamese ASCII folding and POS bitmask")
    void testBuildIndexRow() {
        DictionaryEntry entry = DictionaryEntry.builder()
                .id(21L)
                .jlptLevel((byte) 3)
                .frequencyRank(120)
                .build();
        entry.getForms().add(DictionaryForm.builder()
                .entry(entry).writtenForm("成功").isPrimary(true).build());
        entry.getForms().add(DictionaryForm.builder()
                .entry(entry).writtenForm("成効").isPrimary(false).build());
        entry.getReadings().add(DictionaryReading.builder()
                .entry(entry).reading("せいこう").romaji("seikou").isPrimary(true).build());

        DictionarySense sense = DictionarySense.builder()
                .entry(entry)
                .senseOrder(1)
                .partOfSpeech("n, vs")
                .posCategories(Set.of(PosCategory.NOUN, PosCategory.VERB))
                .build();
        sense.getGlosses().add(SenseGloss.builder()
                .sense(sense).languageCode("vi").meaning("thành công, đạt được").build());
        sense.getGlosses().add(SenseGloss.builder()
                .sense(sense).languageCode("en").meaning("success").build());
        entry.getSenses().add(sense);

        DictionarySearchIndex row = indexService.buildIndexRow(entry);

        assertEquals(21L, row.getEntryId());
        assertEquals((byte) 3, row.getJlptLevel());
        assertEquals(120, row.getFrequencyRank());
        assertEquals(PosCategory.NOUN.getBitMask() | PosCategory.VERB.getBitMask(), row.getPosMask());
        assertEquals("成功", row.getPrimaryForm());
        assertEquals("せいこう", row.getPrimaryReading());
        assertEquals("seikou", row.getPrimaryRomaji());
        assertEquals("thành công, đạt được", row.getPrimaryMeaning());
        assertEquals("成功 成効", row.getFormsText());
        assertEquals("せいこう", row.getReadingsText());
        assertEquals("seikou", row.getRomajiText());
        assertEquals("thành công, đạt được; success", row.getMeaningsText());
        assertEquals("thanh cong, dat duoc; success", row.getMeaningsAscii());
        assertTrue(row.getAsciiSynced());
    }

    @Test
    @DisplayName("Should sync pending ASCII rows and missing unindexed entries in batches")
    void testSyncPendingRows() {
        DictionarySearchIndex pending = DictionarySearchIndex.builder()
                .entryId(1L)
                .meaningsText("đi lại; ăn uống")
                .meaningsAscii("di lại; ăn uống")
                .asciiSynced(false)
                .build();

        when(searchIndexRepository.findByAsciiSyncedFalse(any(Pageable.class)))
                .thenReturn(List.of(pending))
                .thenReturn(Collections.emptyList());

        DictionaryEntry missingEntry = DictionaryEntry.builder().id(2L).jlptLevel((byte) 5).build();
        missingEntry.getForms().add(DictionaryForm.builder()
                .entry(missingEntry).writtenForm("本").isPrimary(true).build());
        missingEntry.getReadings().add(DictionaryReading.builder()
                .entry(missingEntry).reading("ほん").romaji("hon").isPrimary(true).build());

        when(searchIndexRepository.findUnindexedEntryIds(any(Pageable.class)))
                .thenReturn(List.of(2L))
                .thenReturn(Collections.emptyList());
        when(entryRepository.findAllByIdIn(anyCollection())).thenReturn(List.of(missingEntry));

        int totalSynced = indexService.syncPendingRows();

        assertEquals(2, totalSynced);
        assertEquals("di lai; an uong", pending.getMeaningsAscii());
        assertTrue(pending.getAsciiSynced());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DictionarySearchIndex>> captor = ArgumentCaptor.forClass(List.class);
        verify(searchIndexRepository, org.mockito.Mockito.times(2)).saveAll(captor.capture());
        assertEquals(1L, captor.getAllValues().get(0).get(0).getEntryId());
        assertEquals(2L, captor.getAllValues().get(1).get(0).getEntryId());
    }
}

