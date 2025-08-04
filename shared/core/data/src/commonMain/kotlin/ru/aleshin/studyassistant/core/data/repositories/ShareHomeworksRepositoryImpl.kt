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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.extensions.catchIOException
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.share.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.share.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.shared.SharedHomeworksLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SharedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SharedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.repositories.ShareHomeworksRepository
import ru.aleshin.studyassistant.core.remote.datasources.share.SharedHomeworksRemoteDataSource

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
internal class ShareHomeworksRepositoryImpl(
    private val remoteDataSource: SharedHomeworksRemoteDataSource,
    private val localDataSource: SharedHomeworksLocalDataSource,
    private val userSessionProvider: UserSessionProvider,
    private val connectionManager: Konnection,
) : ShareHomeworksRepository {

    override suspend fun addOrUpdateCurrentSharedHomework(homeworks: SharedHomeworks) {
        val currentUser = userSessionProvider.getCurrentUserId()
        val upsertModel = homeworks.mapToRemoteData(userId = currentUser)
        remoteDataSource.addOrUpdateSharedHomeworksForUser(upsertModel, currentUser)
    }

    override suspend fun addOrUpdateSharedHomeworkForUser(homeworks: SharedHomeworks, targetUser: UID) {
        val upsertModel = homeworks.mapToRemoteData(userId = targetUser)
        remoteDataSource.addOrUpdateSharedHomeworksForUser(upsertModel, targetUser)
    }

    override suspend fun fetchCurrentSharedHomeworksDetails(): Flow<SharedHomeworksDetails> {
        return connectionManager.observeHasConnection().flatMapLatest { hasConnection ->
            if (hasConnection) {
                remoteDataSource.fetchItem()
                    .map { sharedHomeworks -> sharedHomeworks?.mapToDomain() }
                    .catchIOException()
            } else {
                localDataSource.fetchItem().map { sharedHomeworks -> sharedHomeworks?.mapToDomain() }
            }
        }.filterNotNull()
    }

    override suspend fun fetchRealtimeSharedHomeworksByUser(targetUser: UID): SharedHomeworks {
        return remoteDataSource.fetchRealtimeSharedHomeworksByUser(targetUser).mapToDomain()
    }
}