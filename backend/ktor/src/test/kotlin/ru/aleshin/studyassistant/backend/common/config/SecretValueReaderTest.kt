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

import io.ktor.server.config.MapApplicationConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SecretValueReaderTest {

    @Test
    fun configValueHasPriority() {
        val reader = SecretValueReader(
            environment = { "environment-secret" },
            fileReader = { "file-secret" },
        )

        val result = reader.read(
            config = MapApplicationConfig("secret" to "config-secret"),
            propertyName = "secret",
            environmentName = "TEST_SECRET",
        )

        assertEquals("config-secret", result)
    }

    @Test
    fun fileSecretSupportsTrailingNewline() {
        val environment = mapOf("TEST_SECRET_FILE" to "/run/secrets/test")
        val reader = SecretValueReader(
            environment = environment::get,
            fileReader = { path ->
                assertEquals("/run/secrets/test", path)
                "file-secret\n"
            },
        )

        assertEquals("file-secret", reader.readEnvironment("TEST_SECRET"))
    }

    @Test
    fun missingSecretFailsClosed() {
        val reader = SecretValueReader(
            environment = { null },
            fileReader = { error("Must not be called") },
        )

        assertFailsWith<IllegalStateException> {
            reader.readEnvironment("TEST_SECRET")
        }
    }
}
