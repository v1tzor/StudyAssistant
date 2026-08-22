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

package ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.openrouter

import io.ktor.server.config.ApplicationConfig
import ru.aleshin.studyassistant.backend.common.config.SecretValueReader

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
data class OpenRouterConfig(
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
    val maxConcurrentRequests: Int = 12,
    val queueTimeoutMs: Long = 3_000L,
    val socksHost: String = DEFAULT_SOCKS_HOST,
    val socksPort: Int = DEFAULT_SOCKS_PORT,
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
        require(socksHost.isNotBlank())
        require(socksPort in 1..MAX_PORT)
    }

    companion object {

        private const val HTTPS_PREFIX = "https://"
        private const val MAX_RETRIES = 5
        private const val MAX_PORT = 65_535
        const val DEFAULT_SOCKS_HOST = "127.0.0.1"
        const val DEFAULT_SOCKS_PORT = 10_808

        const val MODEL = "meta/muse-spark-1.2-contributor"

        fun from(
            applicationConfig: ApplicationConfig,
            secretValueReader: SecretValueReader = SecretValueReader(),
        ): OpenRouterConfig {
            val config = applicationConfig.config("ai.openrouter")

            return OpenRouterConfig(
                apiKey = secretValueReader.read(
                    config = config,
                    propertyName = "apiKey",
                    environmentName = "OPENROUTER_API_KEY",
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
                socksHost = config.propertyOrNull("socksHost")?.getString()?.trim()?.takeIf(String::isNotEmpty) ?: DEFAULT_SOCKS_HOST,
                socksPort = config.propertyOrNull("socksPort")?.getString()?.toInt() ?: DEFAULT_SOCKS_PORT,
            )
        }
    }
}
