export const config = {
  databaseId: process.env.DATABASE_ID || '686052b2001b25f5a09f',
  bucketId: process.env.BUCKET_ID || '68640fd3001e968f42d1',
  scheduleTableId: process.env.SCHEDULE_TABLE_ID || 'schedule_shares',
  homeworkTableId: process.env.HOMEWORK_TABLE_ID || 'homework_shares',
  abuseLimitsTableId: process.env.ABUSE_LIMITS_TABLE_ID || 'abuse_limits',
  aiUsageTableId: process.env.AI_USAGE_TABLE_ID || 'ai_usage',
};

export const limits = {
  scheduleLifetimeMs: 30 * 60 * 1000,
  scheduleClaimLifetimeMs: 5 * 60 * 1000,
  homeworkLifetimeMs: 24 * 60 * 60 * 1000,
  maxPayloadBytes: 1024 * 1024,
  maxShareItems: 20,
  shareCreatesPerHour: 10,
  shareItemsPerDay: 200,
  maxActiveHomeworkItems: 200,
  shareCodeAttempts: 30,
  shareCodeAttemptWindowMs: 10 * 60 * 1000,
  aiCallsPerHour: 30,
  sharedDailyMessages: 25,
};
