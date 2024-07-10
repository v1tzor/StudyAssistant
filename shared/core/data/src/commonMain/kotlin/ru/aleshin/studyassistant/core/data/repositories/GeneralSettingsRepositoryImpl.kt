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
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.data.mappers.settings.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.settings.mapToLocalData
import ru.aleshin.studyassistant.core.database.datasource.settings.GeneralSettingsLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.settings.GeneralSettings
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
class GeneralSettingsRepositoryImpl(
    private val localDataSource: GeneralSettingsLocalDataSource,
) : GeneralSettingsRepository {

    override suspend fun fetchSettings(): Flow<GeneralSettings> {
        return localDataSource.fetchSettings().map { settingsEntity -> settingsEntity.mapToDomain() }
    }

    override suspend fun updateSettings(settings: GeneralSettings) {
        localDataSource.updateSettings(settings.mapToLocalData())
    }
}