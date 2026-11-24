package com.jpdictionary.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionDto {
    private Long id;
    private String term;
    private String reading;
    private String romaji;
    private String meaning;
    private String jlpt;
}

