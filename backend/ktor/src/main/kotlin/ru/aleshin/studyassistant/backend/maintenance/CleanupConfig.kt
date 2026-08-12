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

package ru.aleshin.studyassistant.backend.maintenance

import ru.aleshin.studyassistant.backend.common.config.SecretValueReader
import ru.aleshin.studyassistant.backend.database.DatabaseConfig

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
data class CleanupConfig(
    val database: DatabaseConfig,
) {

    companion object {

        fun fromEnvironment(): CleanupConfig {
            val secretValueReader = SecretValueReader()
            return CleanupConfig(
                database = DatabaseConfig(
                    jdbcUrl = requireEnvironment(name = "DATABASE_URL"),
                    user = requireEnvironment(name = "DATABASE_USER"),
                    password = secretValueReader.readEnvironment("DATABASE_PASSWORD"),
                    maximumPoolSize = 1,
                    connectionTimeoutMs = 5_000,
                ),
            )
        }

        private fun requireEnvironment(name: String): String {
            return requireNotNull(System.getenv(name)) {
                "Environment variable '$name' is required"
            }
        }
    }
}
