/*
 * Copyright 2026 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.aleshin.studyassistant.backend.ai

import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.DeepSeekConfig
import ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.openrouter.OpenRouterConfig
import java.time.Duration

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
fun testAiConfig(): AiConfig {
    return AiConfig(
        dailyMessageLimit = 10,
        rewardedMessageAmount = 10,
        maxRewardedResetsPerDay = 2,
        rewardChallengeLifetime = Duration.ofMinutes(15),
        globalDailyExecutionLimit = 2_000,
        executionLimit = 30,
        maxExecutionsPerMessage = 8,
        maxConcurrentExecutionsPerInstallation = 2,
        executionWindow = Duration.ofHours(1),
        reservationTimeout = Duration.ofMinutes(5),
        maxRequestBodyBytes = 262_144,
        maxMessages = 80,
        maxMessageCharacters = 32_768,
        maxTotalContentCharacters = 131_072,
        maxTools = 32,
        maxToolDescriptionCharacters = 2_048,
        maxToolSchemaCharacters = 16_384,
        maxToolCallsPerMessage = 16,
        maxToolArgumentsCharacters = 16_384,
        maxScheduleRequestBodyBytes = 1_048_576,
        maxScheduleImageBytes = 786_432,
        maxScheduleNoteCharacters = 120,
        maxScheduleEntries = 300,
        maxScheduleFieldCharacters = 512,
        maxScheduleUnparsedLines = 100,
    )
}

fun testDeepSeekConfig(
    maxRetries: Int = 0,
    baseRetryDelayMs: Long = 1,
    maxResponseBodyBytes: Long = 524_288,
    maxConcurrentRequests: Int = 16,
): DeepSeekConfig {
    return DeepSeekConfig(
        apiKey = "test-key",
        baseUrl = "https://api.deepseek.test",
        maxTokens = 4_096,
        maxResponseBodyBytes = maxResponseBodyBytes,
        totalTimeoutMs = 5_000,
        requestTimeoutMs = 2_000,
        connectTimeoutMs = 1_000,
        socketTimeoutMs = 2_000,
        maxRetries = maxRetries,
        baseRetryDelayMs = baseRetryDelayMs,
        maxRetryDelayMs = baseRetryDelayMs,
        retryJitterMs = 0,
        maxConcurrentRequests = maxConcurrentRequests,
        queueTimeoutMs = 100,
    )
}

fun testOpenRouterConfig(
    maxRetries: Int = 0,
    baseRetryDelayMs: Long = 1,
    maxResponseBodyBytes: Long = 524_288,
    maxConcurrentRequests: Int = 8,
): OpenRouterConfig {
    return OpenRouterConfig(
        apiKey = "test-key",
        baseUrl = "https://openrouter.test",
        maxTokens = 4_096,
        maxResponseBodyBytes = maxResponseBodyBytes,
        totalTimeoutMs = 5_000,
        requestTimeoutMs = 2_000,
        connectTimeoutMs = 1_000,
        socketTimeoutMs = 2_000,
        maxRetries = maxRetries,
        baseRetryDelayMs = baseRetryDelayMs,
        maxRetryDelayMs = baseRetryDelayMs,
        retryJitterMs = 0,
        maxConcurrentRequests = maxConcurrentRequests,
        queueTimeoutMs = 100,
    )
}
