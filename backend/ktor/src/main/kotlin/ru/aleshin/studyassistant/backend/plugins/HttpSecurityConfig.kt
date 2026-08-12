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

package ru.aleshin.studyassistant.backend.plugins

import io.ktor.server.config.ApplicationConfig

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
data class HttpSecurityConfig(
    val expectedHost: String?,
    val trustProxyHeaders: Boolean,
    val requireHttps: Boolean,
) {

    init {
        require(expectedHost == null || HOST_PATTERN.matches(expectedHost))
    }

    companion object {

        fun from(applicationConfig: ApplicationConfig): HttpSecurityConfig {
            val config = applicationConfig.config("httpSecurity")
            return HttpSecurityConfig(
                expectedHost = config.propertyOrNull("expectedHost")
                    ?.getString()
                    ?.trim()
                    ?.lowercase()
                    ?.takeIf(String::isNotEmpty),
                trustProxyHeaders = config.boolean("trustProxyHeaders"),
                requireHttps = config.boolean("requireHttps"),
            )
        }

        private fun ApplicationConfig.boolean(name: String): Boolean {
            val value = propertyOrNull(name)?.getString()?.trim()?.lowercase() ?: return false
            return value.toBooleanStrictOrNull()
                ?: throw IllegalArgumentException("$name must be true or false")
        }

        private val HOST_PATTERN = Regex(
            "^(?=.{1,253}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9]{2,63}$",
        )
    }
}
