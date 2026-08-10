import { ID, Query, findRow, listRows } from './appwrite.js';
import { config, limits } from './config.js';
import { hmacSha256, randomToken } from './crypto.js';
import { PublicError, unixMillis } from './http.js';

const TRANSACTION_ATTEMPTS = 3;
const TRANSACTION_TTL_SECONDS = 60;
const HOUR_MS = 60 * 60 * 1000;
const DAY_MS = 24 * HOUR_MS;

export function installationHash(token) {
  if (typeof token !== 'string' || token.length < 16 || token.length > 256) {
    throw new PublicError('invalid', 400);
  }
  return hmacSha256(token, process.env.INSTALLATION_HMAC_SECRET, 'installation');
}

export function evaluateShareLimit(events, itemCount, now = Date.now(), activeLimit) {
  if (!Number.isInteger(itemCount) || itemCount < 1 || itemCount > limits.maxShareItems) {
    return { allowed: false, errorCode: 'item_limit', retryAt: null, events: [] };
  }
  const retainedEvents = normaliseEvents(events, now - DAY_MS, now);
  const dailyEvents = retainedEvents.filter((event) => event.at > now - DAY_MS);
  const hourlyEvents = dailyEvents.filter((event) => event.at > now - HOUR_MS);
  if (hourlyEvents.length >= limits.shareCreatesPerHour) {
    return {
      allowed: false,
      errorCode: 'rate_limit',
      retryAt: Math.min(...hourlyEvents.map((event) => event.at)) + HOUR_MS,
      events: retainedEvents,
    };
  }
  const dailyItems = dailyEvents.reduce((sum, event) => sum + event.items, 0);
  if (dailyItems + itemCount > limits.shareItemsPerDay) {
    return {
      allowed: false,
      errorCode: 'share_limit',
        retryAt: shareItemsRetryAt(dailyEvents, itemCount, now),
        events: retainedEvents,
    };
  }
  if (activeLimit) {
    const activeEventsByExpiry = retainedEvents.filter((event) => event.expiresAt > now);
    const activeItems = activeEventsByExpiry.reduce((sum, event) => sum + event.items, 0);
    if (activeItems + itemCount > activeLimit.maxItems) {
      return {
        allowed: false,
        errorCode: 'share_limit',
        retryAt: activeItemsRetryAt(activeEventsByExpiry, itemCount, activeLimit.maxItems),
        events: retainedEvents,
      };
    }
  }
  return { allowed: true, events: retainedEvents };
}

export async function reserveShareCreation(tables, type, tokenHash, itemCount, activeUntil) {
  if (!Number.isInteger(itemCount) || itemCount < 1 || itemCount > limits.maxShareItems) {
    throw new PublicError('item_limit', 400);
  }
  const scope = `${type}_create`;
  const reservation = {
    id: randomToken(12),
    at: Date.now(),
    items: itemCount,
    ...(activeUntil ? { expiresAt: activeUntil } : {}),
  };
  await updateEvents(tables, scope, tokenHash, (events) => {
    const activeLimit = type === 'homework' ? {
      maxItems: limits.maxActiveHomeworkItems,
    } : undefined;
    const result = evaluateShareLimit(events, itemCount, reservation.at, activeLimit);
    if (!result.allowed) {
      throw new PublicError(result.errorCode, 429, result.retryAt);
    }
    return [...result.events, reservation];
  });
  return { ...reservation, scope, tokenHash };
}

export async function rollbackShareCreation(tables, reservation) {
  await updateEvents(
    tables,
    reservation.scope,
    reservation.tokenHash,
    (events) => events.filter((event) => event.id !== reservation.id),
  );
}

export async function recordShareCodeAttempt(tables, type, tokenHash) {
  const now = Date.now();
  await updateEvents(tables, `${type}_code`, tokenHash, (events) => {
    const activeEvents = normaliseEvents(events, now - limits.shareCodeAttemptWindowMs);
    if (activeEvents.length >= limits.shareCodeAttempts) {
      const retryAt = Math.min(...activeEvents.map((event) => event.at)) + limits.shareCodeAttemptWindowMs;
      throw new PublicError('rate_limit', 429, retryAt);
    }
    return [...activeEvents, { id: randomToken(12), at: now, items: 1 }];
  });
}

