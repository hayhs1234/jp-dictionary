package com.jpdictionary.dictionary.service.search;

import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.DictionarySearchIndexRepository;
import com.jpdictionary.dictionary.service.deinflect.JapaneseDeinflector;
import com.jpdictionary.dictionary.service.util.VietnameseTextNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default MySQL-backed search strategy querying exact/deinflected B-Tree indexes
 * and the flattened dictionary_search_index table (FULLTEXT ngram + bitmask filters).
 */
@Component
@RequiredArgsConstructor
public class MySqlDictionarySearchBackend implements DictionarySearchBackend {

    private final DictionaryEntryRepository entryRepository;
    private final DictionarySearchIndexRepository searchIndexRepository;
    private final JapaneseDeinflector deinflector;

    @Override
    public List<Long> searchEntryIds(String query, Byte jlptLevel, PosCategory category, int limit) {
        String cleanQuery = (query != null) ? query.trim() : "";
        Integer posMask = toPosMask(category);
        if (cleanQuery.isEmpty()) {
            List<Long> filterIds = searchIndexRepository.findIdsByFilters(jlptLevel, posMask, limit);
            return filterIds != null ? filterIds : Collections.emptyList();
        }

        String posName = toPosName(category);
        Set<Long> orderedIds = new LinkedHashSet<>();

        // Tier 1: Exact match via B-Tree on forms & readings (covers secondary forms/readings)
        addIdsUpToLimit(
                orderedIds,
                entryRepository.findExactMatchIdsByTerms(List.of(cleanQuery), jlptLevel, posName, limit),
                limit
        );

        // Tier 2: De-inflection batch lookup
        appendDeinflectedIds(orderedIds, cleanQuery, jlptLevel, posName, limit);

        // Tier 3: Prefix match on flattened dictionary_search_index
        if (orderedIds.size() < limit) {
            String prefixPattern = escapeLikeWildcards(cleanQuery) + "%";
            addIdsUpToLimit(
                    orderedIds,
                    searchIndexRepository.findPrefixMatchIds(prefixPattern, jlptLevel, posMask, limit),
                    limit
            );
        }

        // Tier 4: FULLTEXT (ngram) for >= 2 chars or LIKE fallback for 1-char queries
        if (orderedIds.size() < limit) {
            appendFulltextOrSingleCharIds(orderedIds, cleanQuery, jlptLevel, posMask, limit);
        }

        return new ArrayList<>(orderedIds);
    }

    private Integer toPosMask(PosCategory category) {
        return (category != null && category != PosCategory.ALL) ? category.getBitMask() : null;
    }

    private String toPosName(PosCategory category) {
        return (category != null && category != PosCategory.ALL) ? category.name() : null;
    }

    private void appendDeinflectedIds(
            Set<Long> orderedIds, String cleanQuery, Byte jlptLevel, String posName, int limit) {
        if (orderedIds.size() >= limit || deinflector == null) {
            return;
        }
        List<String> deinflectedRoots = deinflector.deinflect(cleanQuery);
        if (!deinflectedRoots.isEmpty()) {
            addIdsUpToLimit(
                    orderedIds,
                    entryRepository.findExactMatchIdsByTerms(deinflectedRoots, jlptLevel, posName, limit),
                    limit
            );
        }
    }

