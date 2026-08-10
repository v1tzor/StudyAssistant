import assert from 'node:assert/strict';
import test from 'node:test';

import { nextUtcDayMillis, utcDay } from '../src/shared/time.js';

test('quota day and reset are based on UTC', () => {
  const now = new Date('2026-08-04T23:59:59.000Z');
  assert.equal(utcDay(now), '2026-08-04');
  assert.equal(nextUtcDayMillis(now), Date.parse('2026-08-05T00:00:00.000Z'));
});
