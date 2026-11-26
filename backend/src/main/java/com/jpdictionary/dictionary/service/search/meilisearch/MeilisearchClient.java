package com.jpdictionary.dictionary.service.search.meilisearch;

import com.jpdictionary.config.MeilisearchProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Lightweight HTTP client for Meilisearch REST API built on Spring RestClient
 * without adding third-party transitive dependencies.
 */
@Component
@Slf4j
public class MeilisearchClient {

    private final MeilisearchProperties properties;
    private final RestClient restClient;

    @Autowired
    public MeilisearchClient(MeilisearchProperties properties, RestClient.Builder restClientBuilder) {
        this(properties, restClientBuilder, true);
    }

    private MeilisearchClient(
            MeilisearchProperties properties,
            RestClient.Builder restClientBuilder,
            boolean configureFactory
    ) {
        this.properties = properties;

        RestClient.Builder builder = restClientBuilder.baseUrl(properties.getBaseUrl());
        if (configureFactory) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()));
            factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
            builder.requestFactory(factory);
        }

        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + properties.getApiKey().trim());
        }

        this.restClient = builder.build();
    }

    /**
     * Package-private factory preserving a MockRestServiceServer-bound RestClient.Builder in tests.
     */
    static MeilisearchClient withMockBuilder(
            MeilisearchProperties properties,
            RestClient.Builder mockBoundBuilder
    ) {
        return new MeilisearchClient(properties, mockBoundBuilder, false);
    }

    public void ensureIndexAndSettings() {
        String indexUid = properties.getIndexName();
        ensureIndexExists(indexUid);

        Map<String, Object> settings = Map.of(
                "searchableAttributes", List.of(
                        "primaryForm", "primaryReading", "primaryRomaji",
                        "formsText", "readingsText", "romajiText",
                        "meaningsText", "meaningsAscii"
                ),
                "filterableAttributes", List.of("jlptLevel", "posCategories"),
                "sortableAttributes", List.of("frequencyRank", "jlptLevel"),
                "rankingRules", List.of(
                        "words", "typo", "proximity", "attribute", "exactness",
                        "frequencyRank:asc", "jlptLevel:desc", "sort"
                ),
                "localizedAttributes", List.of(
                        Map.of(
                                "locales", List.of("jpn"),
                                "attributePatterns", List.of(
                                        "primaryForm", "primaryReading", "formsText", "readingsText"
                                )
                        ),
                        Map.of(
                                "locales", List.of("vie", "eng"),
                                "attributePatterns", List.of(
                                        "primaryMeaning", "meaningsText", "meaningsAscii"
                                )
                        )
                ),
                "typoTolerance", Map.of(
                        "enabled", true,
                        "minWordSizeForTypos", Map.of("oneTypo", 4, "twoTypos", 8),
                        "disableOnAttributes", List.of("primaryForm")
                )
        );

        restClient.patch()
                .uri("/indexes/{uid}/settings", indexUid)
                .contentType(MediaType.APPLICATION_JSON)
                .body(settings)
                .retrieve()
                .toBodilessEntity();
    }

    private void ensureIndexExists(String indexUid) {
        try {
            restClient.get()
                    .uri("/indexes/{uid}", indexUid)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ex) {
            restClient.post()
                    .uri("/indexes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("uid", indexUid, "primaryKey", "id"))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Created Meilisearch index '{}'", indexUid);
        }
    }

    public MeilisearchSearchResponse.TaskResponse upsertDocuments(List<MeilisearchDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return null;
        }
        return restClient.put()
                .uri("/indexes/{uid}/documents", properties.getIndexName())
                .contentType(MediaType.APPLICATION_JSON)
                .body(documents)
                .retrieve()
                .body(MeilisearchSearchResponse.TaskResponse.class);
    }

    public MeilisearchSearchResponse.TaskResponse deleteDocuments(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return restClient.post()
                .uri("/indexes/{uid}/documents/delete-batch", properties.getIndexName())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ids)
                .retrieve()
                .body(MeilisearchSearchResponse.TaskResponse.class);
    }

    public MeilisearchSearchResponse search(MeilisearchSearchRequest request) {
        return restClient.post()
                .uri("/indexes/{uid}/search", properties.getIndexName())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MeilisearchSearchResponse.class);
    }

    public MeilisearchSearchResponse.MultiSearchResponse multiSearch(List<MeilisearchSearchRequest> queries) {
        String indexUid = properties.getIndexName();
        for (MeilisearchSearchRequest query : queries) {
            if (query.getIndexUid() == null) {
                query.setIndexUid(indexUid);
            }
        }
        return restClient.post()
                .uri("/multi-search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("queries", queries))
                .retrieve()
                .body(MeilisearchSearchResponse.MultiSearchResponse.class);
    }
}
