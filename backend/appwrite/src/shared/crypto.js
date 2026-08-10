import { createHmac, randomBytes, timingSafeEqual } from 'node:crypto';

const SHARE_CODE_ALPHABET = '0123456789ABCDEFGHJKMNPQRSTVWXYZ';
const SHARE_CODE_LENGTH = 12;

export function hmacSha256(value, secret, context) {
  requireSecret(secret);
  return createHmac('sha256', secret)
    .update(`${context}\u0000${value}`)
    .digest('hex');
}

export function verifyHmacSha256(value, signature, secret, context) {
  const expected = Buffer.from(hmacSha256(value, secret, context), 'hex');
  const actual = Buffer.from(String(signature || ''), 'hex');
  return actual.length === expected.length && timingSafeEqual(actual, expected);
}

export function randomToken(bytes = 24) {
  return randomBytes(bytes).toString('base64url');
}

export function generateShareCode() {
  const bytes = randomBytes(8);
  let value = BigInt(`0x${bytes.toString('hex')}`);
  let code = '';
  for (let index = 0; index < SHARE_CODE_LENGTH; index += 1) {
    code += SHARE_CODE_ALPHABET[Number(value & 31n)];
    value >>= 5n;
  }
  return code;
}

export function normalizeShareCode(value) {
  const code = String(value || '').replace(/[\s-]/g, '').toUpperCase();
  if (code.length !== SHARE_CODE_LENGTH || [...code].some((item) => !SHARE_CODE_ALPHABET.includes(item))) {
    throw new Error('invalid_share_code');
  }
  return code;
}

function requireSecret(secret) {
  if (!secret || secret.length < 32) throw new Error('missing_server_secret');
}
