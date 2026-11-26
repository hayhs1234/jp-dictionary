package com.jpdictionary.dictionary.service.search.meilisearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MeilisearchSearchResponse {

    private List<Hit> hits;
    private Long processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Hit {
        private Long id;
        private Byte jlptLevel;
        private List<String> posCategories;
        private Integer frequencyRank;
        private String primaryForm;
        private String primaryReading;
        private String primaryRomaji;
        private String primaryMeaning;

        @JsonProperty("_rankingScore")
        private Double rankingScore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MultiSearchResponse {
        private List<MeilisearchSearchResponse> results;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TaskResponse {
        private Long taskUid;
        private String status;
    }
}

