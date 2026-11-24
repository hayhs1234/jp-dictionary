import Icon from '../common/Icon.jsx';

function formatPosBadge(pos) {
  if (!pos) return '';
  const match = pos.match(/\(([^)]+)\)/);
  if (match && match[1]) {
    return match[1].split('/')[0].trim();
  }
  return pos.replace(/\[.*?\]/, '').trim();
}

export default function SearchResultsList({
  results = [],
  selectedIndex = 0,
  onSelectIndex,
  searchTerm = '',
}) {
  if (!results || results.length <= 1) {
    return null;
  }

  return (
    <section className="bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-md shadow-sm space-y-space-sm">
      <div className="flex items-center justify-between pb-2 border-b border-surface-container-high">
        <div className="flex items-center space-x-2">
          <Icon name="format_list_bulleted" className="text-primary text-xl" />
          <h2 className="text-title-sm font-title-sm font-bold text-on-surface">
            Danh sách kết quả ({results.length})
          </h2>
          {searchTerm && (
            <span className="text-body-sm text-on-surface-variant hidden sm:inline">
              cho &ldquo;{searchTerm}&rdquo;
            </span>
          )}
        </div>
        <span className="text-label-sm text-on-surface-variant">
          Nhấp vào từ để xem giải nghĩa chi tiết
        </span>
      </div>

      {/* Results grid / horizontal list */}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-2 xl:grid-cols-3 gap-2 max-h-72 overflow-y-auto pr-1">
        {results.map((item, index) => {
          const isSelected = index === selectedIndex;
          return (
            <button
              key={`${item.id || index}-${item.kanji}`}
              type="button"
              onClick={() => onSelectIndex && onSelectIndex(index)}
              className={`text-left p-3 rounded-lg border transition-all flex flex-col justify-between space-y-1 ${
                isSelected
                  ? 'border-primary bg-primary-fixed/20 shadow-xs ring-1 ring-primary'
                  : 'border-surface-container-high bg-surface-container-low hover:border-outline-variant hover:bg-surface-container'
              }`}
            >
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-baseline space-x-2 truncate">
                  <span className="font-kanji-headline text-lg font-bold text-on-surface">
                    {item.kanji}
                  </span>
                  {item.hiragana && item.hiragana !== item.kanji && (
                    <span className="text-body-sm text-on-surface-variant font-medium">
                      【{item.hiragana}】
                    </span>
                  )}
                </div>
                <div className="flex items-center space-x-1 shrink-0">
                  {item.jlptLevel && (
                    <span className="px-1.5 py-0.5 rounded text-[11px] font-bold bg-surface-container-highest text-primary">
                      {item.jlptLevel}
                    </span>
                  )}
                  {isSelected && (
                    <span className="w-2 h-2 rounded-full bg-primary" title="Đang xem" />
                  )}
                </div>
              </div>

              <div className="flex items-center justify-between text-body-sm text-on-surface line-clamp-1">
                <div className="flex items-center gap-1.5 min-w-0">
                  <span className="text-body-sm text-on-surface-variant truncate">
                    {item.primaryMeaning || 'Chưa có giải nghĩa'}
                  </span>
                  {item.senses && item.senses.length > 1 && (
                    <span className="text-[10px] bg-primary/10 text-primary font-bold px-1.5 py-0.5 rounded shrink-0">
                      +{item.senses.length - 1} nghĩa
                    </span>
                  )}
                </div>
                {item.partOfSpeech && (
                  <span className="text-[11px] text-outline ml-2 shrink-0">
                    {formatPosBadge(item.partOfSpeech)}
                  </span>
                )}
              </div>
            </button>
          );
        })}
      </div>
    </section>
  );
}

