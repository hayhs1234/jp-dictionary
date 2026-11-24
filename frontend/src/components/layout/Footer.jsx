export default function Footer() {
  return (
    <footer className="w-full bg-surface-container-lowest border-t border-surface-container-high mt-space-xl py-space-md">
      <div className="max-w-7xl mx-auto px-space-lg flex flex-col sm:flex-row items-center justify-between text-label-sm font-label-sm text-on-surface-variant gap-2">
        <div className="flex items-center space-x-2">
          <span className="font-bold text-primary">Komorebi Nihongo Hub</span>
          <span>•</span>
          <span>Từ điển Nhật - Việt chuyên sâu và hệ thống ôn tập SRS thông minh</span>
        </div>
        <div>
          <span>Phiên bản dữ liệu: JLPT 2024-2025 • Đã chuẩn hóa phiên âm Furigana &amp; Hán Việt</span>
        </div>
      </div>
    </footer>
  );
}
