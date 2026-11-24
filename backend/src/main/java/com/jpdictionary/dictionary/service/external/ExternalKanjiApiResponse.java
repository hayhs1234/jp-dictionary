package com.jpdictionary.dictionary.service.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalKanjiApiResponse {

    private String kanji;
    private Integer grade;

    @JsonProperty("stroke_count")
    private Integer strokeCount;

    private List<String> meanings;

    @JsonProperty("kun_readings")
    private List<String> kunReadings;

    @JsonProperty("on_readings")
    private List<String> onReadings;

    @JsonProperty("name_readings")
    private List<String> nameReadings;

    private Integer jlpt;
    private String unicode;

    @JsonProperty("heisig_en")
    private String heisigEn;
}

