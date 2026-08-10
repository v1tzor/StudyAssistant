export function requestBody(req) {
  const body = req.bodyJson;
  if (!body || typeof body !== 'object' || Array.isArray(body)) {
    throw new PublicError('invalid', 400);
  }
  return body;
}

export function requiredString(value, maxLength = 2048) {
  if (typeof value !== 'string' || value.length === 0 || value.length > maxLength) {
    throw new PublicError('invalid', 400);
  }
  return value;
}

export function jsonPayload(value, maxBytes) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    throw new PublicError('invalid', 400);
  }
  const payload = Buffer.from(JSON.stringify(value), 'utf8');
  if (payload.length > maxBytes) throw new PublicError('too_large', 413);
  return payload;
}

export function unixMillis(value) {
  return new Date(value).getTime();
}

export function publicFailure(error) {
  if (error instanceof PublicError) return error;
  if (error?.code === 404) return new PublicError('invalid', 404);
  if (error?.code === 409) return new PublicError('invalid', 409);
  return new PublicError('server_unavailable', 503);
}

export class PublicError extends Error {
  constructor(code, status = 400, retryAt = null) {
    super(code);
    this.publicCode = code;
    this.status = status;
    this.retryAt = retryAt;
  }
}
