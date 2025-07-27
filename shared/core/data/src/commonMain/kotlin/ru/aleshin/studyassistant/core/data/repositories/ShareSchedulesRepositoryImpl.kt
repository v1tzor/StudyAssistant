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
import ru.aleshin.studyassistant.core.data.mappers.share.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.share.mapToDomainShort
import ru.aleshin.studyassistant.core.data.mappers.share.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.shared.SharedSchedulesLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SharedSchedules
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SharedSchedulesShort
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.remote.datasources.share.SharedSchedulesRemoteDataSource

/**
 * @author Stanislav Aleshin on 14.08.2024.
 */
class ShareSchedulesRepositoryImpl(
    private val remoteDataSource: SharedSchedulesRemoteDataSource,
    private val localDataSource: SharedSchedulesLocalDataSource,
    private val userSessionProvider: UserSessionProvider,
    private val connectionManager: Konnection,
) : ShareSchedulesRepository {

    override suspend fun addOrUpdateCurrentSharedSchedules(schedules: SharedSchedules) {
        val currentUser = userSessionProvider.getCurrentUserId()
        val upsertModel = schedules.mapToRemoteData(userId = currentUser)
        remoteDataSource.addOrUpdateSharedSchedulesForUser(upsertModel, currentUser)
    }

    override suspend fun addOrUpdateSharedSchedulesForUser(schedules: SharedSchedules, targetUser: UID) {
        val upsertModel = schedules.mapToRemoteData(userId = targetUser)
        remoteDataSource.addOrUpdateSharedSchedulesForUser(upsertModel, targetUser)
    }

    override suspend fun fetchCurrentSharedSchedules(): Flow<SharedSchedules> {
        return remoteDataSource.fetchItem().map { it?.mapToDomain() }.filterNotNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun fetchCurrentShortSharedSchedules(): Flow<SharedSchedulesShort> {
        return connectionManager.observeHasConnection().flatMapLatest { hasConnection ->
            if (hasConnection) {
                remoteDataSource.fetchItem().catch { exception ->
                    if (exception is InternetConnectionException) emit(null) else throw exception
                }.map { sharedHomeworks ->
                    sharedHomeworks?.mapToDomainShort()
                }
            } else {
                localDataSource.fetchItem().map { friendRequests ->
                    friendRequests?.mapToDomain()
                }
            }
        }.filterNotNull()
    }

    override suspend fun fetchRealtimeSharedSchedulesByUser(targetUser: UID): SharedSchedules {
        return remoteDataSource.fetchRealtimeSharedSchedulesByUser(targetUser).mapToDomain()
    }
}