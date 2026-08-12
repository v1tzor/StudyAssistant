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
import ru.aleshin.studyassistant.core.remote.datasources.installation.InstallationRemoteDataSource
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class InstallationIdProviderImplTest {

    @Test
    fun concurrentRequestsPersistOnlyOneInstallationId() = runBlocking {
        val dataSource = FakeInstallationSecureDataSource()
        val remoteDataSource = FakeInstallationRemoteDataSource()
        val provider = InstallationIdProviderImpl(dataSource, remoteDataSource)

        val ids = coroutineScope {
            List(20) { async { provider.fetchInstallationId() } }.awaitAll()
        }

        assertEquals(1, ids.distinct().size)
        assertEquals(1, dataSource.saveCount)
        assertEquals(1, remoteDataSource.registerCount)
    }

    @Test
    fun invalidStoredTokenIsReplaced() = runBlocking {
        val dataSource = FakeInstallationSecureDataSource(token = "invalid")
        val remoteDataSource = FakeInstallationRemoteDataSource()
        val provider = InstallationIdProviderImpl(dataSource, remoteDataSource)

        val installationId = provider.fetchInstallationId()

        assertEquals(90, installationId.length)
        assertEquals(1, dataSource.saveCount)
        assertEquals(1, remoteDataSource.registerCount)
    }

    @Test
    fun validStoredCredentialIsReused() = runBlocking {
        val storedId = INSTALLATION_CREDENTIAL
        val dataSource = FakeInstallationSecureDataSource(token = storedId)
        val remoteDataSource = FakeInstallationRemoteDataSource()
        val provider = InstallationIdProviderImpl(dataSource, remoteDataSource)

        val installationId = provider.fetchInstallationId()

        assertEquals(storedId, installationId)
        assertEquals(0, dataSource.saveCount)
        assertEquals(0, remoteDataSource.registerCount)
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

    private class FakeInstallationRemoteDataSource(
        private val credential: String = INSTALLATION_CREDENTIAL,
    ) : InstallationRemoteDataSource {

        var registerCount = 0

        override suspend fun register(): String {
            yield()
            registerCount += 1
            return credential
        }
    }

    private companion object {

        val INSTALLATION_CREDENTIAL = "v1.${"A".repeat(43)}.${"B".repeat(43)}"
    }
}
