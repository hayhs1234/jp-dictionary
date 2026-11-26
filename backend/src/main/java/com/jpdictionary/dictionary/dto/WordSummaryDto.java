package com.jpdictionary.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight dictionary search result projection used for search listings
 * without loading heavy detail collections (examples, synonyms, kanji breakdown).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordSummaryDto {
    private Long id;
    private String kanji;
    private String hiragana;
    private String romaji;
    private String hanViet;
    private String pitchAccent;
    private String jlptLevel;
    private String partOfSpeech;
    private String primaryMeaning;
    private int senseCount;

    @Builder.Default
    private List<FuriganaSegmentDto> furiganaSegments = new ArrayList<>();
}

