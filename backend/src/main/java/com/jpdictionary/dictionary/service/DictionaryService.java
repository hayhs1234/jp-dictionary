package com.jpdictionary.dictionary.service;

import com.jpdictionary.dictionary.dto.KanjiBreakdownDto;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.dto.WordDetailDto;
import com.jpdictionary.dictionary.dto.WordSummaryDto;

import java.util.List;

public interface DictionaryService {

    List<WordSummaryDto> search(String query, String jlpt, String type);

    WordDetailDto getWordDetail(Long id);

    List<SuggestionDto> getSuggestions(String query);

    KanjiBreakdownDto getKanjiDetail(String character);

    List<KanjiBreakdownDto> getTrendingKanji();
}

