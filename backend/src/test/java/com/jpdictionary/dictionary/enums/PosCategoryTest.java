package com.jpdictionary.dictionary.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PosCategoryTest {

    @Test
    @DisplayName("Should infer multi-category set for n, vs entries")
    void testInferMultiCategoryNounVerb() {
        Set<PosCategory> result = PosCategory.inferCategorySet("n, vs (Danh từ / Động từ suru)");
        assertEquals(Set.of(PosCategory.NOUN, PosCategory.VERB), result);
        assertEquals("NOUN,VERB", PosCategory.inferCategories("n, vs (Danh từ / Động từ suru)"));
    }

    @Test
    @DisplayName("Should infer NOUN for n, pref and ctr prefixes")
    void testInferNounPrefixes() {
        assertEquals(Set.of(PosCategory.NOUN), PosCategory.inferCategorySet("n, pref (Danh từ / Tiền tố)"));
        assertEquals(Set.of(PosCategory.NOUN), PosCategory.inferCategorySet("ctr (Lượng từ)"));
    }

    @Test
    @DisplayName("Should infer VERB for v1, v5, vk, vz, and aux-v prefixes")
    void testInferVerbPrefixes() {
        assertEquals(Set.of(PosCategory.VERB), PosCategory.inferCategorySet("v5s, aux-v (Động từ / Trợ động từ)"));
        assertEquals(Set.of(PosCategory.VERB), PosCategory.inferCategorySet("vk (Động từ kuru)"));
        assertEquals(Set.of(PosCategory.VERB), PosCategory.inferCategorySet("vz (Động từ zuru)"));
    }

    @Test
    @DisplayName("Should NOT misclassify num or unknown tags as NOUN; fallback to OTHER")
    void testDoesNotMisclassifyNumOrUnknownAsNoun() {
        assertEquals(Set.of(PosCategory.OTHER), PosCategory.inferCategorySet("num (Số từ)"));
        assertEquals(Set.of(PosCategory.OTHER), PosCategory.inferCategorySet("prt (Trợ từ)"));
        assertEquals(Set.of(PosCategory.OTHER), PosCategory.inferCategorySet(""));
        assertEquals(Set.of(PosCategory.OTHER), PosCategory.inferCategorySet(null));
    }

    @Test
    @DisplayName("Should not let Vietnamese notes inside parentheses override JMdict tag")
    void testParentheticalNotesDoNotPolluteTaggedPos() {
        Set<PosCategory> result = PosCategory.inferCategorySet("n (Danh từ, dùng trước động từ)");
        assertEquals(Set.of(PosCategory.NOUN), result);
    }

    @Test
    @DisplayName("Should match Vietnamese display name fallback when no JMdict tag exists")
    void testFallbackMatchesVietnameseDisplayName() {
        assertEquals(Set.of(PosCategory.NOUN), PosCategory.inferCategorySet("Danh từ [名]"));
        assertEquals(Set.of(PosCategory.ADJECTIVE), PosCategory.inferCategorySet("Tính từ đuôi na [形]"));
    }

    @Test
    @DisplayName("Should mark ALL as non-storable and parse codes safely")
    void testStorableAndFromCode() {
        assertFalse(PosCategory.ALL.isStorable());
        assertTrue(PosCategory.NOUN.isStorable());
        assertTrue(PosCategory.OTHER.isStorable());
        assertEquals(PosCategory.ADVERB, PosCategory.fromCode("adverb"));
        assertEquals(PosCategory.OTHER, PosCategory.fromCode("OTHER"));
        assertEquals(PosCategory.ALL, PosCategory.fromCode("unknown-code"));
    }

    @Test
    @DisplayName("Should encode and decode POS bitmasks round-trip")
    void testBitMaskRoundTrip() {
        int mask = PosCategory.toBitMask(Set.of(PosCategory.NOUN, PosCategory.VERB, PosCategory.ALL));
        assertEquals(PosCategory.NOUN.getBitMask() | PosCategory.VERB.getBitMask(), mask);
        assertEquals(List.of("NOUN", "VERB"), PosCategory.fromBitMask(mask));
        assertEquals(List.of(), PosCategory.fromBitMask(0));
    }
}

