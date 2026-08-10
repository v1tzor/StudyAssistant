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
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.share.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.share.mapToRemoteData
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.utils.share.ShareCode
import ru.aleshin.studyassistant.core.database.datasource.shared.ScheduleShareLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShare
import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShareClaim
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.domain.entities.share.ShareLink
import ru.aleshin.studyassistant.core.domain.managers.InstallationIdProvider
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleShareRepository
import ru.aleshin.studyassistant.core.remote.datasources.share.ScheduleShareRemoteDataSource

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal class ScheduleShareRepositoryImpl(
    private val localDataSource: ScheduleShareLocalDataSource,
    private val remoteDataSource: ScheduleShareRemoteDataSource,
    private val installationIdProvider: InstallationIdProvider,
) : ScheduleShareRepository {

    override suspend fun createShare(share: ScheduleShare): ShareLink {
        val link = remoteDataSource.createShare(
            share = share.mapToRemoteData(),
            installationToken = installationIdProvider.fetchInstallationId(),
        )
        val code = ShareCode.format(normalizeCode(link.code))
        return ShareLink(
            code = code,
            deepLink = "studyassistant://share/schedule?code=$code",
            createdAt = link.createdAt.mapEpochTimeToInstant(),
            expiresAt = link.expiresAt.mapEpochTimeToInstant(),
        )
    }

    override suspend fun claimShare(code: String): ScheduleShareClaim {
        val claim = remoteDataSource.claimShare(
            code = normalizeCode(code),
            installationToken = installationIdProvider.fetchInstallationId(),
        )
        return ScheduleShareClaim(
            claimId = claim.claimToken,
            share = claim.share.mapToDomain(),
        )
    }

    override suspend fun confirmShare(claim: ScheduleShareClaim) {
        remoteDataSource.confirmShare(claim.claimId)
    }

    override suspend fun releaseShare(claim: ScheduleShareClaim) {
        remoteDataSource.releaseShare(claim.claimId)
    }

    override suspend fun importShare(
        organizations: List<Organization>,
        schedules: List<BaseSchedule>,
    ) {
        localDataSource.importSchedule(
            organizations = organizations.map { organization -> organization.mapToLocalData() },
            employees = organizations.flatMap { organization -> organization.employee }
                .map { employee -> employee.mapToLocalData() },
            subjects = organizations.flatMap { organization -> organization.subjects }
                .map { subject -> subject.mapToLocalData() },
            schedules = schedules.map { schedule -> schedule.mapToLocalData() },
        )
    }

    private fun normalizeCode(code: String): String = try {
        ShareCode.normalize(code)
    } catch (_: IllegalArgumentException) {
        throw ShareException.InvalidCode()
    }
}
