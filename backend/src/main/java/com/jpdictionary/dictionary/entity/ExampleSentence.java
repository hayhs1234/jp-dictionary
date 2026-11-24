package com.jpdictionary.dictionary.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "example_sentences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleSentence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private DictionaryEntry entry;

    @Column(name = "japanese_text", nullable = false, columnDefinition = "TEXT")
    private String japaneseText;

    @Column(name = "reading", columnDefinition = "TEXT")
    private String reading;

    @Column(name = "source_code", length = 50)
    private String sourceCode;

    @OneToMany(mappedBy = "exampleSentence", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExampleTranslation> translations = new ArrayList<>();
}

