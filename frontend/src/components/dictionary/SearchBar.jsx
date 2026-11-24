import { useState, useEffect, useRef } from 'react';
import Icon from '../common/Icon.jsx';
import dictionaryService from '../../services/dictionaryService.js';

export default function SearchBar({
  searchTerm = '',
  onSearchChange,
  onSearchSubmit,
  selectedJlpt = 'all',
  onJlptChange,
  selectedType = 'all',
  onTypeChange,
  isLoading = false,
}) {
  const [searchMode, setSearchMode] = useState('vocab'); // 'vocab' | 'translate'
  const [imeMode, setImeMode] = useState('romaji'); // 'romaji' | 'kana' | 'kanji'
  const [localInput, setLocalInput] = useState(searchTerm);
  const [suggestions, setSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    setLocalInput(searchTerm);
  }, [searchTerm]);

  // Debounced autocomplete suggestions fetch
  useEffect(() => {
    const trimmed = localInput.trim();
    if (!trimmed || trimmed.length < 1) {
      setSuggestions([]);
      setShowDropdown(false);
      return;
    }

    const timer = setTimeout(async () => {
      const items = await dictionaryService.getSuggestions(trimmed);
      setSuggestions(items);
      setShowDropdown(items.length > 0);
    }, 250);

    return () => clearTimeout(timer);
  }, [localInput]);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const jlptLevels = [
    { id: 'all', label: 'Tất cả' },
    { id: 'N5', label: 'N5' },
    { id: 'N4', label: 'N4' },
    { id: 'N3', label: 'N3' },
    { id: 'N2', label: 'N2' },
    { id: 'N1', label: 'N1' },
  ];

  const wordTypes = [
    { id: 'all', label: 'Tất cả' },
    { id: 'noun', label: 'Danh từ' },
    { id: 'verb', label: 'Động từ' },
    { id: 'adj', label: 'Tính từ' },
    { id: 'idiom', label: 'Quán dụng ngữ' },
  ];

  const handleInputChange = (e) => {
    const val = e.target.value;
    setLocalInput(val);
    if (onSearchChange) {
      onSearchChange(val);
    }
  };

  const handleClear = () => {
    setLocalInput('');
    setSuggestions([]);
    setShowDropdown(false);
    if (onSearchChange) {
      onSearchChange('');
    }
  };

  const handleSubmit = (e) => {
    if (e) e.preventDefault();
    setShowDropdown(false);
    if (onSearchSubmit) {
      onSearchSubmit(localInput, { jlpt: selectedJlpt, type: selectedType });
    }
  };

  const handleSelectSuggestion = (item) => {
    setLocalInput(item.term);
    setShowDropdown(false);
    if (onSearchChange) {
      onSearchChange(item.term);
    }
    if (onSearchSubmit) {
      onSearchSubmit(item.term, { jlpt: selectedJlpt, type: selectedType });
    }
  };

  return (
    <section className="w-full bg-surface-container-lowest rounded-xl border border-surface-container-high p-space-lg shadow-sm">
      <div className="flex flex-col space-y-space-md">
        {/* Search Mode Tabs & Keyboard Triggers */}
        <div className="flex flex-wrap items-center justify-between gap-space-sm">
          <div className="flex items-center bg-surface-container p-1 rounded-lg">
            <button
              type="button"
              onClick={() => setSearchMode('vocab')}
              className={`px-space-md py-1 rounded text-label-md font-label-md transition-colors ${
                searchMode === 'vocab'
                  ? 'bg-surface-container-lowest text-primary font-bold shadow-xs'
                  : 'text-on-surface-variant hover:text-primary'
              }`}
            >
              Tra từ vựng
            </button>
            <button
              type="button"
              onClick={() => setSearchMode('translate')}
              className={`px-space-md py-1 rounded text-label-md font-label-md transition-colors ${
                searchMode === 'translate'
                  ? 'bg-surface-container-lowest text-primary font-bold shadow-xs'
                  : 'text-on-surface-variant hover:text-primary'
              }`}
            >
              Dịch câu / Đoạn văn
            </button>
          </div>

          <div className="flex items-center space-x-space-sm text-label-sm font-label-sm text-on-surface-variant">
            <span className="hidden sm:inline">Bộ gõ hỗ trợ:</span>
            <div className="inline-flex rounded-lg border border-surface-container-high overflow-hidden bg-surface-container-low">
              <button
                type="button"
                onClick={() => setImeMode('romaji')}
                className={`px-2 py-1 transition-colors ${
                  imeMode === 'romaji'
                    ? 'bg-secondary text-on-secondary font-bold'
                    : 'text-on-surface hover:bg-surface-container'
                }`}
              >
                Romaji
              </button>
              <button
                type="button"
                onClick={() => setImeMode('kana')}
                className={`px-2 py-1 transition-colors ${
                  imeMode === 'kana'
                    ? 'bg-secondary text-on-secondary font-bold'
                    : 'text-on-surface hover:bg-surface-container'
                }`}
              >
                かな
              </button>
              <button
                type="button"
                onClick={() => setImeMode('kanji')}
                className={`px-2 py-1 transition-colors ${
                  imeMode === 'kanji'
                    ? 'bg-secondary text-on-secondary font-bold'
                    : 'text-on-surface hover:bg-surface-container'
                }`}
              >
                漢字
              </button>
            </div>
            <button
              type="button"
              className="flex items-center space-x-1 px-2.5 py-1 rounded border border-surface-container-high hover:border-outline text-on-surface bg-surface-container-lowest"
            >
              <Icon name="keyboard" className="text-base" />
              <span className="hidden md:inline">Bàn phím Ảo</span>
            </button>
          </div>
        </div>

        {/* Main Input Bar with Autocomplete Dropdown */}
        <form onSubmit={handleSubmit} className="relative flex items-center" ref={dropdownRef}>
          <div className="absolute left-4 flex items-center pointer-events-none text-primary">
            <Icon name="search" className="text-2xl" />
          </div>
          <input
            type="text"
            value={localInput}
            onChange={handleInputChange}
            onFocus={() => suggestions.length > 0 && setShowDropdown(true)}
            placeholder="Nhập từ vựng, kanji, mẫu ngữ pháp hoặc câu cần tra (ví dụ: komorebi, こもれび, ánh nắng qua lá)..."
            className="w-full pl-12 pr-32 py-3.5 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-headline-sm font-headline-sm text-on-surface placeholder:text-on-surface-variant/50 placeholder:text-body-md transition-all shadow-sm"
          />
          <div className="absolute right-3 flex items-center space-x-1">
            <button
              type="button"
              className="p-2 text-on-surface-variant hover:text-primary hover:bg-surface-container rounded-full transition-colors"
              title="Nhận diện giọng nói"
            >
              <Icon name="mic" className="text-xl" />
            </button>
            {localInput && (
              <button
                type="button"
                onClick={handleClear}
                className="p-2 text-on-surface-variant hover:text-error hover:bg-surface-container rounded-full transition-colors"
                title="Xóa nội dung"
              >
                <Icon name="cancel" className="text-xl" />
              </button>
            )}
            <button
              type="submit"
              disabled={isLoading}
              className="bg-primary text-on-primary px-space-md py-2 rounded-lg font-label-md text-label-md hover:bg-primary-container transition-all flex items-center space-x-1 shadow-sm disabled:opacity-50"
            >
              {isLoading ? (
                <span>Đang tìm...</span>
              ) : (
                <>
                  <Icon name="search" className="text-base" />
                  <span>Tìm kiếm</span>
                </>
              )}
            </button>
          </div>

          {/* Autocomplete Suggestions Floating Dropdown */}
          {showDropdown && suggestions.length > 0 && (
            <div className="absolute top-full left-0 right-0 mt-1.5 bg-surface-container-lowest border border-surface-container-high rounded-xl shadow-xl z-30 overflow-hidden divide-y divide-surface-container-high/60">
              <div className="px-4 py-2 text-xs font-semibold text-outline bg-surface-container-low flex items-center justify-between">
                <span>Gợi ý từ vựng phù hợp</span>
                <span className="text-[11px] font-normal text-on-surface-variant">
                  Nhấn Enter hoặc chọn từ
                </span>
              </div>
              <div className="max-h-72 overflow-y-auto">
                {suggestions.map((item) => (
                  <button
                    key={`${item.id}-${item.term}`}
                    type="button"
                    onClick={() => handleSelectSuggestion(item)}
                    className="w-full text-left px-4 py-3 hover:bg-surface-container flex items-center justify-between gap-space-md transition-colors"
                  >
                    <div className="flex items-center space-x-3">
                      <span className="font-kanji-headline text-lg font-bold text-primary">
                        {item.term}
                      </span>
                      {item.reading && item.reading !== item.term && (
                        <span className="text-body-sm font-medium text-on-surface-variant">
                          【{item.reading}】
                        </span>
                      )}
                      {item.romaji && (
                        <span className="text-label-sm text-outline italic">({item.romaji})</span>
                      )}
                    </div>
                    <div className="flex items-center space-x-2 shrink-0">
                      <span className="text-body-sm text-on-surface line-clamp-1 max-w-[220px]">
                        {item.meaning}
                      </span>
                      {item.jlpt && (
                        <span className="px-2 py-0.5 rounded text-xs font-bold bg-surface-container-highest text-primary">
                          {item.jlpt}
                        </span>
                      )}
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}
        </form>

        {/* Quick Filters Bar (JLPT + Word Types) */}
        <div className="flex flex-wrap items-center justify-between gap-space-sm pt-1 text-label-sm font-label-sm border-t border-surface-container-high">
          {/* JLPT Filter */}
          <div className="flex items-center space-x-1">
            <span className="text-on-surface-variant mr-1">Cấp độ JLPT:</span>
            {jlptLevels.map((lvl) => (
              <button
                key={lvl.id}
                type="button"
                onClick={() => onJlptChange && onJlptChange(lvl.id)}
                className={`px-2 py-0.5 rounded transition-colors ${
                  selectedJlpt === lvl.id
                    ? 'bg-primary-fixed text-on-primary-fixed font-bold border border-outline-variant shadow-xs'
                    : 'text-on-surface-variant hover:bg-surface-container-low'
                }`}
              >
                {lvl.label}
              </button>
            ))}
          </div>

          {/* Lexical Type Filters */}
          <div className="flex items-center space-x-1">
            <span className="text-on-surface-variant mr-1">Loại từ:</span>
            {wordTypes.map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => onTypeChange && onTypeChange(item.id)}
                className={`px-2 py-0.5 rounded transition-colors ${
                  selectedType === item.id
                    ? 'bg-secondary-fixed text-on-secondary-fixed-variant font-medium border border-outline-variant shadow-xs'
                    : 'text-on-surface-variant hover:bg-surface-container-low'
                }`}
              >
                {item.label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
