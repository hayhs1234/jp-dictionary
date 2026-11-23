import Icon from '../common/Icon.jsx';

export default function Header() {
  return (
    <header className="w-full bg-surface-container-lowest border-b border-surface-container-high sticky top-0 z-50 shadow-sm">
      <div className="flex justify-between items-center w-full px-space-lg max-w-7xl mx-auto h-16">
        {/* Brand & Search Input on Left */}
        <div className="flex items-center space-x-space-lg">
          <a
            className="flex items-center space-x-2 text-headline-md font-headline-md font-bold text-primary tracking-tight"
            href="#"
          >
            <Icon name="nature" className="text-primary text-2xl" />
            <span>Komorebi (木漏れ日)</span>
          </a>
        </div>

        {/* Navigation Links */}
        <nav className="hidden md:flex items-center space-x-space-lg">
          {/* Active Nav: Từ điển & Dịch */}
          <a
            className="text-primary font-bold border-b-2 border-primary pb-1 text-label-md font-label-md transition-colors duration-150"
            href="#"
          >
            Từ điển &amp; Dịch
          </a>
          <a
            className="text-on-surface-variant font-medium text-label-md font-label-md hover:text-primary hover:bg-surface-container-low transition-colors duration-150 px-2 py-1 rounded-lg"
            href="#"
          >
            Video bài giảng
          </a>
          <a
            className="text-on-surface-variant font-medium text-label-md font-label-md hover:text-primary hover:bg-surface-container-low transition-colors duration-150 px-2 py-1 rounded-lg"
            href="#"
          >
            Flashcard SRS
          </a>
          <a
            className="text-on-surface-variant font-medium text-label-md font-label-md hover:text-primary hover:bg-surface-container-low transition-colors duration-150 px-2 py-1 rounded-lg"
            href="#"
          >
            Lộ trình JLPT
          </a>
        </nav>

        {/* Trailing Status & Profile Actions */}
        <div className="flex items-center space-x-space-md">
          {/* Streak pill */}
          <div className="flex items-center bg-surface-container-low px-space-sm py-1 rounded-full text-label-sm font-label-sm text-on-surface font-semibold border border-surface-container-high">
            <span>🔥 15 ngày</span>
          </div>

          {/* Level badge */}
          <span className="bg-secondary-container text-on-secondary-container px-2 py-0.5 rounded text-label-sm font-label-sm font-bold">
            N3
          </span>

          {/* Icon actions */}
          <button
            type="button"
            className="p-1.5 text-on-surface-variant hover:text-primary hover:bg-surface-container-low rounded-lg transition-colors"
            title="Thông báo"
          >
            <Icon name="notifications" className="text-xl" />
          </button>
          <button
            type="button"
            className="p-1.5 text-on-surface-variant hover:text-primary hover:bg-surface-container-low rounded-lg transition-colors"
            title="Chuyển ngôn ngữ"
          >
            <Icon name="translate" className="text-xl" />
          </button>

          {/* Avatar */}
          <div className="w-8 h-8 rounded-full overflow-hidden border border-outline-variant ml-1 cursor-pointer">
            <img
              className="w-full h-full object-cover"
              data-alt="A portrait of a focused Vietnamese Japanese-language student in modern scholarly clothes sitting in a sunlit architectural library with warm wooden shelves and natural daylight."
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuBHha7s094LGr9QCpFwc6cJHDmYwpfrS1JgRW6v2RuyYHekDWeePxqBQvrG98XOxOrItkrwWxN6_msJ5gXwNQK2L3ZkiEX9qnv02BGvxm0vajlfo0JwC7-SXRy-GQF3d4j1IZlyEOJaomG00uq7Z1vO-6jifDWJ6yJWb-UBHOT7srs05QSz9Zlr6dwRiNqaRQ3dBuAPS6cz1HdTCobeJvs8bunewVADc9HpBGWq9r5VEyodM2WcvKSR"
              alt="User Avatar"
            />
          </div>
        </div>
      </div>
    </header>
  );
}
