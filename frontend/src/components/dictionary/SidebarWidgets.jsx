import { useState } from 'react';
import Icon from '../common/Icon.jsx';

export default function SidebarWidgets() {
  const [historyItems, setHistoryItems] = useState([
    { id: 1, term: '木漏れ日', tag: 'N2' },
    { id: 2, term: '一期一会', tag: 'Yoji' },
    { id: 3, term: '侘寂', tag: 'Wabi-sabi' },
    { id: 4, term: '懐かしい', tag: 'N3' },
  ]);

  const clearHistory = () => {
    setHistoryItems([]);
  };

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
              onClick={clearHistory}
              className="text-label-sm font-label-sm text-outline hover:text-primary transition-colors"
            >
              Xóa lịch sử
            </button>
          )}
        </div>

        {historyItems.length > 0 ? (
          <div className="flex flex-wrap gap-1.5">
            {historyItems.map((item) => (
              <a
                key={item.id}
                className="px-2.5 py-1 bg-surface-container-low hover:bg-surface-container text-on-surface rounded text-label-sm font-label-sm flex items-center space-x-1 transition-colors"
                href="#"
              >
                <span>{item.term}</span>
                <span className="text-outline text-xs">{item.tag}</span>
              </a>
            ))}
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
          {/* Trending Kanji 1: 桜 */}
          <div className="flex items-center justify-between p-2 rounded-lg bg-surface-bright hover:bg-surface-container-low border border-surface-container-high cursor-pointer transition-colors">
            <div className="flex items-center space-x-2">
              <span className="w-8 h-8 rounded bg-surface-container flex items-center justify-center font-bold text-primary">
                桜
              </span>
              <div>
                <p className="text-label-md font-label-md font-bold text-on-surface">ANH (Hoa anh đào)</p>
                <p className="text-label-sm font-label-sm text-outline">さくら (sakura) • 10 nét</p>
              </div>
            </div>
            <span className="bg-surface-container px-2 py-0.5 rounded text-label-sm font-label-sm font-bold text-on-surface-variant">
              N3
            </span>
          </div>

          {/* Trending Kanji 2: 雨 */}
          <div className="flex items-center justify-between p-2 rounded-lg bg-surface-bright hover:bg-surface-container-low border border-surface-container-high cursor-pointer transition-colors">
            <div className="flex items-center space-x-2">
              <span className="w-8 h-8 rounded bg-surface-container flex items-center justify-center font-bold text-primary">
                雨
              </span>
              <div>
                <p className="text-label-md font-label-md font-bold text-on-surface">VŨ (Cơn mưa)</p>
                <p className="text-label-sm font-label-sm text-outline">あめ (ame) • 8 nét</p>
              </div>
            </div>
            <span className="bg-surface-container px-2 py-0.5 rounded text-label-sm font-label-sm font-bold text-on-surface-variant">
              N5
            </span>
          </div>
        </div>
      </div>

      {/* Study Tip Box */}
      <div className="p-3 rounded-lg bg-surface-container-low border-l-2 border-secondary text-label-sm font-label-sm text-on-surface-variant">
        <span className="font-bold text-secondary">Mẹo ghi nhớ:</span> Từ{' '}
        <ruby>
          木漏れ日<rt>こもれび</rt>
        </ruby>{' '}
        là một từ đặc trưng của văn hoá Nhật không có từ tiếng Anh đơn lập tương đương chính xác; dùng trực tiếp khi miêu tả cảm giác thư thái trong thiên nhiên.
      </div>
    </div>
  );
}

