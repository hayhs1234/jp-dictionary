package com.jpdictionary.dictionary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dictionary_search_index")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictionarySearchIndex {

    @Id
    @Column(name = "entry_id", nullable = false)
    private Long entryId;

    @Column(name = "jlpt_level")
    private Byte jlptLevel;

    @Column(name = "pos_mask", nullable = false)
    @Builder.Default
    private Integer posMask = 0;

    @Column(name = "frequency_rank")
    private Integer frequencyRank;

    @Column(name = "primary_form", nullable = false, length = 255)
    @Builder.Default
    private String primaryForm = "";

    @Column(name = "primary_reading", nullable = false, length = 255)
    @Builder.Default
    private String primaryReading = "";

    @Column(name = "primary_romaji", nullable = false, length = 255)
    @Builder.Default
    private String primaryRomaji = "";

    @Column(name = "primary_meaning", nullable = false, length = 500)
    @Builder.Default
    private String primaryMeaning = "";

    @Column(name = "forms_text", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String formsText = "";

    @Column(name = "readings_text", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String readingsText = "";

    @Column(name = "romaji_text", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String romajiText = "";

    @Column(name = "meanings_text", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String meaningsText = "";

    @Column(name = "meanings_ascii", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String meaningsAscii = "";

    @Column(name = "ascii_synced", nullable = false)
    @Builder.Default
    private Boolean asciiSynced = Boolean.TRUE;

    @Column(name = "meili_synced", nullable = false)
    @Builder.Default
    private Boolean meiliSynced = Boolean.FALSE;

    @Column(name = "meili_synced_at")
    private LocalDateTime meiliSyncedAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}

