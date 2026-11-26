package com.jpdictionary.dictionary.service.search;

import com.jpdictionary.config.MeilisearchProperties;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.service.deinflect.JapaneseDeinflector;
import com.jpdictionary.dictionary.service.search.meilisearch.MeilisearchClient;
import com.jpdictionary.dictionary.service.search.meilisearch.MeilisearchSearchRequest;
import com.jpdictionary.dictionary.service.search.meilisearch.MeilisearchSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Meilisearch-backed search strategy supporting typo tolerance, Japanese tokenization,
 * multi-search deinflection, and transparent fallback to MySQL on errors.
 */
@Component
@Primary
@ConditionalOnProperty(name = "app.search.engine", havingValue = "meilisearch")
@RequiredArgsConstructor
@Slf4j
public class MeilisearchDictionarySearchBackend implements DictionarySearchBackend {

    private static final double HIGH_CONFIDENCE_SCORE = 0.85;
    private static final int MAX_DEINFLECT_QUERIES = 6;
    private static final List<String> SUGGESTION_ATTRIBUTES = List.of(
            "id", "primaryForm", "primaryReading", "primaryRomaji", "primaryMeaning", "jlptLevel"
    );

    private final MeilisearchClient meilisearchClient;
    private final MeilisearchProperties properties;
    private final MySqlDictionarySearchBackend mySqlFallback;
    private final JapaneseDeinflector deinflector;

    @Override
    public List<Long> searchEntryIds(String query, Byte jlptLevel, PosCategory category, int limit) {
        String cleanQuery = (query != null) ? query.trim() : "";
        try {
            return executeMeilisearchEntryIdSearch(cleanQuery, jlptLevel, category, limit);
        } catch (Exception ex) {
            if (properties.isFallbackOnError()) {
                log.warn("Meilisearch search failed for query '{}', falling back to MySQL: {}",
                        cleanQuery, ex.getMessage());
                return mySqlFallback.searchEntryIds(query, jlptLevel, category, limit);
            }
            throw ex;
        }
    }

    @Override
    public List<SuggestionDto> getSuggestions(String query, int limit) {
        String cleanQuery = (query != null) ? query.trim() : "";
        if (cleanQuery.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            MeilisearchSearchRequest request = MeilisearchSearchRequest.builder()
                    .q(cleanQuery)
                    .limit(limit)
                    .attributesToRetrieve(SUGGESTION_ATTRIBUTES)
                    .build();
            MeilisearchSearchResponse response = meilisearchClient.search(request);
            return mapHitsToSuggestions(response, limit);
        } catch (Exception ex) {
            if (properties.isFallbackOnError()) {
                log.warn("Meilisearch suggest failed for query '{}', falling back to MySQL: {}",
                        cleanQuery, ex.getMessage());
                return mySqlFallback.getSuggestions(query, limit);
            }
            throw ex;
        }
    }

    private List<Long> executeMeilisearchEntryIdSearch(
            String cleanQuery, Byte jlptLevel, PosCategory category, int limit) {
        String filter = buildFilterExpression(jlptLevel, category);
        if (cleanQuery.isEmpty()) {
            MeilisearchSearchRequest filterRequest = MeilisearchSearchRequest.builder()
                    .q("")
                    .filter(filter)
                    .sort(List.of("frequencyRank:asc", "jlptLevel:desc"))
                    .limit(limit)
                    .attributesToRetrieve(List.of("id"))
                    .build();
            return extractIds(meilisearchClient.search(filterRequest), limit);
        }

        List<String> roots = collectLimitedRoots(cleanQuery);
        if (roots.isEmpty()) {
            MeilisearchSearchRequest request = MeilisearchSearchRequest.builder()
                    .q(cleanQuery)
                    .filter(filter)
                    .limit(limit)
                    .attributesToRetrieve(List.of("id"))
                    .showRankingScore(Boolean.TRUE)
                    .build();
            return extractIds(meilisearchClient.search(request), limit);
        }

        List<MeilisearchSearchRequest> queries = buildMultiSearchQueries(cleanQuery, roots, filter, limit);
        MeilisearchSearchResponse.MultiSearchResponse multiResponse = meilisearchClient.multiSearch(queries);
        return mergeMultiSearchIds(multiResponse, limit);
    }

    private List<String> collectLimitedRoots(String query) {
        if (deinflector == null) {
            return List.of();
        }
        List<String> roots = deinflector.deinflect(query);
        if (roots == null || roots.isEmpty()) {
            return List.of();
        }
        return roots.size() <= MAX_DEINFLECT_QUERIES ? roots : roots.subList(0, MAX_DEINFLECT_QUERIES);
    }

