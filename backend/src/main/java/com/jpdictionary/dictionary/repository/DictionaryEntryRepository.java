package com.jpdictionary.dictionary.repository;

import com.jpdictionary.dictionary.entity.DictionaryEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, Long> {

    @Query("SELECT DISTINCT e FROM DictionaryEntry e "
            + "LEFT JOIN FETCH e.forms f "
            + "LEFT JOIN FETCH e.readings r "
            + "WHERE e.id = :id")
    Optional<DictionaryEntry> findByIdWithFormsAndReadings(@Param("id") Long id);

    @Query("SELECT DISTINCT e FROM DictionaryEntry e "
            + "JOIN e.forms f "
            + "LEFT JOIN e.readings r "
            + "LEFT JOIN e.senses s "
            + "WHERE (f.writtenForm = :term OR r.reading = :term OR LOWER(r.romaji) = LOWER(:term)) "
            + "AND (:jlptLevel IS NULL OR e.jlptLevel = :jlptLevel) "
            + "AND (:posPattern IS NULL OR LOWER(s.partOfSpeech) LIKE LOWER(CONCAT('%', :posPattern, '%')))")
    List<DictionaryEntry> findExactMatches(
            @Param("term") String term,
            @Param("jlptLevel") Byte jlptLevel,
            @Param("posPattern") String posPattern
    );

    @Query("SELECT DISTINCT e FROM DictionaryEntry e "
            + "LEFT JOIN e.forms f "
            + "LEFT JOIN e.readings r "
            + "LEFT JOIN e.senses s "
            + "LEFT JOIN s.glosses g "
            + "WHERE (:query IS NULL OR :query = '' "
            + "   OR LOWER(f.writtenForm) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "   OR LOWER(r.reading) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "   OR LOWER(r.romaji) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "   OR LOWER(g.meaning) LIKE LOWER(CONCAT('%', :query, '%'))) "
            + "AND (:jlptLevel IS NULL OR e.jlptLevel = :jlptLevel) "
            + "AND (:posPattern IS NULL OR LOWER(s.partOfSpeech) LIKE LOWER(CONCAT('%', :posPattern, '%')))")
    List<DictionaryEntry> searchEntries(
            @Param("query") String query,
            @Param("jlptLevel") Byte jlptLevel,
            @Param("posPattern") String posPattern,
            Pageable pageable
    );

    @Query("SELECT DISTINCT f.writtenForm, r.reading, r.romaji, g.meaning, e.id "
            + "FROM DictionaryEntry e "
            + "JOIN e.forms f "
            + "LEFT JOIN e.readings r "
            + "LEFT JOIN e.senses s "
            + "LEFT JOIN s.glosses g "
            + "WHERE f.isPrimary = true "
            + "AND (LOWER(f.writtenForm) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "  OR LOWER(r.reading) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "  OR LOWER(r.romaji) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "  OR LOWER(g.meaning) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Object[]> findSuggestionsRaw(@Param("query") String query, Pageable pageable);
}
