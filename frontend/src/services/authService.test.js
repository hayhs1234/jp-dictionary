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

  test('register, verifyRegistrationOtp, and resendRegistrationOtp call expected endpoints', async () => {
    const calls = [];
    axiosClient.post = async (url, body) => {
      calls.push({ url, body });
      if (url === '/auth/register') {
        return {
          success: true,
          data: { email: 'new@gmail.com', expiresInSeconds: 300, resendCooldownSeconds: 60 },
        };
      }
      if (url === '/auth/register/resend-otp') {
        return {
          success: true,
          data: { email: 'new@gmail.com', expiresInSeconds: 300, resendCooldownSeconds: 60 },
        };
      }
      return {
        success: true,
        data: {
          accessToken: 'reg-jwt',
          tokenType: 'Bearer',
          user: { username: 'new_user', role: 'USER', jlptTarget: 'N4' },
        },
      };
    };

    const dispatch = await authService.register({
      username: 'new_user',
      email: 'new@gmail.com',
      password: 'Password1',
      displayName: 'New User',
      jlptTarget: 'N4',
    });
    assert.equal(dispatch.email, 'new@gmail.com');
    assert.equal(dispatch.expiresInSeconds, 300);

    const resent = await authService.resendRegistrationOtp({ email: 'new@gmail.com' });
    assert.equal(resent.resendCooldownSeconds, 60);

    const verified = await authService.verifyRegistrationOtp({
      email: 'new@gmail.com',
      otp: '123456',
    });
    assert.equal(verified.accessToken, 'reg-jwt');
    assert.equal(verified.user.jlptTarget, 'N4');
    assert.deepEqual(
      calls.map((c) => c.url),
      ['/auth/register', '/auth/register/resend-otp', '/auth/register/verify-otp']
    );
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

