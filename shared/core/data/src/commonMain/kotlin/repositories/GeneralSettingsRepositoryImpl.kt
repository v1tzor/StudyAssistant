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

import database.settings.GeneralSettingsLocalDataSource
import entities.settings.GeneralSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mappers.mapToData
import mappers.mapToDomain

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
class GeneralSettingsRepositoryImpl(
    private val localDataSource: GeneralSettingsLocalDataSource,
) : GeneralSettingsRepository {

    override suspend fun fetchSettings(): Flow<GeneralSettings> {
        return localDataSource.fetchSettings().map { it.mapToDomain() }
    }

    override suspend fun updateSettings(settings: GeneralSettings) {
        localDataSource.updateSettings(settings.mapToData())
    }
}