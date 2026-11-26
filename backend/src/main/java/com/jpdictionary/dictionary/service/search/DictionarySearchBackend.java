package com.jpdictionary.dictionary.service.search;

import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.enums.PosCategory;

import java.util.List;

/**
 * Strategy interface for dictionary search and autocomplete backends
 * (e.g. MySQL flattened index vs Meilisearch).
 */
public interface DictionarySearchBackend {

    List<Long> searchEntryIds(String query, Byte jlptLevel, PosCategory category, int limit);

    List<SuggestionDto> getSuggestions(String query, int limit);
}

