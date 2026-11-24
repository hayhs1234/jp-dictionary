package com.jpdictionary.dictionary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "kanji_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanjiEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "character_text", nullable = false, unique = true, length = 10)
    private String characterText;

    @Column(name = "stroke_count", nullable = false)
    private Integer strokeCount;

    @Column(name = "jlpt_level", length = 20)
    private String jlptLevel;

    @Column(name = "han_viet", nullable = false, length = 100)
    private String hanViet;

    @Column(name = "on_readings")
    private String onReadings;

    @Column(name = "kun_readings")
    private String kunReadings;

    @Column(name = "meaning", nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(name = "radicals", length = 50)
    private String radicals;

    @Column(name = "frequency_rank")
    private Integer frequencyRank;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

