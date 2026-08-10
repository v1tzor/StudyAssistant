import { createServices } from '../shared/appwrite.js';
import { limits } from '../shared/config.js';
import { PublicError, publicFailure, requestBody, requiredString } from '../shared/http.js';
import { installationHash, recordAiAttempt } from '../shared/rate-limit.js';
import { nextUtcDayMillis } from '../shared/time.js';
import { completeChat } from './deepseek.js';
import { releaseMessageQuota, reserveMessageQuota } from './quota.js';

const MAX_COMPLETION_BYTES = 256 * 1024;

export default async ({ req, res, error: logError }) => {
  let reservation;
  let services;
  try {
    const body = requestBody(req);
    if (body.operation !== 'complete') throw new PublicError('invalid', 400);
    services = createServices(req);
    const tokenHash = installationHash(body.installationToken);
    await recordAiAttempt(services.tables, tokenHash);
    const quotaKey = requiredString(body.quotaKey, 64);
    const completion = completionBody(body.completion);
    reservation = await reserveMessageQuota(services.tables, tokenHash, quotaKey);
    const response = await completeChat(completion);
    return res.json({
      response,
      quotaRemaining: reservation.remaining,
      quotaResetAt: nextUtcDayMillis(),
    });
  } catch (error) {
    if (reservation?.changed && services) {
      await releaseMessageQuota(services.tables, reservation);
    }
    if (!(error instanceof PublicError)) {
      logError(`${error?.name || 'Error'}:${error?.code || ''}:${error?.message || ''}`);
    }
    const failure = publicFailure(error);
    return res.json(
      {
        errorCode: failure.publicCode,
        retryAt: failure.retryAt,
        quotaResetAt: failure.publicCode === 'quota' ? nextUtcDayMillis() : null,
      },
      failure.status,
    );
  }
};

function completionBody(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    throw new PublicError('invalid', 400);
  }
  const body = JSON.stringify(value);
  if (Buffer.byteLength(body, 'utf8') > MAX_COMPLETION_BYTES) {
    throw new PublicError('too_large', 413);
  }
  return body;
}
