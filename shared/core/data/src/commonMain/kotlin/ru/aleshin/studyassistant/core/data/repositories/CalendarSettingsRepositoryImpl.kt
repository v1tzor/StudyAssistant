/*
 * Copyright 2024 Stanislav Aleshin
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.payments.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.mappers.settings.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.settings.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.settings.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.settings.CalendarSettingsLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.remote.datasources.settings.CalendarSettingsRemoteDataSource

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
class CalendarSettingsRepositoryImpl(
    private val localDataSource: CalendarSettingsLocalDataSource,
    private val remoteDataSource: CalendarSettingsRemoteDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : CalendarSettingsRepository {

    override suspend fun fetchSettings(targetUser: UID): Flow<CalendarSettings> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchSettings(targetUser).map { settingsPojo -> settingsPojo.mapToDomain() }
        } else {
            localDataSource.fetchSettings().map { settingsEntity -> settingsEntity.mapToDomain() }
        }
    }

    override suspend fun updateSettings(settings: CalendarSettings, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateSettings(settings.mapToRemoteData(), targetUser)
        } else {
            localDataSource.updateSettings(settings.mapToLocalData())
        }
    }

    override suspend fun transferData(direction: DataTransferDirection, targetUser: UID) {
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allSettings = remoteDataSource.fetchSettings(
                    targetUser = targetUser,
                ).let { settingsFlow ->
                    return@let settingsFlow.first().mapToDomain().mapToLocalData()
                }
                localDataSource.deleteSettings()
                localDataSource.updateSettings(allSettings)
                remoteDataSource.deleteSettings(targetUser)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allSettings = localDataSource.fetchSettings().let { settingsFlow ->
                    return@let settingsFlow.first().mapToDomain().mapToRemoteData()
                }
                remoteDataSource.deleteSettings(targetUser)
                remoteDataSource.addOrUpdateSettings(allSettings, targetUser)
            }
        }
    }
}