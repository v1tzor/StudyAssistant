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

package ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek

import io.ktor.server.config.ApplicationConfig
import ru.aleshin.studyassistant.backend.common.config.SecretValueReader

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
data class DeepSeekConfig(
    val apiKey: String,
    val baseUrl: String,
    val maxTokens: Int,
    val maxResponseBodyBytes: Long,
    val totalTimeoutMs: Long,
    val requestTimeoutMs: Long,
    val connectTimeoutMs: Long,
    val socketTimeoutMs: Long,
    val maxRetries: Int,
    val baseRetryDelayMs: Long,
    val maxRetryDelayMs: Long,
    val retryJitterMs: Long,
    val maxConcurrentRequests: Int = 16,
    val queueTimeoutMs: Long = 3_000L,
) {

    init {
        require(apiKey.isNotBlank())
        require(baseUrl.startsWith(HTTPS_PREFIX) && !baseUrl.contains('?') && !baseUrl.contains('#'))
        require(maxTokens > 0)
        require(maxResponseBodyBytes > 0)
        require(totalTimeoutMs > 0)
        require(requestTimeoutMs > 0)
        require(connectTimeoutMs > 0)
        require(socketTimeoutMs > 0)
        require(requestTimeoutMs <= totalTimeoutMs)
        require(maxRetries in 0..MAX_RETRIES)
        require(baseRetryDelayMs > 0)
        require(maxRetryDelayMs >= baseRetryDelayMs)
        require(retryJitterMs >= 0)
        require(retryJitterMs <= maxRetryDelayMs)
        require(maxConcurrentRequests > 0)
        require(queueTimeoutMs > 0)
        require(queueTimeoutMs < totalTimeoutMs)
    }

    companion object {

        private const val HTTPS_PREFIX = "https://"
        private const val MAX_RETRIES = 5

        const val MODEL = "deepseek-v4-flash"

        fun from(
            applicationConfig: ApplicationConfig,
            secretValueReader: SecretValueReader = SecretValueReader(),
        ): DeepSeekConfig {
            val config = applicationConfig.config("ai.deepseek")

            return DeepSeekConfig(
                apiKey = secretValueReader.read(
                    config = config,
                    propertyName = "apiKey",
                    environmentName = "DEEPSEEK_API_KEY",
                ),
                baseUrl = config.property("baseUrl").getString().trimEnd('/'),
                maxTokens = config.property("maxTokens").getString().toInt(),
                maxResponseBodyBytes = config.property("maxResponseBodyBytes").getString().toLong(),
                totalTimeoutMs = config.property("totalTimeoutMs").getString().toLong(),
                requestTimeoutMs = config.property("requestTimeoutMs").getString().toLong(),
                connectTimeoutMs = config.property("connectTimeoutMs").getString().toLong(),
                socketTimeoutMs = config.property("socketTimeoutMs").getString().toLong(),
                maxRetries = config.property("maxRetries").getString().toInt(),
                baseRetryDelayMs = config.property("baseRetryDelayMs").getString().toLong(),
                maxRetryDelayMs = config.property("maxRetryDelayMs").getString().toLong(),
                retryJitterMs = config.property("retryJitterMs").getString().toLong(),
                maxConcurrentRequests = config.property("maxConcurrentRequests").getString().toInt(),
                queueTimeoutMs = config.property("queueTimeoutMs").getString().toLong(),
            )
        }
    }
}
