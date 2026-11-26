package com.jpdictionary.dictionary.service.search.meilisearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Search document indexed in Meilisearch, projected directly from dictionary_search_index.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeilisearchDocument {
    private Long id;
    private Byte jlptLevel;
    private List<String> posCategories;
    private Integer frequencyRank;
    private String primaryForm;
    private String primaryReading;
    private String primaryRomaji;
    private String primaryMeaning;
    private String formsText;
    private String readingsText;
    private String romajiText;
    private String meaningsText;
    private String meaningsAscii;
}

