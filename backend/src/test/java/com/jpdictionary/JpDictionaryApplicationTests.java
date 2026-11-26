package com.jpdictionary;

import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.DictionaryFormRepository;
import com.jpdictionary.dictionary.repository.DictionarySearchIndexRepository;
import com.jpdictionary.dictionary.repository.KanjiEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class JpDictionaryApplicationTests {

    @MockitoBean
    private DictionaryEntryRepository dictionaryEntryRepository;

    @MockitoBean
    private DictionaryFormRepository dictionaryFormRepository;

    @MockitoBean
    private DictionarySearchIndexRepository dictionarySearchIndexRepository;

    @MockitoBean
    private KanjiEntryRepository kanjiEntryRepository;

    @Test
    void contextLoads() {
    }
}
