package com.jpdictionary.dictionary.repository;

import com.jpdictionary.dictionary.entity.DictionaryForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictionaryFormRepository extends JpaRepository<DictionaryForm, Long> {

    List<DictionaryForm> findByWrittenForm(String writtenForm);
}

