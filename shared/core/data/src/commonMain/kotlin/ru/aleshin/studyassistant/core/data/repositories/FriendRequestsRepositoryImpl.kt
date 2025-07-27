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

@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.aleshin.studyassistant.core.data.repositories

import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.requsts.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.requsts.mapToDomainDetails
import ru.aleshin.studyassistant.core.data.mappers.requsts.mapToRemote
import ru.aleshin.studyassistant.core.database.datasource.requests.FriendRequestsLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequests
import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequestsDetails
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.remote.datasources.requests.FriendRequestsRemoteDataSource

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
class FriendRequestsRepositoryImpl(
    private val remoteDataSource: FriendRequestsRemoteDataSource,
    private val localDataSource: FriendRequestsLocalDataSource,
    private val userSessionProvider: UserSessionProvider,
    private val connectionManager: Konnection,
) : FriendRequestsRepository {

    override suspend fun addOrUpdateCurrentRequests(requests: FriendRequests) {
        val currentUser = userSessionProvider.getCurrentUserId()
        val upsertModel = requests.mapToRemote(currentUser)
        remoteDataSource.addOrUpdateRequestsForUser(upsertModel, currentUser)
    }

    override suspend fun addOrUpdateRequestsForUser(requests: FriendRequests, targetUser: UID) {
        val upsertModel = requests.mapToRemote(targetUser)
        remoteDataSource.addOrUpdateRequestsForUser(upsertModel, targetUser)
    }

    override suspend fun fetchCurrentRequestsDetails(): Flow<FriendRequestsDetails> {
        return connectionManager.observeHasConnection().flatMapLatest { hasConnection ->
            if (hasConnection) {
                remoteDataSource.fetchItem().catch { exception ->
                    if (exception is InternetConnectionException) emit(null) else throw exception
                }.map { friendRequests ->
                    friendRequests?.mapToDomainDetails()
                }
            } else {
                localDataSource.fetchItem().map { friendRequests ->
                    friendRequests?.mapToDomainDetails()
                }
            }
        }.filterNotNull()
    }

    override suspend fun fetchCurrentRequests(): Flow<FriendRequests> {
        return connectionManager.observeHasConnection().flatMapLatest { hasConnection ->
            if (hasConnection) {
                remoteDataSource.fetchItem().catch { exception ->
                    if (exception is InternetConnectionException) emit(null) else throw exception
                }.map { friendRequests ->
                    friendRequests?.mapToDomain()
                }
            } else {
                localDataSource.fetchItem().map { friendRequests ->
                    friendRequests?.mapToDomain()
                }
            }
        }.filterNotNull()
    }

    override suspend fun fetchRealtimeRequestsByUser(targetUser: UID): FriendRequests {
        return remoteDataSource.fetchRealtimeShortRequestsByUser(targetUser).mapToDomain()
    }
}