package com.jpdictionary.dictionary.service.search;

import com.jpdictionary.config.MeilisearchProperties;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.service.deinflect.JapaneseDeinflector;
import com.jpdictionary.dictionary.service.search.meilisearch.MeilisearchClient;
import com.jpdictionary.dictionary.service.search.meilisearch.MeilisearchSearchRequest;
import com.jpdictionary.dictionary.service.search.meilisearch.MeilisearchSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeilisearchDictionarySearchBackendTest {

    @Mock
    private MeilisearchClient meilisearchClient;

    @Mock
    private MySqlDictionarySearchBackend mySqlFallback;

    private MeilisearchProperties properties;
    private MeilisearchDictionarySearchBackend backend;

    @BeforeEach
    void setUp() {
        properties = new MeilisearchProperties();
        properties.setFallbackOnError(true);
        backend = new MeilisearchDictionarySearchBackend(
                meilisearchClient,
                properties,
                mySqlFallback,
                new JapaneseDeinflector()
        );
    }

    @Test
    @DisplayName("Should merge multi-search hits prioritizing high-confidence primary, then deinflected roots")
    void testSearchEntryIdsMultiSearchPriority() {
        MeilisearchSearchResponse primary = new MeilisearchSearchResponse(
                List.of(
                        MeilisearchSearchResponse.Hit.builder().id(10L).rankingScore(0.95).build(),
                        MeilisearchSearchResponse.Hit.builder().id(99L).rankingScore(0.45).build()
                ),
                3L
        );
        MeilisearchSearchResponse rootMatch = new MeilisearchSearchResponse(
                List.of(
                        MeilisearchSearchResponse.Hit.builder().id(1L).rankingScore(0.98).build()
                ),
                2L
        );
        when(meilisearchClient.multiSearch(anyList()))
                .thenReturn(new MeilisearchSearchResponse.MultiSearchResponse(List.of(primary, rootMatch)));

        List<Long> ids = backend.searchEntryIds("食べた", (byte) 5, PosCategory.VERB, 20);
        assertEquals(List.of(10L, 1L, 99L), ids);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MeilisearchSearchRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(meilisearchClient).multiSearch(captor.capture());
        assertEquals("jlptLevel = 5 AND posCategories = \"VERB\"", captor.getValue().get(0).getFilter());
        assertEquals("食べる", captor.getValue().get(1).getQ());
    }

    @Test
    @DisplayName("Should sort filter-only queries by frequencyRank asc and jlptLevel desc")
    void testSearchEntryIdsFilterOnly() {
        when(meilisearchClient.search(any(MeilisearchSearchRequest.class)))
                .thenReturn(new MeilisearchSearchResponse(
                        List.of(MeilisearchSearchResponse.Hit.builder().id(5L).build()),
                        1L
                ));

        List<Long> ids = backend.searchEntryIds("", (byte) 5, PosCategory.ALL, 20);
        assertEquals(List.of(5L), ids);

        ArgumentCaptor<MeilisearchSearchRequest> captor = ArgumentCaptor.forClass(MeilisearchSearchRequest.class);
        verify(meilisearchClient).search(captor.capture());
        assertEquals("jlptLevel = 5", captor.getValue().getFilter());
        assertEquals(List.of("frequencyRank:asc", "jlptLevel:desc"), captor.getValue().getSort());
    }

    @Test
    @DisplayName("Should map suggestions directly from Meilisearch response without MySQL")
    void testGetSuggestionsFromMeilisearch() {
        when(meilisearchClient.search(any(MeilisearchSearchRequest.class)))
                .thenReturn(new MeilisearchSearchResponse(
                        List.of(MeilisearchSearchResponse.Hit.builder()
                                .id(28L)
                                .primaryForm("木漏れ日")
                                .primaryReading("こもれび")
                                .primaryRomaji("komorebi")
                                .primaryMeaning("Ánh nắng lọt qua kẽ lá")
                                .jlptLevel((byte) 2)
                                .build()),
                        2L
                ));

        List<SuggestionDto> suggestions = backend.getSuggestions("komroebi", 8);
        assertEquals(1, suggestions.size());
        assertEquals(28L, suggestions.get(0).getId());
        assertEquals("木漏れ日", suggestions.get(0).getTerm());
        assertEquals("N2", suggestions.get(0).getJlpt());
    }

    @Test
    @DisplayName("Should fall back to MySQL backend on Meilisearch error when fallbackOnError=true")
    void testFallbackToMySqlOnError() {
        when(meilisearchClient.search(any(MeilisearchSearchRequest.class)))
                .thenThrow(new IllegalStateException("Connection refused"));
        when(mySqlFallback.searchEntryIds("komorebi", null, PosCategory.ALL, 20))
                .thenReturn(List.of(28L));
        when(mySqlFallback.getSuggestions("komorebi", 8))
                .thenReturn(List.of(SuggestionDto.builder().id(28L).term("木漏れ日").build()));

        assertEquals(List.of(28L), backend.searchEntryIds("komorebi", null, PosCategory.ALL, 20));
        assertEquals(1, backend.getSuggestions("komorebi", 8).size());

        properties.setFallbackOnError(false);
        assertThrows(IllegalStateException.class,
                () -> backend.searchEntryIds("komorebi", null, PosCategory.ALL, 20));
    }
}

