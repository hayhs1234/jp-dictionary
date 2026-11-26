import { useState, useCallback, useRef } from 'react';
import dictionaryService from '../services/dictionaryService.js';

const STORAGE_KEY = 'jp_dict_history';

function loadInitialHistory(defaultHistory) {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) return JSON.parse(saved);
  } catch {
    // ignore
  }
  return defaultHistory;
}

function isFullWordDetail(item) {
  return Boolean(item && Array.isArray(item.senses));
}

export default function useDictionarySearch({
  initialQuery = '',
  initialJlpt = 'all',
  initialType = 'all',
  defaultHistory = [],
  onSyncUrlParams = null,
} = {}) {
  const [searchTerm, setSearchTerm] = useState(initialQuery);
  const [selectedJlpt, setSelectedJlpt] = useState(initialJlpt);
  const [selectedType, setSelectedType] = useState(initialType);
  const [searchResults, setSearchResults] = useState([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [currentWord, setCurrentWord] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [trendingKanji, setTrendingKanji] = useState([]);
  const [historyItems, setHistoryItems] = useState(() => loadInitialHistory(defaultHistory));

  const detailCacheRef = useRef(new Map());
  const detailRequestSeqRef = useRef(0);
  const searchRequestSeqRef = useRef(0);

  const saveHistory = useCallback((term, tag = null) => {
    if (!term || !term.trim()) return;
    const clean = term.trim();
    setHistoryItems((prev) => {
      const filtered = prev.filter((item) => {
        const text = typeof item === 'string' ? item : item.term;
        return text !== clean;
      });
      const updated = [{ id: Date.now(), term: clean, tag: tag || 'Tra cứu' }, ...filtered].slice(
        0,
        10
      );
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
      } catch {
        // ignore
      }
      return updated;
    });
  }, []);

  const clearHistory = useCallback(() => {
    setHistoryItems([]);
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // ignore
    }
  }, []);

  const loadWordDetail = useCallback(async (summaryItem) => {
    const seq = ++detailRequestSeqRef.current;
    if (!summaryItem) {
      setCurrentWord(null);
      setIsDetailLoading(false);
      return;
    }

    if (isFullWordDetail(summaryItem)) {
      detailCacheRef.current.set(summaryItem.id, summaryItem);
      setCurrentWord(summaryItem);
      setIsDetailLoading(false);
      return;
    }

    const cached = detailCacheRef.current.get(summaryItem.id);
    if (cached) {
      setCurrentWord(cached);
      setIsDetailLoading(false);
      return;
    }

    setIsDetailLoading(true);
    try {
      const detail = await dictionaryService.getWordDetail(summaryItem.id);
      if (seq !== detailRequestSeqRef.current) return;
      if (detail) {
        detailCacheRef.current.set(summaryItem.id, detail);
        setCurrentWord(detail);
      } else {
        setCurrentWord(summaryItem);
      }
    } catch {
      if (seq === detailRequestSeqRef.current) {
        setCurrentWord(summaryItem);
      }
    } finally {
      if (seq === detailRequestSeqRef.current) {
        setIsDetailLoading(false);
      }
    }
  }, []);

  const handleSearch = useCallback(
    async (query, filters = {}) => {
      const termToSearch = query !== undefined ? query : searchTerm;
      const jlptToUse = filters.jlpt !== undefined ? filters.jlpt : selectedJlpt;
      const typeToUse = filters.type !== undefined ? filters.type : selectedType;

      if (onSyncUrlParams) {
        onSyncUrlParams(termToSearch, jlptToUse, typeToUse);
      }

      const searchSeq = ++searchRequestSeqRef.current;
      setIsLoading(true);
      try {
        const results = await dictionaryService.search({
          q: termToSearch,
          jlpt: jlptToUse,
          type: typeToUse,
        });

        if (searchSeq !== searchRequestSeqRef.current) return;

        if (results && results.length > 0) {
          setSearchResults(results);
          setSelectedIndex(0);
          if (termToSearch.trim()) {
            saveHistory(results[0].kanji || termToSearch, results[0].jlptLevel);
          }
          await loadWordDetail(results[0]);
        } else {
          detailRequestSeqRef.current++;
          setSearchResults([]);
          setSelectedIndex(0);
          setCurrentWord(null);
          setIsDetailLoading(false);
        }
      } catch {
        if (searchSeq === searchRequestSeqRef.current) {
          detailRequestSeqRef.current++;
          setSearchResults([]);
          setSelectedIndex(0);
          setCurrentWord(null);
          setIsDetailLoading(false);
        }
      } finally {
        if (searchSeq === searchRequestSeqRef.current) {
          setIsLoading(false);
        }
      }
    },
    [searchTerm, selectedJlpt, selectedType, saveHistory, loadWordDetail, onSyncUrlParams]
  );

  const handleSelectIndex = useCallback(
    (index) => {
      setSelectedIndex(index);
      const selected = searchResults[index];
      if (selected) {
        saveHistory(selected.kanji || searchTerm, selected.jlptLevel);
        loadWordDetail(selected);
      }
    },
    [searchResults, searchTerm, saveHistory, loadWordDetail]
  );

  const handleJlptChange = useCallback(
    (lvl) => {
      setSelectedJlpt(lvl);
      handleSearch(searchTerm, { jlpt: lvl, type: selectedType });
    },
    [handleSearch, searchTerm, selectedType]
  );

  const handleTypeChange = useCallback(
    (t) => {
      setSelectedType(t);
      handleSearch(searchTerm, { jlpt: selectedJlpt, type: t });
    },
    [handleSearch, searchTerm, selectedJlpt]
  );

  return {
    searchTerm,
    setSearchTerm,
    selectedJlpt,
    selectedType,
    searchResults,
    selectedIndex,
    currentWord,
    isLoading,
    isDetailLoading,
    trendingKanji,
    setTrendingKanji,
    historyItems,
    clearHistory,
    handleSearch,
    handleSelectIndex,
    handleJlptChange,
    handleTypeChange,
  };
}

