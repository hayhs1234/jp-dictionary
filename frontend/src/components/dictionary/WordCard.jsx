import { useState } from 'react';
import Icon from '../common/Icon.jsx';
import KanjiCard from './KanjiCard.jsx';

export default function WordCard({ word, isLoading = false }) {
  const [deckMenuOpen, setDeckMenuOpen] = useState(false);
  const [selectedDeck, setSelectedDeck] = useState('Từ vựng N3-N2, Thiên nhiên');

  const handleSpeak = (text) => {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window && text) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'ja-JP';
      utterance.rate = 0.85;
      window.speechSynthesis.speak(utterance);
    }
  };

  // 1. Loading Skeleton State
  if (isLoading) {
    return (
      <article className="bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-lg shadow-sm space-y-space-lg animate-pulse">
        <div className="flex justify-between items-start">
          <div className="space-y-3">
            <div className="h-10 w-48 bg-surface-container rounded-lg" />
            <div className="flex gap-2">
              <div className="h-5 w-24 bg-surface-container rounded" />
              <div className="h-5 w-28 bg-surface-container rounded" />
            </div>
          </div>
          <div className="h-9 w-36 bg-surface-container rounded-lg" />
        </div>
        <div className="space-y-2 py-4">
          <div className="h-4 w-32 bg-surface-container rounded" />
          <div className="h-16 w-full bg-surface-container-low rounded-lg" />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-space-md">
          <div className="h-32 bg-surface-container-low rounded-xl" />
          <div className="h-32 bg-surface-container-low rounded-xl" />
          <div className="h-32 bg-surface-container-low rounded-xl" />
        </div>
      </article>
    );
  }

  // 2. Empty / Not Found State
  if (!word) {
    return (
      <article className="bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-xl shadow-sm text-center space-y-space-md">
        <div className="w-16 h-16 mx-auto rounded-full bg-surface-container-high flex items-center justify-center text-outline">
          <Icon name="search_off" className="text-3xl" />
        </div>
        <div className="space-y-1">
          <h2 className="text-headline-sm font-headline-sm text-on-surface font-bold">
            Không tìm thấy từ vựng phù hợp
          </h2>
          <p className="text-body-md text-on-surface-variant max-w-md mx-auto">
            Vui lòng kiểm tra lại chính tả hoặc thử tìm kiếm bằng chữ Hán, Hiragana, Romaji hoặc nghĩa tiếng Việt.
          </p>
        </div>
      </article>
    );
  }

  const {
    kanji = '',
    hiragana = '',
    romaji = '',
    hanViet = '',
    pitchAccent = null,
    jlptLevel = 'N5',
    partOfSpeech = 'Từ vựng',
    primaryMeaning = '',
    detailedMeaning = '',
    senses = [],
    furiganaSegments = [],
    kanjiBreakdown = [],
    examples = [],
    synonyms = [],
  } = word;

  return (
    <article className="bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-lg shadow-sm relative overflow-hidden">
      {/* Subtle Japanese Watermark Kanji in Corner */}
      <div className="absolute -right-6 -bottom-8 select-none pointer-events-none opacity-5 font-kanji-headline text-[130px] text-on-surface">
        {kanji || hiragana}
      </div>

      {/* Header & Kanji Meta */}
      <div className="flex flex-col md:flex-row md:items-start justify-between gap-space-md border-b border-surface-container-high pb-space-lg">
        <div>
          {/* Big Kanji Display with Furigana */}
          <div className="flex items-baseline space-x-space-md">
            <h1 className="font-kanji-headline text-kanji-headline text-on-surface tracking-wide">
              {furiganaSegments && furiganaSegments.length > 0 ? (
                furiganaSegments.map((seg, idx) => (
                  <ruby key={idx} className="mr-0.5">
                    {seg.ruby}
                    {seg.rt && <rt>{seg.rt}</rt>}
                  </ruby>
                ))
              ) : (
                <ruby>
                  {kanji}
                  {hiragana && <rt>{hiragana}</rt>}
                </ruby>
              )}
            </h1>
            {/* Native Audio Pronunciation Button */}
            <button
              type="button"
              onClick={() => handleSpeak(kanji || hiragana)}
              className="w-10 h-10 rounded-full bg-surface-container-low hover:bg-primary-fixed border border-outline-variant text-primary flex items-center justify-center transition-transform active:scale-95 shadow-xs"
              title="Phát âm chuẩn Tokyo"
            >
              <Icon name="volume_up" className="text-2xl" />
            </button>
          </div>

          {/* Sub-readings: Romaji, Hán Việt, Pitch Accent */}
          <div className="flex flex-wrap items-center gap-space-md mt-2 text-body-sm font-body-sm text-on-surface-variant">
            {romaji && (
              <span className="bg-surface-container px-2.5 py-0.5 rounded text-on-surface font-medium">
                Romaji: <strong className="text-primary">{romaji}</strong>
              </span>
            )}
            {hanViet && (
              <span className="bg-surface-container px-2.5 py-0.5 rounded text-on-surface font-medium">
                Hán Việt: <strong className="text-on-surface">{hanViet}</strong>
              </span>
            )}
            {pitchAccent && (
              <span className="flex items-center space-x-1 text-label-sm font-label-sm text-secondary">
                <Icon name="graphic_eq" className="text-base" />
                <span>Âm điệu: {pitchAccent}</span>
              </span>
            )}
          </div>
        </div>

        {/* Primary CTAs & Deck selector */}
        <div className="flex flex-col items-start md:items-end space-y-space-sm shrink-0">
          <div className="flex items-center space-x-2">
            {jlptLevel && (
              <span className="bg-surface-container-highest text-on-surface px-2.5 py-1 rounded text-label-sm font-label-sm font-bold">
                {jlptLevel}
              </span>
            )}
            {partOfSpeech && (
              <span className="bg-secondary-fixed text-on-secondary-fixed-variant px-2.5 py-1 rounded text-label-sm font-label-sm font-bold">
                {partOfSpeech}
              </span>
            )}
          </div>

          {/* Add to Flashcard Button with Dropdown picker */}
          <div className="relative inline-flex rounded-lg shadow-sm">
            <button
              type="button"
              className="bg-primary hover:bg-primary-container text-on-primary px-space-md py-2 rounded-l-lg text-label-md font-label-md font-semibold flex items-center space-x-1.5 transition-colors"
            >
              <Icon name="bookmark_add" className="text-lg" />
              <span>+ Thêm vào Flashcard</span>
            </button>
            <div className="relative">
              <button
                type="button"
                onClick={() => setDeckMenuOpen(!deckMenuOpen)}
                className="bg-tertiary-container hover:bg-primary text-on-primary px-2 py-2 rounded-r-lg border-l border-primary-fixed/20 flex items-center transition-colors"
                title="Chọn bộ thẻ"
              >
                <Icon name="arrow_drop_down" className="text-lg" />
              </button>
              {deckMenuOpen && (
                <div className="absolute right-0 mt-1 w-56 bg-surface-container-lowest border border-surface-container-high rounded-lg shadow-lg py-1 z-20">
                  <div className="px-3 py-1.5 text-xs text-outline font-semibold border-b border-surface-container-high">
                    Chọn bộ thẻ lưu trữ
                  </div>
                  {['Từ vựng N3-N2, Thiên nhiên', 'Từ vựng thường nhật', 'Bộ thẻ Yoji & Văn học'].map(
                    (deck) => (
                      <button
                        key={deck}
                        type="button"
                        onClick={() => {
                          setSelectedDeck(deck);
                          setDeckMenuOpen(false);
                        }}
                        className="w-full text-left px-3 py-2 text-label-sm font-label-sm text-on-surface hover:bg-surface-container-low flex items-center justify-between"
                      >
                        <span>{deck}</span>
                        {selectedDeck === deck && <span className="text-primary text-xs">✓</span>}
                      </button>
                    )
                  )}
                </div>
              )}
            </div>
          </div>
          <span className="text-label-sm font-label-sm text-outline">
            Đã thêm vào: <em className="not-italic text-on-surface font-medium">{selectedDeck}</em>
          </span>
        </div>
      </div>

      {/* Meaning & Vietnamese Definition */}
      <div className="py-space-md space-y-space-sm">
        <div className="flex items-center justify-between">
          <h2 className="text-label-md font-label-md text-primary font-bold uppercase tracking-wider flex items-center space-x-1.5">
            <Icon name="menu_book" className="text-lg" />
            <span>Ý nghĩa &amp; Định nghĩa chi tiết</span>
          </h2>
          {senses && senses.length > 1 && (
            <span className="bg-primary/10 text-primary border border-primary/20 px-2.5 py-0.5 rounded-full text-label-sm font-bold flex items-center gap-1">
              <Icon name="auto_stories" className="text-sm" />
              <span>{senses.length} nét nghĩa</span>
            </span>
          )}
        </div>

        {senses && senses.length > 0 ? (
          <div className="space-y-3">
            {senses.map((sense, index) => {
              const order = sense.senseOrder || index + 1;
              const hasEnglish = sense.englishGlosses && sense.englishGlosses.length > 0;
              return (
                <div
                  key={index}
                  className="p-space-md bg-surface-container-low rounded-xl border border-surface-container-high hover:border-primary/40 transition-colors space-y-1.5"
                >
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="w-6 h-6 rounded-full bg-primary text-on-primary flex items-center justify-center text-xs font-bold shrink-0">
                      {order}
                    </span>
                    {sense.partOfSpeech && (
                      <span className="bg-secondary-fixed text-on-secondary-fixed-variant px-2 py-0.5 rounded text-[11px] font-bold">
                        {sense.partOfSpeech}
                      </span>
                    )}
                    <h3 className="text-title-md font-bold text-on-surface leading-snug">
                      {sense.meaning || primaryMeaning}
                    </h3>
                  </div>

                  {sense.detailedMeaning && sense.detailedMeaning !== sense.meaning && (
                    <p className="text-body-md font-body-md text-on-surface-variant pl-8 leading-normal">
                      {sense.detailedMeaning}
                    </p>
                  )}

                  {hasEnglish && (
                    <div className="pl-8 pt-0.5 flex items-center gap-1.5 text-body-sm text-outline">
                      <Icon name="language" className="text-sm shrink-0" />
                      <span className="italic">
                        Tiếng Anh: {sense.englishGlosses.join(', ')}
                      </span>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        ) : (
          <div className="p-space-md bg-surface-container-low rounded-lg border-l-4 border-primary">
            <p className="text-headline-sm font-headline-sm text-on-surface font-semibold leading-relaxed">
              {primaryMeaning || 'Đang cập nhật định nghĩa...'}
            </p>
            {detailedMeaning && (
              <p className="text-body-md font-body-md text-on-surface-variant mt-1 leading-normal">
                {detailedMeaning}
              </p>
            )}
          </div>
        )}
      </div>

      {/* KANJI BREAKDOWN SECTION */}
      {kanjiBreakdown && kanjiBreakdown.length > 0 && (
        <div className="pt-space-md border-t border-surface-container-high">
          <h3 className="text-label-md font-label-md text-secondary font-bold mb-space-md flex items-center space-x-1.5">
            <Icon name="draw" className="text-lg" />
            <span>Phân tích cấu tạo chữ Hán (Hán tự cấu thành)</span>
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-space-md">
            {kanjiBreakdown.map((kanjiItem) => (
              <KanjiCard
                key={kanjiItem.character}
                character={kanjiItem.character}
                strokeCount={kanjiItem.strokeCount}
                jlptLevel={kanjiItem.jlptLevel}
                hanViet={kanjiItem.hanViet}
                onReadings={kanjiItem.onReadings}
                kunReadings={kanjiItem.kunReadings}
                meaning={kanjiItem.meaning}
              />
            ))}
          </div>
        </div>
      )}

      {/* EXAMPLE SENTENCES WITH FURIGANA & AUDIO */}
      {examples && examples.length > 0 && (
        <div className="mt-space-lg pt-space-md border-t border-surface-container-high">
          <h3 className="text-label-md font-label-md text-primary font-bold mb-space-md flex items-center space-x-1.5">
            <Icon name="format_quote" className="text-lg" />
            <span>Ví dụ thực tế trong ngữ cảnh (例文)</span>
          </h3>
          <div className="space-y-space-md">
            {examples.map((ex) => (
              <div
                key={ex.id || ex.japaneseText}
                className="p-space-md bg-surface-container-lowest border border-surface-container-high rounded-xl hover:border-outline-variant transition-colors"
              >
                <div className="flex items-start justify-between gap-space-sm">
                  <div className="space-y-1">
                    <div className="text-body-lg font-body-lg text-on-surface font-medium">
                      {ex.japaneseText}
                    </div>
                    {ex.reading && ex.reading !== ex.japaneseText && (
                      <p className="text-body-sm font-body-sm text-outline font-medium">
                        {ex.reading}
                      </p>
                    )}
                    <p className="text-body-md font-body-md text-on-surface-variant font-medium">
                      {ex.translation}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleSpeak(ex.japaneseText)}
                    className="shrink-0 p-2 text-secondary hover:text-primary hover:bg-surface-container rounded-lg transition-colors"
                    title="Nghe câu ví dụ"
                  >
                    <Icon name="volume_up" className="text-xl" />
                  </button>
                </div>
                {(ex.context || ex.level) && (
                  <div className="mt-2 flex items-center space-x-2 text-label-sm font-label-sm text-outline">
                    {ex.context && (
                      <span className="bg-surface-container-low px-2 py-0.5 rounded">
                        Ngữ cảnh: {ex.context}
                      </span>
                    )}
                    {ex.context && ex.level && <span>•</span>}
                    {ex.level && <span>Trình độ: {ex.level}</span>}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* RELATED SYNONYMS & GRAMMAR SECTION */}
      {synonyms && synonyms.length > 0 && (
        <div className="mt-space-lg pt-space-md border-t border-surface-container-high">
          <h3 className="text-label-md font-label-md text-secondary font-bold mb-space-sm flex items-center space-x-1.5">
            <Icon name="hub" className="text-lg" />
            <span>Từ đồng nghĩa &amp; Mẫu ngữ pháp liên quan</span>
          </h3>
          <div className="flex flex-wrap gap-space-sm">
            {synonyms.map((item, idx) => (
              <div
                key={idx}
                className="p-2.5 rounded-lg border border-surface-container-high bg-surface-bright flex items-center space-x-2"
              >
                <span
                  className={`font-bold ${
                    item.type === 'grammar' ? 'text-secondary' : 'text-primary'
                  }`}
                >
                  {item.word}
                </span>
                <span className="text-label-sm font-label-sm text-on-surface-variant">
                  {item.meaning}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </article>
  );
}
