package com.jpdictionary.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jpdictionary.dictionary.dto.KanjiBreakdownDto;
import com.jpdictionary.dictionary.dto.SuggestionDto;
import com.jpdictionary.dictionary.dto.WordDetailDto;
import com.jpdictionary.dictionary.dto.WordSummaryDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String WORD_SEARCH = "wordSearch";
    public static final String WORD_SUGGEST = "wordSuggest";
    public static final String WORD_DETAIL = "wordDetail";
    public static final String KANJI_DETAIL = "kanjiDetail";
    public static final String TRENDING_KANJI = "trendingKanji";

    private static final Duration SEARCH_TTL = Duration.ofMinutes(10);
    private static final Duration DETAIL_TTL = Duration.ofMinutes(30);
    private static final Duration KANJI_TTL = Duration.ofHours(6);

    @Configuration
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
    static class CaffeineCacheConfiguration {

        @Bean
        public CacheManager cacheManager() {
            SimpleCacheManager manager = new SimpleCacheManager();
            manager.setCaches(List.of(
                    buildCaffeineCache(WORD_SEARCH, SEARCH_TTL, 5_000),
                    buildCaffeineCache(WORD_SUGGEST, SEARCH_TTL, 5_000),
                    buildCaffeineCache(WORD_DETAIL, DETAIL_TTL, 10_000),
                    buildCaffeineCache(KANJI_DETAIL, KANJI_TTL, 5_000),
                    buildCaffeineCache(TRENDING_KANJI, DETAIL_TTL, 10)
            ));
            return manager;
        }

        private CaffeineCache buildCaffeineCache(String name, Duration ttl, long maxSize) {
            return new CaffeineCache(name, Caffeine.newBuilder()
                    .expireAfterWrite(ttl)
                    .maximumSize(maxSize)
                    .build());
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    static class RedisCacheConfigurationSetup {

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
            JavaType summaryListType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, WordSummaryDto.class);
            JavaType suggestListType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, SuggestionDto.class);
            JavaType kanjiListType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, KanjiBreakdownDto.class);

            Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                    WORD_SEARCH, buildRedisConfig(objectMapper, summaryListType, SEARCH_TTL),
                    WORD_SUGGEST, buildRedisConfig(objectMapper, suggestListType, SEARCH_TTL),
                    WORD_DETAIL, buildRedisConfig(
                            objectMapper, objectMapper.constructType(WordDetailDto.class), DETAIL_TTL),
                    KANJI_DETAIL, buildRedisConfig(
                            objectMapper, objectMapper.constructType(KanjiBreakdownDto.class), KANJI_TTL),
                    TRENDING_KANJI, buildRedisConfig(objectMapper, kanjiListType, DETAIL_TTL)
            );

            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(buildRedisConfig(
                            objectMapper, objectMapper.constructType(Object.class), SEARCH_TTL))
                    .withInitialCacheConfigurations(cacheConfigs)
                    .build();
        }

        private RedisCacheConfiguration buildRedisConfig(
                ObjectMapper objectMapper, JavaType valueType, Duration ttl) {
            Jackson2JsonRedisSerializer<Object> serializer =
                    new Jackson2JsonRedisSerializer<>(objectMapper, valueType);
            return RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(ttl)
                    .disableCachingNullValues()
                    .serializeKeysWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(serializer));
        }
    }
}

