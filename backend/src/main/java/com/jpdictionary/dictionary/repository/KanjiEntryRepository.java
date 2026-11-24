package com.jpdictionary.dictionary.repository;

import com.jpdictionary.dictionary.entity.KanjiEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface KanjiEntryRepository extends JpaRepository<KanjiEntry, Long> {

    Optional<KanjiEntry> findByCharacterText(String characterText);

    List<KanjiEntry> findByCharacterTextIn(Collection<String> characterTexts);

    @Query("SELECT k FROM KanjiEntry k WHERE k.frequencyRank IS NOT NULL ORDER BY k.frequencyRank ASC")
    List<KanjiEntry> findTopTrendingKanji(Pageable pageable);
}

