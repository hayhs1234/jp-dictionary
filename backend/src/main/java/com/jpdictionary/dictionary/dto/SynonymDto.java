package com.jpdictionary.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SynonymDto {
    private String word;
    private String reading;
    private String meaning;
    private String type; // "synonym" or "grammar"
}

