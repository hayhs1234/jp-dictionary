package com.jpdictionary.dictionary.repository;

import com.jpdictionary.dictionary.entity.DictionaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, Long> {

    @Query("SELECT DISTINCT e FROM DictionaryEntry e "
            + "LEFT JOIN FETCH e.forms f "
            + "LEFT JOIN FETCH e.readings r "
            + "WHERE e.id = :id")
    Optional<DictionaryEntry> findByIdWithFormsAndReadings(@Param("id") Long id);

    List<DictionaryEntry> findAllByIdIn(Collection<Long> ids);

    @Query(value = "(SELECT f.entry_id AS id "
            + " FROM dictionary_forms f "
            + " INNER JOIN dictionary_entries e ON e.id = f.entry_id "
            + " WHERE f.written_form IN (:terms) "
            + "   AND (:jlptLevel IS NULL OR e.jlpt_level = :jlptLevel) "
            + "   AND (:posCategory IS NULL OR EXISTS ("
            + "       SELECT 1 FROM dictionary_senses s "
            + "       INNER JOIN dictionary_sense_pos_categories pc ON pc.sense_id = s.id "
            + "       WHERE s.entry_id = e.id AND pc.pos_category = :posCategory)) "
            + " ORDER BY f.is_primary DESC, COALESCE(e.frequency_rank, 999999) ASC, f.entry_id ASC "
            + " LIMIT :limit) "
            + "UNION "
            + "(SELECT r.entry_id AS id "
            + " FROM dictionary_readings r "
            + " INNER JOIN dictionary_entries e ON e.id = r.entry_id "
            + " WHERE r.reading IN (:terms) "
            + "   AND (:jlptLevel IS NULL OR e.jlpt_level = :jlptLevel) "
            + "   AND (:posCategory IS NULL OR EXISTS ("
            + "       SELECT 1 FROM dictionary_senses s "
            + "       INNER JOIN dictionary_sense_pos_categories pc ON pc.sense_id = s.id "
            + "       WHERE s.entry_id = e.id AND pc.pos_category = :posCategory)) "
            + " ORDER BY r.is_primary DESC, COALESCE(e.frequency_rank, 999999) ASC, r.entry_id ASC "
            + " LIMIT :limit) "
            + "UNION "
            + "(SELECT r.entry_id AS id "
            + " FROM dictionary_readings r "
            + " INNER JOIN dictionary_entries e ON e.id = r.entry_id "
            + " WHERE r.romaji IN (:terms) "
            + "   AND (:jlptLevel IS NULL OR e.jlpt_level = :jlptLevel) "
            + "   AND (:posCategory IS NULL OR EXISTS ("
            + "       SELECT 1 FROM dictionary_senses s "
            + "       INNER JOIN dictionary_sense_pos_categories pc ON pc.sense_id = s.id "
            + "       WHERE s.entry_id = e.id AND pc.pos_category = :posCategory)) "
            + " ORDER BY r.is_primary DESC, COALESCE(e.frequency_rank, 999999) ASC, r.entry_id ASC "
            + " LIMIT :limit) "
            + "LIMIT :limit",
            nativeQuery = true)
    List<Long> findExactMatchIdsByTerms(
            @Param("terms") Collection<String> terms,
            @Param("jlptLevel") Byte jlptLevel,
            @Param("posCategory") String posCategory,
            @Param("limit") int limit
    );
}
