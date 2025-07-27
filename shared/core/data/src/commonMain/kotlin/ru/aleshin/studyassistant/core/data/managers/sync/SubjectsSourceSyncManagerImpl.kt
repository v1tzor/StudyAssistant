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
import ru.aleshin.studyassistant.core.data.mappers.subjects.SubjectSyncMapper
import ru.aleshin.studyassistant.core.data.utils.sync.BaseSourceSyncManager
import ru.aleshin.studyassistant.core.database.datasource.subjects.SubjectsLocalDataSource
import ru.aleshin.studyassistant.core.database.models.subjects.BaseSubjectEntity
import ru.aleshin.studyassistant.core.database.storages.ChangeQueueStorage
import ru.aleshin.studyassistant.core.domain.managers.sync.SubjectsSourceSyncManager
import ru.aleshin.studyassistant.core.remote.datasources.subjects.SubjectsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectPojo

/**
 * @author Stanislav Aleshin on 24.07.2025.
 */
class SubjectsSourceSyncManagerImpl(
    localDataSource: SubjectsLocalDataSource.SyncStorage,
    remoteDataSource: SubjectsRemoteDataSource,
    userSessionProvider: UserSessionProvider,
    changeQueueStorage: ChangeQueueStorage,
    mappers: SubjectSyncMapper,
    coroutineManager: CoroutineManager,
    connectionManger: Konnection,
) : SubjectsSourceSyncManager, BaseSourceSyncManager.MultipleDocuments<BaseSubjectEntity, SubjectPojo>(
    localDataSource = localDataSource,
    remoteDataSource = remoteDataSource,
    userSessionProvider = userSessionProvider,
    changeQueueStorage = changeQueueStorage,
    coroutineManager = coroutineManager,
    connectionManger = connectionManger,
    mappers = mappers,
)