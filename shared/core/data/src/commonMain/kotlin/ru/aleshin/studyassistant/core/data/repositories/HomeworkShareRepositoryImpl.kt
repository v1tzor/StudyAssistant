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

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.mappers.share.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.share.mapToRemoteData
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToLocalData
import ru.aleshin.studyassistant.core.data.utils.share.ShareCode
import ru.aleshin.studyassistant.core.data.utils.withValidInstallation
import ru.aleshin.studyassistant.core.database.datasource.shared.HomeworkShareLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.share.HomeworkShare
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.domain.entities.share.ShareLink
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.managers.InstallationIdProvider
import ru.aleshin.studyassistant.core.domain.repositories.HomeworkShareRepository
import ru.aleshin.studyassistant.core.remote.datasources.share.HomeworkShareRemoteDataSource

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal class HomeworkShareRepositoryImpl(
    private val localDataSource: HomeworkShareLocalDataSource,
    private val remoteDataSource: HomeworkShareRemoteDataSource,
    private val installationIdProvider: InstallationIdProvider,
    private val dateManager: DateManager,
) : HomeworkShareRepository {

    override suspend fun createShare(share: HomeworkShare): ShareLink {
        val link = installationIdProvider.withValidInstallation { token ->
            remoteDataSource.createShare(
                share = share.mapToRemoteData(),
                installationToken = token,
            )
        }
        val code = ShareCode.format(normalizeCode(link.code))
        return ShareLink(
            code = code,
            deepLink = "studyassistant://share/homework?code=$code",
            createdAt = link.createdAt.mapEpochTimeToInstant(),
            expiresAt = link.expiresAt.mapEpochTimeToInstant(),
        )
    }

    override suspend fun fetchShare(code: String): HomeworkShare {
        return installationIdProvider.withValidInstallation { token ->
            remoteDataSource.fetchShare(
                code = normalizeCode(code),
                installationToken = token,
            )
        }.mapToDomain()
    }

    override suspend fun isShareImported(code: String): Boolean {
        return localDataSource.contains(normalizeCode(code))
    }

    override suspend fun importShare(code: String, homeworks: List<Homework>) {
        val imported = localDataSource.importHomeworks(
            shareCode = normalizeCode(code),
            importedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
            homeworks = homeworks.map { homework -> homework.mapToLocalData() },
        )
        if (!imported) throw ShareException.Duplicate()
    }

    private fun normalizeCode(code: String): String = try {
        ShareCode.normalize(code)
    } catch (_: IllegalArgumentException) {
        throw ShareException.InvalidCode()
    }
}
