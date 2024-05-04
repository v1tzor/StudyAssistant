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

package repositories

import database.settings.CalendarSettingsLocalDataSource
import entities.settings.CalendarSettings
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mappers.organizations.mapToData
import mappers.organizations.mapToDomain
import mappers.settings.mapToData
import mappers.settings.mapToDomain
import payments.SubscriptionChecker
import remote.settings.CalendarSettingsRemoteDataSource

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
        val settingsFlow = if (isSubscriber) {
            remoteDataSource.fetchSettings(targetUser)
        } else {
            localDataSource.fetchSettings()
        }
        return settingsFlow.map { settingsDetails ->
            settingsDetails.mapToDomain()
        }
    }

    override suspend fun updateSettings(settings: CalendarSettings, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        return if (isSubscriber) {
            remoteDataSource.addOrUpdateSettings(settings.mapToData(), targetUser)
        } else {
            localDataSource.updateSettings(settings.mapToData())
        }
    }
}