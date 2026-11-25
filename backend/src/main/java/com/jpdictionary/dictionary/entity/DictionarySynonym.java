package com.jpdictionary.dictionary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Related synonym or grammar pattern associated with a dictionary entry.
 */
@Entity
@Table(name = "dictionary_synonyms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictionarySynonym {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private DictionaryEntry entry;

    @Column(name = "word", nullable = false, length = 255)
    private String word;

    @Column(name = "reading", length = 255)
    private String reading;

    @Column(name = "meaning", nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(name = "relation_type", nullable = false, length = 20)
    @Builder.Default
    private String relationType = "synonym";

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}

