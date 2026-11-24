package com.jpdictionary.dictionary.service.deinflect;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Japanese morphological de-inflector.
 * Transforms conjugated verb/adjective forms (past, negative, te-form, polite, etc.)
 * back to their dictionary forms (辞書形).
 */
@Component
public class JapaneseDeinflector {

    private record SuffixRule(String suffix, int minLength, String replacement) {}

    private static final Set<String> KURU_KANJI_FORMS = Set.of(
            "来ない", "来ます", "来ました", "来ません", "来ませんでした",
            "来た", "来て", "来れば", "来られる", "来させる", "来よう"
    );

    private static final Set<String> KURU_KANA_FORMS = Set.of(
            "こない", "きます", "きました", "きません", "きませんでした",
            "きた", "きて", "くれば", "こられる", "こさせる", "こよう"
    );

    private static final List<String> SURU_SUFFIXES = List.of(
            "しませんでした", "しました", "しません", "させる", "される",
            "できる", "しない", "します", "すれば", "しよう", "した", "して"
    );

    private static final List<SuffixRule> I_ADJ_RULES = List.of(
            new SuffixRule("くなかった", 5, "い"),
            new SuffixRule("かった", 3, "い"),
            new SuffixRule("くない", 3, "い"),
            new SuffixRule("くて", 2, "い"),
            new SuffixRule("く", 2, "い")
    );

    private static final List<SuffixRule> NA_ADJ_RULES = List.of(
            new SuffixRule("ではない", 4, ""),
            new SuffixRule("じゃない", 4, ""),
            new SuffixRule("だった", 3, ""),
            new SuffixRule("な", 2, ""),
            new SuffixRule("に", 2, ""),
            new SuffixRule("だ", 2, "")
    );

    private static final List<SuffixRule> ICHIDAN_RULES = List.of(
            new SuffixRule("ませんでした", 6, "る"),
            new SuffixRule("なかった", 4, "る"),
            new SuffixRule("ました", 3, "る"),
            new SuffixRule("ません", 3, "る"),
            new SuffixRule("ない", 2, "る"),
            new SuffixRule("ます", 2, "る"),
            new SuffixRule("たい", 2, "る"),
            new SuffixRule("た", 1, "る"),
            new SuffixRule("て", 1, "る"),
            new SuffixRule("れば", 2, "る"),
            new SuffixRule("よう", 2, "る"),
            new SuffixRule("られる", 3, "る"),
            new SuffixRule("させる", 3, "る")
    );

    private static final Map<Character, String> NEGATIVE_A_LINE_MAP = Map.of(
            'わ', "う", 'か', "く", 'が', "ぐ", 'さ', "す", 'た', "つ",
            'な', "ぬ", 'ば', "ぶ", 'ま', "む", 'ら', "る"
    );

    private static final Map<Character, String> POLITE_I_LINE_MAP = Map.of(
            'い', "う", 'き', "く", 'ぎ', "ぐ", 'し', "す", 'ち', "つ",
            'に', "ぬ", 'び', "ぶ", 'み', "む", 'り', "る"
    );

    private static final List<String> POLITE_SUFFIXES = List.of(
            "ませんでした", "ました", "ません", "ます", "たい"
    );

    public List<String> deinflect(String term) {
        if (term == null || term.trim().length() < 2) {
            return List.of();
        }

        String word = term.trim();
        Set<String> candidates = new LinkedHashSet<>();

        deinflectIrregular(word, candidates);
        deinflectAdjectives(word, candidates);
        deinflectIchidanVerbs(word, candidates);
        deinflectGodanVerbs(word, candidates);

        candidates.remove(word);
        return new ArrayList<>(candidates);
    }

    private void deinflectIrregular(String word, Set<String> candidates) {
        if (KURU_KANJI_FORMS.contains(word)) {
            candidates.add("来る");
        }
        if (KURU_KANA_FORMS.contains(word)) {
            candidates.add("くる");
        }
        for (String suffix : SURU_SUFFIXES) {
            if (word.endsWith(suffix)) {
                String prefix = word.substring(0, word.length() - suffix.length());
                candidates.add(prefix + "する");
                break;
            }
        }
    }

    private void deinflectAdjectives(String word, Set<String> candidates) {
        applyRules(word, I_ADJ_RULES, candidates);
        applyRules(word, NA_ADJ_RULES, candidates);
    }

    private void applyRules(String word, List<SuffixRule> rules, Set<String> candidates) {
        for (SuffixRule rule : rules) {
            if (word.endsWith(rule.suffix()) && word.length() > rule.minLength()) {
                candidates.add(word.substring(0, word.length() - rule.suffix().length()) + rule.replacement());
                break;
            }
        }
    }

    private void deinflectIchidanVerbs(String word, Set<String> candidates) {
        applyRules(word, ICHIDAN_RULES, candidates);
    }

    private void deinflectGodanVerbs(String word, Set<String> candidates) {
        handleGodanTeAndPast(word, candidates);
        handleGodanNegative(word, candidates);
        handleGodanPolite(word, candidates);
    }

    private void handleGodanTeAndPast(String word, Set<String> candidates) {
        if (word.length() < 2) {
            return;
        }
        String suffix = word.substring(word.length() - 2);
        String stem = word.substring(0, word.length() - 2);
        if (stem.isEmpty()) {
            return;
        }

        switch (suffix) {
            case "って", "った" -> handleTteStem(stem, candidates);
            case "いて", "いた" -> candidates.add(stem + "く");
            case "いで", "いだ" -> candidates.add(stem + "ぐ");
            case "して", "した" -> candidates.add(stem + "す");
            case "んで", "んだ" -> {
                candidates.add(stem + "む");
                candidates.add(stem + "ぶ");
                candidates.add(stem + "ぬ");
            }
            default -> { }
        }
    }

    private void handleTteStem(String stem, Set<String> candidates) {
        candidates.add(stem + "う");
        candidates.add(stem + "つ");
        candidates.add(stem + "る");
        if (stem.equals("行")) {
            candidates.add("行く");
        } else if (stem.equals("い")) {
            candidates.add("いく");
        }
    }

    private void handleGodanNegative(String word, Set<String> candidates) {
        String base = extractNegativeBase(word);
        if (base != null && !base.isEmpty()) {
            char lastChar = base.charAt(base.length() - 1);
            String replacement = NEGATIVE_A_LINE_MAP.get(lastChar);
            if (replacement != null) {
                candidates.add(base.substring(0, base.length() - 1) + replacement);
            }
        }
    }

    private String extractNegativeBase(String word) {
        if (word.endsWith("なかった") && word.length() > 4) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("ない") && word.length() > 2) {
            return word.substring(0, word.length() - 2);
        }
        return null;
    }

    private void handleGodanPolite(String word, Set<String> candidates) {
        String base = extractPoliteBase(word);
        if (base != null && !base.isEmpty()) {
            char lastChar = base.charAt(base.length() - 1);
            String replacement = POLITE_I_LINE_MAP.get(lastChar);
            if (replacement != null) {
                candidates.add(base.substring(0, base.length() - 1) + replacement);
            }
        }
    }

    private String extractPoliteBase(String word) {
        for (String suffix : POLITE_SUFFIXES) {
            if (word.endsWith(suffix) && word.length() > suffix.length()) {
                return word.substring(0, word.length() - suffix.length());
            }
        }
        return null;
    }
}


