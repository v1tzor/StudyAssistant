import { deleteFile, downloadBytes } from './appwrite.js';
import { config } from './config.js';
import { PublicError, unixMillis } from './http.js';

export function validateAvailableShare(row, now = Date.now()) {
  if (!row) throw new PublicError('invalid', 404);
  if (unixMillis(row.expires_at) <= now) throw new PublicError('expired', 410);
  if (row.consumed_at) throw new PublicError('consumed', 410);
}

export async function sharePayload(storage, row) {
  return JSON.parse((await downloadBytes(storage, row.file_id)).toString('utf8'));
}

export async function removeShare(tables, storage, tableId, row) {
  await deleteFile(storage, row.file_id);
  await tables.deleteRow({
    databaseId: config.databaseId,
    tableId,
    rowId: row.$id,
  });
}
