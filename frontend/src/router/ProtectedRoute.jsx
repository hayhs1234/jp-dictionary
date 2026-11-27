import { Link, Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext.jsx';
import Icon from '../components/common/Icon.jsx';

export default function ProtectedRoute({ children, allowedRoles = [] }) {
  const { user, isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="max-w-xl mx-auto w-full bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-xl shadow-sm text-center space-y-3 animate-pulse">
        <div className="w-12 h-12 mx-auto rounded-[9999px] bg-surface-container" />
        <div className="h-4 w-48 mx-auto bg-surface-container rounded" />
        <p className="text-body-sm text-on-surface-variant">Đang xác thực phiên đăng nhập...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles.length > 0 && !allowedRoles.includes(user?.role)) {
    return (
      <section className="max-w-xl mx-auto w-full bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-xl shadow-sm text-center space-y-space-md">
        <div className="w-14 h-14 mx-auto rounded-[9999px] bg-error-container text-on-error-container flex items-center justify-center">
          <Icon name="gpp_bad" className="text-3xl" />
        </div>
        <div className="space-y-1">
          <h1 className="text-headline-sm font-headline-sm font-bold text-on-surface">
            Không có quyền truy cập (403)
          </h1>
          <p className="text-body-md text-on-surface-variant">
            Tài khoản hiện tại có vai trò <strong>{user?.role}</strong>, không đủ quyền truy cập khu vực này.
          </p>
        </div>
        <Link
          to="/"
          className="inline-flex items-center space-x-1.5 bg-primary hover:bg-primary-container text-on-primary px-space-md py-2 rounded-lg text-label-md font-semibold transition-colors"
        >
          <Icon name="arrow_back" className="text-base" />
          <span>Quay về trang chủ</span>
        </Link>
      </section>
    );
  }

  return children;
}
