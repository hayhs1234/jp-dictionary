package com.jpdictionary.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.search.meilisearch")
@Getter
@Setter
public class MeilisearchProperties {

    private String baseUrl = "http://localhost:7700";
    private String apiKey = "";
    private String indexName = "dictionary_entries";
    private int connectTimeoutMs = 1500;
    private int readTimeoutMs = 2500;
    private boolean fallbackOnError = true;
    private int syncBatchSize = 1000;
    private long syncIntervalMs = 30000;
}

