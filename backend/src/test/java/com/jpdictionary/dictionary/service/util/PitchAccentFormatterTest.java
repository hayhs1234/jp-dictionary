package com.jpdictionary.dictionary.service.util;

import com.jpdictionary.dictionary.entity.DictionaryReading;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PitchAccentFormatterTest {

    @Test
    @DisplayName("Should return null when reading or pitchAccent is null or blank")
    void testReturnsNullWhenMissing() {
        assertNull(PitchAccentFormatter.format(null));
        assertNull(PitchAccentFormatter.format("たべる", null));
        assertNull(PitchAccentFormatter.format("たべる", "   "));

        DictionaryReading emptyReading = DictionaryReading.builder()
                .reading("たべる")
                .pitchAccent(null)
                .build();
        assertNull(PitchAccentFormatter.format(emptyReading));
    }

    @Test
    @DisplayName("Should classify Heiban, Atamadaka, Nakadaka, and Odaka accurately")
    void testClassifiesAllFourPitchPatterns() {
        assertEquals("[0] 平板 (Heiban)", PitchAccentFormatter.format("にほんご", "0"));
        assertEquals("[1] 頭高 (Atamadaka)", PitchAccentFormatter.format("ほん", "1"));
        assertEquals("[1] 頭高 (Atamadaka)", PitchAccentFormatter.format("き", "1"));
        assertEquals("[2] 中高 (Nakadaka)", PitchAccentFormatter.format("たべる", "2"));
        assertEquals("[4] 中高 (Nakadaka)", PitchAccentFormatter.format("あたらしい", "4"));
        assertEquals("[2] 尾高 (Odaka)", PitchAccentFormatter.format("はな", "2"));
        assertEquals("[3] 尾高 (Odaka)", PitchAccentFormatter.format("おとこ", "3"));
    }

    @Test
    @DisplayName("Should count morae accurately with small combining kana, sokuon, and moraic n")
    void testMoraCountingWithSpecialKana() {
        assertEquals(3, PitchAccentFormatter.countMorae("どりょく"));
        assertEquals("[3] 尾高 (Odaka)", PitchAccentFormatter.format("どりょく", "3"));
        assertEquals("[1] 頭高 (Atamadaka)", PitchAccentFormatter.format("どりょく", "1"));

        assertEquals(4, PitchAccentFormatter.countMorae("がっこう"));
        assertEquals(4, PitchAccentFormatter.countMorae("こーひー"));
        assertEquals("[3] 中高 (Nakadaka)", PitchAccentFormatter.format("こーひー", "3"));
    }

    @Test
    @DisplayName("Should support multiple comma-separated pitch kernels and ignore invalid values")
    void testMultipleKernelsAndInvalidInputs() {
        assertEquals(
                "[0] 平板 (Heiban), [3] 中高 (Nakadaka)",
                PitchAccentFormatter.format("こもれび", "0, 3")
        );
        assertNull(PitchAccentFormatter.format("ほん", "5"));
        assertNull(PitchAccentFormatter.format("ほん", "-1"));
        assertNull(PitchAccentFormatter.format("ほん", "abc"));
    }
}

