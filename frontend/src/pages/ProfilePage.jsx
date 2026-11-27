import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Icon from '../components/common/Icon.jsx';
import { useAuth } from '../contexts/AuthContext.jsx';
import { extractApiError } from '../services/authService.js';

const JLPT_LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];

export default function ProfilePage() {
  const { user, isAdmin, logout, updateProfile } = useAuth();
  const navigate = useNavigate();

  const [displayName, setDisplayName] = useState(user?.displayName || '');
  const [jlptTarget, setJlptTarget] = useState(user?.jlptTarget || 'N5');
  const [bio, setBio] = useState(user?.bio || '');
  const [saving, setSaving] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    setDisplayName(user?.displayName || '');
    setJlptTarget(user?.jlptTarget || 'N5');
    setBio(user?.bio || '');
  }, [user]);

  const handleSave = async (e) => {
    e.preventDefault();
    setStatusMessage('');
    setErrorMsg('');

    if (!displayName.trim() || displayName.trim().length < 2) {
      setErrorMsg('Tên hiển thị phải có ít nhất 2 ký tự.');
      return;
    }

    setSaving(true);
    try {
      await updateProfile({
        displayName: displayName.trim(),
        jlptTarget,
        bio: bio.trim(),
      });
      setStatusMessage('Đã lưu thông tin cá nhân thành công.');
    } catch (err) {
      setErrorMsg(extractApiError(err, 'Không thể cập nhật thông tin cá nhân.'));
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const initials = (user?.displayName || user?.username || 'U').slice(0, 2).toUpperCase();

  return (
    <div className="space-y-space-lg">
      {/* Header Summary Card */}
      <section className="bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-lg shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-space-md relative overflow-hidden">
        <div className="absolute -right-4 -bottom-6 select-none pointer-events-none opacity-5 font-kanji-headline text-[110px] text-on-surface">
          人
        </div>

        <div className="flex items-center space-x-space-md relative z-10">
          <div className="w-16 h-16 rounded-[9999px] bg-primary-fixed text-primary border-2 border-outline-variant flex items-center justify-center font-bold text-headline-sm shrink-0 shadow-xs overflow-hidden">
            {user?.avatarUrl ? (
              <img
                src={user.avatarUrl}
                alt={user.displayName}
                className="w-full h-full object-cover"
              />
            ) : (
              <span>{initials}</span>
            )}
          </div>
          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-headline-md font-headline-md font-bold text-on-surface">
                {user?.displayName || user?.username}
              </h1>
              <span
                className={`px-2.5 py-0.5 rounded text-label-sm font-bold ${
                  isAdmin
                    ? 'bg-primary-fixed text-primary border border-outline-variant'
                    : 'bg-secondary-fixed text-on-secondary-fixed-variant'
                }`}
              >
                {user?.role}
              </span>
              <span className="px-2 py-0.5 rounded bg-surface-container text-on-surface text-label-sm font-bold">
                Mục tiêu {user?.jlptTarget || 'N5'}
              </span>
            </div>
            <p className="text-body-sm text-on-surface-variant">
              Username: <strong className="text-on-surface">@{user?.username}</strong> • Email:{' '}
              <strong className="text-on-surface">{user?.email}</strong>
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2 relative z-10">
          {isAdmin && (
            <Link
              to="/admin"
              className="px-3.5 py-2 rounded-lg bg-secondary hover:bg-on-secondary-container text-on-secondary text-label-md font-semibold flex items-center space-x-1.5 transition-colors"
            >
              <Icon name="admin_panel_settings" className="text-lg" />
              <span>Quản trị tài khoản</span>
            </Link>
          )}
          <button
            type="button"
            onClick={handleLogout}
            className="px-3.5 py-2 rounded-lg border border-error/40 text-error hover:bg-error-container/40 text-label-md font-semibold flex items-center space-x-1.5 transition-colors cursor-pointer"
          >
            <Icon name="logout" className="text-lg" />
            <span>Đăng xuất</span>
          </button>
        </div>
      </section>

      {/* Main Bento Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-space-lg items-start">
        <section className="lg:col-span-8 bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-lg shadow-sm space-y-space-md">
          <div className="flex items-center space-x-2 border-b border-surface-container-high pb-space-sm">
            <Icon name="badge" className="text-primary text-xl" />
            <h2 className="text-headline-sm font-headline-sm font-bold text-on-surface">
              Thông tin cá nhân
            </h2>
          </div>

          {statusMessage && (
            <div className="p-3 rounded-lg bg-surface-container-low border-l-2 border-secondary text-body-sm text-on-surface font-medium">
              {statusMessage}
            </div>
          )}

          {errorMsg && (
            <div className="p-3 rounded-lg bg-error-container/70 border border-error/30 text-on-error-container text-body-sm">
              {errorMsg}
            </div>
          )}

          <form onSubmit={handleSave} className="space-y-space-md">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-space-md">
              <div className="space-y-1">
                <label className="block text-label-md font-semibold text-on-surface-variant">
                  Tên đăng nhập (Username)
                </label>
                <input
                  type="text"
                  disabled
                  value={user?.username || ''}
                  className="w-full px-3.5 py-2.5 bg-surface-container border border-surface-container-high rounded-xl text-body-md text-outline cursor-not-allowed"
                />
              </div>
              <div className="space-y-1">
                <label className="block text-label-md font-semibold text-on-surface-variant">
                  Email / Gmail
                </label>
                <input
                  type="email"
                  disabled
                  value={user?.email || ''}
                  className="w-full px-3.5 py-2.5 bg-surface-container border border-surface-container-high rounded-xl text-body-md text-outline cursor-not-allowed"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-space-md">
              <div className="space-y-1">
                <label
                  htmlFor="profile-display-name"
                  className="block text-label-md font-semibold text-on-surface"
                >
                  Tên hiển thị
                </label>
                <input
                  id="profile-display-name"
                  type="text"
                  value={displayName}
                  onChange={(e) => setDisplayName(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface"
                />
              </div>
              <div className="space-y-1">
                <label
                  htmlFor="profile-jlpt"
                  className="block text-label-md font-semibold text-on-surface"
                >
                  Mục tiêu JLPT
                </label>
                <select
                  id="profile-jlpt"
                  value={jlptTarget}
                  onChange={(e) => setJlptTarget(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface"
                >
                  {JLPT_LEVELS.map((lvl) => (
                    <option key={lvl} value={lvl}>
                      JLPT {lvl}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="space-y-1">
              <label
                htmlFor="profile-bio"
                className="block text-label-md font-semibold text-on-surface"
              >
                Giới thiệu &amp; mục tiêu học tập
              </label>
              <textarea
                id="profile-bio"
                rows={3}
                maxLength={255}
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                placeholder="Ghi chú mục tiêu tiếng Nhật của bạn..."
                className="w-full px-3.5 py-2.5 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface resize-none"
              />
            </div>

            <div className="flex justify-end">
              <button
                type="submit"
                disabled={saving}
                className="bg-primary hover:bg-primary-container text-on-primary px-space-lg py-2.5 rounded-xl text-label-md font-bold flex items-center space-x-1.5 shadow-sm transition-colors disabled:opacity-60 cursor-pointer"
              >
                <Icon name="save" className="text-lg" />
                <span>{saving ? 'Đang lưu...' : 'Cập nhật hồ sơ'}</span>
              </button>
            </div>
          </form>
        </section>

        <aside className="lg:col-span-4 bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-lg shadow-sm space-y-space-md">
          <div className="flex items-center space-x-2 border-b border-surface-container-high pb-space-sm">
            <Icon name="verified_user" className="text-secondary text-xl" />
            <h2 className="text-headline-sm font-headline-sm font-bold text-on-surface">
              Quyền hạn tài khoản
            </h2>
          </div>

          <div className="space-y-2 text-body-sm text-on-surface-variant">
            <p>
              Vai trò hiện tại:{' '}
              <strong className="text-primary">{user?.role}</strong>
            </p>
            <ul className="space-y-1.5 list-disc list-inside">
              <li>Tra cứu từ điển Nhật - Việt &amp; phân tích Kanji</li>
              <li>Xem và cập nhật thông tin hồ sơ cá nhân</li>
              {isAdmin && (
                <li className="text-primary font-semibold">
                  Truy cập bảng quản trị tài khoản (/admin)
                </li>
              )}
            </ul>
          </div>
        </aside>
      </div>
    </div>
  );
}
