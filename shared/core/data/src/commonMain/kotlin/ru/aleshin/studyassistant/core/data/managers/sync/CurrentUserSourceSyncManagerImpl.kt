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
import ru.aleshin.studyassistant.core.data.mappers.users.AppUserSyncMapper
import ru.aleshin.studyassistant.core.data.utils.sync.BaseSourceSyncManager
import ru.aleshin.studyassistant.core.database.datasource.user.UserLocalDataSource
import ru.aleshin.studyassistant.core.database.models.users.BaseAppUserEntity
import ru.aleshin.studyassistant.core.database.storages.ChangeQueueStorage
import ru.aleshin.studyassistant.core.domain.managers.sync.CurrentUserSourceSyncManager
import ru.aleshin.studyassistant.core.remote.datasources.users.UsersRemoteDataSource
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo

/**
 * @author Stanislav Aleshin on 22.07.2025.
 */
class CurrentUserSourceSyncManagerImpl(
    localDataSource: UserLocalDataSource,
    remoteDataSource: UsersRemoteDataSource,
    changeQueueStorage: ChangeQueueStorage,
    mappers: AppUserSyncMapper,
    coroutineManager: CoroutineManager,
    connectionManger: Konnection,
) : CurrentUserSourceSyncManager, BaseSourceSyncManager.SingleDocument<BaseAppUserEntity, AppUserPojo>(
    localDataSource = localDataSource,
    remoteDataSource = remoteDataSource,
    changeQueueStorage = changeQueueStorage,
    coroutineManager = coroutineManager,
    connectionManger = connectionManger,
    mappers = mappers,
)