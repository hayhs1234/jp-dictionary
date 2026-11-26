import { useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import SearchBar from '../components/dictionary/SearchBar.jsx';
import WordCard from '../components/dictionary/WordCard.jsx';
import SearchResultsList from '../components/dictionary/SearchResultsList.jsx';
import SidebarWidgets from '../components/dictionary/SidebarWidgets.jsx';
import dictionaryService from '../services/dictionaryService.js';
import useDictionarySearch from '../hooks/useDictionarySearch.js';

export default function SearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQuery = searchParams.get('q') || '';
  const initialJlpt = searchParams.get('jlpt') || 'all';
  const initialType = searchParams.get('type') || 'all';

  const syncUrlParams = useCallback(
    (term, jlpt, type) => {
      const newParams = {};
      if (term.trim()) newParams.q = term.trim();
      if (jlpt !== 'all') newParams.jlpt = jlpt;
      if (type !== 'all') newParams.type = type;
      setSearchParams(newParams, { replace: true });
    },
    [setSearchParams]
  );

  const {
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
  } = useDictionarySearch({
    initialQuery,
    initialJlpt,
    initialType,
    onSyncUrlParams: syncUrlParams,
  });

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
          <WordCard word={currentWord} isLoading={isLoading || isDetailLoading} />
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
