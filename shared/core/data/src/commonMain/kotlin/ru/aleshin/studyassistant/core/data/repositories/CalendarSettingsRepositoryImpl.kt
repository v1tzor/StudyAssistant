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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.data.mappers.settings.convertToLocal
import ru.aleshin.studyassistant.core.data.mappers.settings.convertToRemote
import ru.aleshin.studyassistant.core.data.mappers.settings.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.settings.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.settings.mapToRemoteData
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.settings.CalendarSettingsLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.managers.sync.CalendarSettingsSourceSyncManager.Companion.CALENDAR_SETTINGS_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.remote.datasources.settings.CalendarSettingsRemoteDataSource

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
class CalendarSettingsRepositoryImpl(
    private val localDataSource: CalendarSettingsLocalDataSource,
    private val remoteDataSource: CalendarSettingsRemoteDataSource,
    private val userSessionProvider: UserSessionProvider,
    private val subscriptionChecker: SubscriptionChecker,
    private val resultSyncHandler: RemoteResultSyncHandler,
) : CalendarSettingsRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun fetchSettings(): Flow<CalendarSettings> {
        return subscriptionChecker.getSubscriptionActiveFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchItem().mapNotNull { settingsEntity ->
                    settingsEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchItem().mapNotNull { settingsEntity ->
                    settingsEntity?.mapToDomain()
                }
            }
        }
    }

    override suspend fun updateSettings(settings: CalendarSettings) {
        val isSubscriber = subscriptionChecker.getSubscriptionActive()
        val currentUser = userSessionProvider.getCurrentUserId()

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItem(settings.mapToLocalData(currentUser))
            resultSyncHandler.executeOrAddToQueue(
                data = settings.mapToRemoteData(currentUser),
                type = OfflineChangeType.UPSERT,
                sourceKey = CALENDAR_SETTINGS_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItem(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItem(settings.mapToLocalData(currentUser))
        }
    }

    override suspend fun transferData(direction: DataTransferDirection) {
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allSettings = remoteDataSource.fetchItem().first()?.convertToLocal() ?: return
                localDataSource.offline().deleteItem()
                localDataSource.offline().addOrUpdateItem(allSettings)
            }

            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allSettings = localDataSource.offline().fetchItem().first()
                val settingsRemote = allSettings?.convertToRemote() ?: return

                remoteDataSource.deleteItem()
                remoteDataSource.addOrUpdateItem(settingsRemote)

                localDataSource.sync().deleteItem()
                localDataSource.sync().addOrUpdateItem(allSettings)
            }
        }
    }
}