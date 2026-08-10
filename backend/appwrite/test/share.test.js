import assert from 'node:assert/strict';
import test from 'node:test';

import { jsonPayload } from '../src/shared/http.js';
import { validateAvailableShare } from '../src/shared/share.js';

test('available share accepts a future expiry', () => {
  assert.doesNotThrow(() => validateAvailableShare({
    expires_at: '2026-08-04T12:30:00.000Z',
    consumed_at: null,
  }, Date.parse('2026-08-04T12:00:00.000Z')));
});

test('share availability distinguishes invalid, expired, and consumed data', () => {
  assert.throws(
    () => validateAvailableShare(null, 0),
    (error) => error.publicCode === 'invalid' && error.status === 404,
  );
  assert.throws(
    () => validateAvailableShare({ expires_at: '2026-08-04T12:00:00.000Z' }, Date.parse('2026-08-04T12:00:00.000Z')),
    (error) => error.publicCode === 'expired' && error.status === 410,
  );
  assert.throws(
    () => validateAvailableShare({
      expires_at: '2026-08-04T12:30:00.000Z',
      consumed_at: '2026-08-04T12:01:00.000Z',
    }, Date.parse('2026-08-04T12:00:00.000Z')),
    (error) => error.publicCode === 'consumed' && error.status === 410,
  );
});

test('JSON share payload enforces the encoded byte limit', () => {
  assert.deepEqual(jsonPayload({ ok: true }, 11), Buffer.from('{"ok":true}'));
  assert.throws(
    () => jsonPayload({ text: 'too large' }, 3),
    (error) => error.publicCode === 'too_large' && error.status === 413,
  );
});
