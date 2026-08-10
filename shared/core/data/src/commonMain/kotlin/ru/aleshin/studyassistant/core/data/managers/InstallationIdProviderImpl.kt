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

package ru.aleshin.studyassistant.core.data.managers

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.data.datasources.InstallationSecureDataSource
import ru.aleshin.studyassistant.core.domain.managers.InstallationIdProvider

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class InstallationIdProviderImpl(
    private val secureDataSource: InstallationSecureDataSource,
) : InstallationIdProvider {

    private val mutex = Mutex()

    override suspend fun fetchInstallationId(): String = mutex.withLock {
        secureDataSource.fetchInstallationToken()
            ?.takeIf { token -> token.length in MIN_TOKEN_LENGTH..MAX_TOKEN_LENGTH }
            ?.let { token -> return@withLock token }
        randomUUID().also { token -> secureDataSource.saveInstallationToken(token) }
    }

    private companion object {
        const val MIN_TOKEN_LENGTH = 16
        const val MAX_TOKEN_LENGTH = 256
    }
}
