package com.jpdictionary.dictionary.controller;

import com.jpdictionary.common.response.ApiResponse;
import com.jpdictionary.dictionary.dto.KanjiBreakdownDto;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.dto.WordDetailDto;
import com.jpdictionary.dictionary.dto.WordSummaryDto;
import com.jpdictionary.dictionary.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<WordSummaryDto>>> search(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "jlpt", required = false) String jlpt,
            @RequestParam(name = "type", required = false) String type
    ) {
        List<WordSummaryDto> results = dictionaryService.search(query, jlpt, type);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/words/{id}")
    public ResponseEntity<ApiResponse<WordDetailDto>> getWordDetail(@PathVariable("id") Long id) {
        WordDetailDto word = dictionaryService.getWordDetail(id);
        return ResponseEntity.ok(ApiResponse.success(word));
    }

    @GetMapping("/suggest")
    public ResponseEntity<ApiResponse<List<SuggestionDto>>> getSuggestions(
            @RequestParam(name = "q", defaultValue = "") String query
    ) {
        List<SuggestionDto> suggestions = dictionaryService.getSuggestions(query);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    @GetMapping("/kanji/{character}")
    public ResponseEntity<ApiResponse<KanjiBreakdownDto>> getKanjiDetail(
            @PathVariable("character") String character
    ) {
        KanjiBreakdownDto kanji = dictionaryService.getKanjiDetail(character);
        return ResponseEntity.ok(ApiResponse.success(kanji));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<KanjiBreakdownDto>>> getTrendingKanji() {
        List<KanjiBreakdownDto> trending = dictionaryService.getTrendingKanji();
        return ResponseEntity.ok(ApiResponse.success(trending));
    }
}

