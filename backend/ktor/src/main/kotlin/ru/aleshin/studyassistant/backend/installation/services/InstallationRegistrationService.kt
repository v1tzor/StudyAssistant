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

package ru.aleshin.studyassistant.backend.installation.services

import ru.aleshin.studyassistant.backend.common.api.RateLimitException
import ru.aleshin.studyassistant.backend.installation.domain.model.InstallationRegistration
import ru.aleshin.studyassistant.backend.installation.domain.repository.InstallationRegistrationRepository
import ru.aleshin.studyassistant.backend.installation.domain.result.InstallationRegistrationStorageResult
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService
import ru.aleshin.studyassistant.backend.security.NetworkHasher
import java.time.Clock

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class InstallationRegistrationService(
    private val repository: InstallationRegistrationRepository,
    private val credentialService: InstallationCredentialService,
    private val networkHasher: NetworkHasher,
    private val clock: Clock,
) {

    suspend fun register(remoteAddress: String): InstallationRegistration {
        val now = clock.instant()
        return when (
            val result = repository.reserve(
                networkHash = networkHasher.hash(remoteAddress = remoteAddress),
                now = now,
            )
        ) {
            InstallationRegistrationStorageResult.Reserved -> InstallationRegistration(
                credential = credentialService.issue(),
            )

            is InstallationRegistrationStorageResult.RateLimited -> throw RateLimitException(
                retryAt = result.retryAt.toEpochMilli(),
            )
        }
    }
}
