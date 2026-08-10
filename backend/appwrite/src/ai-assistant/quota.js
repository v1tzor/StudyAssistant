import { ID, Query, findRow } from '../shared/appwrite.js';
import { config, limits } from '../shared/config.js';
import { PublicError } from '../shared/http.js';
import { applyMessageQuota } from '../shared/quota.js';
import { utcDay } from '../shared/time.js';

const TRANSACTION_ATTEMPTS = 3;
const TRANSACTION_TTL_SECONDS = 60;

export async function reserveMessageQuota(tables, installationHash, quotaKey) {
  for (let attempt = 0; attempt < TRANSACTION_ATTEMPTS; attempt += 1) {
    const transaction = await tables.createTransaction({ ttl: TRANSACTION_TTL_SECONDS });
    try {
      const day = utcDay();
      const usage = await findRow(tables, config.aiUsageTableId, [
        Query.equal('installation_hash', [installationHash]),
        Query.equal('utc_day', [day]),
      ], transaction.$id);
      const result = applyMessageQuota(usage, quotaKey, limits.sharedDailyMessages);
      if (result.exceeded) throw new PublicError('quota', 429);
      if (!result.changed) {
        await rollbackTransaction(tables, transaction.$id);
        return { changed: false, installationHash, quotaKey, day, remaining: result.remaining };
      }
      const data = {
        installation_hash: installationHash,
        utc_day: day,
        used: result.used,
        message_keys: result.messageKeys,
        updated_at: new Date().toISOString(),
      };
      if (usage) {
        await tables.updateRow({
          databaseId: config.databaseId,
          tableId: config.aiUsageTableId,
          rowId: usage.$id,
          data,
          transactionId: transaction.$id,
        });
      } else {
        await tables.createRow({
          databaseId: config.databaseId,
          tableId: config.aiUsageTableId,
          rowId: ID.unique(),
          data,
          permissions: [],
          transactionId: transaction.$id,
        });
      }
      await tables.updateTransaction({ transactionId: transaction.$id, commit: true });
      return {
        changed: true,
        installationHash,
        quotaKey,
        day,
        remaining: result.remaining,
      };
    } catch (error) {
      await rollbackTransaction(tables, transaction.$id);
      if (error instanceof PublicError) throw error;
      if (error?.code === 409 && attempt < TRANSACTION_ATTEMPTS - 1) continue;
      throw error;
    }
  }
  throw new PublicError('server_unavailable', 503);
}

export async function releaseMessageQuota(tables, reservation) {
  for (let attempt = 0; attempt < TRANSACTION_ATTEMPTS; attempt += 1) {
    const transaction = await tables.createTransaction({ ttl: TRANSACTION_TTL_SECONDS });
    try {
      const usage = await findRow(tables, config.aiUsageTableId, [
        Query.equal('installation_hash', [reservation.installationHash]),
        Query.equal('utc_day', [reservation.day]),
      ], transaction.$id);
      if (!usage || !usage.message_keys.includes(reservation.quotaKey)) {
        await rollbackTransaction(tables, transaction.$id);
        return;
      }
      const messageKeys = usage.message_keys.filter((key) => key !== reservation.quotaKey);
      await tables.updateRow({
        databaseId: config.databaseId,
        tableId: config.aiUsageTableId,
        rowId: usage.$id,
        data: {
          used: Math.max(0, Number(usage.used || 0) - 1),
          message_keys: messageKeys,
          updated_at: new Date().toISOString(),
        },
        transactionId: transaction.$id,
      });
      await tables.updateTransaction({ transactionId: transaction.$id, commit: true });
      return;
    } catch (error) {
      await rollbackTransaction(tables, transaction.$id);
      if (error?.code === 409 && attempt < TRANSACTION_ATTEMPTS - 1) continue;
      return;
    }
  }
}

async function rollbackTransaction(tables, transactionId) {
  try {
    await tables.updateTransaction({ transactionId, rollback: true });
  } catch {
    return;
  }
}
