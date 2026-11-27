package com.jpdictionary.dictionary.service.search;

import com.jpdictionary.auth.repository.UserRepository;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.DictionaryFormRepository;
import com.jpdictionary.dictionary.repository.DictionarySearchIndexRepository;
import com.jpdictionary.dictionary.repository.KanjiEntryRepository;
import com.jpdictionary.dictionary.service.search.meilisearch.MeilisearchClient;
import com.jpdictionary.dictionary.service.search.meilisearch.MeilisearchSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "app.search.engine=meilisearch",
        "app.search-index.sync-on-startup=false"
})
@ActiveProfiles("test")
class MeilisearchContextWiringTest {

    @Autowired
    private DictionarySearchBackend searchBackend;

    @Autowired
    private MeilisearchSyncService syncService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private MeilisearchClient meilisearchClient;

    @MockitoBean
    private DictionaryEntryRepository entryRepository;

    @MockitoBean
    private DictionaryFormRepository formRepository;

    @MockitoBean
    private DictionarySearchIndexRepository searchIndexRepository;

    @MockitoBean
    private KanjiEntryRepository kanjiEntryRepository;

    @Test
    @DisplayName("Should wire MeilisearchDictionarySearchBackend as primary when app.search.engine=meilisearch")
    void testMeilisearchBackendIsWiredAsPrimary() {
        assertInstanceOf(MeilisearchDictionarySearchBackend.class, searchBackend);
        assertNotNull(syncService);
    }
}

