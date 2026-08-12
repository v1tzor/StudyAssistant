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

package ru.aleshin.studyassistant.backend.security

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class InstallationHasher(
    secret: ByteArray,
) {

    private val hasher = HmacHasher(
        secret = secret,
        domain = DOMAIN,
    )

    fun hash(installationToken: String): ByteArray {
        require(installationToken.isNotBlank()) {
            "Installation token must not be blank"
        }

        return hasher.hash(value = installationToken)
    }

    private companion object {
        const val DOMAIN = "studyassistant:installation:v1"
    }
}