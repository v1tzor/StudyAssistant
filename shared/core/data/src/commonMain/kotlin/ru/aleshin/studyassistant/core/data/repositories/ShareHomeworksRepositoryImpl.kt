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
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.share.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.share.mapToRemoteData
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SharedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SharedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.repositories.ShareHomeworksRepository
import ru.aleshin.studyassistant.core.remote.datasources.share.ShareHomeworksRemoteDataSource

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
internal class ShareHomeworksRepositoryImpl(
    private val remoteDataSource: ShareHomeworksRemoteDataSource,
) : ShareHomeworksRepository {

    override suspend fun addOrUpdateSharedHomework(homeworks: SharedHomeworks, targetUser: UID) {
        remoteDataSource.addOrUpdateSharedHomework(homeworks.mapToRemoteData(), targetUser)
    }

    override suspend fun fetchSharedHomeworksByUser(uid: UID): Flow<SharedHomeworksDetails> {
        return remoteDataSource.fetchSharedHomeworksByUser(uid).map { it.mapToDomain() }
    }

    override suspend fun fetchShortSharedHomeworksByUser(uid: UID): Flow<SharedHomeworks> {
        return remoteDataSource.fetchShortSharedHomeworksByUser(uid).map { it.mapToDomain() }
    }

    override suspend fun fetchRealtimeSharedHomeworksByUser(uid: UID): SharedHomeworks {
        return remoteDataSource.fetchRealtimeSharedHomeworksByUser(uid).mapToDomain()
    }
}