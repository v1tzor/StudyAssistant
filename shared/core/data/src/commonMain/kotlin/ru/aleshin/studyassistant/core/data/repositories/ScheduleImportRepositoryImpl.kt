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

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.data.handlers.AiSettingsHandler
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToRemote
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceException
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportDraft
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportRequest
import ru.aleshin.studyassistant.core.domain.managers.InstallationIdProvider
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleImportRepository
import ru.aleshin.studyassistant.core.remote.datasources.ai.ScheduleExtractionRemoteDataSource

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal class ScheduleImportRepositoryImpl(
    private val remoteDataSource: ScheduleExtractionRemoteDataSource,
    private val installationIdProvider: InstallationIdProvider,
    private val settingsHandler: AiSettingsHandler,
) : ScheduleImportRepository {

    override suspend fun extractDraft(request: ScheduleImportRequest): ScheduleImportDraft {
        try {
            return remoteDataSource.extract(
                request = request.mapToRemote(),
                installationToken = installationIdProvider.fetchInstallationId(),
            ).also { response ->
                settingsHandler.updateQuota(
                    remaining = response.quotaRemaining,
                    resetAt = Instant.fromEpochMilliseconds(response.quotaResetAt),
                )
            }.draft.mapToDomain()
        } catch (error: AiServiceException.QuotaExceeded) {
            settingsHandler.updateQuota(
                remaining = 0,
                resetAt = error.resetAtEpochMillis?.let(Instant::fromEpochMilliseconds),
            )
            throw error
        }
    }
}
