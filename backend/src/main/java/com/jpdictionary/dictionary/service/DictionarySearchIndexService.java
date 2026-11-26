package com.jpdictionary.dictionary.service;

import java.util.Collection;

public interface DictionarySearchIndexService {

    void upsertEntryIndex(Long entryId);

    void upsertBatch(Collection<Long> entryIds);

    int syncPendingRows();

    int reindexAll();
}

