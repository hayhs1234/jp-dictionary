import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import { t } from './index.js';

describe('i18n skeleton test', () => {
  test('returns key', () => {
    assert.equal(t('hello'), 'hello');
  });
});
