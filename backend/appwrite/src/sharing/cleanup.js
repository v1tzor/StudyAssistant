import { Query, listRows } from '../shared/appwrite.js';
import { config } from '../shared/config.js';
import { unixMillis } from '../shared/http.js';
import { removeExpiredLimits } from '../shared/rate-limit.js';
import { removeShare } from '../shared/share.js';

export async function cleanupTemporaryContent({ tables, storage }) {
  const now = Date.now();
  const expiryQuery = Query.lessThanEqual('expires_at', new Date(now).toISOString());
  const scheduleRows = await listRows(tables, config.scheduleTableId, [expiryQuery]);
  const homeworkRows = await listRows(tables, config.homeworkTableId, [expiryQuery]);
  let shares = 0;
  for (const [tableId, rows] of [
    [config.scheduleTableId, scheduleRows],
    [config.homeworkTableId, homeworkRows],
  ]) {
    for (const row of rows.filter((item) => unixMillis(item.expires_at) <= now)) {
      await removeShare(tables, storage, tableId, row);
      shares += 1;
    }
  }
  return {
    removedShares: shares,
    removedLimits: await removeExpiredLimits(tables, now),
  };
}
