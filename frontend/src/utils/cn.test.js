import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import { cn } from './cn.js';

describe('Utility: cn (clsx + tailwind-merge)', () => {
  test('merges simple class names', () => {
    const result = cn('px-4', 'py-2');
    assert.equal(result, 'px-4 py-2');
  });

  test('resolves conflicting tailwind classes', () => {
    const result = cn('px-2', 'px-4');
    assert.equal(result, 'px-4');
  });

  test('handles conditional classes correctly', () => {
    const isActive = true;
    const isDisabled = false;
    const result = cn(
      'base-class',
      isActive && 'is-active',
      isDisabled && 'is-disabled',
      { 'bg-red-500': true, 'text-white': false }
    );
    assert.equal(result, 'base-class is-active bg-red-500');
  });

  test('handles empty or undefined inputs without error', () => {
    const result = cn(null, undefined, '', false);
    assert.equal(result, '');
  });
});
