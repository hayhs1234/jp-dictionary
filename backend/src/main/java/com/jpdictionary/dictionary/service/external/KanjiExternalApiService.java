package com.jpdictionary.dictionary.service.external;

import com.jpdictionary.dictionary.entity.KanjiEntry;
import com.jpdictionary.dictionary.repository.KanjiEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class KanjiExternalApiService {

    private final KanjiEntryRepository kanjiEntryRepository;
    private final RestClient restClient;

    public KanjiExternalApiService(
            KanjiEntryRepository kanjiEntryRepository,
            @Value("${app.kanji-api.base-url:https://kanjiapi.dev/v1/kanji}") String baseUrl
    ) {
        this.kanjiEntryRepository = kanjiEntryRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * Fetch kanji details from external API (e.g. kanjiapi.dev) and cache into local DB.
     *
     * @param character Single kanji character
     * @return Optional containing the cached KanjiEntry if found, empty otherwise
     */
    @Transactional
    public Optional<KanjiEntry> fetchAndCacheKanji(String character) {
        if (character == null || character.trim().isEmpty()) {
            return Optional.empty();
        }

        String ch = character.trim().substring(0, 1);

        // 1. Double check local DB
        Optional<KanjiEntry> existing = kanjiEntryRepository.findByCharacterText(ch);
        if (existing.isPresent()) {
            return existing;
        }

        // 2. Call external API
        return fetchExternalKanji(ch);
    }

    private Optional<KanjiEntry> fetchExternalKanji(String ch) {
        try {
            String encoded = URLEncoder.encode(ch, StandardCharsets.UTF_8);
            ExternalKanjiApiResponse res = restClient.get()
                    .uri("/{char}", encoded)
                    .retrieve()
                    .body(ExternalKanjiApiResponse.class);

            if (res == null || res.getKanji() == null) {
                return Optional.empty();
            }

            KanjiEntry newEntry = buildKanjiEntry(ch, res);
            KanjiEntry saved = kanjiEntryRepository.save(newEntry);
            log.info("Hybrid Model: Successfully fetched & cached external kanji '{}' into database", ch);
            return Optional.of(saved);

        } catch (HttpClientErrorException.NotFound e) {
            log.debug("External kanji API returned 404 for character '{}'", ch);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to fetch external kanji for '{}': {}", ch, e.getMessage());
            return Optional.empty();
        }
    }

    private KanjiEntry buildKanjiEntry(String ch, ExternalKanjiApiResponse res) {
        String onReadings = formatReadings(res.getOnReadings(), "-");
        String kunReadings = formatReadings(res.getKunReadings(), "-");
        String meanings = formatReadings(res.getMeanings(), "Chữ Hán tiếng Nhật");
        String jlptStr = res.getJlpt() != null ? "N" + res.getJlpt() : "Ngoài JLPT";
        int strokeCount = res.getStrokeCount() != null ? res.getStrokeCount() : 0;

        return KanjiEntry.builder()
                .characterText(ch)
                .strokeCount(strokeCount)
                .jlptLevel(jlptStr)
                .hanViet("HÁN TỰ (" + ch + ")")
                .onReadings(onReadings)
                .kunReadings(kunReadings)
                .meaning(meanings)
                .radicals("")
                .build();
    }

    private String formatReadings(List<String> list, String defaultVal) {
        if (list != null && !list.isEmpty()) {
            return String.join(", ", list);
        }
        return defaultVal;
    }
}

