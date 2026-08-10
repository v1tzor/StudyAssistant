import { ID, Query, deleteFile, findRow, uploadBytes } from '../shared/appwrite.js';
import { config, limits } from '../shared/config.js';
import { generateShareCode, hmacSha256, normalizeShareCode } from '../shared/crypto.js';
import { PublicError, jsonPayload } from '../shared/http.js';
import {
  installationHash,
  recordShareCodeAttempt,
  reserveShareCreation,
  rollbackShareCreation,
} from '../shared/rate-limit.js';
import { sharePayload, validateAvailableShare } from '../shared/share.js';

const CODE_ATTEMPTS = 5;

export async function createHomeworkShare({ tables, storage }, body) {
  const creatorHash = installationHash(body.installationToken);
  const homeworks = body.share?.homeworks;
  const itemCount = Array.isArray(homeworks) ? homeworks.length : 0;
  const payload = jsonPayload(body.share, limits.maxPayloadBytes);
  const activeUntil = Date.now() + limits.homeworkLifetimeMs;
  const reservation = await reserveShareCreation(
    tables,
    'homework',
    creatorHash,
    itemCount,
    activeUntil,
  );
  let fileId;
  try {
    fileId = await uploadBytes(storage, payload, 'homework-share.bin');
    return await createRow(tables, creatorHash, itemCount, fileId, activeUntil);
  } catch (error) {
    await rollbackShareCreation(tables, reservation);
    if (fileId) await deleteFile(storage, fileId);
    throw error;
  }
}

export async function fetchHomeworkShare({ tables, storage }, body) {
  const tokenHash = installationHash(body.installationToken);
  await recordShareCodeAttempt(tables, 'homework', tokenHash);
  const codeHash = codeHashValue(body.code);
  const row = await findRow(tables, config.homeworkTableId, [
    Query.equal('code_hash', [codeHash]),
  ]);
  validateAvailableShare(row);
  return { share: await sharePayload(storage, row) };
}

async function createRow(tables, creatorHash, itemCount, fileId, activeUntil) {
  const now = new Date();
  const expiresAt = new Date(activeUntil);
  for (let attempt = 0; attempt < CODE_ATTEMPTS; attempt += 1) {
    const code = generateShareCode();
    try {
      await tables.createRow({
        databaseId: config.databaseId,
        tableId: config.homeworkTableId,
        rowId: ID.unique(),
        data: {
          code_hash: codeHashValue(code),
          creator_hash: creatorHash,
          item_count: itemCount,
          file_id: fileId,
          created_at: now.toISOString(),
          expires_at: expiresAt.toISOString(),
        },
        permissions: [],
      });
      return {
        link: {
          code,
          createdAt: now.getTime(),
          expiresAt: expiresAt.getTime(),
        },
      };
    } catch (error) {
      if (error?.code !== 409 || attempt === CODE_ATTEMPTS - 1) throw error;
    }
  }
  throw new PublicError('server_unavailable', 503);
}

function codeHashValue(code) {
  try {
    return hmacSha256(
      normalizeShareCode(code),
      process.env.SHARE_HMAC_SECRET,
      'homework-code',
    );
  } catch {
    throw new PublicError('invalid', 400);
  }
}
