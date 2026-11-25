package com.jpdictionary.dictionary.service.util;

import com.jpdictionary.dictionary.entity.DictionaryReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Formats Tokyo pitch accent kernel numbers (e.g. "0", "2", "0,3") into
 * learner-friendly labels based on the reading's mora count.
 */
public final class PitchAccentFormatter {

    private static final Set<Character> SMALL_COMBINING_KANA = Set.of(
            'ゃ', 'ゅ', 'ょ', 'ぁ', 'ぃ', 'ぅ', 'ぇ', 'ぉ', 'ゎ',
            'ャ', 'ュ', 'ョ', 'ァ', 'ィ', 'ゥ', 'ェ', 'ォ', 'ヮ'
    );

    private PitchAccentFormatter() {
    }

    public static String format(DictionaryReading reading) {
        if (reading == null) {
            return null;
        }
        return format(reading.getReading(), reading.getPitchAccent());
    }

    public static String format(String kanaReading, String rawPitchAccent) {
        if (rawPitchAccent == null || rawPitchAccent.isBlank()) {
            return null;
        }

        int moraCount = countMorae(kanaReading);
        String[] tokens = rawPitchAccent.split(",");
        List<String> formatted = new ArrayList<>();

        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int kernel;
            try {
                kernel = Integer.parseInt(trimmed);
            } catch (NumberFormatException ex) {
                continue;
            }
            String label = classifyKernel(kernel, moraCount);
            if (label != null) {
                formatted.add(label);
            }
        }

        return formatted.isEmpty() ? null : String.join(", ", formatted);
    }

    static int countMorae(String kanaReading) {
        if (kanaReading == null || kanaReading.isBlank()) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < kanaReading.length(); i++) {
            char ch = kanaReading.charAt(i);
            if (Character.isWhitespace(ch) || ch == '・' || SMALL_COMBINING_KANA.contains(ch)) {
                continue;
            }
            count++;
        }
        return count;
    }

    private static String classifyKernel(int kernel, int moraCount) {
        if (kernel < 0) {
            return null;
        }
        if (kernel == 0) {
            return "[0] 平板 (Heiban)";
        }
        if (kernel == 1) {
            return "[1] 頭高 (Atamadaka)";
        }
        if (moraCount <= 0 || kernel > moraCount) {
            return null;
        }
        if (kernel == moraCount) {
            return "[" + kernel + "] 尾高 (Odaka)";
        }
        return "[" + kernel + "] 中高 (Nakadaka)";
    }
}

