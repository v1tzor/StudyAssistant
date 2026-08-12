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

package ru.aleshin.studyassistant.backend.database

import io.ktor.server.config.ApplicationConfig
import ru.aleshin.studyassistant.backend.common.config.SecretValueReader

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
data class DatabaseConfig(
    val jdbcUrl: String,
    val user: String,
    val password: String,
    val maximumPoolSize: Int,
    val connectionTimeoutMs: Long,
    val statementTimeoutMs: Long = 125_000L,
    val idleTransactionTimeoutMs: Long = 30_000L,
    val maxLifetimeMs: Long = 1_800_000L,
) {

    init {
        require(jdbcUrl.startsWith("jdbc:postgresql://"))
        require(user.isNotBlank())
        require(password.isNotBlank())
        require(maximumPoolSize > 0)
        require(connectionTimeoutMs > 0)
        require(statementTimeoutMs > 0)
        require(idleTransactionTimeoutMs > 0)
        require(maxLifetimeMs >= 30_000L)
    }

    companion object {

        fun from(
            applicationConfig: ApplicationConfig,
            secretValueReader: SecretValueReader = SecretValueReader(),
        ): DatabaseConfig {
            val config = applicationConfig.config("database")

            return DatabaseConfig(
                jdbcUrl = config.property("jdbcUrl").getString(),
                user = config.property("user").getString(),
                password = secretValueReader.read(
                    config = config,
                    propertyName = "password",
                    environmentName = "DATABASE_PASSWORD",
                ),
                maximumPoolSize = config.property("maximumPoolSize").getString().toInt(),
                connectionTimeoutMs = config.property("connectionTimeoutMs").getString().toLong(),
                statementTimeoutMs = config.property("statementTimeoutMs").getString().toLong(),
                idleTransactionTimeoutMs = config
                    .property("idleTransactionTimeoutMs")
                    .getString()
                    .toLong(),
                maxLifetimeMs = config.property("maxLifetimeMs").getString().toLong(),
            )
        }
    }
}
