import { useState } from 'react';
import Icon from '../common/Icon.jsx';
import KanjiCard from './KanjiCard.jsx';

export default function WordCard() {
  const [deckMenuOpen, setDeckMenuOpen] = useState(false);

  return (
    <article className="bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-lg shadow-sm relative overflow-hidden">
      {/* Subtle Japanese Watermark Kanji in Corner */}
      <div className="absolute -right-6 -bottom-8 select-none pointer-events-none opacity-5 font-kanji-headline text-[130px] text-on-surface">
        木漏日
      </div>

      {/* Header & Kanji Meta */}
      <div className="flex flex-col md:flex-row md:items-start justify-between gap-space-md border-b border-surface-container-high pb-space-lg">
        <div>
          {/* Big Kanji Display with Furigana */}
          <div className="flex items-baseline space-x-space-md">
            <h1 className="font-kanji-headline text-kanji-headline text-on-surface tracking-wide">
              <ruby>
                木<rt>こ</rt>漏<rt>れ</rt>日<rt>び</rt>
              </ruby>
            </h1>
            {/* Native Audio Pronunciation */}
            <button
              type="button"
              className="w-10 h-10 rounded-full bg-surface-container-low hover:bg-primary-fixed border border-outline-variant text-primary flex items-center justify-center transition-transform active:scale-95 shadow-xs"
              title="Phát âm chuẩn Tokyo (Heiban)"
            >
              <Icon name="volume_up" className="text-2xl" />
            </button>
          </div>

          {/* Sub-readings: Romaji & Hán Việt */}
          <div className="flex flex-wrap items-center gap-space-md mt-2 text-body-sm font-body-sm text-on-surface-variant">
            <span className="bg-surface-container px-2.5 py-0.5 rounded text-on-surface font-medium">
              Romaji: <strong className="text-primary">komorebi</strong>
            </span>
            <span className="bg-surface-container px-2.5 py-0.5 rounded text-on-surface font-medium">
              Hán Việt: <strong className="text-on-surface">MỘC LẬU NHẬT</strong>
            </span>
            <span className="flex items-center space-x-1 text-label-sm font-label-sm text-secondary">
              <Icon name="graphic_eq" className="text-base" />
              <span>Âm điệu: [0] 平板 (Heiban)</span>
            </span>
          </div>
        </div>

        {/* Primary CTAs & Deck selector */}
        <div className="flex flex-col items-start md:items-end space-y-space-sm shrink-0">
          <div className="flex items-center space-x-2">
            <span className="bg-surface-container-highest text-on-surface px-2.5 py-1 rounded text-label-sm font-label-sm font-bold">
              N2 / Từ vựng cao cấp
            </span>
            <span className="bg-secondary-fixed text-on-secondary-fixed-variant px-2.5 py-1 rounded text-label-sm font-label-sm font-bold">
              Danh từ [名]
            </span>
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
                <div className="absolute right-0 mt-1 w-52 bg-surface-container-lowest border border-surface-container-high rounded-lg shadow-lg py-1 z-20">
                  <div className="px-3 py-1.5 text-xs text-outline font-semibold border-b border-surface-container-high">
                    Chọn bộ thẻ lưu trữ
                  </div>
                  <button
                    type="button"
                    onClick={() => setDeckMenuOpen(false)}
                    className="w-full text-left px-3 py-2 text-label-sm font-label-sm text-on-surface hover:bg-surface-container-low flex items-center justify-between"
                  >
                    <span>Từ vựng N3-N2, Thiên nhiên</span>
                    <span className="text-primary text-xs">✓</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => setDeckMenuOpen(false)}
                    className="w-full text-left px-3 py-2 text-label-sm font-label-sm text-on-surface hover:bg-surface-container-low"
                  >
                    Từ vựng thường nhật
                  </button>
                  <button
                    type="button"
                    onClick={() => setDeckMenuOpen(false)}
                    className="w-full text-left px-3 py-2 text-label-sm font-label-sm text-on-surface hover:bg-surface-container-low"
                  >
                    Bộ thẻ Yoji & Văn học
                  </button>
                </div>
              )}
            </div>
          </div>
          <span className="text-label-sm font-label-sm text-outline">
            Đã thêm vào: <em className="not-italic text-on-surface font-medium">Từ vựng N3-N2, Thiên nhiên</em>
          </span>
        </div>
      </div>

      {/* Meaning & Vietnamese Definition */}
      <div className="py-space-md space-y-space-sm">
        <h2 className="text-label-md font-label-md text-primary font-bold uppercase tracking-wider flex items-center space-x-1.5">
          <Icon name="menu_book" className="text-lg" />
          <span>Ý nghĩa chính &amp; Định nghĩa</span>
        </h2>
        <div className="p-space-md bg-surface-container-low rounded-lg border-l-4 border-primary">
          <p className="text-headline-sm font-headline-sm text-on-surface font-semibold leading-relaxed">
            Ánh nắng lọt qua kẽ lá
          </p>
          <p className="text-body-md font-body-md text-on-surface-variant mt-1 leading-normal">
            Hiện tượng quang học và mỹ học tự nhiên khi ánh sáng mặt trời chiếu xuyên qua vòm tán lá cây rậm rạp tạo thành những vệt sáng lấp lánh đan xen trên mặt đất hoặc bờ tường.
          </p>
        </div>
      </div>

      {/* KANJI BREAKDOWN SECTION */}
      <div className="pt-space-md border-t border-surface-container-high">
        <h3 className="text-label-md font-label-md text-secondary font-bold mb-space-md flex items-center space-x-1.5">
          <Icon name="draw" className="text-lg" />
          <span>Phân tích cấu tạo chữ Hán (Hán tự cấu thành)</span>
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-space-md">
          {/* Kanji 1: 木 */}
          <KanjiCard
            character="木"
            strokeCount="4"
            jlptLevel="Cấp độ N5"
            hanViet="MỘC (Cây)"
            onReadings="モク, ボク"
            kunReadings="き, こ"
            meaning="Cây cối, gỗ, chất liệu thực vật."
          />

          {/* Kanji 2: 漏 */}
          <KanjiCard
            character="漏"
            strokeCount="14"
            jlptLevel="Cấp độ N1"
            hanViet="LẬU (Rò rỉ, lọt)"
            onReadings="ロウ"
            kunReadings="も-る, も-れる"
            meaning="Rò rỉ nước, lọt ánh sáng, sơ hở."
          />

          {/* Kanji 3: 日 */}
          <KanjiCard
            character="日"
            strokeCount="4"
            jlptLevel="Cấp độ N5"
            hanViet="NHẬT (Mặt trời)"
            onReadings="ニチ, ジツ"
            kunReadings="ひ, -び"
            meaning="Thái dương, ban ngày, nhật quang."
          />
        </div>
      </div>

      {/* EXAMPLE SENTENCES WITH FURIGANA & AUDIO */}
      <div className="mt-space-lg pt-space-md border-t border-surface-container-high">
        <h3 className="text-label-md font-label-md text-primary font-bold mb-space-md flex items-center space-x-1.5">
          <Icon name="format_quote" className="text-lg" />
          <span>Ví dụ thực tế trong ngữ cảnh (例文)</span>
        </h3>
        <div className="space-y-space-md">
          {/* Example 1 */}
          <div className="p-space-md bg-surface-container-lowest border border-surface-container-high rounded-xl hover:border-outline-variant transition-colors">
            <div className="flex items-start justify-between gap-space-sm">
              <div className="space-y-1">
                <div className="text-body-lg font-body-lg text-on-surface">
                  <ruby>
                    森<rt>もり</rt>
                  </ruby>
                  の
                  <ruby>
                    中<rt>なか</rt>
                  </ruby>
                  を
                  <ruby>
                    歩<rt>ある</rt>
                  </ruby>
                  くと、
                  <ruby>
                    美<rt>うつく</rt>
                  </ruby>
                  しい
                  <ruby>
                    木漏<rt>こも</rt>
                  </ruby>
                  れ
                  <ruby>
                    日<rt>び</rt>
                  </ruby>
                  が
                  <ruby>
                    地面<rt>じめん</rt>
                  </ruby>
                  を
                  <ruby>
                    照<rt>て</rt>
                  </ruby>
                  らしていた。
                </div>
                <p className="text-body-md font-body-md text-on-surface-variant font-medium">
                  Đi dạo trong rừng, những tia nắng len qua kẽ lá rọi sáng tuyệt đẹp xuống mặt đất.
                </p>
              </div>
              <button
                type="button"
                className="shrink-0 p-2 text-secondary hover:text-primary hover:bg-surface-container rounded-lg"
                title="Nghe câu ví dụ"
              >
                <Icon name="volume_up" className="text-xl" />
              </button>
            </div>
            <div className="mt-2 flex items-center space-x-2 text-label-sm font-label-sm text-outline">
              <span className="bg-surface-container-low px-2 py-0.5 rounded">Ngữ cảnh: Miêu tả văn học / Cảnh sắc</span>
              <span>•</span>
              <span>Trình độ: Trung cấp N3</span>
            </div>
          </div>

          {/* Example 2 */}
          <div className="p-space-md bg-surface-container-lowest border border-surface-container-high rounded-xl hover:border-outline-variant transition-colors">
            <div className="flex items-start justify-between gap-space-sm">
              <div className="space-y-1">
                <div className="text-body-lg font-body-lg text-on-surface">
                  <ruby>
                    窓<rt>まど</rt>
                  </ruby>
                  から
                  <ruby>
                    差<rt>さ</rt>
                  </ruby>
                  し
                  <ruby>
                    込<rt>こ</rt>
                  </ruby>
                  む
                  <ruby>
                    木漏<rt>こも</rt>
                  </ruby>
                  れ
                  <ruby>
                    日<rt>び</rt>
                  </ruby>
                  を
                  <ruby>
                    眺<rt>なが</rt>
                  </ruby>
                  めながら、
                  <ruby>
                    静<rt>しず</rt>
                  </ruby>
                  かに
                  <ruby>
                    本<rt>ほん</rt>
                  </ruby>
                  を
                  <ruby>
                    読<rt>よ</rt>
                  </ruby>
                  むのが
                  <ruby>
                    好<rt>す</rt>
                  </ruby>
                  きだ。
                </div>
                <p className="text-body-md font-body-md text-on-surface-variant font-medium">
                  Tôi thích ngồi đọc sách trong yên ả, ngắm nhìn vạt nắng xuyên qua kẽ lá rọi vào qua khung cửa sổ.
                </p>
              </div>
              <button
                type="button"
                className="shrink-0 p-2 text-secondary hover:text-primary hover:bg-surface-container rounded-lg"
                title="Nghe câu ví dụ"
              >
                <Icon name="volume_up" className="text-xl" />
              </button>
            </div>
            <div className="mt-2 flex items-center space-x-2 text-label-sm font-label-sm text-outline">
              <span className="bg-surface-container-low px-2 py-0.5 rounded">Ngữ cảnh: Đời sống thường nhật</span>
              <span>•</span>
              <span>Trình độ: N3</span>
            </div>
          </div>
        </div>
      </div>

      {/* RELATED SYNONYMS & GRAMMAR SECTION */}
      <div className="mt-space-lg pt-space-md border-t border-surface-container-high">
        <h3 className="text-label-md font-label-md text-secondary font-bold mb-space-sm flex items-center space-x-1.5">
          <Icon name="hub" className="text-lg" />
          <span>Từ đồng nghĩa &amp; Mẫu ngữ pháp liên quan</span>
        </h3>
        <div className="flex flex-wrap gap-space-sm">
          <div className="p-2.5 rounded-lg border border-surface-container-high bg-surface-bright flex items-center space-x-2">
            <span className="text-primary font-bold">日光 (にっこう)</span>
            <span className="text-label-sm font-label-sm text-on-surface-variant">Ánh sáng mặt trời (Đồng nghĩa rộng)</span>
          </div>
          <div className="p-2.5 rounded-lg border border-surface-container-high bg-surface-bright flex items-center space-x-2">
            <span className="text-primary font-bold">陽光 (ようこう)</span>
            <span className="text-label-sm font-label-sm text-on-surface-variant">Dương quang, nắng ấm</span>
          </div>
          <div className="p-2.5 rounded-lg border border-surface-container-high bg-surface-bright flex items-center space-x-2">
            <span className="text-secondary font-bold">〜ながら (Vừa... vừa)</span>
            <span className="text-label-sm font-label-sm text-on-surface-variant">Ngữ pháp N4 thường dùng kèm</span>
          </div>
        </div>
      </div>
    </article>
  );
}
