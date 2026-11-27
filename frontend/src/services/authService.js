import axiosClient from './api/axiosClient.js';

function unwrapData(res) {
  return res?.data !== undefined ? res.data : res;
}

export function extractApiError(error, fallback = 'Đã xảy ra lỗi, vui lòng thử lại.') {
  const backendMessage = error?.response?.data?.message;
  if (backendMessage) return backendMessage;

  const fieldErrors = error?.response?.data?.data;
  if (fieldErrors && typeof fieldErrors === 'object') {
    const firstMsg = Object.values(fieldErrors)[0];
    if (typeof firstMsg === 'string' && firstMsg.trim()) return firstMsg;
  }

  return error?.message || fallback;
}

export const authService = {
  async login({ identifier, password }) {
    const res = await axiosClient.post('/auth/login', { identifier, password });
    return unwrapData(res);
  },

  async logout() {
    const res = await axiosClient.post('/auth/logout');
    return unwrapData(res);
  },

  async getCurrentUser() {
    const res = await axiosClient.get('/auth/me');
    return unwrapData(res);
  },

  async updateProfile(payload) {
    const res = await axiosClient.patch('/auth/me', payload);
    return unwrapData(res);
  },

  async getAdminUsers() {
    const res = await axiosClient.get('/admin/users');
    return unwrapData(res);
  },
};

export default authService;
