import { useState } from 'react';
import Icon from '../common/Icon.jsx';

const HIRAGANA_GRID = [
  ['あ', 'い', 'う', 'え', 'お'],
  ['か', 'き', 'く', 'け', 'こ'],
  ['さ', 'し', 'す', 'せ', 'そ'],
  ['た', 'ち', 'つ', 'て', 'と'],
  ['な', 'に', 'ぬ', 'ね', 'の'],
  ['は', 'ひ', 'ふ', 'へ', 'ほ'],
  ['ま', 'み', 'む', 'め', 'も'],
  ['や', '（', 'ゆ', '）', 'よ'],
  ['ら', 'り', 'る', 'れ', 'ろ'],
  ['わ', 'を', 'ん', 'ー', '〜'],
];

const HIRAGANA_DAKUTEN = [
  ['が', 'ぎ', 'ぐ', 'げ', 'ご'],
  ['ざ', 'じ', 'ず', 'ぜ', 'ぞ'],
  ['だ', 'ぢ', 'づ', 'で', 'ど'],
  ['ば', 'び', 'ぶ', 'べ', 'ぼ'],
  ['ぱ', 'ぴ', 'ぷ', 'ぺ', 'ぽ'],
  ['ゃ', 'ゅ', 'ょ', 'っ', '・'],
];

const KATAKANA_GRID = [
  ['ア', 'イ', 'ウ', 'エ', 'オ'],
  ['カ', 'キ', 'ク', 'ケ', 'コ'],
  ['サ', 'シ', 'ス', 'セ', 'ソ'],
  ['タ', 'チ', 'ツ', 'テ', 'ト'],
  ['ナ', 'ニ', 'ヌ', 'ネ', 'ノ'],
  ['ハ', 'ヒ', 'フ', 'ヘ', 'ホ'],
  ['マ', 'ミ', 'ム', 'メ', 'モ'],
  ['ヤ', '（', 'ユ', '）', 'ヨ'],
  ['ラ', 'リ', 'ル', 'レ', 'ロ'],
  ['ワ', 'ヲ', 'ン', 'ー', '・'],
];

const KATAKANA_DAKUTEN = [
  ['ガ', 'ギ', 'グ', 'ゲ', 'ゴ'],
  ['ザ', 'ジ', 'ズ', 'ゼ', 'ゾ'],
  ['ダ', 'ヂ', 'ヅ', 'デ', 'ド'],
  ['バ', 'ビ', 'ブ', 'ベ', 'ボ'],
  ['パ', 'ピ', 'プ', 'ペ', 'ポ'],
  ['ャ', 'ュ', 'ョ', 'ッ', 'ヴ'],
];

export default function VirtualKeyboard({ isOpen, onClose, onCharClick, onBackspace, onClear }) {
  const [tab, setTab] = useState('hiragana'); // 'hiragana' | 'katakana'

  if (!isOpen) return null;

  const currentGrid = tab === 'hiragana' ? HIRAGANA_GRID : KATAKANA_GRID;
  const currentDakuten = tab === 'hiragana' ? HIRAGANA_DAKUTEN : KATAKANA_DAKUTEN;

  return (
    <div className="mt-3 p-3 bg-surface-container-lowest border border-surface-container-high rounded-xl shadow-lg animate-in fade-in slide-in-from-top-2 duration-150">
      {/* Header with Tabs & Close button */}
      <div className="flex items-center justify-between pb-2 mb-2 border-b border-surface-container-high">
        <div className="flex items-center space-x-2">
          <Icon name="keyboard" className="text-primary text-lg" />
          <span className="text-label-md font-bold text-on-surface">Bàn phím ảo tiếng Nhật</span>
          <div className="flex bg-surface-container p-0.5 rounded-lg text-label-sm">
            <button
              type="button"
              onClick={() => setTab('hiragana')}
              className={`px-3 py-1 rounded transition-colors ${
                tab === 'hiragana'
                  ? 'bg-surface-container-lowest text-primary font-bold shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface'
              }`}
            >
              Hiragana (かな)
            </button>
            <button
              type="button"
              onClick={() => setTab('katakana')}
              className={`px-3 py-1 rounded transition-colors ${
                tab === 'katakana'
                  ? 'bg-surface-container-lowest text-primary font-bold shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface'
              }`}
            >
              Katakana (カナ)
            </button>
          </div>
        </div>

        <div className="flex items-center space-x-1">
          <button
            type="button"
            onClick={onBackspace}
            className="px-2.5 py-1 text-label-sm font-medium bg-surface-container-low hover:bg-surface-container border border-surface-container-high rounded-md text-on-surface flex items-center space-x-1"
            title="Xóa 1 ký tự"
          >
            <Icon name="backspace" className="text-sm" />
            <span>Xóa</span>
          </button>
          <button
            type="button"
            onClick={onClear}
            className="px-2.5 py-1 text-label-sm font-medium bg-surface-container-low hover:bg-surface-container border border-surface-container-high rounded-md text-outline hover:text-error"
            title="Xóa trắng"
          >
            Xóa hết
          </button>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-md text-on-surface-variant hover:bg-surface-container hover:text-on-surface"
            title="Đóng bàn phím"
          >
            <Icon name="close" className="text-lg" />
          </button>
        </div>
      </div>

      {/* Grid Layout: Main Gojūon (Left) + Dakuten/Small (Right) */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-3">
        {/* Main 50-sound rows (8 cols) */}
        <div className="md:col-span-8 grid grid-cols-5 gap-1">
          {currentGrid.flat().map((char, idx) => (
            <button
              key={`${char}-${idx}`}
              type="button"
              onClick={() => onCharClick(char)}
              disabled={char === '（' || char === '）'}
              className={`h-9 font-kanji-headline text-base rounded border transition-all flex items-center justify-center ${
                char === '（' || char === '）'
                  ? 'opacity-20 cursor-default border-transparent'
                  : 'bg-surface-container-low hover:bg-primary-fixed hover:border-primary text-on-surface border-surface-container-high active:scale-95'
              }`}
            >
              {char}
            </button>
          ))}
        </div>

        {/* Dakuten, Handakuten & Small kana (4 cols) */}
        <div className="md:col-span-4 grid grid-cols-5 md:grid-cols-5 gap-1 border-t md:border-t-0 md:border-l border-surface-container-high pt-2 md:pt-0 md:pl-3">
          {currentDakuten.flat().map((char, idx) => (
            <button
              key={`${char}-${idx}`}
              type="button"
              onClick={() => onCharClick(char)}
              className="h-9 font-kanji-headline text-base rounded border border-surface-container-high bg-surface-container-lowest hover:bg-secondary-fixed hover:border-secondary text-on-surface transition-all flex items-center justify-center active:scale-95"
            >
              {char}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

