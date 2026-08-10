export function applyMessageQuota(usage, messageKey, limit) {
  if (!usage) {
    return {
      changed: true,
      exceeded: false,
      used: 1,
      messageKeys: [messageKey],
      remaining: limit - 1,
    };
  }
  const messageKeys = usage.message_keys || [];
  if (messageKeys.includes(messageKey)) {
    return {
      changed: false,
      exceeded: false,
      used: usage.used,
      messageKeys,
      remaining: Math.max(0, limit - usage.used),
    };
  }
  if (usage.used >= limit) {
    return {
      changed: false,
      exceeded: true,
      used: usage.used,
      messageKeys,
      remaining: 0,
    };
  }
  return {
    changed: true,
    exceeded: false,
    used: usage.used + 1,
    messageKeys: [...messageKeys, messageKey],
    remaining: limit - usage.used - 1,
  };
}
