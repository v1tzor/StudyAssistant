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

package ru.aleshin.studyassistant.backend.common.config

import io.ktor.server.config.ApplicationConfig
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class SecretValueReader(
    private val environment: (String) -> String? = System::getenv,
    private val fileReader: (String) -> String = ::readSecretFile,
) {

    fun read(
        config: ApplicationConfig,
        propertyName: String,
        environmentName: String,
    ): String {
        config.propertyOrNull(propertyName)?.getString()?.takeIf(String::isNotBlank)?.let {
            return it
        }
        return readEnvironment(environmentName)
    }

    fun readEnvironment(environmentName: String): String {
        environment(environmentName)?.takeIf(String::isNotBlank)?.let { return it }
        val fileEnvironmentName = environmentName + FILE_ENVIRONMENT_SUFFIX
        val filePath = environment(fileEnvironmentName)?.takeIf(String::isNotBlank)
            ?: error("Missing required secret: $environmentName or $fileEnvironmentName")
        return fileReader(filePath).trimEnd('\r', '\n').also { secret ->
            require(secret.isNotBlank()) { "Secret file for $environmentName is empty" }
        }
    }

    private companion object {
        const val FILE_ENVIRONMENT_SUFFIX = "_FILE"

        fun readSecretFile(value: String): String {
            val path = Path.of(value)
            require(path.isAbsolute) { "Secret file path must be absolute" }
            require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                "Secret file must be a regular file"
            }
            val size = Files.size(path)
            require(size in 1..MAX_SECRET_FILE_BYTES) { "Secret file has an invalid size" }
            return Files.readString(path, StandardCharsets.UTF_8)
        }

        const val MAX_SECRET_FILE_BYTES = 65_536L
    }
}
