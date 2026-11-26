package com.jpdictionary.dictionary.service.search.meilisearch;

import com.jpdictionary.config.CacheConfig;
import com.jpdictionary.config.MeilisearchProperties;
import com.jpdictionary.dictionary.entity.DictionarySearchIndex;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.repository.DictionarySearchIndexRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeilisearchSyncServiceTest {

    @Mock
    private DictionarySearchIndexRepository searchIndexRepository;

    @Mock
    private MeilisearchClient meilisearchClient;

    @Mock
    private ObjectProvider<CacheManager> cacheManagerProvider;

    @Mock
    private ObjectProvider<TransactionTemplate> transactionTemplateProvider;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache searchCache;

    @Mock
    private Cache suggestCache;

    private MeilisearchSyncService syncService;

    @BeforeEach
    void setUp() {
        MeilisearchProperties properties = new MeilisearchProperties();
        properties.setSyncBatchSize(100);
        syncService = new MeilisearchSyncService(
                searchIndexRepository,
                meilisearchClient,
                properties,
                cacheManagerProvider,
                transactionTemplateProvider
        );
    }

    @Test
    @DisplayName("Should sync pending rows in batch, decode posMask, mark synced, and evict search caches")
    void testSyncPendingToMeilisearch() {
        DictionarySearchIndex row = DictionarySearchIndex.builder()
                .entryId(21L)
                .jlptLevel((byte) 3)
                .posMask(PosCategory.NOUN.getBitMask() | PosCategory.VERB.getBitMask())
                .frequencyRank(null)
                .primaryForm("成功")
                .primaryReading("せいこう")
                .primaryRomaji("seikou")
                .primaryMeaning("thành công")
                .formsText("成功")
                .readingsText("せいこう")
                .romajiText("seikou")
                .meaningsText("thành công")
                .meaningsAscii("thanh cong")
                .meiliSynced(false)
                .build();

        when(searchIndexRepository.findByMeiliSyncedFalseOrderByEntryIdAsc(any(Pageable.class)))
                .thenReturn(List.of(row));
        when(cacheManagerProvider.getIfAvailable()).thenReturn(cacheManager);
        when(cacheManager.getCache(CacheConfig.WORD_SEARCH)).thenReturn(searchCache);
        when(cacheManager.getCache(CacheConfig.WORD_SUGGEST)).thenReturn(suggestCache);

        int count = syncService.syncPendingToMeilisearch();
        assertEquals(1, count);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MeilisearchDocument>> docsCaptor = ArgumentCaptor.forClass(List.class);
        verify(meilisearchClient).upsertDocuments(docsCaptor.capture());

        MeilisearchDocument doc = docsCaptor.getValue().get(0);
        assertEquals(21L, doc.getId());
        assertEquals(List.of("NOUN", "VERB"), doc.getPosCategories());
        assertEquals(999_999, doc.getFrequencyRank());
        assertEquals("thanh cong", doc.getMeaningsAscii());

        verify(searchIndexRepository).markMeiliSynced(eq(List.of(21L)), any(LocalDateTime.class));
        verify(searchCache).clear();
        verify(suggestCache).clear();
    }

    @Test
    @DisplayName("Should not evict caches when no pending documents exist")
    void testSyncDoesNotEvictWhenNothingPending() {
        when(searchIndexRepository.findByMeiliSyncedFalseOrderByEntryIdAsc(any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        int count = syncService.syncPendingToMeilisearch();
        assertEquals(0, count);
        verify(meilisearchClient, never()).upsertDocuments(any());
        verify(cacheManagerProvider, never()).getIfAvailable();
    }
}

