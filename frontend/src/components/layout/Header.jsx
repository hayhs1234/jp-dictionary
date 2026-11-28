import { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Icon from '../common/Icon.jsx';
import { useAuth } from '../../contexts/AuthContext.jsx';

function getUserInitials(user) {
  const source = (user?.displayName || user?.username || 'U').trim();
  const parts = source.split(/\s+/).filter(Boolean);
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
  }
  return source.slice(0, 2).toUpperCase();
}

export default function Header() {
  const { user, isAuthenticated, isAdmin, isLoading, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname, isAuthenticated]);

  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setMenuOpen(false);
      }
    }
    function handleEscape(event) {
      if (event.key === 'Escape') {
        setMenuOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEscape);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  const handleLogout = async () => {
    setMenuOpen(false);
    await logout();
    if (location.pathname.startsWith('/profile') || location.pathname.startsWith('/admin')) {
      navigate('/login');
    }
  };

  return (
    <header className="w-full bg-surface-container-lowest border-b border-surface-container-high sticky top-0 z-50 shadow-sm">
      <div className="flex justify-between items-center w-full px-space-lg max-w-7xl mx-auto h-16">
        {/* Brand on Left */}
        <div className="flex items-center space-x-space-lg">
          <Link
            className="flex items-center space-x-2 text-headline-md font-headline-md font-bold text-primary tracking-tight"
            to="/"
          >
            <Icon name="nature" className="text-primary text-2xl" />
            <span>Komorebi (木漏れ日)</span>
          </Link>
        </div>

        {/* Navigation Links */}
        <nav className="hidden md:flex items-center space-x-space-lg">
          <Link
            className={`text-label-md font-label-md transition-colors duration-150 ${
              location.pathname === '/' || location.pathname === '/search'
                ? 'text-primary font-bold border-b-2 border-primary pb-1'
                : 'text-on-surface-variant font-medium hover:text-primary hover:bg-surface-container-low px-2 py-1 rounded-lg'
            }`}
            to="/"
          >
            Từ điển &amp; Dịch
          </Link>
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

        {/* Trailing Status & Auth Action */}
        <div className="flex items-center space-x-space-md">
          {isAuthenticated && (
            <>
              <div className="hidden sm:flex items-center bg-surface-container-low px-space-sm py-1 rounded-full text-label-sm font-label-sm text-on-surface font-semibold border border-surface-container-high">
                <span>🔥 15 ngày</span>
              </div>
              <span className="bg-secondary-container text-on-secondary-container px-2 py-0.5 rounded text-label-sm font-label-sm font-bold">
                {user?.jlptTarget || 'N5'}
              </span>
            </>
          )}

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

          {/* Auth Slot: Login button when signed out, circular button + dropdown when signed in */}
          {isLoading ? (
            <div className="w-24 h-9 rounded-lg bg-surface-container animate-pulse ml-1" />
          ) : !isAuthenticated ? (
            <div className="flex items-center space-x-2 ml-1">
              <Link
                to="/register"
                state={{ from: location }}
                className="hidden sm:inline-flex items-center space-x-1 px-3 py-1.5 rounded-lg border border-outline-variant text-primary hover:bg-primary-fixed/40 text-label-md font-label-md font-semibold transition-colors"
              >
                <Icon name="person_add" className="text-lg" />
                <span>Đăng ký</span>
              </Link>
              <Link
                to="/login"
                state={{ from: location }}
                className="bg-primary hover:bg-primary-container text-on-primary px-3.5 py-1.5 rounded-lg text-label-md font-label-md font-semibold flex items-center space-x-1.5 shadow-sm transition-colors"
              >
                <Icon name="login" className="text-lg" />
                <span>Đăng nhập</span>
              </Link>
            </div>
          ) : (
            <div className="relative ml-1" ref={menuRef}>
              <button
                type="button"
                onClick={() => setMenuOpen((prev) => !prev)}
                aria-expanded={menuOpen}
                aria-haspopup="menu"
                aria-label="Mở menu tài khoản cá nhân"
                className="w-9 h-9 rounded-[9999px] overflow-hidden border-2 border-outline-variant hover:border-primary bg-primary-fixed text-primary font-bold text-label-md flex items-center justify-center transition-all shadow-xs cursor-pointer focus:outline-none focus:ring-2 focus:ring-primary/30"
              >
                {user?.avatarUrl ? (
                  <img
                    className="w-full h-full object-cover"
                    src={user.avatarUrl}
                    alt={user.displayName || user.username}
                  />
                ) : (
                  <span>{getUserInitials(user)}</span>
                )}
              </button>

              {menuOpen && (
                <div
                  role="menu"
                  className="absolute right-0 mt-2 w-72 bg-surface-container-lowest border border-surface-container-high rounded-xl shadow-xl z-50 overflow-hidden"
                >
                  {/* Personal Info Summary */}
                  <div className="p-space-md bg-surface-container-low border-b border-surface-container-high">
                    <div className="flex items-center space-x-3">
                      <div className="w-11 h-11 rounded-[9999px] bg-primary text-on-primary font-bold flex items-center justify-center shrink-0 shadow-xs">
                        {user?.avatarUrl ? (
                          <img
                            className="w-full h-full object-cover rounded-[9999px]"
                            src={user.avatarUrl}
                            alt={user.displayName || user.username}
                          />
                        ) : (
                          <span>{getUserInitials(user)}</span>
                        )}
                      </div>
                      <div className="min-w-0 flex-1">
                        <p className="text-body-md font-bold text-on-surface truncate">
                          {user?.displayName || user?.username}
                        </p>
                        <p className="text-label-sm text-on-surface-variant truncate">
                          @{user?.username}
                        </p>
                        <p className="text-label-sm text-outline truncate">{user?.email}</p>
                      </div>
                    </div>

                    <div className="mt-3 flex items-center justify-between gap-2">
                      <span
                        className={`px-2 py-0.5 rounded text-label-sm font-bold ${
                          isAdmin
                            ? 'bg-primary-fixed text-primary border border-outline-variant'
                            : 'bg-secondary-fixed text-on-secondary-fixed-variant'
                        }`}
                      >
                        {isAdmin ? 'ADMIN • Quản trị viên' : 'USER • Thành viên'}
                      </span>
                      <span className="px-2 py-0.5 rounded bg-surface-container-lowest border border-surface-container-high text-label-sm font-bold text-on-surface">
                        JLPT {user?.jlptTarget || 'N5'}
                      </span>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="p-1.5 space-y-1">
                    <Link
                      to="/profile"
                      role="menuitem"
                      onClick={() => setMenuOpen(false)}
                      className="w-full flex items-center space-x-2.5 px-3 py-2 rounded-lg text-body-sm font-medium text-on-surface hover:bg-surface-container-low hover:text-primary transition-colors"
                    >
                      <Icon name="person" className="text-lg text-secondary" />
                      <span>Xem thông tin cá nhân</span>
                    </Link>

                    {isAdmin && (
                      <Link
                        to="/admin"
                        role="menuitem"
                        onClick={() => setMenuOpen(false)}
                        className="w-full flex items-center space-x-2.5 px-3 py-2 rounded-lg text-body-sm font-medium text-on-surface hover:bg-surface-container-low hover:text-primary transition-colors"
                      >
                        <Icon name="admin_panel_settings" className="text-lg text-primary" />
                        <span>Quản trị tài khoản (Admin)</span>
                      </Link>
                    )}

                    <div className="border-t border-surface-container-high my-1" />

                    <button
                      type="button"
                      role="menuitem"
                      onClick={handleLogout}
                      className="w-full flex items-center space-x-2.5 px-3 py-2 rounded-lg text-body-sm font-semibold text-error hover:bg-error-container/40 transition-colors cursor-pointer"
                    >
                      <Icon name="logout" className="text-lg" />
                      <span>Đăng xuất</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