export async function recordAiAttempt(tables, tokenHash) {
  const now = Date.now();
  await updateEvents(tables, 'ai_complete', tokenHash, (events) => {
    const activeEvents = normaliseEvents(events, now - HOUR_MS);
    if (activeEvents.length >= limits.aiCallsPerHour) {
      const retryAt = Math.min(...activeEvents.map((event) => event.at)) + HOUR_MS;
      throw new PublicError('rate_limit', 429, retryAt);
    }
    return [...activeEvents, { id: randomToken(12), at: now, items: 1 }];
  });
}

export async function removeExpiredLimits(tables, now = Date.now()) {
  const rows = await listRows(tables, config.abuseLimitsTableId, [
    Query.lessThanEqual('expires_at', new Date(now).toISOString()),
  ]);
  for (const row of rows.filter((item) => unixMillis(item.expires_at) <= now)) {
    await tables.deleteRow({
      databaseId: config.databaseId,
      tableId: config.abuseLimitsTableId,
      rowId: row.$id,
    });
  }
  return rows.length;
}

async function updateEvents(tables, scope, tokenHash, transform) {
  for (let attempt = 0; attempt < TRANSACTION_ATTEMPTS; attempt += 1) {
    const transaction = await tables.createTransaction({ ttl: TRANSACTION_TTL_SECONDS });
    try {
      const row = await findRow(tables, config.abuseLimitsTableId, [
        Query.equal('installation_hash', [tokenHash]),
        Query.equal('scope', [scope]),
      ], transaction.$id);
      const events = transform(parseEvents(row?.events));
      const now = new Date();
      const data = {
        installation_hash: tokenHash,
        scope,
        events: JSON.stringify(events),
        updated_at: now.toISOString(),
        expires_at: new Date(expiryFor(scope, events, now.getTime())).toISOString(),
      };
      if (row) {
        await tables.updateRow({
          databaseId: config.databaseId,
          tableId: config.abuseLimitsTableId,
          rowId: row.$id,
          data,
          transactionId: transaction.$id,
        });
      } else {
        await tables.createRow({
          databaseId: config.databaseId,
          tableId: config.abuseLimitsTableId,
          rowId: ID.unique(),
          data,
          permissions: [],
          transactionId: transaction.$id,
        });
      }
      await tables.updateTransaction({ transactionId: transaction.$id, commit: true });
      return;
    } catch (error) {
      await rollbackTransaction(tables, transaction.$id);
      if (error instanceof PublicError) throw error;
      if (error?.code === 409 && attempt < TRANSACTION_ATTEMPTS - 1) continue;
      throw error;
    }
  }
  throw new PublicError('server_unavailable', 503);
}

async function rollbackTransaction(tables, transactionId) {
  try {
    await tables.updateTransaction({ transactionId, rollback: true });
  } catch {
    return;
  }
}

function parseEvents(value) {
  try {
    const events = JSON.parse(value || '[]');
    return Array.isArray(events) ? events : [];
  } catch {
    return [];
  }
}

function normaliseEvents(events, after, now = Number.POSITIVE_INFINITY) {
  return events
    .filter((event) => {
      return Number.isFinite(event?.at) &&
        Number.isFinite(event?.items) &&
        (event.at > after || event.expiresAt > now);
    })
    .sort((first, second) => first.at - second.at);
}

function shareItemsRetryAt(events, requestedItems, now) {
  let itemCount = events.reduce((sum, event) => sum + event.items, 0);
  for (const event of events) {
    itemCount -= event.items;
    if (itemCount + requestedItems <= limits.shareItemsPerDay) return event.at + DAY_MS;
  }
  return now + DAY_MS;
}

function activeItemsRetryAt(events, requestedItems, maxItems) {
  let itemCount = events.reduce((sum, event) => sum + event.items, 0);
  for (const event of events.toSorted((first, second) => first.expiresAt - second.expiresAt)) {
    itemCount -= event.items;
    if (itemCount + requestedItems <= maxItems) return event.expiresAt;
  }
  return Math.min(...events.map((event) => event.expiresAt));
}

function expiryFor(scope, events, now) {
  const window = scope.endsWith('_code') ? limits.shareCodeAttemptWindowMs : DAY_MS;
  const lastEvent = events.at(-1)?.at || now;
  return lastEvent + window;
}
