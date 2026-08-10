import { PublicError } from '../shared/http.js';

const DEEPSEEK_URL = 'https://api.deepseek.com/chat/completions';
const MAX_ATTEMPTS = 3;
const REQUEST_DEADLINE_MS = 25 * 1000;
const MAX_RESPONSE_BYTES = 1024 * 1024;

export async function completeChat(requestBody) {
  const key = process.env.DEEPSEEK_API_KEY;
  if (!key) throw new PublicError('server_unavailable', 503);
  const deadline = Date.now() + REQUEST_DEADLINE_MS;
  for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt += 1) {
    const remaining = deadline - Date.now();
    if (remaining <= 0) throw new PublicError('server_unavailable', 503);
    try {
      const response = await fetch(DEEPSEEK_URL, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${key}`,
          'Content-Type': 'application/json',
        },
        body: requestBody,
        signal: AbortSignal.timeout(remaining),
      });
      if (response.ok) return await responseJson(response);
      const retryable = response.status === 429 || response.status >= 500;
      if (!retryable || attempt === MAX_ATTEMPTS - 1) {
        throw responseError(response.status);
      }
      await wait(retryDelay(response.headers.get('retry-after'), attempt, deadline));
    } catch (error) {
      if (error instanceof PublicError || attempt === MAX_ATTEMPTS - 1) {
        throw error instanceof PublicError ? error : new PublicError('server_unavailable', 503);
      }
      await wait(retryDelay(null, attempt, deadline));
    }
  }
  throw new PublicError('server_unavailable', 503);
}

async function responseJson(response) {
  const text = await response.text();
  if (Buffer.byteLength(text, 'utf8') > MAX_RESPONSE_BYTES) {
    throw new PublicError('server_unavailable', 503);
  }
  try {
    return JSON.parse(text);
  } catch {
    throw new PublicError('server_unavailable', 503);
  }
}

function responseError(status) {
  if (status === 429) return new PublicError('rate_limit', 429);
  return new PublicError('server_unavailable', 503);
}

function retryDelay(retryAfter, attempt, deadline) {
  const seconds = Number(retryAfter);
  const delay = Number.isFinite(seconds) && seconds > 0
    ? seconds * 1000
    : (500 * (2 ** attempt)) + Math.floor(Math.random() * 300);
  return Math.max(0, Math.min(delay, deadline - Date.now()));
}

function wait(milliseconds) {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}
