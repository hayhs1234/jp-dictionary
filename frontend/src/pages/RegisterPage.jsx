import { useEffect, useRef, useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import Icon from '../components/common/Icon.jsx';
import { useAuth } from '../contexts/AuthContext.jsx';
import authService, { extractApiError } from '../services/authService.js';

const JLPT_LEVELS = ['N5', 'N4', 'N3', 'N2', 'N1'];
const USERNAME_PATTERN = /^[a-zA-Z0-9_.-]{3,50}$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,72}$/;
const OTP_LENGTH = 6;

function formatCountdown(totalSeconds) {
  const safe = Math.max(0, Number(totalSeconds) || 0);
  const mins = Math.floor(safe / 60);
  const secs = safe % 60;
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

function validateRegistrationForm(fields) {
  const cleanUsername = fields.username.trim();
  const cleanDisplayName = fields.displayName.trim();
  const cleanEmail = fields.email.trim();

  if (!USERNAME_PATTERN.test(cleanUsername)) {
    return 'Tên đăng nhập phải từ 3–50 ký tự, chỉ gồm chữ không dấu, số và ký tự _ . -';
  }
  if (cleanDisplayName.length < 2 || cleanDisplayName.length > 100) {
    return 'Tên hiển thị phải từ 2 đến 100 ký tự.';
  }
  if (!EMAIL_PATTERN.test(cleanEmail)) {
    return 'Vui lòng nhập địa chỉ email hợp lệ để nhận mã OTP.';
  }
  if (!PASSWORD_PATTERN.test(fields.password)) {
    return 'Mật khẩu phải từ 8–72 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường và 1 chữ số.';
  }
  if (fields.password !== fields.confirmPassword) {
    return 'Xác nhận mật khẩu chưa khớp.';
  }
  return '';
}

export default function RegisterPage() {
  const { isAuthenticated, isLoading: isAuthBootstrapping, verifyRegistrationOtp } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = location.state?.from?.pathname || '/';

  const [step, setStep] = useState('form');
  const [username, setUsername] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [jlptTarget, setJlptTarget] = useState('N5');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [registeredEmail, setRegisteredEmail] = useState('');
  const [otpDigits, setOtpDigits] = useState(() => Array(OTP_LENGTH).fill(''));
  const [otpSecondsLeft, setOtpSecondsLeft] = useState(0);
  const [resendSecondsLeft, setResendSecondsLeft] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [resending, setResending] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [infoMsg, setInfoMsg] = useState('');

  const otpInputRefs = useRef([]);

  useEffect(() => {
    if (step !== 'otp') return undefined;
    const timer = window.setInterval(() => {
      setOtpSecondsLeft((prev) => (prev > 0 ? prev - 1 : 0));
      setResendSecondsLeft((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => window.clearInterval(timer);
  }, [step]);

  useEffect(() => {
    if (step === 'otp') {
      otpInputRefs.current[0]?.focus();
    }
  }, [step]);

  if (!isAuthBootstrapping && isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setInfoMsg('');

    const validationError = validateRegistrationForm({
      username,
      displayName,
      email,
      password,
      confirmPassword,
    });
    if (validationError) {
      setErrorMsg(validationError);
      return;
    }

    setSubmitting(true);
    try {
      const dispatch = await authService.register({
        username: username.trim(),
        displayName: displayName.trim(),
        email: email.trim(),
        password,
        jlptTarget,
      });
      setRegisteredEmail(dispatch?.email || email.trim().toLowerCase());
      setOtpSecondsLeft(dispatch?.expiresInSeconds ?? 300);
      setResendSecondsLeft(dispatch?.resendCooldownSeconds ?? 60);
      setOtpDigits(Array(OTP_LENGTH).fill(''));
      setInfoMsg('Mã OTP 6 chữ số đã được gửi tới email của bạn. Vui lòng kiểm tra hộp thư hoặc mục Spam.');
      setStep('otp');
    } catch (err) {
      setErrorMsg(extractApiError(err, 'Không thể gửi yêu cầu đăng ký. Vui lòng thử lại.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleOtpDigitChange = (index, rawValue) => {
    const cleanDigits = rawValue.replace(/\D/g, '');
    if (!cleanDigits) {
      setOtpDigits((prev) => {
        const next = [...prev];
        next[index] = '';
        return next;
      });
      return;
    }

    setOtpDigits((prev) => {
      const next = [...prev];
      if (cleanDigits.length > 1) {
        for (let i = 0; i < OTP_LENGTH; i += 1) {
          next[i] = cleanDigits[i] || '';
        }
      } else {
        next[index] = cleanDigits;
      }
      return next;
    });

    if (cleanDigits.length > 1) {
      const focusIdx = Math.min(cleanDigits.length - 1, OTP_LENGTH - 1);
      otpInputRefs.current[focusIdx]?.focus();
    } else if (index < OTP_LENGTH - 1) {
      otpInputRefs.current[index + 1]?.focus();
    }
  };

  const handleOtpKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otpDigits[index] && index > 0) {
      otpInputRefs.current[index - 1]?.focus();
    }
  };

  const handleOtpPaste = (e) => {
    const pasted = e.clipboardData?.getData('text')?.replace(/\D/g, '') || '';
    if (!pasted) return;
    e.preventDefault();
    const nextDigits = Array.from({ length: OTP_LENGTH }, (_, idx) => pasted[idx] || '');
    setOtpDigits(nextDigits);
    const focusIdx = Math.min(pasted.length, OTP_LENGTH) - 1;
    if (focusIdx >= 0) {
      otpInputRefs.current[focusIdx]?.focus();
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setInfoMsg('');

    const code = otpDigits.join('');
    if (!/^\d{6}$/.test(code)) {
      setErrorMsg('Vui lòng nhập đủ 6 chữ số OTP từ email.');
      return;
    }

    setSubmitting(true);
    try {
      await verifyRegistrationOtp({ email: registeredEmail, otp: code });
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setErrorMsg(extractApiError(err, 'Mã OTP không chính xác hoặc đã hết hạn.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleResendOtp = async () => {
    if (resendSecondsLeft > 0 || resending) return;
    setErrorMsg('');
    setInfoMsg('');
    setResending(true);
    try {
      const dispatch = await authService.resendRegistrationOtp({ email: registeredEmail });
      setOtpSecondsLeft(dispatch?.expiresInSeconds ?? 300);
      setResendSecondsLeft(dispatch?.resendCooldownSeconds ?? 60);
      setOtpDigits(Array(OTP_LENGTH).fill(''));
      setInfoMsg('Đã gửi lại mã OTP mới tới email của bạn.');
      otpInputRefs.current[0]?.focus();
    } catch (err) {
      setErrorMsg(extractApiError(err, 'Không thể gửi lại mã OTP lúc này.'));
    } finally {
      setResending(false);
    }
  };

  const handleBackToForm = () => {
    setStep('form');
    setErrorMsg('');
    setInfoMsg('');
  };

  return (
    <div className="flex-1 flex items-center justify-center py-space-md">
      <section className="w-full max-w-4xl bg-surface-container-lowest border border-surface-container-high rounded-xl shadow-sm overflow-hidden grid grid-cols-1 lg:grid-cols-12">
        {/* Left Cultural Panel */}
        <div className="lg:col-span-5 seigaiha-pattern border-b lg:border-b-0 lg:border-r border-surface-container-high p-space-xl relative overflow-hidden flex flex-col justify-between gap-space-lg">
          <div className="absolute -right-4 -bottom-8 select-none pointer-events-none opacity-5 font-kanji-headline text-[140px] leading-none text-on-surface">
            始
          </div>

          <div className="flex items-center space-x-3 relative z-10">
            <div className="w-12 h-12 rounded-lg bg-primary text-on-primary border-2 border-on-primary-fixed-variant shadow-sm flex flex-col items-center justify-center leading-tight select-none">
              <span className="font-kanji-headline text-sm font-bold tracking-widest">木漏</span>
              <span className="text-[9px] tracking-wider opacity-90">新規</span>
            </div>
            <div>
              <p className="text-label-sm font-bold uppercase tracking-widest text-primary">
                Komorebi Nihongo Hub
              </p>
              <p className="text-body-sm text-on-surface-variant">
                Khởi đầu hành trình tiếng Nhật
              </p>
            </div>
          </div>

          <div className="space-y-2 relative z-10 my-2">
            <p className="text-label-sm font-bold text-secondary tracking-wider uppercase">
              はじめまして • Đăng ký mới
            </p>
            <h1 className="font-kanji-headline text-3xl md:text-4xl font-bold text-on-surface tracking-wide leading-snug">
              <ruby>
                新<rt>あたら</rt>
              </ruby>
              しい
              <ruby>
                一歩<rt>いっぽ</rt>
              </ruby>
            </h1>
            <p className="text-body-md text-on-surface-variant leading-relaxed">
              Tạo tài khoản cá nhân để lưu lộ trình JLPT, đồng bộ từ vựng và xác thực an toàn qua mã
              OTP gửi trực tiếp đến Gmail của bạn.
            </p>
          </div>

          <div className="relative z-10 p-3.5 rounded-lg bg-surface-container-lowest/90 border-l-2 border-secondary shadow-xs space-y-1">
            <p className="text-body-md font-bold text-on-surface font-kanji-headline">
              <ruby>
                千里<rt>せんり</rt>
              </ruby>
              の
              <ruby>
                道<rt>みち</rt>
              </ruby>
              も
              <ruby>
                一歩<rt>いっぽ</rt>
              </ruby>
              から
            </p>
            <p className="text-label-sm text-on-surface-variant">
              <span className="font-bold text-secondary">Châm ngôn Nhật:</span> Hành trình ngàn dặm
              luôn bắt đầu từ một bước chân đầu tiên.
            </p>
          </div>
        </div>

        {/* Right Form / OTP Panel */}
        <div className="lg:col-span-7 p-space-xl flex flex-col justify-center space-y-space-md bg-surface-container-lowest">
          <div className="space-y-1 border-b border-surface-container-high pb-space-md">
            <div className="flex items-center justify-between gap-2">
              <h2 className="text-headline-md font-headline-md font-bold text-on-surface">
                {step === 'form' ? 'Đăng ký tài khoản' : 'Xác thực mã OTP Email'}
              </h2>
              <span className="px-2.5 py-0.5 rounded bg-primary-fixed text-on-primary-fixed text-label-sm font-bold border border-outline-variant shrink-0">
                {step === 'form' ? 'Bước 1 / 2' : 'Bước 2 / 2'}
              </span>
            </div>
            <p className="text-body-sm text-on-surface-variant">
              {step === 'form'
                ? 'Điền thông tin bên dưới. Hệ thống sẽ gửi mã OTP 6 chữ số đến email của bạn.'
                : 'Nhập mã OTP 6 chữ số vừa được gửi qua Gmail để kích hoạt và đăng nhập ngay.'}
            </p>
          </div>

          {infoMsg && (
            <div
              role="status"
              className="flex items-start space-x-2 p-3 rounded-lg bg-surface-container-low border-l-2 border-secondary text-on-surface text-body-sm"
            >
              <Icon name="mark_email_read" className="text-lg text-secondary shrink-0 mt-0.5" />
              <span>{infoMsg}</span>
            </div>
          )}

          {errorMsg && (
            <div
              role="alert"
              className="flex items-start space-x-2 p-3 rounded-lg bg-error-container/70 border border-error/30 text-on-error-container text-body-sm"
            >
              <Icon name="error" className="text-lg shrink-0 mt-0.5" />
              <span>{errorMsg}</span>
            </div>
          )}

          {step === 'form' ? (
            <form onSubmit={handleRegisterSubmit} className="space-y-3.5" noValidate>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
                <div className="space-y-1">
                  <label
                    htmlFor="reg-username"
                    className="block text-label-md font-semibold text-on-surface"
                  >
                    Tên đăng nhập (Username)
                  </label>
                  <div className="relative flex items-center">
                    <span className="absolute left-3 text-on-surface-variant flex items-center pointer-events-none">
                      <Icon name="alternate_email" className="text-lg" />
                    </span>
                    <input
                      id="reg-username"
                      type="text"
                      autoComplete="username"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      placeholder="vd: minh_anh_jp"
                      className="w-full pl-10 pr-3 py-2 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface placeholder:text-outline/70 transition-all"
                    />
                  </div>
                </div>

                <div className="space-y-1">
                  <label
                    htmlFor="reg-display-name"
                    className="block text-label-md font-semibold text-on-surface"
                  >
                    Tên hiển thị
                  </label>
                  <div className="relative flex items-center">
                    <span className="absolute left-3 text-on-surface-variant flex items-center pointer-events-none">
                      <Icon name="badge" className="text-lg" />
                    </span>
                    <input
                      id="reg-display-name"
                      type="text"
                      autoComplete="name"
                      value={displayName}
                      onChange={(e) => setDisplayName(e.target.value)}
                      placeholder="vd: Nguyễn Minh Anh"
                      className="w-full pl-10 pr-3 py-2 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface placeholder:text-outline/70 transition-all"
                    />
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-12 gap-3.5">
                <div className="md:col-span-8 space-y-1">
                  <label
                    htmlFor="reg-email"
                    className="block text-label-md font-semibold text-on-surface"
                  >
                    Email / Gmail nhận mã OTP
                  </label>
                  <div className="relative flex items-center">
                    <span className="absolute left-3 text-on-surface-variant flex items-center pointer-events-none">
                      <Icon name="mail" className="text-lg" />
                    </span>
                    <input
                      id="reg-email"
                      type="email"
                      autoComplete="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="vd: tenban@gmail.com"
                      className="w-full pl-10 pr-3 py-2 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface placeholder:text-outline/70 transition-all"
                    />
                  </div>
                </div>

                <div className="md:col-span-4 space-y-1">
                  <label
                    htmlFor="reg-jlpt"
                    className="block text-label-md font-semibold text-on-surface"
                  >
                    Mục tiêu JLPT
                  </label>
                  <select
                    id="reg-jlpt"
                    value={jlptTarget}
                    onChange={(e) => setJlptTarget(e.target.value)}
                    className="w-full px-3 py-2 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface transition-all"
                  >
                    {JLPT_LEVELS.map((lvl) => (
                      <option key={lvl} value={lvl}>
                        JLPT {lvl}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
                <div className="space-y-1">
                  <label
                    htmlFor="reg-password"
                    className="block text-label-md font-semibold text-on-surface"
                  >
                    Mật khẩu
                  </label>
                  <div className="relative flex items-center">
                    <span className="absolute left-3 text-on-surface-variant flex items-center pointer-events-none">
                      <Icon name="lock" className="text-lg" />
                    </span>
                    <input
                      id="reg-password"
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="new-password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Tối thiểu 8 ký tự"
                      className="w-full pl-10 pr-10 py-2 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface placeholder:text-outline/70 transition-all"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword((prev) => !prev)}
                      className="absolute right-2.5 p-1 text-on-surface-variant hover:text-primary rounded transition-colors cursor-pointer"
                      aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                    >
                      <Icon name={showPassword ? 'visibility_off' : 'visibility'} className="text-lg" />
                    </button>
                  </div>
                </div>

                <div className="space-y-1">
                  <label
                    htmlFor="reg-confirm-password"
                    className="block text-label-md font-semibold text-on-surface"
                  >
                    Xác nhận mật khẩu
                  </label>
                  <div className="relative flex items-center">
                    <span className="absolute left-3 text-on-surface-variant flex items-center pointer-events-none">
                      <Icon name="lock_reset" className="text-lg" />
                    </span>
                    <input
                      id="reg-confirm-password"
                      type={showConfirmPassword ? 'text' : 'password'}
                      autoComplete="new-password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="Nhập lại mật khẩu"
                      className="w-full pl-10 pr-10 py-2 bg-surface-container-lowest border border-outline-variant focus:border-primary-container focus:ring-2 focus:ring-primary-container/20 rounded-xl text-body-md text-on-surface placeholder:text-outline/70 transition-all"
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword((prev) => !prev)}
                      className="absolute right-2.5 p-1 text-on-surface-variant hover:text-primary rounded transition-colors cursor-pointer"
                      aria-label={showConfirmPassword ? 'Ẩn mật khẩu xác nhận' : 'Hiện mật khẩu xác nhận'}
                    >
                      <Icon
                        name={showConfirmPassword ? 'visibility_off' : 'visibility'}
                        className="text-lg"
                      />
                    </button>
                  </div>
                </div>
              </div>

              <p className="text-label-sm text-on-surface-variant">
                Mật khẩu yêu cầu tối thiểu 8 ký tự, gồm chữ hoa (A-Z), chữ thường (a-z) và chữ số (0-9).
              </p>

              <button
                type="submit"
                disabled={submitting}
                className="w-full bg-primary hover:bg-primary-container text-on-primary py-3 px-space-md rounded-xl text-body-md font-bold flex items-center justify-center space-x-2 shadow-sm transition-all disabled:opacity-60 cursor-pointer"
              >
                <Icon name="send" className="text-xl" />
                <span>{submitting ? 'Đang gửi mã OTP...' : 'Đăng ký & Gửi mã OTP'}</span>
              </button>
            </form>
          ) : (
            <form onSubmit={handleVerifyOtp} className="space-y-space-md" noValidate>
              <div className="p-3.5 rounded-xl bg-surface-container-low border border-surface-container-high flex flex-wrap items-center justify-between gap-2">
                <div className="text-body-sm text-on-surface-variant">
                  Mã xác thực đã gửi tới{' '}
                  <strong className="text-on-surface">{registeredEmail}</strong>
                </div>
                <button
                  type="button"
                  onClick={handleBackToForm}
                  className="text-label-md font-semibold text-secondary hover:text-primary underline cursor-pointer"
                >
                  Sửa lại email
                </button>
              </div>

              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <label className="text-label-md font-semibold text-on-surface">
                    Nhập mã OTP (6 chữ số)
                  </label>
                  <span
                    className={`text-label-sm font-bold ${
                      otpSecondsLeft === 0 ? 'text-error' : 'text-secondary'
                    }`}
                  >
                    {otpSecondsLeft > 0
                      ? `Hết hạn sau ${formatCountdown(otpSecondsLeft)}`
                      : 'Mã OTP đã hết hạn'}
                  </span>
                </div>

                <div className="grid grid-cols-6 gap-2 sm:gap-3" onPaste={handleOtpPaste}>
                  {otpDigits.map((digit, index) => (
                    <input
                      key={index}
                      ref={(el) => {
                        otpInputRefs.current[index] = el;
                      }}
                      type="text"
                      inputMode="numeric"
                      autoComplete={index === 0 ? 'one-time-code' : 'off'}
                      maxLength={6}
                      aria-label={`Chữ số OTP thứ ${index + 1}`}
                      value={digit}
                      onChange={(e) => handleOtpDigitChange(index, e.target.value)}
                      onKeyDown={(e) => handleOtpKeyDown(index, e)}
                      className="w-full h-12 sm:h-14 text-center text-headline-md font-bold bg-surface-container-lowest border border-outline-variant focus:border-primary focus:ring-2 focus:ring-primary/20 rounded-xl text-on-surface transition-all"
                    />
                  ))}
                </div>
              </div>

              <button
                type="submit"
                disabled={submitting || otpSecondsLeft === 0}
                className="w-full bg-primary hover:bg-primary-container text-on-primary py-3 px-space-md rounded-xl text-body-md font-bold flex items-center justify-center space-x-2 shadow-sm transition-all disabled:opacity-60 cursor-pointer"
              >
                <Icon name="verified" className="text-xl" />
                <span>{submitting ? 'Đang xác thực...' : 'Xác thực & Hoàn tất đăng ký'}</span>
              </button>

              <div className="flex items-center justify-between text-body-sm pt-1">
                <span className="text-on-surface-variant">Chưa nhận được email?</span>
                <button
                  type="button"
                  onClick={handleResendOtp}
                  disabled={resendSecondsLeft > 0 || resending}
                  className="font-semibold text-primary hover:text-primary-container disabled:text-outline disabled:cursor-not-allowed cursor-pointer transition-colors"
                >
                  {resending
                    ? 'Đang gửi lại...'
                    : resendSecondsLeft > 0
                      ? `Gửi lại mã sau ${resendSecondsLeft}s`
                      : 'Gửi lại mã OTP'}
                </button>
              </div>
            </form>
          )}

          <div className="flex flex-wrap items-center justify-between gap-2 text-body-sm text-on-surface-variant pt-2 border-t border-surface-container-high">
            <Link
              to="/"
              className="inline-flex items-center space-x-1 text-secondary hover:text-primary font-medium transition-colors"
            >
              <Icon name="arrow_back" className="text-base" />
              <span>Quay lại tra từ điển</span>
            </Link>
            <div>
              Đã có tài khoản?{' '}
              <Link
                to="/login"
                state={{ from: location.state?.from }}
                className="font-bold text-primary hover:text-primary-container transition-colors"
              >
                Đăng nhập
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

