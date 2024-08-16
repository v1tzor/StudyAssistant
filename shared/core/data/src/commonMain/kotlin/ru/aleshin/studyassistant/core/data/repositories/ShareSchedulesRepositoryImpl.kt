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
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SharedSchedules
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SharedSchedulesShort
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.remote.datasources.share.ShareSchedulesRemoteDataSource

/**
 * @author Stanislav Aleshin on 14.08.2024.
 */
class ShareSchedulesRepositoryImpl(
    private val remoteDataSource: ShareSchedulesRemoteDataSource,
) : ShareSchedulesRepository {

    override suspend fun addOrUpdateSharedSchedules(schedules: SharedSchedules, targetUser: UID) {
        remoteDataSource.addOrUpdateSharedSchedules(schedules.mapToRemoteData(), targetUser)
    }

    override suspend fun fetchShareSchedulesByUser(uid: UID): Flow<SharedSchedules> {
        return remoteDataSource.fetchSharedSchedulesByUser(uid).map { it.mapToDomain() }
    }

    override suspend fun fetchShortSharedSchedulesByUser(uid: UID): Flow<SharedSchedulesShort> {
        return remoteDataSource.fetchShortSharedSchedulesByUser(uid).map { it.mapToDomain() }
    }

    override suspend fun fetchRealtimeSharedSchedulesByUser(uid: UID): SharedSchedules {
        return remoteDataSource.fetchRealtimeSharedSchedulesByUser(uid).mapToDomain()
    }
}