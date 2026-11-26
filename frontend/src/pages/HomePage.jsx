import { useEffect } from 'react';
import SearchBar from '../components/dictionary/SearchBar.jsx';
import WordCard from '../components/dictionary/WordCard.jsx';
import SearchResultsList from '../components/dictionary/SearchResultsList.jsx';
import TranslatorWidget from '../components/dictionary/TranslatorWidget.jsx';
import SidebarWidgets from '../components/dictionary/SidebarWidgets.jsx';
import dictionaryService from '../services/dictionaryService.js';
import useDictionarySearch from '../hooks/useDictionarySearch.js';

const DEFAULT_HOME_HISTORY = [
  { id: 1, term: '木漏れ日', tag: 'N2' },
  { id: 2, term: '食べる', tag: 'N5' },
  { id: 3, term: '日本語', tag: 'N5' },
  { id: 4, term: '親切', tag: 'N4' },
];

export default function HomePage() {
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
    initialQuery: '木漏れ日',
    defaultHistory: DEFAULT_HOME_HISTORY,
  });

  // Initial load
  useEffect(() => {
    handleSearch('木漏れ日');
    dictionaryService.getTrendingKanji().then((data) => {
      if (data && data.length > 0) {
        setTrendingKanji(data);
      }
    });
  }, []);

  return (
    <>
      {/* 1. HERO SEARCH & INPUT CONTROL SECTION */}
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

      {/* 2 & 3. MAIN CONTENT: 12-COLUMN ASYMMETRIC BENTO GRID */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-space-lg items-start">
        {/* LEFT & CENTER VIEW (8 COLS): RICH DETAILED DEFINITION */}
        <section className="lg:col-span-8 flex flex-col space-y-space-lg">
          {/* Multi-result selector */}
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

        {/* RIGHT COLUMN (4 COLS): DUAL TRANSLATOR & SEARCH RADAR */}
        <aside className="lg:col-span-4 flex flex-col space-y-space-lg">
          <TranslatorWidget />
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
    </>
  );
}
