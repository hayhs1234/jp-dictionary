package com.jpdictionary.dictionary.service.impl;

import com.jpdictionary.dictionary.service.DictionarySearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(10)
@ConditionalOnProperty(name = "app.search-index.sync-on-startup", havingValue = "true", matchIfMissing = true)
public class DictionarySearchIndexInitializer implements ApplicationRunner {

    private final DictionarySearchIndexService searchIndexService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            searchIndexService.syncPendingRows();
        } catch (Exception ex) {
            log.warn("Skipped dictionary_search_index startup sync: {}", ex.getMessage());
        }
    }
}

