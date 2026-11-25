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
public class WordSenseDto {
    private Integer senseOrder;
    private String partOfSpeech;

    @Builder.Default
    private List<String> posCategories = new ArrayList<>();

    private String meaning;
    private String detailedMeaning;

    @Builder.Default
    private List<String> glosses = new ArrayList<>();

    @Builder.Default
    private List<String> englishGlosses = new ArrayList<>();
}

