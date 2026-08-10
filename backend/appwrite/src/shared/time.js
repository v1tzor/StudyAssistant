export function utcDay(now = new Date()) {
  return now.toISOString().slice(0, 10);
}

export function nextUtcDayMillis(now = new Date()) {
  return Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate() + 1);
}