    @Override
    public List<SuggestionDto> getSuggestions(String query, int limit) {
        String cleanQuery = (query != null) ? query.trim() : "";
        if (cleanQuery.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, SuggestionDto> ordered = new LinkedHashMap<>();
        String prefixPattern = escapeLikeWildcards(cleanQuery) + "%";
        addSuggestionRowsUpToLimit(
                ordered,
                searchIndexRepository.findSuggestionRowsPrefix(prefixPattern, limit),
                limit
        );

        if (ordered.size() < limit) {
            appendFulltextOrSingleCharSuggestions(ordered, cleanQuery, limit);
        }

        return new ArrayList<>(ordered.values());
    }

    private void appendFulltextOrSingleCharIds(
            Set<Long> orderedIds, String query, Byte jlpt, Integer posMask, int limit) {
        int cpLen = VietnameseTextNormalizer.codePointLength(query);
        String booleanQuery = VietnameseTextNormalizer.toBooleanFulltextQuery(query);
        if (cpLen >= 2 && !booleanQuery.isEmpty()) {
            String asciiQuery = VietnameseTextNormalizer.toBooleanFulltextQuery(
                    VietnameseTextNormalizer.toAsciiFolded(query));
            String effectiveAscii = asciiQuery.isEmpty() ? booleanQuery : asciiQuery;
            addIdsUpToLimit(
                    orderedIds,
                    searchIndexRepository.findFulltextMatchIds(booleanQuery, effectiveAscii, jlpt, posMask, limit),
                    limit
            );
            return;
        }

        String containsPattern = "%" + escapeLikeWildcards(query) + "%";
        addIdsUpToLimit(
                orderedIds,
                searchIndexRepository.findSingleCharSubstringIds(containsPattern, jlpt, posMask, limit),
                limit
        );
    }

    private void appendFulltextOrSingleCharSuggestions(
            Map<Long, SuggestionDto> ordered, String cleanQuery, int limit) {
        int cpLen = VietnameseTextNormalizer.codePointLength(cleanQuery);
        String booleanQuery = VietnameseTextNormalizer.toBooleanFulltextQuery(cleanQuery);
        if (cpLen >= 2 && !booleanQuery.isEmpty()) {
            String asciiQuery = VietnameseTextNormalizer.toBooleanFulltextQuery(
                    VietnameseTextNormalizer.toAsciiFolded(cleanQuery));
            String effectiveAscii = asciiQuery.isEmpty() ? booleanQuery : asciiQuery;
            addSuggestionRowsUpToLimit(
                    ordered,
                    searchIndexRepository.findSuggestionRowsFulltext(booleanQuery, effectiveAscii, limit),
                    limit
            );
            return;
        }

        String containsPattern = "%" + escapeLikeWildcards(cleanQuery) + "%";
        addSuggestionRowsUpToLimit(
                ordered,
                searchIndexRepository.findSuggestionRowsSingleChar(containsPattern, limit),
                limit
        );
    }

    private void addIdsUpToLimit(Set<Long> target, List<Long> candidates, int limit) {
        if (candidates == null || candidates.isEmpty() || target.size() >= limit) {
            return;
        }
        for (Long id : candidates) {
            if (id != null) {
                target.add(id);
                if (target.size() >= limit) {
                    return;
                }
            }
        }
    }

    private void addSuggestionRowsUpToLimit(
            Map<Long, SuggestionDto> target, List<Object[]> rows, int limit) {
        if (rows == null || rows.isEmpty() || target.size() >= limit) {
            return;
        }
        for (Object[] row : rows) {
            if (row != null && row[0] instanceof Number idNum) {
                long id = idNum.longValue();
                target.putIfAbsent(id, mapSuggestionRow(id, row));
                if (target.size() >= limit) {
                    return;
                }
            }
        }
    }

    private SuggestionDto mapSuggestionRow(Long id, Object[] row) {
        String form = row[1] != null ? row[1].toString() : "";
        String reading = row[2] != null ? row[2].toString() : null;
        String romaji = row[3] != null ? row[3].toString() : null;
        String meaning = row[4] != null ? row[4].toString() : null;
        String jlpt = (row.length > 5 && row[5] instanceof Number lvl) ? "N" + lvl.intValue() : null;

        return SuggestionDto.builder()
                .id(id)
                .term(form)
                .reading(reading)
                .romaji(romaji)
                .meaning(meaning)
                .jlpt(jlpt)
                .build();
    }

    private String escapeLikeWildcards(String input) {
        return input
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}

