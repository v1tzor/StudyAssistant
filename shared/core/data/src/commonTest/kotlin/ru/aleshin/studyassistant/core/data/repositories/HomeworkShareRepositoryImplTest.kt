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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.database.datasource.shared.HomeworkShareLocalDataSource
import ru.aleshin.studyassistant.core.database.models.tasks.BaseHomeworkEntity
import ru.aleshin.studyassistant.core.domain.entities.share.HomeworkShare
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.domain.managers.InstallationIdProvider
import ru.aleshin.studyassistant.core.remote.datasources.share.HomeworkShareRemoteDataSource
import ru.aleshin.studyassistant.core.remote.models.shared.HomeworkSharePayloadPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ShareLinkResponsePojo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class HomeworkShareRepositoryImplTest {

    @Test
    fun createAndFetchUseNormalizedCodeAndInstallationId() = runBlocking {
        val remoteDataSource = FakeRemoteDataSource()
        val repository = createRepository(remoteDataSource)
        val share = HomeworkShare(
            senderName = "Sender",
            date = Instant.fromEpochMilliseconds(1_000L),
            homeworks = emptyList(),
        )

        val link = repository.createShare(share)
        val fetched = repository.fetchShare("0123-4567-89ab")

        assertEquals("0123-4567-89AB", link.code)
        assertEquals("0123456789AB", remoteDataSource.fetchedCode)
        assertEquals(INSTALLATION_ID, remoteDataSource.installationToken)
        assertEquals("Remote sender", fetched.senderName)
    }

    @Test
    fun secondImportOfTheSameShareIsRejected() = runBlocking {
        val localDataSource = FakeLocalDataSource()
        val repository = createRepository(
            remoteDataSource = FakeRemoteDataSource(),
            localDataSource = localDataSource,
        )

        repository.importShare("0123-4567-89ab", emptyList())

        assertFailsWith<ShareException.Duplicate> {
            repository.importShare("0123456789AB", emptyList())
        }
        assertEquals(listOf("0123456789AB", "0123456789AB"), localDataSource.codes)
    }

    @Test
    fun malformedCodeIsRejectedBeforeRemoteCall() = runBlocking {
        val remoteDataSource = FakeRemoteDataSource()
        val repository = createRepository(remoteDataSource)

        assertFailsWith<ShareException.InvalidCode> {
            repository.fetchShare("bad")
        }
        assertTrue(remoteDataSource.fetchedCode == null)
    }

    private fun createRepository(
        remoteDataSource: FakeRemoteDataSource,
        localDataSource: HomeworkShareLocalDataSource = FakeLocalDataSource(),
    ) = HomeworkShareRepositoryImpl(
        localDataSource = localDataSource,
        remoteDataSource = remoteDataSource,
        installationIdProvider = object : InstallationIdProvider {
            override suspend fun fetchInstallationId() = INSTALLATION_ID
        },
        dateManager = FakeDateManager,
    )

    private class FakeRemoteDataSource : HomeworkShareRemoteDataSource {

        var fetchedCode: String? = null
        var installationToken: String? = null

        override suspend fun createShare(
            share: HomeworkSharePayloadPojo,
            installationToken: String,
        ): ShareLinkResponsePojo {
            this.installationToken = installationToken
            return ShareLinkResponsePojo(
                code = "0123456789AB",
                createdAt = 1_000L,
                expiresAt = 2_000L,
            )
        }

        override suspend fun fetchShare(
            code: String,
            installationToken: String,
        ): HomeworkSharePayloadPojo {
            fetchedCode = code
            this.installationToken = installationToken
            return HomeworkSharePayloadPojo(
                senderName = "Remote sender",
                date = 1_000L,
                homeworks = emptyList(),
            )
        }
    }

    private class FakeLocalDataSource : HomeworkShareLocalDataSource {

        val codes = mutableListOf<String>()

        override suspend fun contains(shareCode: String) = shareCode in codes

        override suspend fun importHomeworks(
            shareCode: String,
            importedAt: Long,
            homeworks: List<BaseHomeworkEntity>,
        ): Boolean {
            codes += shareCode
            return codes.size == 1
        }
    }

    private object FakeDateManager : DateManager {
        override fun fetchCurrentInstant() = Instant.fromEpochMilliseconds(5_000L)
        override fun fetchCurrentWeek(): TimeRange = error("Unused")
        override fun fetchBeginningCurrentInstant(): Instant = error("Unused")
        override fun fetchEndCurrentInstant(): Instant = error("Unused")
        override fun isCurrentDay(date: Instant): Boolean = error("Unused")
        override fun calculateLeftDateTime(endDateTime: Instant): Long = error("Unused")
        override fun calculateLeftTime(endTime: LocalTime): Long = error("Unused")
        override fun calculateProgress(startTime: Instant, endTime: Instant): Float = error("Unused")
        override fun secondTicker(): Flow<Unit> = emptyFlow()
        override fun minuteTicker(): Flow<Instant> = emptyFlow()
    }

    private companion object {
        const val INSTALLATION_ID = "installation-token"
    }
}
