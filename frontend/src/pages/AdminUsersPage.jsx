import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Icon from '../components/common/Icon.jsx';
import authService, { extractApiError } from '../services/authService.js';

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    let active = true;
    authService
      .getAdminUsers()
      .then((data) => {
        if (active) setUsers(Array.isArray(data) ? data : []);
      })
      .catch((err) => {
        if (active) setErrorMsg(extractApiError(err, 'Không thể tải danh sách người dùng.'));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <section className="bg-surface-container-lowest border border-surface-container-high rounded-xl p-space-lg shadow-sm space-y-space-md">
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-surface-container-high pb-space-md">
        <div className="flex items-center space-x-2">
          <Icon name="admin_panel_settings" className="text-primary text-2xl" />
          <div>
            <h1 className="text-headline-sm font-headline-sm font-bold text-on-surface">
              Quản trị tài khoản (ADMIN)
            </h1>
            <p className="text-body-sm text-on-surface-variant">
              Khu vực phân quyền dành riêng cho vai trò ADMIN qua Spring Security.
            </p>
          </div>
        </div>

        <Link
          to="/profile"
          className="px-3 py-1.5 rounded-lg bg-surface-container-low hover:bg-surface-container text-on-surface text-label-md font-medium border border-surface-container-high transition-colors"
        >
          Về trang cá nhân
        </Link>
      </div>

      {errorMsg && (
        <div className="p-3 rounded-lg bg-error-container/70 border border-error/30 text-on-error-container text-body-sm">
          {errorMsg}
        </div>
      )}

      {loading ? (
        <div className="space-y-2 animate-pulse">
          <div className="h-10 bg-surface-container rounded-lg" />
          <div className="h-10 bg-surface-container-low rounded-lg" />
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-body-sm">
            <thead>
              <tr className="border-b border-surface-container-high text-label-sm uppercase text-on-surface-variant">
                <th className="py-2.5 px-3">ID</th>
                <th className="py-2.5 px-3">Username</th>
                <th className="py-2.5 px-3">Họ tên</th>
                <th className="py-2.5 px-3">Email</th>
                <th className="py-2.5 px-3">Vai trò</th>
                <th className="py-2.5 px-3">JLPT</th>
                <th className="py-2.5 px-3">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container-high">
              {users.map((item) => (
                <tr key={item.id} className="hover:bg-surface-container-low/60">
                  <td className="py-2.5 px-3 font-medium text-outline">{item.id}</td>
                  <td className="py-2.5 px-3 font-semibold text-on-surface">@{item.username}</td>
                  <td className="py-2.5 px-3 text-on-surface">{item.displayName}</td>
                  <td className="py-2.5 px-3 text-on-surface-variant">{item.email}</td>
                  <td className="py-2.5 px-3">
                    <span
                      className={`px-2 py-0.5 rounded text-label-sm font-bold ${
                        item.role === 'ADMIN'
                          ? 'bg-primary-fixed text-primary border border-outline-variant'
                          : 'bg-secondary-fixed text-on-secondary-fixed-variant'
                      }`}
                    >
                      {item.role}
                    </span>
                  </td>
                  <td className="py-2.5 px-3 font-semibold">{item.jlptTarget || 'N5'}</td>
                  <td className="py-2.5 px-3">
                    <span className="text-secondary font-medium">
                      {item.active ? 'Hoạt động' : 'Đã khóa'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

