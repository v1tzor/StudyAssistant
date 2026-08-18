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

package ru.aleshin.studyassistant.backend.common.api

import io.ktor.server.application.ApplicationCall
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
fun ApplicationCall.requireInstallationToken(
    credentialService: InstallationCredentialService,
): String {
    val token = request.headers[INSTALLATION_TOKEN_HEADER]?.trim()

    if (token == null || !credentialService.isValid(token)) {
        throw InvalidInstallationException()
    }

    return token
}

const val INSTALLATION_TOKEN_HEADER = "X-Installation-Token"
