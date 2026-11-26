package com.jpdictionary.dictionary.service.search.meilisearch;

import com.jpdictionary.config.CacheConfig;
import com.jpdictionary.config.MeilisearchProperties;
import com.jpdictionary.dictionary.entity.DictionarySearchIndex;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.repository.DictionarySearchIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Synchronizes flattened rows from MySQL dictionary_search_index to Meilisearch
 * in bounded batches using the meili_synced outbox-style tracking flag.
 */
@Service
@ConditionalOnProperty(name = "app.search.engine", havingValue = "meilisearch")
@RequiredArgsConstructor
@Slf4j
public class MeilisearchSyncService {

    private static final int UNRANKED_FREQUENCY_FALLBACK = 999_999;

    private final DictionarySearchIndexRepository searchIndexRepository;
    private final MeilisearchClient meilisearchClient;
    private final MeilisearchProperties properties;
    private final ObjectProvider<CacheManager> cacheManagerProvider;
    private final ObjectProvider<TransactionTemplate> transactionTemplateProvider;

    public void initializeIndexSettings() {
        meilisearchClient.ensureIndexAndSettings();
    }

    public int syncPendingToMeilisearch() {
        int batchSize = Math.max(1, properties.getSyncBatchSize());
        int totalSynced = 0;

        while (true) {
            List<DictionarySearchIndex> batch =
                    searchIndexRepository.findByMeiliSyncedFalseOrderByEntryIdAsc(PageRequest.of(0, batchSize));
            if (batch == null || batch.isEmpty()) {
                break;
            }

            List<MeilisearchDocument> docs = batch.stream().map(this::toDocument).toList();
            meilisearchClient.upsertDocuments(docs);

            List<Long> ids = batch.stream().map(DictionarySearchIndex::getEntryId).toList();
            markBatchSynced(ids, LocalDateTime.now());
            totalSynced += docs.size();

            if (batch.size() < batchSize) {
                break;
            }
        }

        if (totalSynced > 0) {
            evictSearchCaches();
            log.info("Synchronized {} documents to Meilisearch index '{}'", totalSynced, properties.getIndexName());
        }
        return totalSynced;
    }

    public int fullReindexMeilisearch() {
        TransactionTemplate txTemplate = transactionTemplateProvider.getIfAvailable();
        if (txTemplate != null) {
            txTemplate.executeWithoutResult(status -> searchIndexRepository.resetAllMeiliSynced());
        } else {
            searchIndexRepository.resetAllMeiliSynced();
        }
        return syncPendingToMeilisearch();
    }

    MeilisearchDocument toDocument(DictionarySearchIndex row) {
        int posMask = row.getPosMask() != null ? row.getPosMask() : 0;
        Integer freq = row.getFrequencyRank() != null ? row.getFrequencyRank() : UNRANKED_FREQUENCY_FALLBACK;
        return MeilisearchDocument.builder()
                .id(row.getEntryId())
                .jlptLevel(row.getJlptLevel())
                .posCategories(PosCategory.fromBitMask(posMask))
                .frequencyRank(freq)
                .primaryForm(row.getPrimaryForm())
                .primaryReading(row.getPrimaryReading())
                .primaryRomaji(row.getPrimaryRomaji())
                .primaryMeaning(row.getPrimaryMeaning())
                .formsText(row.getFormsText())
                .readingsText(row.getReadingsText())
                .romajiText(row.getRomajiText())
                .meaningsText(row.getMeaningsText())
                .meaningsAscii(row.getMeaningsAscii())
                .build();
    }

    private void markBatchSynced(List<Long> ids, LocalDateTime syncedAt) {
        TransactionTemplate txTemplate = transactionTemplateProvider.getIfAvailable();
        if (txTemplate != null) {
            txTemplate.executeWithoutResult(status -> searchIndexRepository.markMeiliSynced(ids, syncedAt));
        } else {
            searchIndexRepository.markMeiliSynced(ids, syncedAt);
        }
    }

    private void evictSearchCaches() {
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        if (cacheManager == null) {
            return;
        }
        clearCache(cacheManager, CacheConfig.WORD_SEARCH);
        clearCache(cacheManager, CacheConfig.WORD_SUGGEST);
    }

    private void clearCache(CacheManager cacheManager, String name) {
        Cache cache = cacheManager.getCache(name);
        if (cache != null) {
            cache.clear();
        }
    }
}

