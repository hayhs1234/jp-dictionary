package com.jpdictionary.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordDetailDto {
    private Long id;
    private String kanji;
    private String hiragana;
    private String romaji;
    private String hanViet;
    private String pitchAccent;
    private String jlptLevel;
    private String partOfSpeech;
    private String primaryMeaning;
    private String detailedMeaning;

    @Builder.Default
    private List<WordSenseDto> senses = new ArrayList<>();

    @Builder.Default
    private List<FuriganaSegmentDto> furiganaSegments = new ArrayList<>();

    @Builder.Default
    private List<KanjiBreakdownDto> kanjiBreakdown = new ArrayList<>();

    @Builder.Default
    private List<ExampleSentenceDto> examples = new ArrayList<>();

    @Builder.Default
    private List<SynonymDto> synonyms = new ArrayList<>();
}

