package com.jpdictionary.dictionary.repository;

import com.jpdictionary.dictionary.entity.DictionarySearchIndex;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface DictionarySearchIndexRepository extends JpaRepository<DictionarySearchIndex, Long> {

    List<DictionarySearchIndex> findByAsciiSyncedFalse(Pageable pageable);

    List<DictionarySearchIndex> findByMeiliSyncedFalseOrderByEntryIdAsc(Pageable pageable);

    @Modifying
    @Query("UPDATE DictionarySearchIndex d SET d.meiliSynced = true, d.meiliSyncedAt = :syncedAt "
            + "WHERE d.entryId IN :entryIds")
    int markMeiliSynced(@Param("entryIds") Collection<Long> entryIds, @Param("syncedAt") LocalDateTime syncedAt);

    @Modifying
    @Query("UPDATE DictionarySearchIndex d SET d.meiliSynced = false, d.meiliSyncedAt = null")
    int resetAllMeiliSynced();

    @Query("SELECT e.id FROM DictionaryEntry e "
            + "WHERE NOT EXISTS (SELECT 1 FROM DictionarySearchIndex d WHERE d.entryId = e.id)")
    List<Long> findUnindexedEntryIds(Pageable pageable);

    @Query(value = "(SELECT d.entry_id AS id "
            + " FROM dictionary_search_index d "
            + " WHERE d.primary_form LIKE :prefixPattern ESCAPE '!' "
            + "   AND (:jlptLevel IS NULL OR d.jlpt_level = :jlptLevel) "
            + "   AND (:posMask IS NULL OR (d.pos_mask & :posMask) > 0) "
            + " ORDER BY COALESCE(d.frequency_rank, 999999) ASC, d.jlpt_level DESC, d.entry_id ASC "
            + " LIMIT :limit) "
            + "UNION "
            + "(SELECT d.entry_id AS id "
            + " FROM dictionary_search_index d "
            + " WHERE d.primary_reading LIKE :prefixPattern ESCAPE '!' "
            + "   AND (:jlptLevel IS NULL OR d.jlpt_level = :jlptLevel) "
            + "   AND (:posMask IS NULL OR (d.pos_mask & :posMask) > 0) "
            + " ORDER BY COALESCE(d.frequency_rank, 999999) ASC, d.jlpt_level DESC, d.entry_id ASC "
            + " LIMIT :limit) "
            + "UNION "
            + "(SELECT d.entry_id AS id "
            + " FROM dictionary_search_index d "
            + " WHERE d.primary_romaji LIKE :prefixPattern ESCAPE '!' "
            + "   AND (:jlptLevel IS NULL OR d.jlpt_level = :jlptLevel) "
            + "   AND (:posMask IS NULL OR (d.pos_mask & :posMask) > 0) "
            + " ORDER BY COALESCE(d.frequency_rank, 999999) ASC, d.jlpt_level DESC, d.entry_id ASC "
            + " LIMIT :limit) "
            + "LIMIT :limit",
            nativeQuery = true)
    List<Long> findPrefixMatchIds(
            @Param("prefixPattern") String prefixPattern,
            @Param("jlptLevel") Byte jlptLevel,
            @Param("posMask") Integer posMask,
            @Param("limit") int limit
    );

    @Query(value = "SELECT ranked.id "
            + "FROM ("
            + "  (SELECT d.entry_id AS id, "
            + "          (MATCH(d.forms_text, d.readings_text) "
            + "           AGAINST(:booleanQuery IN BOOLEAN MODE) * 3.0) AS score, "
            + "          COALESCE(d.frequency_rank, 999999) AS freq "
            + "   FROM dictionary_search_index d "
            + "   WHERE MATCH(d.forms_text, d.readings_text) AGAINST(:booleanQuery IN BOOLEAN MODE) "
            + "     AND (:jlptLevel IS NULL OR d.jlpt_level = :jlptLevel) "
            + "     AND (:posMask IS NULL OR (d.pos_mask & :posMask) > 0) "
            + "   ORDER BY score DESC, freq ASC "
            + "   LIMIT :limit) "
            + "  UNION ALL "
            + "  (SELECT d.entry_id AS id, "
            + "          (MATCH(d.romaji_text) "
            + "           AGAINST(:asciiBooleanQuery IN BOOLEAN MODE) * 2.0) AS score, "
            + "          COALESCE(d.frequency_rank, 999999) AS freq "
            + "   FROM dictionary_search_index d "
            + "   WHERE MATCH(d.romaji_text) AGAINST(:asciiBooleanQuery IN BOOLEAN MODE) "
            + "     AND (:jlptLevel IS NULL OR d.jlpt_level = :jlptLevel) "
            + "     AND (:posMask IS NULL OR (d.pos_mask & :posMask) > 0) "
            + "   ORDER BY score DESC, freq ASC "
            + "   LIMIT :limit) "
            + "  UNION ALL "
            + "  (SELECT d.entry_id AS id, "
            + "          (MATCH(d.meanings_text, d.meanings_ascii) "
            + "           AGAINST(:asciiBooleanQuery IN BOOLEAN MODE) * 1.0) AS score, "
            + "          COALESCE(d.frequency_rank, 999999) AS freq "
            + "   FROM dictionary_search_index d "
            + "   WHERE MATCH(d.meanings_text, d.meanings_ascii) AGAINST(:asciiBooleanQuery IN BOOLEAN MODE) "
            + "     AND (:jlptLevel IS NULL OR d.jlpt_level = :jlptLevel) "
            + "     AND (:posMask IS NULL OR (d.pos_mask & :posMask) > 0) "
            + "   ORDER BY score DESC, freq ASC "
            + "   LIMIT :limit)"
            + ") ranked "
            + "GROUP BY ranked.id "
            + "ORDER BY MAX(ranked.score) DESC, MIN(ranked.freq) ASC, ranked.id ASC "
            + "LIMIT :limit",
            nativeQuery = true)
    List<Long> findFulltextMatchIds(
            @Param("booleanQuery") String booleanQuery,
            @Param("asciiBooleanQuery") String asciiBooleanQuery,
            @Param("jlptLevel") Byte jlptLevel,
            @Param("posMask") Integer posMask,
            @Param("limit") int limit
    );

    @Query(value = "SELECT d.entry_id "
            + "FROM dictionary_search_index d "
            + "WHERE (:jlptLevel IS NULL OR d.jlpt_level = :jlptLevel) "
            + "  AND (:posMask IS NULL OR (d.pos_mask & :posMask) > 0) "
            + "  AND (d.forms_text LIKE :containsPattern ESCAPE '!' "
            + "       OR d.readings_text LIKE :containsPattern ESCAPE '!' "
            + "       OR d.romaji_text LIKE :containsPattern ESCAPE '!' "
            + "       OR d.meanings_text LIKE :containsPattern ESCAPE '!' "
            + "       OR d.meanings_ascii LIKE :containsPattern ESCAPE '!') "
            + "ORDER BY COALESCE(d.frequency_rank, 999999) ASC, d.jlpt_level DESC, d.entry_id ASC "
            + "LIMIT :limit",
            nativeQuery = true)
    List<Long> findSingleCharSubstringIds(
            @Param("containsPattern") String containsPattern,
            @Param("jlptLevel") Byte jlptLevel,
            @Param("posMask") Integer posMask,
            @Param("limit") int limit
    );

    @Query(value = "SELECT d.entry_id "
            + "FROM dictionary_search_index d "
            + "WHERE (:jlptLevel IS NULL OR d.jlpt_level = :jlptLevel) "
            + "  AND (:posMask IS NULL OR (d.pos_mask & :posMask) > 0) "
            + "ORDER BY COALESCE(d.frequency_rank, 999999) ASC, d.jlpt_level DESC, d.entry_id ASC "
            + "LIMIT :limit",
            nativeQuery = true)
    List<Long> findIdsByFilters(
            @Param("jlptLevel") Byte jlptLevel,
            @Param("posMask") Integer posMask,
            @Param("limit") int limit
    );

    @Query(value = "(SELECT d.entry_id, d.primary_form, d.primary_reading, "
            + "         d.primary_romaji, d.primary_meaning, d.jlpt_level "
            + " FROM dictionary_search_index d "
            + " WHERE d.primary_form LIKE :prefixPattern ESCAPE '!' "
            + " ORDER BY COALESCE(d.frequency_rank, 999999) ASC, d.primary_form ASC "
            + " LIMIT :limit) "
            + "UNION "
            + "(SELECT d.entry_id, d.primary_form, d.primary_reading, "
            + "         d.primary_romaji, d.primary_meaning, d.jlpt_level "
            + " FROM dictionary_search_index d "
            + " WHERE d.primary_reading LIKE :prefixPattern ESCAPE '!' "
            + " ORDER BY COALESCE(d.frequency_rank, 999999) ASC, d.primary_reading ASC "
            + " LIMIT :limit) "
            + "UNION "
            + "(SELECT d.entry_id, d.primary_form, d.primary_reading, "
            + "         d.primary_romaji, d.primary_meaning, d.jlpt_level "
            + " FROM dictionary_search_index d "
            + " WHERE d.primary_romaji LIKE :prefixPattern ESCAPE '!' "
            + " ORDER BY COALESCE(d.frequency_rank, 999999) ASC, d.primary_romaji ASC "
            + " LIMIT :limit) "
            + "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findSuggestionRowsPrefix(
            @Param("prefixPattern") String prefixPattern,
            @Param("limit") int limit
    );

    @Query(value = "SELECT d.entry_id, d.primary_form, d.primary_reading, "
            + "       d.primary_romaji, d.primary_meaning, d.jlpt_level "
            + "FROM dictionary_search_index d "
            + "INNER JOIN ("
            + "  SELECT ranked.id, MAX(ranked.score) AS max_score, MIN(ranked.freq) AS min_freq "
            + "  FROM ("
            + "    (SELECT entry_id AS id, "
            + "            (MATCH(forms_text, readings_text) AGAINST(:booleanQuery IN BOOLEAN MODE) * 3.0) AS score, "
            + "            COALESCE(frequency_rank, 999999) AS freq "
            + "     FROM dictionary_search_index "
            + "     WHERE MATCH(forms_text, readings_text) AGAINST(:booleanQuery IN BOOLEAN MODE) "
            + "     ORDER BY score DESC, freq ASC LIMIT :limit) "
            + "    UNION ALL "
            + "    (SELECT entry_id AS id, "
            + "            (MATCH(romaji_text) AGAINST(:asciiBooleanQuery IN BOOLEAN MODE) * 2.0) AS score, "
            + "            COALESCE(frequency_rank, 999999) AS freq "
            + "     FROM dictionary_search_index "
            + "     WHERE MATCH(romaji_text) AGAINST(:asciiBooleanQuery IN BOOLEAN MODE) "
            + "     ORDER BY score DESC, freq ASC LIMIT :limit) "
            + "    UNION ALL "
            + "    (SELECT entry_id AS id, "
            + "            (MATCH(meanings_text, meanings_ascii) "
            + "             AGAINST(:asciiBooleanQuery IN BOOLEAN MODE) * 1.0) AS score, "
            + "            COALESCE(frequency_rank, 999999) AS freq "
            + "     FROM dictionary_search_index "
            + "     WHERE MATCH(meanings_text, meanings_ascii) AGAINST(:asciiBooleanQuery IN BOOLEAN MODE) "
            + "     ORDER BY score DESC, freq ASC LIMIT :limit)"
            + "  ) ranked "
            + "  GROUP BY ranked.id "
            + "  ORDER BY max_score DESC, min_freq ASC, ranked.id ASC "
            + "  LIMIT :limit"
            + ") top_ids ON top_ids.id = d.entry_id "
            + "ORDER BY top_ids.max_score DESC, top_ids.min_freq ASC, d.entry_id ASC",
            nativeQuery = true)
    List<Object[]> findSuggestionRowsFulltext(
            @Param("booleanQuery") String booleanQuery,
            @Param("asciiBooleanQuery") String asciiBooleanQuery,
            @Param("limit") int limit
    );

    @Query(value = "SELECT d.entry_id, d.primary_form, d.primary_reading, "
            + "       d.primary_romaji, d.primary_meaning, d.jlpt_level "
            + "FROM dictionary_search_index d "
            + "WHERE d.forms_text LIKE :containsPattern ESCAPE '!' "
            + "   OR d.readings_text LIKE :containsPattern ESCAPE '!' "
            + "   OR d.romaji_text LIKE :containsPattern ESCAPE '!' "
            + "   OR d.meanings_text LIKE :containsPattern ESCAPE '!' "
            + "   OR d.meanings_ascii LIKE :containsPattern ESCAPE '!' "
            + "ORDER BY COALESCE(d.frequency_rank, 999999) ASC, d.jlpt_level DESC, d.entry_id ASC "
            + "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findSuggestionRowsSingleChar(
            @Param("containsPattern") String containsPattern,
            @Param("limit") int limit
    );
}