    private List<MeilisearchSearchRequest> buildMultiSearchQueries(
            String primaryQuery, List<String> roots, String filter, int limit) {
        List<MeilisearchSearchRequest> queries = new ArrayList<>(1 + roots.size());
        queries.add(MeilisearchSearchRequest.builder()
                .q(primaryQuery)
                .filter(filter)
                .limit(limit)
                .attributesToRetrieve(List.of("id"))
                .showRankingScore(Boolean.TRUE)
                .build());

        for (String root : roots) {
            queries.add(MeilisearchSearchRequest.builder()
                    .q(root)
                    .filter(filter)
                    .limit(limit)
                    .matchingStrategy("all")
                    .attributesToRetrieve(List.of("id"))
                    .showRankingScore(Boolean.TRUE)
                    .build());
        }
        return queries;
    }

    private List<Long> mergeMultiSearchIds(
            MeilisearchSearchResponse.MultiSearchResponse multiResponse, int limit) {
        if (multiResponse == null || multiResponse.getResults() == null || multiResponse.getResults().isEmpty()) {
            return Collections.emptyList();
        }
        List<MeilisearchSearchResponse> results = multiResponse.getResults();
        MeilisearchSearchResponse primary = results.get(0);
        Set<Long> orderedIds = new LinkedHashSet<>();

        // 1. High-confidence exact/prefix hits for the original query
        addHitsByScoreThreshold(orderedIds, primary, HIGH_CONFIDENCE_SCORE, true, limit);

        // 2. De-inflected dictionary root matches
        for (int i = 1; i < results.size() && orderedIds.size() < limit; i++) {
            addHitsByScoreThreshold(orderedIds, results.get(i), 0.0, true, limit);
        }

        // 3. Remaining lower-score hits (typos / meanings) for the original query
        addHitsByScoreThreshold(orderedIds, primary, HIGH_CONFIDENCE_SCORE, false, limit);

        return new ArrayList<>(orderedIds);
    }

    private void addHitsByScoreThreshold(
            Set<Long> target,
            MeilisearchSearchResponse response,
            double threshold,
            boolean atLeast,
            int limit
    ) {
        if (response == null || response.getHits() == null || target.size() >= limit) {
            return;
        }
        for (MeilisearchSearchResponse.Hit hit : response.getHits()) {
            if (isMatchingHit(hit, threshold, atLeast) && target.add(hit.getId()) && target.size() >= limit) {
                return;
            }
        }
    }

    private boolean isMatchingHit(MeilisearchSearchResponse.Hit hit, double threshold, boolean atLeast) {
        if (hit == null || hit.getId() == null) {
            return false;
        }
        double score = hit.getRankingScore() != null ? hit.getRankingScore() : 1.0;
        return atLeast ? (score >= threshold) : (score < threshold);
    }

    private List<Long> extractIds(MeilisearchSearchResponse response, int limit) {
        if (response == null || response.getHits() == null) {
            return Collections.emptyList();
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (MeilisearchSearchResponse.Hit hit : response.getHits()) {
            if (hit != null && hit.getId() != null) {
                ids.add(hit.getId());
                if (ids.size() >= limit) {
                    break;
                }
            }
        }
        return new ArrayList<>(ids);
    }

    private List<SuggestionDto> mapHitsToSuggestions(MeilisearchSearchResponse response, int limit) {
        if (response == null || response.getHits() == null) {
            return Collections.emptyList();
        }
        List<SuggestionDto> suggestions = new ArrayList<>();
        for (MeilisearchSearchResponse.Hit hit : response.getHits()) {
            if (hit == null || hit.getId() == null) {
                continue;
            }
            suggestions.add(SuggestionDto.builder()
                    .id(hit.getId())
                    .term(hit.getPrimaryForm() != null ? hit.getPrimaryForm() : "")
                    .reading(hit.getPrimaryReading())
                    .romaji(hit.getPrimaryRomaji())
                    .meaning(hit.getPrimaryMeaning())
                    .jlpt(hit.getJlptLevel() != null ? "N" + hit.getJlptLevel() : null)
                    .build());
            if (suggestions.size() >= limit) {
                break;
            }
        }
        return suggestions;
    }

    private String buildFilterExpression(Byte jlptLevel, PosCategory category) {
        List<String> clauses = new ArrayList<>(2);
        if (jlptLevel != null) {
            clauses.add("jlptLevel = " + jlptLevel);
        }
        if (category != null && category != PosCategory.ALL) {
            clauses.add("posCategories = \"" + category.name() + "\"");
        }
        return clauses.isEmpty() ? null : String.join(" AND ", clauses);
    }
}

