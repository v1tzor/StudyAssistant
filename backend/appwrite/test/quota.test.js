import assert from 'node:assert/strict';
import test from 'node:test';

import { applyMessageQuota } from '../src/shared/quota.js';

test('tool continuations reuse the same daily quota unit', () => {
  const first = applyMessageQuota(null, 'message-1', 25);
  const continuation = applyMessageQuota(
    { used: first.used, message_keys: first.messageKeys },
    'message-1',
    25,
  );
  assert.equal(first.remaining, 24);
  assert.equal(continuation.changed, false);
  assert.equal(continuation.used, 1);
  assert.equal(continuation.remaining, 24);
});

test('the 26th distinct user message is rejected', () => {
  let usage = null;
  for (let index = 1; index <= 25; index += 1) {
    const result = applyMessageQuota(usage, `message-${index}`, 25);
    assert.equal(result.exceeded, false);
    usage = { used: result.used, message_keys: result.messageKeys };
  }
  const exceeded = applyMessageQuota(usage, 'message-26', 25);
  assert.equal(exceeded.exceeded, true);
  assert.equal(exceeded.remaining, 0);
});
