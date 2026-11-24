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

@Entity
@Table(name = "example_translations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "example_sentence_id", nullable = false)
    private ExampleSentence exampleSentence;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "translated_text", nullable = false, columnDefinition = "TEXT")
    private String translatedText;
}

