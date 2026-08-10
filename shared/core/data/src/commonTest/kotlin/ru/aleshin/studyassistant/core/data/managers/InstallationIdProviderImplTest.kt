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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import ru.aleshin.studyassistant.core.data.datasources.InstallationSecureDataSource
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class InstallationIdProviderImplTest {

    @Test
    fun concurrentRequestsPersistOnlyOneInstallationId() = runBlocking {
        val dataSource = FakeInstallationSecureDataSource()
        val provider = InstallationIdProviderImpl(dataSource)

        val ids = coroutineScope {
            List(20) { async { provider.fetchInstallationId() } }.awaitAll()
        }

        assertEquals(1, ids.distinct().size)
        assertEquals(1, dataSource.saveCount)
    }

    @Test
    fun invalidStoredTokenIsReplaced() = runBlocking {
        val dataSource = FakeInstallationSecureDataSource(token = "invalid")
        val provider = InstallationIdProviderImpl(dataSource)

        val installationId = provider.fetchInstallationId()

        assertEquals(36, installationId.length)
        assertEquals(1, dataSource.saveCount)
    }

    private class FakeInstallationSecureDataSource(
        private var token: String? = null,
    ) : InstallationSecureDataSource {

        var saveCount = 0

        override suspend fun fetchInstallationToken(): String? {
            yield()
            return token
        }

        override suspend fun saveInstallationToken(token: String) {
            yield()
            this.token = token
            saveCount += 1
        }
    }
}
