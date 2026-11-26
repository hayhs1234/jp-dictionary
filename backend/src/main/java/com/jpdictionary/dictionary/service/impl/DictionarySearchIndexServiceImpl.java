package com.jpdictionary.dictionary.service.impl;

import com.jpdictionary.config.CacheConfig;
import com.jpdictionary.dictionary.entity.DictionaryEntry;
import com.jpdictionary.dictionary.entity.DictionaryForm;
import com.jpdictionary.dictionary.entity.DictionaryReading;
import com.jpdictionary.dictionary.entity.DictionarySearchIndex;
import com.jpdictionary.dictionary.entity.DictionarySense;
import com.jpdictionary.dictionary.entity.SenseGloss;
import com.jpdictionary.dictionary.enums.PosCategory;
import com.jpdictionary.dictionary.repository.DictionaryEntryRepository;
import com.jpdictionary.dictionary.repository.DictionarySearchIndexRepository;
import com.jpdictionary.dictionary.service.DictionarySearchIndexService;
import com.jpdictionary.dictionary.service.util.VietnameseTextNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DictionarySearchIndexServiceImpl implements DictionarySearchIndexService {

    private static final int BATCH_SIZE = 500;
    private static final int MAX_PRIMARY_MEANING_LEN = 500;

    private final DictionaryEntryRepository entryRepository;
    private final DictionarySearchIndexRepository searchIndexRepository;

    @Override
    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.WORD_SEARCH, CacheConfig.WORD_SUGGEST}, allEntries = true)
    public void upsertEntryIndex(Long entryId) {
        if (entryId == null) {
            return;
        }
        upsertBatch(List.of(entryId));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.WORD_SEARCH, CacheConfig.WORD_SUGGEST}, allEntries = true)
    public void upsertBatch(Collection<Long> entryIds) {
        if (entryIds == null || entryIds.isEmpty()) {
            return;
        }
        List<DictionaryEntry> entries = entryRepository.findAllByIdIn(entryIds);
        if (entries == null || entries.isEmpty()) {
            return;
        }
        List<DictionarySearchIndex> indexRows = entries.stream()
                .map(this::buildIndexRow)
                .toList();
        searchIndexRepository.saveAll(indexRows);
    }

    @Override
    @Transactional
    public int syncPendingRows() {
        int updatedCount = 0;

        while (true) {
            List<DictionarySearchIndex> pendingAscii =
                    searchIndexRepository.findByAsciiSyncedFalse(PageRequest.of(0, BATCH_SIZE));
            if (pendingAscii == null || pendingAscii.isEmpty()) {
                break;
            }
            for (DictionarySearchIndex row : pendingAscii) {
                row.setMeaningsAscii(VietnameseTextNormalizer.toAsciiFolded(row.getMeaningsText()));
                row.setAsciiSynced(Boolean.TRUE);
                row.setMeiliSynced(Boolean.FALSE);
                row.setMeiliSyncedAt(null);
            }
            searchIndexRepository.saveAll(pendingAscii);
            updatedCount += pendingAscii.size();
        }

        while (true) {
            List<Long> missingIds = searchIndexRepository.findUnindexedEntryIds(PageRequest.of(0, BATCH_SIZE));
            if (missingIds == null || missingIds.isEmpty()) {
                break;
            }
            upsertBatch(missingIds);
            updatedCount += missingIds.size();
        }

        if (updatedCount > 0) {
            log.info("Synchronized {} rows in dictionary_search_index", updatedCount);
        }
        return updatedCount;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.WORD_SEARCH, CacheConfig.WORD_SUGGEST}, allEntries = true)
    public int reindexAll() {
        int totalIndexed = 0;
        int page = 0;
        while (true) {
            Page<DictionaryEntry> batch = entryRepository.findAll(
                    PageRequest.of(page, BATCH_SIZE, Sort.by("id").ascending()));
            if (batch == null || batch.isEmpty()) {
                break;
            }
            List<DictionarySearchIndex> indexRows = batch.getContent().stream()
                    .map(this::buildIndexRow)
                    .toList();
            searchIndexRepository.saveAll(indexRows);
            totalIndexed += indexRows.size();
            if (!batch.hasNext()) {
                break;
            }
            page++;
        }
        log.info("Rebuilt dictionary_search_index for {} entries", totalIndexed);
        return totalIndexed;
    }

    DictionarySearchIndex buildIndexRow(DictionaryEntry entry) {
        String primaryForm = resolvePrimaryForm(entry);
        DictionaryReading primaryReadingObj = resolvePrimaryReading(entry);
        String primaryReading = primaryReadingObj != null && primaryReadingObj.getReading() != null
                ? primaryReadingObj.getReading() : "";
        String primaryRomaji = primaryReadingObj != null && primaryReadingObj.getRomaji() != null
                ? primaryReadingObj.getRomaji() : "";

        String formsText = joinUniqueValues(entry.getForms(), DictionaryForm::getWrittenForm);
        String readingsText = joinUniqueValues(entry.getReadings(), DictionaryReading::getReading);
        String romajiText = joinUniqueValues(entry.getReadings(), DictionaryReading::getRomaji);
        String primaryMeaning = resolvePrimaryMeaning(entry.getSenses());
        String meaningsText = joinAllMeanings(entry.getSenses());
        String meaningsAscii = VietnameseTextNormalizer.toAsciiFolded(meaningsText);
        int posMask = computePosMask(entry.getSenses());

        return DictionarySearchIndex.builder()
                .entryId(entry.getId())
                .jlptLevel(entry.getJlptLevel())
                .posMask(posMask)
                .frequencyRank(entry.getFrequencyRank())
                .primaryForm(primaryForm)
                .primaryReading(primaryReading)
                .primaryRomaji(primaryRomaji)
                .primaryMeaning(truncate(primaryMeaning, MAX_PRIMARY_MEANING_LEN))
                .formsText(formsText)
                .readingsText(readingsText)
                .romajiText(romajiText)
                .meaningsText(meaningsText)
                .meaningsAscii(meaningsAscii)
                .asciiSynced(Boolean.TRUE)
                .meiliSynced(Boolean.FALSE)
                .meiliSyncedAt(null)
                .build();
    }

    private String resolvePrimaryForm(DictionaryEntry entry) {
        if (entry.getForms() == null || entry.getForms().isEmpty()) {
            return "";
        }
        return entry.getForms().stream()
                .filter(f -> Boolean.TRUE.equals(f.getIsPrimary()))
                .map(DictionaryForm::getWrittenForm)
                .findFirst()
                .orElse(entry.getForms().get(0).getWrittenForm());
    }

    private DictionaryReading resolvePrimaryReading(DictionaryEntry entry) {
        if (entry.getReadings() == null || entry.getReadings().isEmpty()) {
            return null;
        }
        return entry.getReadings().stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsPrimary()))
                .findFirst()
                .orElse(entry.getReadings().get(0));
    }

    private <T> String joinUniqueValues(List<T> items, Function<T, String> extractor) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.stream()
                .map(extractor)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .collect(Collectors.joining(" "));
    }

    private String resolvePrimaryMeaning(List<DictionarySense> senses) {
        if (senses == null || senses.isEmpty()) {
            return "";
        }
        List<SenseGloss> firstGlosses = senses.get(0).getGlosses();
        if (firstGlosses == null || firstGlosses.isEmpty()) {
            return "";
        }
        List<String> vi = extractMeaningsByLang(firstGlosses, "vi");
        if (!vi.isEmpty()) {
            return String.join("; ", vi);
        }
        List<String> en = extractMeaningsByLang(firstGlosses, "en");
        if (!en.isEmpty()) {
            return String.join("; ", en);
        }
        return firstGlosses.get(0).getMeaning() != null ? firstGlosses.get(0).getMeaning() : "";
    }

    private List<String> extractMeaningsByLang(List<SenseGloss> glosses, String lang) {
        List<String> list = new ArrayList<>();
        for (SenseGloss gloss : glosses) {
            if (lang.equalsIgnoreCase(gloss.getLanguageCode()) && gloss.getMeaning() != null) {
                list.add(gloss.getMeaning());
            }
        }
        return list;
    }

    private String joinAllMeanings(List<DictionarySense> senses) {
        if (senses == null || senses.isEmpty()) {
            return "";
        }
        Set<String> all = new LinkedHashSet<>();
        for (DictionarySense sense : senses) {
            if (sense.getGlosses() == null) {
                continue;
            }
            for (SenseGloss gloss : sense.getGlosses()) {
                if (gloss.getMeaning() != null && !gloss.getMeaning().isBlank()) {
                    all.add(gloss.getMeaning().trim());
                }
            }
        }
        return String.join("; ", all);
    }

    private int computePosMask(List<DictionarySense> senses) {
        if (senses == null || senses.isEmpty()) {
            return 0;
        }
        int mask = 0;
        for (DictionarySense sense : senses) {
            Set<PosCategory> cats = (sense.getPosCategories() != null && !sense.getPosCategories().isEmpty())
                    ? sense.getPosCategories()
                    : Collections.emptySet();
            if (cats.isEmpty() && sense.getPartOfSpeech() != null) {
                cats = PosCategory.inferCategorySet(sense.getPartOfSpeech());
            }
            mask |= PosCategory.toBitMask(cats);
        }
        return mask;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLen ? text : text.substring(0, maxLen);
    }
}
