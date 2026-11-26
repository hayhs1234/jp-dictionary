package com.jpdictionary.dictionary.service.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VietnameseTextNormalizerTest {

    @Test
    @DisplayName("Should fold Vietnamese diacritics and đ/Đ to lowercase ASCII")
    void testToAsciiFolded() {
        assertEquals("", VietnameseTextNormalizer.toAsciiFolded(null));
        assertEquals("", VietnameseTextNormalizer.toAsciiFolded("   "));
        assertEquals(
                "anh nang lot qua ke la; di lai dep",
                VietnameseTextNormalizer.toAsciiFolded("Ánh nắng lọt qua kẽ lá; đi lại Đẹp")
        );
    }

    @Test
    @DisplayName("Should build safe MySQL FULLTEXT boolean query and strip special operators")
    void testToBooleanFulltextQuery() {
        assertEquals("", VietnameseTextNormalizer.toBooleanFulltextQuery(null));
        assertEquals("", VietnameseTextNormalizer.toBooleanFulltextQuery("木"));
        assertEquals("", VietnameseTextNormalizer.toBooleanFulltextQuery("+++ --- ()"));
        assertEquals("+an +uong", VietnameseTextNormalizer.toBooleanFulltextQuery("an uong"));
        assertEquals("+từ +vựng", VietnameseTextNormalizer.toBooleanFulltextQuery("từ-vựng* (n)"));
        assertEquals("+tu +vung", VietnameseTextNormalizer.toBooleanFulltextQuery(
                VietnameseTextNormalizer.toAsciiFolded("từ-vựng* (n)")));
        assertEquals("+nghĩa", VietnameseTextNormalizer.toBooleanFulltextQuery("ý nghĩa"));
        assertEquals("+木漏れ日", VietnameseTextNormalizer.toBooleanFulltextQuery("木漏れ日"));
    }

    @Test
    @DisplayName("Should count Unicode code points accurately including surrogate pairs")
    void testCodePointLength() {
        assertEquals(0, VietnameseTextNormalizer.codePointLength(null));
        assertEquals(0, VietnameseTextNormalizer.codePointLength(""));
        assertEquals(1, VietnameseTextNormalizer.codePointLength("木"));
        assertEquals(1, VietnameseTextNormalizer.codePointLength("\uD842\uDFB7"));
        assertEquals(3, VietnameseTextNormalizer.codePointLength("木漏れ"));
    }
}

