package com.jpdictionary.dictionary.enums;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Slf4j
public enum PosCategory {
    ALL("all", "Tất cả", List.of(), 0),
    NOUN("noun", "Danh từ", List.of("n", "ctr", "pref"), 1),
    VERB("verb", "Động từ", List.of("v1", "v5*", "vs", "vk", "vz", "aux-v"), 2),
    ADJECTIVE("adj", "Tính từ", List.of("adj-i", "adj-na", "adj-no", "adj-pn", "adj-t", "adj-f"), 4),
    ADVERB("adverb", "Phó từ", List.of("adv", "adv-to"), 8),
    IDIOM("idiom", "Quán dụng ngữ", List.of("id", "exp"), 16),
    OTHER("other", "Khác", List.of(), 32);

    private final String code;
    private final String displayNameVi;
    private final List<String> standardPrefixes;
    private final int bitMask;

    PosCategory(String code, String displayNameVi, List<String> standardPrefixes, int bitMask) {
        this.code = code;
        this.displayNameVi = displayNameVi;
        this.standardPrefixes = standardPrefixes;
        this.bitMask = bitMask;
    }

    /**
     * Combine a collection of storable PosCategory values into a single bitmask.
     */
    public static int toBitMask(Collection<PosCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return 0;
        }
        int mask = 0;
        for (PosCategory cat : categories) {
            if (cat != null && cat.isStorable()) {
                mask |= cat.getBitMask();
            }
        }
        return mask;
    }

    /**
     * Decode a stored POS bitmask back into storable PosCategory enum names.
     */
    public static List<String> fromBitMask(int posMask) {
        if (posMask <= 0) {
            return List.of();
        }
        List<String> categories = new ArrayList<>();
        for (PosCategory cat : values()) {
            if (cat.isStorable() && (posMask & cat.bitMask) != 0) {
                categories.add(cat.name());
            }
        }
        return categories;
    }

    /**
     * Whether this category is valid for persistence (ALL is filter-only).
     */
    public boolean isStorable() {
        return this != ALL;
    }

    /**
     * Parse query parameter to PosCategory safely without case sensitivity.
     */
    public static PosCategory fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return ALL;
        }
        String clean = code.trim();
        return Arrays.stream(values())
                .filter(c -> c.code.equalsIgnoreCase(clean) || c.name().equalsIgnoreCase(clean))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Unknown PosCategory filter code '{}', falling back to ALL", clean);
                    return ALL;
                });
    }

    /**
     * Check if a raw partOfSpeech string matches this category.
     */
    public boolean matches(String rawPos) {
        if (this == ALL) {
            return true;
        }
        if (rawPos == null || rawPos.trim().isEmpty()) {
            return this == OTHER;
        }
        return inferCategorySet(rawPos).contains(this);
    }

    /**
     * Infer standardized PosCategory set from a raw partOfSpeech string.
     * Falls back to OTHER when rawPos is blank or does not match known categories.
     */
    public static Set<PosCategory> inferCategorySet(String rawPos) {
        if (rawPos == null || rawPos.trim().isEmpty()) {
            return EnumSet.of(OTHER);
        }
        String lower = rawPos.trim().toLowerCase(Locale.ROOT);
        List<String> tagTokens = extractTagTokens(lower);

        Set<PosCategory> matched = collectByTagTokens(tagTokens);
        if (matched.isEmpty()) {
            matched = collectByDisplayName(lower);
        }
        return matched.isEmpty() ? EnumSet.of(OTHER) : matched;
    }

    /**
     * Infer comma-separated PosCategories from a raw partOfSpeech string.
     */
    public static String inferCategories(String rawPos) {
        return inferCategorySet(rawPos).stream()
                .map(PosCategory::name)
                .collect(Collectors.joining(","));
    }

    private static Set<PosCategory> collectByTagTokens(List<String> tagTokens) {
        Set<PosCategory> result = EnumSet.noneOf(PosCategory.class);
        if (tagTokens.isEmpty()) {
            return result;
        }
        for (PosCategory cat : values()) {
            if (cat.isStorable() && cat.matchesAnyTagToken(tagTokens)) {
                result.add(cat);
            }
        }
        return result;
    }

    private static Set<PosCategory> collectByDisplayName(String lowerRawPos) {
        Set<PosCategory> result = EnumSet.noneOf(PosCategory.class);
        for (PosCategory cat : values()) {
            if (cat.isStorable() && cat != OTHER && cat.matchesDisplayName(lowerRawPos)) {
                result.add(cat);
            }
        }
        return result;
    }

    private boolean matchesAnyTagToken(List<String> tagTokens) {
        for (String token : tagTokens) {
            for (String prefix : standardPrefixes) {
                if (matchesToken(token, prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesToken(String token, String prefix) {
        if (prefix.endsWith("*")) {
            String base = prefix.substring(0, prefix.length() - 1);
            return token.startsWith(base);
        }
        return token.equals(prefix) || token.startsWith(prefix + "-");
    }

    private boolean matchesDisplayName(String lowerRawPos) {
        return !displayNameVi.isEmpty()
                && lowerRawPos.contains(displayNameVi.toLowerCase(Locale.ROOT));
    }

    private static List<String> extractTagTokens(String lowerRawPos) {
        int cutIdx = lowerRawPos.length();
        int parenIdx = lowerRawPos.indexOf('(');
        int bracketIdx = lowerRawPos.indexOf('[');
        if (parenIdx >= 0) {
            cutIdx = Math.min(cutIdx, parenIdx);
        }
        if (bracketIdx >= 0) {
            cutIdx = Math.min(cutIdx, bracketIdx);
        }
        String tagSection = lowerRawPos.substring(0, cutIdx).trim();
        if (tagSection.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(tagSection.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .toList();
    }
}

