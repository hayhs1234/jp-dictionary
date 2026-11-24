package com.jpdictionary.dictionary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleSentenceDto {
    private Long id;
    private String japaneseText;
    private String reading;
    private List<FuriganaSegmentDto> furiganaSegments;
    private String translation;
    private String context;
    private String level;
}

