import assert from 'node:assert/strict';
import test from 'node:test';

import { evaluateShareLimit } from '../src/shared/rate-limit.js';

const hour = 60 * 60 * 1000;
const day = 24 * hour;
const now = Date.parse('2026-08-07T12:00:00.000Z');

test('share accepts one and twenty items but rejects the twenty-first', () => {
  assert.equal(evaluateShareLimit([], 1, now).allowed, true);
  assert.equal(evaluateShareLimit([], 20, now).allowed, true);
  assert.equal(evaluateShareLimit([], 0, now).errorCode, 'item_limit');
  assert.equal(evaluateShareLimit([], 21, now).errorCode, 'item_limit');
});

test('the eleventh create in an hour is rate limited', () => {
  const events = Array.from({ length: 10 }, (_, index) => ({
    id: String(index),
    at: now - (index * 1000),
    items: 1,
  }));
  const result = evaluateShareLimit(events, 1, now);
  assert.equal(result.allowed, false);
  assert.equal(result.errorCode, 'rate_limit');
  assert.equal(result.retryAt, events.at(-1).at + hour);
});

test('daily item limit accepts 200 and rejects 201', () => {
  const events = [{ id: 'first', at: now - hour, items: 199 }];
  assert.equal(evaluateShareLimit(events, 1, now).allowed, true);
  const exceeded = evaluateShareLimit(events, 2, now);
  assert.equal(exceeded.allowed, false);
  assert.equal(exceeded.errorCode, 'share_limit');
  assert.equal(exceeded.retryAt, events[0].at + day);
});

test('events outside the rolling day no longer consume quota', () => {
  const events = [{ id: 'expired', at: now - day - 1, items: 200 }];
  assert.equal(evaluateShareLimit(events, 20, now).allowed, true);
});

test('active homework item limit includes shares retained beyond the daily window', () => {
  const events = [{
    id: 'active',
    at: now - day - 1,
    items: 199,
    expiresAt: now + hour,
  }];
  const activeLimit = { maxItems: 200 };
  assert.equal(evaluateShareLimit(events, 1, now, activeLimit).allowed, true);
  const exceeded = evaluateShareLimit(events, 2, now, activeLimit);
  assert.equal(exceeded.errorCode, 'share_limit');
  assert.equal(exceeded.retryAt, events[0].expiresAt);
});
