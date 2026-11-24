package com.jpdictionary.dictionary.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum PosCategory {
    ALL("all", "Tất cả", List.of()),
    NOUN("noun", "Danh từ", List.of("n", "ctr", "pref")),
    VERB("verb", "Động từ", List.of("v1", "v5", "vs", "vk", "vz", "aux-v")),
    ADJECTIVE("adj", "Tính từ", List.of("adj-i", "adj-na", "adj-no", "adj-pn", "adj-t", "adj-f")),
    ADVERB("adverb", "Phó từ", List.of("adv", "adv-to")),
    IDIOM("idiom", "Quán dụng ngữ", List.of("id", "exp"));

    private final String code;
    private final String displayNameVi;
    private final List<String> standardPrefixes;

    PosCategory(String code, String displayNameVi, List<String> standardPrefixes) {
        this.code = code;
        this.displayNameVi = displayNameVi;
        this.standardPrefixes = standardPrefixes;
    }

    /**
     * Parse query parameter to PosCategory safely without case sensitivity.
     */
    public static PosCategory fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return ALL;
        }
        String clean = code.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(c -> c.code.equalsIgnoreCase(clean) || c.name().equalsIgnoreCase(clean))
                .findFirst()
                .orElse(ALL);
    }

    /**
     * Check if a raw partOfSpeech string matches this category.
     */
    public boolean matches(String rawPos) {
        if (this == ALL) {
            return true;
        }
        if (rawPos == null || rawPos.trim().isEmpty()) {
            return false;
        }
        String lower = rawPos.trim().toLowerCase();

        // 1. Direct match with Vietnamese display name
        if (lower.contains(displayNameVi.toLowerCase())) {
            return true;
        }

        // 2. Token match with standard JMdict prefixes before parentheses or space
        for (String prefix : standardPrefixes) {
            if (lower.startsWith(prefix) || lower.contains("," + prefix) || lower.contains(", " + prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Infer the comma-separated PosCategories from a raw partOfSpeech string.
     * E.g. "n, vs (Danh từ / Động từ suru)" -> "NOUN,VERB"
     */
    public static String inferCategories(String rawPos) {
        if (rawPos == null || rawPos.trim().isEmpty()) {
            return NOUN.name();
        }
        StringBuilder sb = new StringBuilder();
        for (PosCategory cat : values()) {
            if (cat != ALL && cat.matches(rawPos)) {
                if (!sb.isEmpty()) {
                    sb.append(",");
                }
                sb.append(cat.name());
            }
        }
        return sb.isEmpty() ? NOUN.name() : sb.toString();
    }
}

