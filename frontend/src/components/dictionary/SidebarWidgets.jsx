import Icon from '../common/Icon.jsx';

export default function SidebarWidgets({
  historyItems = [],
  onSelectHistory,
  onClearHistory,
  trendingKanji = [],
  currentWord,
}) {
  const defaultTrending = [
    {
      character: '桜',
      hanViet: 'ANH (Hoa anh đào)',
      reading: 'さくら (sakura)',
      strokes: '10 nét',
      jlpt: 'N3',
    },
    {
      character: '雨',
      hanViet: 'VŨ (Cơn mưa)',
      reading: 'あめ (ame)',
      strokes: '8 nét',
      jlpt: 'N5',
    },
  ];

  const displayTrending = trendingKanji.length > 0 ? trendingKanji : defaultTrending;

  return (
    <div className="bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-md shadow-sm space-y-space-md">
      {/* Recent Searches */}
      <div>
        <div className="flex items-center justify-between mb-space-sm">
          <span className="text-label-md font-label-md font-bold text-on-surface flex items-center space-x-1">
            <Icon name="history" className="text-base text-primary" />
            <span>Lịch sử tra cứu gần đây</span>
          </span>
          {historyItems.length > 0 && (
            <button
              type="button"
              onClick={onClearHistory}
              className="text-label-sm font-label-sm text-outline hover:text-primary transition-colors"
            >
              Xóa lịch sử
            </button>
          )}
        </div>

        {historyItems.length > 0 ? (
          <div className="flex flex-wrap gap-1.5">
            {historyItems.map((item, idx) => {
              const term = typeof item === 'string' ? item : item.term;
              const tag = typeof item === 'object' ? item.tag : null;
              return (
                <button
                  key={`${term}-${idx}`}
                  type="button"
                  onClick={() => onSelectHistory && onSelectHistory(term)}
                  className="px-2.5 py-1 bg-surface-container-low hover:bg-surface-container text-on-surface rounded text-label-sm font-label-sm flex items-center space-x-1 transition-colors cursor-pointer border border-transparent hover:border-outline-variant"
                >
                  <span>{term}</span>
                  {tag && <span className="text-outline text-xs">{tag}</span>}
                </button>
              );
            })}
          </div>
        ) : (
          <p className="text-label-sm text-outline italic">Chưa có lịch sử tra cứu.</p>
        )}
      </div>

      {/* Trending Kanji of the Day */}
      <div className="border-t border-surface-container-high pt-space-md">
        <div className="flex items-center justify-between mb-space-sm">
          <span className="text-label-md font-label-md font-bold text-on-surface flex items-center space-x-1">
            <Icon name="local_fire_department" className="text-base text-tertiary-container" />
            <span>Chữ Hán thịnh hành hôm nay</span>
          </span>
          <span className="text-label-sm font-label-sm text-secondary font-semibold">JLPT Hot</span>
        </div>
        <div className="space-y-2">
          {displayTrending.map((item) => (
            <div
              key={item.character}
              onClick={() => onSelectHistory && onSelectHistory(item.character)}
              className="flex items-center justify-between p-2 rounded-lg bg-surface-bright hover:bg-surface-container-low border border-surface-container-high cursor-pointer transition-colors"
            >
              <div className="flex items-center space-x-2">
                <span className="w-8 h-8 rounded bg-surface-container flex items-center justify-center font-bold text-primary">
                  {item.character}
                </span>
                <div>
                  <p className="text-label-md font-label-md font-bold text-on-surface">
                    {item.hanViet || item.character}
                  </p>
                  <p className="text-label-sm font-label-sm text-outline">
                    {item.reading || item.kunReadings || item.onReadings} • {item.strokes || `${item.strokeCount} nét`}
                  </p>
                </div>
              </div>
              <span className="bg-surface-container px-2 py-0.5 rounded text-label-sm font-label-sm font-bold text-on-surface-variant">
                {item.jlpt || item.jlptLevel || 'JLPT'}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Study Tip Box */}
      <div className="p-3 rounded-lg bg-surface-container-low border-l-2 border-secondary text-label-sm font-label-sm text-on-surface-variant">
        <span className="font-bold text-secondary">Mẹo ghi nhớ:</span>{' '}
        {currentWord?.kanji === '木漏れ日' ? (
          <>
            Từ{' '}
            <ruby>
              木漏れ日<rt>こもれび</rt>
            </ruby>{' '}
            là một từ đặc trưng của văn hoá Nhật không có từ tiếng Anh đơn lập tương đương chính xác; dùng trực tiếp khi miêu tả cảm giác thư thái trong thiên nhiên.
          </>
        ) : (
          <>
            Học chữ Hán qua các bộ thủ và từ ghép giúp liên tưởng nghĩa nhanh hơn và ghi nhớ mặt chữ lâu hơn gấp 3 lần.
          </>
        )}
      </div>
    </div>
  );
}
