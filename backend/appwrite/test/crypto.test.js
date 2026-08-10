import assert from 'node:assert/strict';
import test from 'node:test';

import {
  generateShareCode,
  hmacSha256,
  normalizeShareCode,
  verifyHmacSha256,
} from '../src/shared/crypto.js';

const secret = 'test-secret-with-at-least-thirty-two-characters';

test('share code uses twelve unambiguous Crockford characters', () => {
  const code = generateShareCode();
  assert.match(code, /^[0-9A-HJKMNP-TV-Z]{12}$/);
  assert.equal(normalizeShareCode(`${code.slice(0, 4)}-${code.slice(4, 8)}-${code.slice(8)}`), code);
});

test('context-separated HMAC verifies exact values', () => {
  const signature = hmacSha256('value', secret, 'share-code');
  assert.equal(verifyHmacSha256('value', signature, secret, 'share-code'), true);
  assert.equal(verifyHmacSha256('value', signature, secret, 'claim'), false);
  assert.equal(verifyHmacSha256('other', signature, secret, 'share-code'), false);
});
