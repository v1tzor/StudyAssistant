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
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.data.mappers.users.EmployeeSyncMapper
import ru.aleshin.studyassistant.core.data.utils.sync.BaseSourceSyncManager
import ru.aleshin.studyassistant.core.database.datasource.employee.EmployeeLocalDataSource
import ru.aleshin.studyassistant.core.database.models.employee.BaseEmployeeEntity
import ru.aleshin.studyassistant.core.database.storages.ChangeQueueStorage
import ru.aleshin.studyassistant.core.domain.managers.sync.EmployeeSourceSyncManager
import ru.aleshin.studyassistant.core.remote.datasources.employee.EmployeeRemoteDataSource
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 24.07.2025.
 */
class EmployeeSourceSyncManagerImpl(
    localDataSource: EmployeeLocalDataSource.SyncStorage,
    remoteDataSource: EmployeeRemoteDataSource,
    userSessionProvider: UserSessionProvider,
    changeQueueStorage: ChangeQueueStorage,
    mappers: EmployeeSyncMapper,
    coroutineManager: CoroutineManager,
    connectionManger: Konnection,
) : EmployeeSourceSyncManager, BaseSourceSyncManager.MultipleDocuments<BaseEmployeeEntity, EmployeePojo>(
    localDataSource = localDataSource,
    remoteDataSource = remoteDataSource,
    userSessionProvider = userSessionProvider,
    changeQueueStorage = changeQueueStorage,
    coroutineManager = coroutineManager,
    connectionManger = connectionManger,
    mappers = mappers,
)