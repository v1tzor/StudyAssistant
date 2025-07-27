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

package ru.aleshin.studyassistant.core.data.managers.sync

import dev.tmapps.konnection.Konnection
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.data.mappers.settings.CalendarSettingsSyncMapper
import ru.aleshin.studyassistant.core.data.utils.sync.BaseSourceSyncManager
import ru.aleshin.studyassistant.core.database.datasource.settings.CalendarSettingsLocalDataSource
import ru.aleshin.studyassistant.core.database.models.settings.BaseCalendarSettingsEntity
import ru.aleshin.studyassistant.core.database.storages.ChangeQueueStorage
import ru.aleshin.studyassistant.core.domain.managers.sync.CalendarSettingsSourceSyncManager
import ru.aleshin.studyassistant.core.remote.datasources.settings.CalendarSettingsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.models.settings.CalendarSettingsPojo

/**
 * @author Stanislav Aleshin on 22.07.2025.
 */
class CalendarSettingsSourceSyncManagerImpl(
    localDataSource: CalendarSettingsLocalDataSource.SyncStorage,
    remoteDataSource: CalendarSettingsRemoteDataSource,
    changeQueueStorage: ChangeQueueStorage,
    mappers: CalendarSettingsSyncMapper,
    coroutineManager: CoroutineManager,
    connectionManger: Konnection,
) : CalendarSettingsSourceSyncManager, BaseSourceSyncManager.SingleDocument<BaseCalendarSettingsEntity, CalendarSettingsPojo>(
    localDataSource = localDataSource,
    remoteDataSource = remoteDataSource,
    changeQueueStorage = changeQueueStorage,
    coroutineManager = coroutineManager,
    connectionManger = connectionManger,
    mappers = mappers,
)