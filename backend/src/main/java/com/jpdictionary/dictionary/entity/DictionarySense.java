package com.jpdictionary.dictionary.entity;

import com.jpdictionary.dictionary.enums.PosCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "dictionary_senses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictionarySense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private DictionaryEntry entry;

    @Column(name = "sense_order", nullable = false)
    @Builder.Default
    private Integer senseOrder = 0;

    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "dictionary_sense_pos_categories",
            joinColumns = @JoinColumn(name = "sense_id", nullable = false)
    )
    @Column(name = "pos_category", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @BatchSize(size = 50)
    @Builder.Default
    private Set<PosCategory> posCategories = new LinkedHashSet<>();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "sense", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<SenseGloss> glosses = new ArrayList<>();

    /**
     * Update raw partOfSpeech and immediately re-derive normalized posCategories.
     */
    public void assignPartOfSpeech(String newPartOfSpeech) {
        this.partOfSpeech = newPartOfSpeech;
        this.posCategories = new LinkedHashSet<>(PosCategory.inferCategorySet(newPartOfSpeech));
    }

    /**
     * Ensure posCategories is populated and contains only storable PosCategory values.
     */
    @PrePersist
    @PreUpdate
    public void normalizeAndValidatePosCategories() {
        if (this.posCategories == null || this.posCategories.isEmpty()) {
            this.posCategories = new LinkedHashSet<>(PosCategory.inferCategorySet(this.partOfSpeech));
        }
        for (PosCategory category : this.posCategories) {
            if (category == null || !category.isStorable()) {
                throw new IllegalArgumentException("Invalid storable PosCategory: " + category);
            }
        }
    }
}

