import { ID, Query, deleteFile, findRow, uploadBytes } from '../shared/appwrite.js';
import { config, limits } from '../shared/config.js';
import {
  generateShareCode,
  hmacSha256,
  normalizeShareCode,
  randomToken,
  verifyHmacSha256,
} from '../shared/crypto.js';
import { PublicError, jsonPayload, requiredString, unixMillis } from '../shared/http.js';
import {
  installationHash,
  recordShareCodeAttempt,
  reserveShareCreation,
  rollbackShareCreation,
} from '../shared/rate-limit.js';
import { sharePayload, validateAvailableShare } from '../shared/share.js';

const CODE_ATTEMPTS = 5;

export async function createScheduleShare({ tables, storage }, body) {
  const creatorHash = installationHash(body.installationToken);
  const schedules = body.share?.schedules;
  const itemCount = Array.isArray(schedules) ? schedules.length : 0;
  const payload = jsonPayload(body.share, limits.maxPayloadBytes);
  const reservation = await reserveShareCreation(tables, 'schedule', creatorHash, itemCount);
  let fileId;
  try {
    fileId = await uploadBytes(storage, payload, 'schedule-share.bin');
    return await createRow(tables, creatorHash, itemCount, fileId);
  } catch (error) {
    await rollbackShareCreation(tables, reservation);
    if (fileId) await deleteFile(storage, fileId);
    throw error;
  }
}

export async function claimScheduleShare({ tables, storage }, body) {
  const tokenHash = installationHash(body.installationToken);
  await recordShareCodeAttempt(tables, 'schedule', tokenHash);
  const codeHash = codeHashValue(body.code);
  const now = Date.now();
  const claimSecret = randomToken(24);
  const transaction = await tables.createTransaction({ ttl: 60 });
  let row;
  try {
    row = await findRow(tables, config.scheduleTableId, [
      Query.equal('code_hash', [codeHash]),
    ], transaction.$id);
    validateAvailableShare(row, now);
    if (row.claimed_until && unixMillis(row.claimed_until) > now) {
      throw new PublicError('claimed', 409);
    }
    await tables.updateRow({
      databaseId: config.databaseId,
      tableId: config.scheduleTableId,
      rowId: row.$id,
      data: {
        claim_hash: claimHash(row.$id, claimSecret),
        claimed_until: new Date(now + limits.scheduleClaimLifetimeMs).toISOString(),
      },
      transactionId: transaction.$id,
    });
    await tables.updateTransaction({ transactionId: transaction.$id, commit: true });
  } catch (error) {
    await rollbackTransaction(tables, transaction.$id);
    if (error?.code === 409 && !(error instanceof PublicError)) {
      throw new PublicError('claimed', 409);
    }
    throw error;
  }
  try {
    return {
      claim: {
        claimToken: `${row.$id}.${claimSecret}`,
        share: await sharePayload(storage, row),
      },
    };
  } catch (error) {
    await clearClaim(tables, row.$id, claimSecret);
    throw error;
  }
}

export async function confirmScheduleShare({ tables, storage }, body) {
  const claim = await claimedRow(tables, body.claimToken);
  if (!claim.row.consumed_at) {
    await tables.updateRow({
      databaseId: config.databaseId,
      tableId: config.scheduleTableId,
      rowId: claim.row.$id,
      data: { consumed_at: new Date().toISOString() },
    });
    await deleteFile(storage, claim.row.file_id);
  }
  return { success: true };
}

export async function releaseScheduleShare({ tables }, body) {
  const claim = await claimedRow(tables, body.claimToken);
  if (claim.row.consumed_at) throw new PublicError('consumed', 410);
  await tables.updateRow({
    databaseId: config.databaseId,
    tableId: config.scheduleTableId,
    rowId: claim.row.$id,
    data: { claim_hash: null, claimed_until: null },
  });
  return { success: true };
}

async function createRow(tables, creatorHash, itemCount, fileId) {
  const now = new Date();
  const expiresAt = new Date(now.getTime() + limits.scheduleLifetimeMs);
  for (let attempt = 0; attempt < CODE_ATTEMPTS; attempt += 1) {
    const code = generateShareCode();
    try {
      await tables.createRow({
        databaseId: config.databaseId,
        tableId: config.scheduleTableId,
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

async function claimedRow(tables, tokenInput) {
  const token = requiredString(tokenInput, 256);
  const separator = token.indexOf('.');
  if (separator <= 0 || separator === token.length - 1) throw new PublicError('claimed', 409);
  const rowId = token.slice(0, separator);
  const secret = token.slice(separator + 1);
  let row;
  try {
    row = await tables.getRow({
      databaseId: config.databaseId,
      tableId: config.scheduleTableId,
      rowId,
    });
  } catch (error) {
    if (error?.code === 404) throw new PublicError('invalid', 404);
    throw error;
  }
  if (!verifyHmacSha256(
    `${rowId}.${secret}`,
    row.claim_hash,
    process.env.SHARE_HMAC_SECRET,
    'schedule-claim',
  )) {
    throw new PublicError('claimed', 409);
  }
  if (!row.claimed_until || unixMillis(row.claimed_until) <= Date.now()) {
    throw new PublicError('claimed', 409);
  }
  return { row, secret };
}

async function clearClaim(tables, rowId, secret) {
  try {
    const row = await tables.getRow({
      databaseId: config.databaseId,
      tableId: config.scheduleTableId,
      rowId,
    });
    if (row.claim_hash !== claimHash(rowId, secret)) return;
    await tables.updateRow({
      databaseId: config.databaseId,
      tableId: config.scheduleTableId,
      rowId,
      data: { claim_hash: null, claimed_until: null },
    });
  } catch {
    return;
  }
}

async function rollbackTransaction(tables, transactionId) {
  try {
    await tables.updateTransaction({ transactionId, rollback: true });
  } catch {
    return;
  }
}

function codeHashValue(code) {
  try {
    return hmacSha256(
      normalizeShareCode(code),
      process.env.SHARE_HMAC_SECRET,
      'schedule-code',
    );
  } catch {
    throw new PublicError('invalid', 400);
  }
}

function claimHash(rowId, secret) {
  return hmacSha256(
    `${rowId}.${secret}`,
    process.env.SHARE_HMAC_SECRET,
    'schedule-claim',
  );
}
