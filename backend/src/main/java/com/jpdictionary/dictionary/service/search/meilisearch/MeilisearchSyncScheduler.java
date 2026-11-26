package com.jpdictionary.dictionary.service.search.meilisearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Ensures Meilisearch settings and pending document sync run safely after
 * DictionarySearchIndexInitializer (which carries default lowest-precedence order).
 */
@Component
@ConditionalOnProperty(name = "app.search.engine", havingValue = "meilisearch")
@Order(20)
@RequiredArgsConstructor
@Slf4j
public class MeilisearchSyncScheduler implements ApplicationRunner {

    private final MeilisearchSyncService syncService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            syncService.initializeIndexSettings();
            syncService.syncPendingToMeilisearch();
        } catch (Exception ex) {
            log.warn("Deferred Meilisearch initial sync (will retry on schedule): {}", ex.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${app.search.meilisearch.sync-interval-ms:30000}")
    public void scheduledSync() {
        try {
            syncService.syncPendingToMeilisearch();
        } catch (Exception ex) {
            log.warn("Scheduled Meilisearch sync failed (will retry): {}", ex.getMessage());
        }
    }
}
