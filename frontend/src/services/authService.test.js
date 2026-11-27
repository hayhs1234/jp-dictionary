import { test, describe, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import axiosClient from './api/axiosClient.js';
import authService, { extractApiError } from './authService.js';

describe('authService unit tests', () => {
  const originalPost = axiosClient.post;
  const originalGet = axiosClient.get;
  const originalPatch = axiosClient.patch;

  afterEach(() => {
    axiosClient.post = originalPost;
    axiosClient.get = originalGet;
    axiosClient.patch = originalPatch;
  });

  test('login sends identifier and password and unwraps ApiResponse data', async () => {
    let capturedUrl = '';
    let capturedBody = null;
    axiosClient.post = async (url, body) => {
      capturedUrl = url;
      capturedBody = body;
      return {
        success: true,
        data: {
          accessToken: 'token-123',
          tokenType: 'Bearer',
          user: { username: 'komorebi_user', role: 'USER' },
        },
      };
    };

    const result = await authService.login({ identifier: 'komorebi_user', password: 'User@123' });
    assert.equal(capturedUrl, '/auth/login');
    assert.deepEqual(capturedBody, { identifier: 'komorebi_user', password: 'User@123' });
    assert.equal(result.accessToken, 'token-123');
    assert.equal(result.user.role, 'USER');
  });

  test('getCurrentUser, updateProfile, and getAdminUsers unwrap ApiResponse payloads', async () => {
    axiosClient.get = async (url) => {
      if (url === '/auth/me') {
        return { success: true, data: { username: 'komorebi_admin', role: 'ADMIN' } };
      }
      if (url === '/admin/users') {
        return { success: true, data: [{ id: 1 }, { id: 2 }] };
      }
      return null;
    };
    axiosClient.patch = async (url, body) => ({
      success: true,
      data: { username: 'komorebi_admin', displayName: body.displayName, url },
    });

    const me = await authService.getCurrentUser();
    assert.equal(me.role, 'ADMIN');

    const updated = await authService.updateProfile({ displayName: 'Sensei' });
    assert.equal(updated.displayName, 'Sensei');

    const users = await authService.getAdminUsers();
    assert.equal(users.length, 2);
  });

  test('extractApiError prioritizes backend message then field validation error', () => {
    assert.equal(
      extractApiError({ response: { data: { message: 'Sai mật khẩu' } } }),
      'Sai mật khẩu'
    );
    assert.equal(
      extractApiError({ response: { data: { data: { identifier: 'Không được để trống' } } } }),
      'Không được để trống'
    );
    assert.equal(extractApiError(null, 'Lỗi mặc định'), 'Lỗi mặc định');
  });
});

