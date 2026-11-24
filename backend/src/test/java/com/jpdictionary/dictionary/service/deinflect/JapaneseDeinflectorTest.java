package com.jpdictionary.dictionary.service.deinflect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JapaneseDeinflectorTest {

    private JapaneseDeinflector deinflector;

    @BeforeEach
    void setUp() {
        deinflector = new JapaneseDeinflector();
    }

    @Test
    @DisplayName("Deinflects ichidan verbs past and te-form")
    void testDeinflectIchidan() {
        List<String> rootsPast = deinflector.deinflect("食べた");
        assertTrue(rootsPast.contains("食べる"));

        List<String> rootsTe = deinflector.deinflect("食べて");
        assertTrue(rootsTe.contains("食べる"));

        List<String> rootsNeg = deinflector.deinflect("食べない");
        assertTrue(rootsNeg.contains("食べる"));

        List<String> rootsPolite = deinflector.deinflect("食べます");
        assertTrue(rootsPolite.contains("食べる"));
    }

    @Test
    @DisplayName("Deinflects godan verbs past, te-form, and polite")
    void testDeinflectGodan() {
        // iku
        List<String> rootsIku = deinflector.deinflect("行った");
        assertTrue(rootsIku.contains("行く"));

        List<String> rootsIkanai = deinflector.deinflect("行かない");
        assertTrue(rootsIkanai.contains("行く"));

        // nomu
        List<String> rootsNomu = deinflector.deinflect("飲んだ");
        assertTrue(rootsNomu.contains("飲む"));

        List<String> rootsNonde = deinflector.deinflect("飲んで");
        assertTrue(rootsNonde.contains("飲む"));

        // hanasu
        List<String> rootsHanasu = deinflector.deinflect("話しました");
        assertTrue(rootsHanasu.contains("話す"));
    }

    @Test
    @DisplayName("Deinflects irregular verbs kuru and suru")
    void testDeinflectIrregular() {
        List<String> rootsKuru = deinflector.deinflect("来た");
        assertTrue(rootsKuru.contains("来る"));

        List<String> rootsKite = deinflector.deinflect("きて");
        assertTrue(rootsKite.contains("くる"));

        List<String> rootsSuru = deinflector.deinflect("勉強した");
        assertTrue(rootsSuru.contains("勉強する"));
    }

    @Test
    @DisplayName("Deinflects i-adjectives and na-adjectives")
    void testDeinflectAdjectives() {
        List<String> rootsAdjI = deinflector.deinflect("新しかった");
        assertTrue(rootsAdjI.contains("新しい"));

        List<String> rootsAdjINeg = deinflector.deinflect("新しくない");
        assertTrue(rootsAdjINeg.contains("新しい"));

        List<String> rootsAdjNa = deinflector.deinflect("親切な");
        assertTrue(rootsAdjNa.contains("親切"));

        List<String> rootsAdjNaNi = deinflector.deinflect("便利に");
        assertTrue(rootsAdjNaNi.contains("便利"));
    }
}

