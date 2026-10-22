import axiosClient from './api/axiosClient.js';

export const authService = {
  login: (data) => axiosClient.post('/auth/login', data),
  loginWithGoogle: (credential) => axiosClient.post('/auth/google', { credential }),
  logout: () => axiosClient.post('/auth/logout'),
  getCurrentUser: () => axiosClient.get('/auth/me'),
};
