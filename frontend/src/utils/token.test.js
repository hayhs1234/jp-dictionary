import { test, describe, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { tokenUtils } from './token.js';

describe('tokenUtils tests', () => {
  afterEach(() => {
    delete globalThis.window;
  });

  test('returns null safely when window is undefined (Node/SSR)', () => {
    delete globalThis.window;
    assert.equal(tokenUtils.getToken(), null);
    assert.doesNotThrow(() => tokenUtils.setToken('abc'));
    assert.doesNotThrow(() => tokenUtils.removeToken());
  });

  test('stores, reads, and removes access token when localStorage exists', () => {
    const store = new Map();
    globalThis.window = {
      localStorage: {
        getItem: (key) => (store.has(key) ? store.get(key) : null),
        setItem: (key, value) => store.set(key, String(value)),
        removeItem: (key) => store.delete(key),
      },
    };

    tokenUtils.setToken('jwt-sample-123');
    assert.equal(tokenUtils.getToken(), 'jwt-sample-123');

    tokenUtils.removeToken();
    assert.equal(tokenUtils.getToken(), null);
  });
});

