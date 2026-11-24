import { test, describe } from 'node:test';
import assert from 'node:assert/strict';
import dictionaryService from './dictionaryService.js';

describe('Frontend dictionaryService unit tests', () => {
  test('search returns matching fallback words when query matches', async () => {
    const results = await dictionaryService.search({ q: 'taberu' });
    assert.ok(Array.isArray(results));
    assert.ok(results.length > 0);
    assert.equal(results[0].kanji, '食べる');
    assert.equal(results[0].hiragana, 'たべる');
    assert.equal(results[0].primaryMeaning, 'ăn, dùng bữa');
  });

  test('search returns empty array when query does not match any word', async () => {
    const results = await dictionaryService.search({ q: 'nonexistentword999' });
    assert.ok(Array.isArray(results));
    assert.equal(results.length, 0);
  });

  test('search filters by JLPT level correctly', async () => {
    const results = await dictionaryService.search({ q: '', jlpt: 'N5' });
    assert.ok(Array.isArray(results));
    assert.ok(results.every((w) => w.jlptLevel === 'N5'));
  });

  test('getSuggestions returns autocomplete items matching query', async () => {
    const suggestions = await dictionaryService.getSuggestions('nhật');
    assert.ok(Array.isArray(suggestions));
    assert.ok(suggestions.length > 0);
    assert.ok(suggestions.some((s) => s.term === '日本語'));
  });

  test('getWordDetail returns complete word detail with breakdown and examples', async () => {
    const word = await dictionaryService.getWordDetail(100);
    assert.ok(word);
    assert.equal(word.kanji, '木漏れ日');
    assert.ok(Array.isArray(word.kanjiBreakdown));
    assert.equal(word.kanjiBreakdown.length, 3);
    assert.ok(Array.isArray(word.examples));
    assert.ok(word.examples.length >= 2);
    assert.ok(Array.isArray(word.senses));
    assert.equal(word.senses.length, 1);
  });

  test('search and getWordDetail return multiple senses for polysemous words', async () => {
    const results = await dictionaryService.search({ q: '直す' });
    assert.ok(Array.isArray(results));
    assert.ok(results.length > 0);
    const word = results[0];
    assert.equal(word.kanji, '直す');
    assert.ok(Array.isArray(word.senses));
    assert.equal(word.senses.length, 4);
    assert.equal(word.senses[0].senseOrder, 1);
    assert.equal(word.senses[0].meaning, 'sửa chữa, chỉnh sửa (đồ vật hỏng)');
    assert.equal(word.senses[1].senseOrder, 2);
    assert.equal(word.senses[1].meaning, 'đính chính, sửa lỗi sai, chữa trị');
    assert.equal(word.primaryMeaning, 'sửa chữa, chỉnh sửa');
  });
});

