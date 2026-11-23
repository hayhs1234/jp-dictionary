import { useState } from 'react';
import Icon from '../common/Icon.jsx';

export default function TranslatorWidget() {
  const [sourceLang, setSourceLang] = useState('ja');
  const [inputText, setInputText] = useState('森を散策すると、心地よい木漏れ日が降り注ぐ。');
  const [copied, setCopied] = useState(false);

  const translationResult =
    'Hễ tản bộ vào khu rừng, những tia nắng len qua kẽ lá dễ chịu lại tràn ngập rọi xuống.';

  const handleSwap = () => {
    setSourceLang(sourceLang === 'ja' ? 'vi' : 'ja');
  };

  const handleCopy = () => {
    navigator.clipboard?.writeText(translationResult);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-md shadow-sm">
      <div className="flex items-center justify-between border-b border-surface-container-high pb-space-sm mb-space-sm">
        <div className="flex items-center space-x-2">
          <Icon name="g_translate" className="text-primary text-xl" />
          <span className="font-headline-sm text-headline-sm text-on-surface">Dịch thuật AI song ngữ</span>
        </div>
        {/* Language Swap Button */}
        <button
          type="button"
          onClick={handleSwap}
          className="flex items-center space-x-1 text-label-sm font-label-sm text-secondary font-semibold hover:text-primary transition-colors bg-surface-container-low px-2 py-1 rounded"
        >
          <span>{sourceLang === 'ja' ? 'Nhật' : 'Việt'}</span>
          <Icon name="sync_alt" className="text-sm" />
          <span>{sourceLang === 'ja' ? 'Việt' : 'Nhật'}</span>
        </button>
      </div>

      {/* Source Text Input Area */}
      <div className="space-y-2">
        <div className="flex items-center justify-between text-label-sm font-label-sm text-on-surface-variant">
          <span>{sourceLang === 'ja' ? 'Tiếng Nhật (Phát hiện tự động)' : 'Tiếng Việt'}</span>
          {inputText && (
            <button
              type="button"
              onClick={() => setInputText('')}
              className="text-outline hover:text-error text-xs"
              title="Làm mới"
            >
              Xóa
            </button>
          )}
        </div>
        <textarea
          rows={3}
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          placeholder="Nhập câu tiếng Nhật hoặc đoạn văn cần dịch..."
          className="w-full text-body-md font-body-md p-2.5 bg-surface-container-low border border-surface-container-high rounded-lg focus:border-primary focus:ring-1 focus:ring-primary text-on-surface resize-none placeholder:text-outline"
        />
      </div>

      {/* Grammar Particles Parser Pills in Sentence */}
      <div className="my-space-sm p-2 bg-surface-container rounded-lg border border-surface-container-high">
        <div className="text-label-sm font-label-sm font-bold text-on-surface-variant mb-1 flex items-center space-x-1">
          <Icon name="scatter_plot" className="text-sm text-primary" />
          <span>Phân tích trợ từ cốt lõi trong câu:</span>
        </div>
        <div className="flex flex-wrap gap-1 text-label-sm font-label-sm">
          <span className="px-2 py-0.5 bg-surface-container-lowest rounded border border-outline-variant text-primary font-bold">
            を <span className="text-on-surface-variant font-normal">(Chỉ nơi chốn chuyển động)</span>
          </span>
          <span className="px-2 py-0.5 bg-surface-container-lowest rounded border border-outline-variant text-secondary font-bold">
            と <span className="text-on-surface-variant font-normal">(Khi/Hễ mà - Điều kiện)</span>
          </span>
          <span className="px-2 py-0.5 bg-surface-container-lowest rounded border border-outline-variant text-primary font-bold">
            が <span className="text-on-surface-variant font-normal">(Chủ ngữ tự nhiên)</span>
          </span>
        </div>
      </div>

      {/* Target Translation Result Area */}
      <div className="space-y-2 pt-1 border-t border-surface-container-high">
        <div className="flex items-center justify-between text-label-sm font-label-sm text-on-surface-variant">
          <span>Bản dịch {sourceLang === 'ja' ? 'Tiếng Việt:' : 'Tiếng Nhật:'}</span>
          <div className="flex items-center space-x-1">
            <button
              type="button"
              onClick={handleCopy}
              className="p-1 hover:text-primary rounded transition-colors"
              title="Sao chép kết quả"
            >
              <Icon name="content_copy" className="text-base" />
            </button>
            <button
              type="button"
              className="p-1 hover:text-primary rounded transition-colors"
              title="Phát âm tiếng Việt"
            >
              <Icon name="volume_up" className="text-base" />
            </button>
            {copied && <span className="text-xs text-primary font-medium">Đã chép!</span>}
          </div>
        </div>
        <div className="p-3 bg-surface-container-lowest rounded-lg border border-outline-variant text-body-md font-body-md text-on-surface font-medium leading-relaxed">
          {translationResult}
        </div>
      </div>

      <button
        type="button"
        className="w-full mt-space-sm bg-secondary hover:bg-on-secondary-container text-on-secondary py-2 rounded-lg text-label-md font-label-md font-semibold flex items-center justify-center space-x-1 transition-colors"
      >
        <Icon name="auto_awesome" className="text-lg" />
        <span>Dịch chi tiết &amp; Lưu ngữ pháp</span>
      </button>
    </div>
  );
}

