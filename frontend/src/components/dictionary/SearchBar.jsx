import { useState } from 'react';
import Icon from '../common/Icon.jsx';

export default function SearchBar() {
  const [searchMode, setSearchMode] = useState('vocab'); // 'vocab' | 'translate'
  const [imeMode, setImeMode] = useState('romaji'); // 'romaji' | 'kana' | 'kanji'
  const [searchTerm, setSearchTerm] = useState('木漏れ日');
  const [selectedJlpt, setSelectedJlpt] = useState('N2');
  const [selectedType, setSelectedType] = useState('noun');

  const jlptLevels = [
    { id: 'all', label: 'Tất cả' },
    { id: 'N5', label: 'N5' },
    { id: 'N4', label: 'N4' },
    { id: 'N3', label: 'N3' },
    { id: 'N2', label: 'N2' },
    { id: 'N1', label: 'N1' },
  ];

  const wordTypes = [
    { id: 'noun', label: 'Danh từ' },
    { id: 'verb', label: 'Động từ' },
    { id: 'adj', label: 'Tính từ' },
    { id: 'idiom', label: 'Quán dụng ngữ' },
  ];

  const handleClear = () => {
    setSearchTerm('');
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

        {/* Main Input Bar */}
        <div className="relative flex items-center">
          <div className="absolute left-4 flex items-center pointer-events-none text-primary">
            <Icon name="search" className="text-2xl" />
          </div>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Nhập từ vựng, kanji, mẫu ngữ pháp hoặc câu cần tra (ví dụ: komorebi, こもれび, ánh nắng qua lá)..."
            className="w-full pl-12 pr-28 py-3.5 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-headline-sm font-headline-sm text-on-surface placeholder:text-on-surface-variant/50 placeholder:text-body-md transition-all shadow-sm"
          />
          <div className="absolute right-3 flex items-center space-x-1">
            <button
              type="button"
              className="p-2 text-on-surface-variant hover:text-primary hover:bg-surface-container rounded-full transition-colors"
              title="Nhận diện giọng nói"
            >
              <Icon name="mic" className="text-xl" />
            </button>
            {searchTerm && (
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
              type="button"
              className="bg-primary text-on-primary px-space-md py-2 rounded-lg font-label-md text-label-md hover:bg-primary-container transition-all flex items-center space-x-1 shadow-sm"
            >
              <span>Tìm kiếm</span>
            </button>
          </div>
        </div>

        {/* Quick Filters Bar (JLPT + Word Types) */}
        <div className="flex flex-wrap items-center justify-between gap-space-sm pt-1 text-label-sm font-label-sm border-t border-surface-container-high">
          {/* JLPT Filter */}
          <div className="flex items-center space-x-1">
            <span className="text-on-surface-variant mr-1">Cấp độ JLPT:</span>
            {jlptLevels.map((lvl) => (
              <button
                key={lvl.id}
                type="button"
                onClick={() => setSelectedJlpt(lvl.id)}
                className={`px-2 py-0.5 rounded transition-colors ${
                  selectedJlpt === lvl.id
                    ? lvl.id === 'N2'
                      ? 'bg-primary-fixed text-on-primary-fixed font-bold border border-outline-variant'
                      : 'bg-surface-container-highest text-on-surface font-semibold'
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
                onClick={() => setSelectedType(item.id)}
                className={`px-2 py-0.5 rounded transition-colors ${
                  selectedType === item.id
                    ? 'bg-surface-container-low text-secondary font-medium hover:bg-surface-container'
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
