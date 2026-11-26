package com.jpdictionary.dictionary.service.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Text normalization utilities for Vietnamese ASCII folding and
 * safe MySQL FULLTEXT (ngram / boolean mode) query construction.
 */
public final class VietnameseTextNormalizer {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern BOOLEAN_OPERATORS = Pattern.compile("[+\\-~*\"@<>()]+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private VietnameseTextNormalizer() {
    }

    /**
     * Fold Vietnamese/Latin diacritics to lowercase ASCII (including đ/Đ -> d).
     */
    public static String toAsciiFolded(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String replaced = input.replace('đ', 'd').replace('Đ', 'D');
        String decomposed = Normalizer.normalize(replaced, Normalizer.Form.NFD);
        String stripped = COMBINING_MARKS.matcher(decomposed).replaceAll("");
        return WHITESPACE.matcher(stripped).replaceAll(" ").trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Build a safe MySQL FULLTEXT IN BOOLEAN MODE query string.
     * Each whitespace-separated token of length >= 2 is prefixed with '+' so all words must match.
     * Returns empty string when no valid ngram token (>= 2 code points) can be formed.
     */
    public static String toBooleanFulltextQuery(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String cleaned = BOOLEAN_OPERATORS.matcher(input.trim()).replaceAll(" ");
        String[] parts = WHITESPACE.split(cleaned.trim());
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty() && part.codePointCount(0, part.length()) >= 2) {
                tokens.add("+" + part);
            }
        }
        return String.join(" ", tokens);
    }

    /**
     * Count Unicode code points safely (handles surrogate pairs properly).
     */
    public static int codePointLength(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        return input.codePointCount(0, input.length());
    }
}

