import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import SearchBar from '../components/dictionary/SearchBar.jsx';
import WordCard from '../components/dictionary/WordCard.jsx';
import SearchResultsList from '../components/dictionary/SearchResultsList.jsx';
import SidebarWidgets from '../components/dictionary/SidebarWidgets.jsx';
import dictionaryService from '../services/dictionaryService.js';

const STORAGE_KEY = 'jp_dict_history';

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQuery = searchParams.get('q') || '';
  const initialJlpt = searchParams.get('jlpt') || 'all';
  const initialType = searchParams.get('type') || 'all';

  const [searchTerm, setSearchTerm] = useState(initialQuery);
  const [selectedJlpt, setSelectedJlpt] = useState(initialJlpt);
  const [selectedType, setSelectedType] = useState(initialType);
  const [searchResults, setSearchResults] = useState([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [currentWord, setCurrentWord] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [trendingKanji, setTrendingKanji] = useState([]);
  const [historyItems, setHistoryItems] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) return JSON.parse(saved);
    } catch {
      // ignore
    }
    return [];
  });

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

  const handleSearch = useCallback(
    async (query, filters = {}) => {
      const termToSearch = query !== undefined ? query : searchTerm;
      const jlptToUse = filters.jlpt !== undefined ? filters.jlpt : selectedJlpt;
      const typeToUse = filters.type !== undefined ? filters.type : selectedType;

      // Update URL query params
      const newParams = {};
      if (termToSearch.trim()) newParams.q = termToSearch.trim();
      if (jlptToUse !== 'all') newParams.jlpt = jlptToUse;
      if (typeToUse !== 'all') newParams.type = typeToUse;
      setSearchParams(newParams, { replace: true });

      setIsLoading(true);
      try {
        const results = await dictionaryService.search({
          q: termToSearch,
          jlpt: jlptToUse,
          type: typeToUse,
        });

        if (results && results.length > 0) {
          setSearchResults(results);
          setSelectedIndex(0);
          setCurrentWord(results[0]);
          if (termToSearch.trim()) {
            saveHistory(results[0].kanji || termToSearch, results[0].jlptLevel);
          }
        } else {
          setSearchResults([]);
          setSelectedIndex(0);
          setCurrentWord(null);
        }
      } catch {
        setSearchResults([]);
        setSelectedIndex(0);
        setCurrentWord(null);
      } finally {
        setIsLoading(false);
      }
    },
    [searchTerm, selectedJlpt, selectedType, saveHistory, setSearchParams]
  );

  useEffect(() => {
    dictionaryService.getTrendingKanji().then((data) => {
      if (data && data.length > 0) {
        setTrendingKanji(data);
      }
    });

    if (initialQuery) {
      handleSearch(initialQuery, { jlpt: initialJlpt, type: initialType });
    }
  }, []);

  const handleSelectIndex = (index) => {
    setSelectedIndex(index);
    const selected = searchResults[index];
    if (selected) {
      setCurrentWord(selected);
      saveHistory(selected.kanji || searchTerm, selected.jlptLevel);
    }
  };

  const handleJlptChange = (lvl) => {
    setSelectedJlpt(lvl);
    handleSearch(searchTerm, { jlpt: lvl, type: selectedType });
  };

  const handleTypeChange = (t) => {
    setSelectedType(t);
    handleSearch(searchTerm, { jlpt: selectedJlpt, type: t });
  };

  return (
    <div className="space-y-space-lg">
      <SearchBar
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        onSearchSubmit={(term, filters) => handleSearch(term, filters)}
        selectedJlpt={selectedJlpt}
        onJlptChange={handleJlptChange}
        selectedType={selectedType}
        onTypeChange={handleTypeChange}
        isLoading={isLoading}
      />

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-space-lg items-start">
        <section className="lg:col-span-8 flex flex-col space-y-space-lg">
          {!isLoading && searchResults.length > 1 && (
            <SearchResultsList
              results={searchResults}
              selectedIndex={selectedIndex}
              onSelectIndex={handleSelectIndex}
              searchTerm={searchTerm}
            />
          )}
          <WordCard word={currentWord} isLoading={isLoading} />
        </section>

        <aside className="lg:col-span-4 flex flex-col space-y-space-lg">
          <SidebarWidgets
            historyItems={historyItems}
            onSelectHistory={(term) => {
              setSearchTerm(term);
              handleSearch(term);
            }}
            onClearHistory={clearHistory}
            trendingKanji={trendingKanji}
            currentWord={currentWord}
          />
        </aside>
      </div>
    </div>
  );
}
