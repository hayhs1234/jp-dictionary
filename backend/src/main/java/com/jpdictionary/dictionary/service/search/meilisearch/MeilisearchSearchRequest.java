package com.jpdictionary.dictionary.service.search.meilisearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeilisearchSearchRequest {
    private String indexUid;
    private String q;
    private String filter;
    private List<String> sort;
    private Integer limit;
    private String matchingStrategy;
    private List<String> attributesToRetrieve;
    private Boolean showRankingScore;
}

