package com.jpdictionary.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanjiBreakdownDto {
    private String character;
    private Integer strokeCount;
    private String jlptLevel;
    private String hanViet;
    private String onReadings;
    private String kunReadings;
    private String meaning;
}

