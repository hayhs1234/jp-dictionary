package com.jpdictionary.dictionary.entity;

import com.jpdictionary.dictionary.enums.PosCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DictionarySenseTest {

    @Test
    @DisplayName("Should auto-infer posCategories when empty on lifecycle normalization")
    void testAutoInferWhenEmpty() {
        DictionarySense sense = DictionarySense.builder()
                .partOfSpeech("n, vs (Danh từ / Động từ suru)")
                .build();

        sense.normalizeAndValidatePosCategories();

        assertEquals(Set.of(PosCategory.NOUN, PosCategory.VERB), sense.getPosCategories());
    }

    @Test
    @DisplayName("Should fallback to OTHER when partOfSpeech is unknown on lifecycle normalization")
    void testFallbackToOtherWhenUnknown() {
        DictionarySense sense = DictionarySense.builder()
                .partOfSpeech("prt (Trợ từ)")
                .build();

        sense.normalizeAndValidatePosCategories();

        assertEquals(Set.of(PosCategory.OTHER), sense.getPosCategories());
    }

    @Test
    @DisplayName("Should reject non-storable PosCategory.ALL")
    void testRejectsAllCategory() {
        DictionarySense sense = DictionarySense.builder()
                .partOfSpeech("n (Danh từ)")
                .posCategories(Set.of(PosCategory.ALL))
                .build();

        assertThrows(IllegalArgumentException.class, sense::normalizeAndValidatePosCategories);
    }

    @Test
    @DisplayName("Should reject null category element")
    void testRejectsNullElement() {
        Set<PosCategory> invalid = new LinkedHashSet<>();
        invalid.add(PosCategory.NOUN);
        invalid.add(null);

        DictionarySense sense = DictionarySense.builder()
                .partOfSpeech("n (Danh từ)")
                .posCategories(invalid)
                .build();

        assertThrows(IllegalArgumentException.class, sense::normalizeAndValidatePosCategories);
    }

    @Test
    @DisplayName("Should sync posCategories when calling assignPartOfSpeech")
    void testAssignPartOfSpeechSyncsCategories() {
        DictionarySense sense = DictionarySense.builder()
                .partOfSpeech("n (Danh từ)")
                .posCategories(new LinkedHashSet<>(Set.of(PosCategory.NOUN)))
                .build();

        sense.assignPartOfSpeech("adj-i (Tính từ đuôi i)");

        assertEquals("adj-i (Tính từ đuôi i)", sense.getPartOfSpeech());
        assertEquals(Set.of(PosCategory.ADJECTIVE), sense.getPosCategories());
    }
}

