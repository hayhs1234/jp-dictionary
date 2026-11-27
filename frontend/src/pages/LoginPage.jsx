import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import Icon from '../components/common/Icon.jsx';
import { useAuth } from '../contexts/AuthContext.jsx';
import { extractApiError } from '../services/authService.js';

export default function LoginPage() {
  const { isAuthenticated, isLoading: isAuthBootstrapping, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = location.state?.from?.pathname || '/';

  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  if (!isAuthBootstrapping && isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');

    const cleanIdentifier = identifier.trim();
    if (!cleanIdentifier || !password) {
      setErrorMsg('Vui lòng nhập đầy đủ tên đăng nhập (hoặc email) và mật khẩu.');
      return;
    }

    setSubmitting(true);
    try {
      await login({ identifier: cleanIdentifier, password });
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setErrorMsg(
        extractApiError(err, 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin tài khoản.')
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleQuickFill = (demoIdentifier, demoPassword) => {
    setIdentifier(demoIdentifier);
    setPassword(demoPassword);
    setErrorMsg('');
  };

  const isDevEnv = typeof import.meta !== 'undefined' && Boolean(import.meta.env?.DEV);

  return (
    <div className="flex-1 flex items-center justify-center py-space-md">
      <section className="w-full max-w-4xl bg-surface-container-lowest border border-surface-container-high rounded-xl shadow-sm overflow-hidden grid grid-cols-1 lg:grid-cols-12">
        {/* Left Cultural Panel: Japanese Washi, Hanko Stamp & Seigaiha */}
        <div className="lg:col-span-5 seigaiha-pattern border-b lg:border-b-0 lg:border-r border-surface-container-high p-space-xl relative overflow-hidden flex flex-col justify-between gap-space-lg">
          {/* Subtle Kanji Watermark matching WordCard */}
          <div className="absolute -right-4 -bottom-8 select-none pointer-events-none opacity-5 font-kanji-headline text-[140px] leading-none text-on-surface">
            和
          </div>

          {/* Hanko Stamp + Brand */}
          <div className="flex items-center space-x-3 relative z-10">
            <div className="w-12 h-12 rounded-lg bg-primary text-on-primary border-2 border-on-primary-fixed-variant shadow-sm flex flex-col items-center justify-center leading-tight select-none">
              <span className="font-kanji-headline text-sm font-bold tracking-widest">木漏</span>
              <span className="text-[9px] tracking-wider opacity-90">印章</span>
            </div>
            <div>
              <p className="text-label-sm font-bold uppercase tracking-widest text-primary">
                Komorebi Nihongo Hub
              </p>
              <p className="text-body-sm text-on-surface-variant">
                Cổng học tập &amp; từ điển Nhật - Việt
              </p>
            </div>
          </div>

          {/* Japanese Greeting with Furigana */}
          <div className="space-y-2 relative z-10 my-2">
            <p className="text-label-sm font-bold text-secondary tracking-wider uppercase">
              ようこそ • Chào đón
            </p>
            <h1 className="font-kanji-headline text-3xl md:text-4xl font-bold text-on-surface tracking-wide leading-snug">
              <ruby>
                お帰り<rt>おかえり</rt>
              </ruby>
              なさい
            </h1>
            <p className="text-body-md text-on-surface-variant leading-relaxed">
              Đăng nhập để đồng bộ lộ trình JLPT, hồ sơ học tập cá nhân và trải nghiệm trọn vẹn hệ
              sinh thái tiếng Nhật chuẩn Washi.
            </p>
          </div>

          {/* Study Proverb Box matching SidebarWidgets */}
          <div className="relative z-10 p-3.5 rounded-lg bg-surface-container-lowest/90 border-l-2 border-secondary shadow-xs space-y-1">
            <p className="text-body-md font-bold text-on-surface font-kanji-headline">
              <ruby>
                継続<rt>けいぞく</rt>
              </ruby>
              は
              <ruby>
                力<rt>ちから</rt>
              </ruby>
              なり
            </p>
            <p className="text-label-sm text-on-surface-variant">
              <span className="font-bold text-secondary">Châm ngôn Nhật:</span> Kiên trì tích lũy
              từng ngày chính là sức mạnh bền vững nhất.
            </p>
          </div>
        </div>

        {/* Right Form Panel */}
        <div className="lg:col-span-7 p-space-xl flex flex-col justify-center space-y-space-lg bg-surface-container-lowest">
          <div className="space-y-1 border-b border-surface-container-high pb-space-md">
            <div className="flex items-center justify-between">
              <h2 className="text-headline-md font-headline-md font-bold text-on-surface">
                Đăng nhập
              </h2>
              <span className="px-2.5 py-0.5 rounded bg-primary-fixed text-on-primary-fixed text-label-sm font-bold border border-outline-variant">
                Thành viên &amp; Quản trị
              </span>
            </div>
            <p className="text-body-sm text-on-surface-variant">
              Bạn có thể đăng nhập bằng <strong>Username</strong> hoặc <strong>Email / Gmail</strong>.
            </p>
          </div>

          {errorMsg && (
            <div
              role="alert"
              className="flex items-start space-x-2 p-3 rounded-lg bg-error-container/70 border border-error/30 text-on-error-container text-body-sm"
            >
              <Icon name="error" className="text-lg shrink-0 mt-0.5" />
              <span>{errorMsg}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-space-md" noValidate>
            {/* Username or Email Input */}
            <div className="space-y-1.5">
              <label
                htmlFor="login-identifier"
                className="block text-label-md font-label-md font-semibold text-on-surface"
              >
                Tên đăng nhập hoặc Email
              </label>
              <div className="relative flex items-center">
                <span className="absolute left-3.5 text-on-surface-variant flex items-center pointer-events-none">
                  <Icon name="person" className="text-xl" />
                </span>
                <input
                  id="login-identifier"
                  type="text"
                  autoComplete="username"
                  value={identifier}
                  onChange={(e) => setIdentifier(e.target.value)}
                  placeholder="vd: komorebi_user hoặc ten@gmail.com"
                  className="w-full pl-11 pr-4 py-2.5 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface placeholder:text-outline/70 transition-all"
                />
              </div>
            </div>

            {/* Password Input */}
            <div className="space-y-1.5">
              <label
                htmlFor="login-password"
                className="block text-label-md font-label-md font-semibold text-on-surface"
              >
                Mật khẩu
              </label>
              <div className="relative flex items-center">
                <span className="absolute left-3.5 text-on-surface-variant flex items-center pointer-events-none">
                  <Icon name="lock" className="text-xl" />
                </span>
                <input
                  id="login-password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Nhập mật khẩu của bạn"
                  className="w-full pl-11 pr-11 py-2.5 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface placeholder:text-outline/70 transition-all"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((prev) => !prev)}
                  className="absolute right-3 p-1 text-on-surface-variant hover:text-primary rounded transition-colors cursor-pointer"
                  aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                >
                  <Icon name={showPassword ? 'visibility_off' : 'visibility'} className="text-xl" />
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full bg-primary hover:bg-primary-container text-on-primary py-3 px-space-md rounded-xl text-body-md font-bold flex items-center justify-center space-x-2 shadow-sm transition-all disabled:opacity-60 cursor-pointer"
            >
              <Icon name="login" className="text-xl" />
              <span>{submitting ? 'Đang xác thực...' : 'Đăng nhập ngay'}</span>
            </button>
          </form>

          {/* Dev Quick-Test Helper */}
          {isDevEnv && (
            <div className="pt-space-sm border-t border-surface-container-high space-y-2">
              <p className="text-label-sm font-semibold text-outline">
                Tài khoản thử nghiệm (Dev):
              </p>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={() => handleQuickFill('komorebi_user', 'User@123')}
                  className="px-3 py-1.5 rounded-lg bg-surface-container-low hover:bg-secondary-fixed text-on-surface text-label-sm font-semibold border border-surface-container-high transition-colors cursor-pointer"
                >
                  User (komorebi_user)
                </button>
                <button
                  type="button"
                  onClick={() => handleQuickFill('admin@komorebi.jp', 'Admin@123')}
                  className="px-3 py-1.5 rounded-lg bg-surface-container-low hover:bg-primary-fixed text-primary text-label-sm font-semibold border border-outline-variant transition-colors cursor-pointer"
                >
                  Admin (admin@komorebi.jp)
                </button>
              </div>
            </div>
          )}

          <div className="flex items-center justify-between text-body-sm text-on-surface-variant pt-1">
            <Link
              to="/"
              className="inline-flex items-center space-x-1 text-secondary hover:text-primary font-medium transition-colors"
            >
              <Icon name="arrow_back" className="text-base" />
              <span>Quay lại tra từ điển</span>
            </Link>
            <span className="text-label-sm text-outline">Spring Security • JWT</span>
          </div>
        </div>
      </section>
    </div>
  );
}

