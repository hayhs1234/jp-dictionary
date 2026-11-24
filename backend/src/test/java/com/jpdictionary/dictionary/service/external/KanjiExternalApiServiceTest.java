package com.jpdictionary.dictionary.service.external;

import com.jpdictionary.dictionary.entity.KanjiEntry;
import com.jpdictionary.dictionary.repository.KanjiEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KanjiExternalApiServiceTest {

    @Mock
    private KanjiEntryRepository kanjiEntryRepository;

    private KanjiExternalApiService apiService;

    @BeforeEach
    void setUp() {
        apiService = new KanjiExternalApiService(kanjiEntryRepository, "https://kanjiapi.dev/v1/kanji");
    }

    @Test
    @DisplayName("Should return existing kanji if already in database without calling external network")
    void testExistingKanjiReturnsFromDb() {
        KanjiEntry existing = KanjiEntry.builder()
                .id(1L)
                .characterText("木")
                .strokeCount(4)
                .jlptLevel("N5")
                .hanViet("MỘC")
                .meaning("Cây cối")
                .build();

        when(kanjiEntryRepository.findByCharacterText("木")).thenReturn(Optional.of(existing));

        Optional<KanjiEntry> result = apiService.fetchAndCacheKanji("木");
        assertTrue(result.isPresent());
        assertEquals("木", result.get().getCharacterText());
        assertEquals(4, result.get().getStrokeCount());
    }

    @Test
    @DisplayName("Should return empty optional for invalid or null input")
    void testInvalidInputReturnsEmpty() {
        Optional<KanjiEntry> res1 = apiService.fetchAndCacheKanji(null);
        assertFalse(res1.isPresent());

        Optional<KanjiEntry> res2 = apiService.fetchAndCacheKanji("   ");
        assertFalse(res2.isPresent());
    }
}

