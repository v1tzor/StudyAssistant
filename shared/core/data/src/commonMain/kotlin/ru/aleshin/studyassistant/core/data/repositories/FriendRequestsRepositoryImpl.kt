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
import ru.aleshin.studyassistant.core.data.mappers.requsts.mapToData
import ru.aleshin.studyassistant.core.data.mappers.requsts.mapToRemote
import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequests
import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequestsDetails
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.remote.datasources.requests.FriendRequestsRemoteDataSource

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
class FriendRequestsRepositoryImpl(
    private val remoteDataSource: FriendRequestsRemoteDataSource,
) : FriendRequestsRepository {

    override suspend fun addOrUpdateRequests(requests: FriendRequests, targetUser: UID) {
        remoteDataSource.addOrUpdateRequests(requests.mapToRemote(), targetUser)
    }

    override suspend fun fetchRequestsByUser(uid: UID): Flow<FriendRequestsDetails> {
        return remoteDataSource.fetchRequestsByUser(uid).map { it.mapToData() }
    }

    override suspend fun fetchShortRequestsByUser(uid: UID): Flow<FriendRequests> {
        return remoteDataSource.fetchShortRequestsByUser(uid).map { it.mapToData() }
    }

    override suspend fun fetchRealtimeShortRequestsByUser(uid: UID): FriendRequests {
        return remoteDataSource.fetchRealtimeShortRequestsByUser(uid).mapToData()
    }
}