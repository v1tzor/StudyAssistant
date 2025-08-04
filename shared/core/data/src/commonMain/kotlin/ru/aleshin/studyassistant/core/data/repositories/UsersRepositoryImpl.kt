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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.retryOnReconnect
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.user.UserLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.AuthUser
import ru.aleshin.studyassistant.core.domain.managers.sync.CurrentUserSourceSyncManager.Companion.CURRENT_USER_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.core.remote.datasources.users.UsersRemoteDataSource

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
class UsersRepositoryImpl(
    private val remoteDataSource: UsersRemoteDataSource,
    private val localDataSource: UserLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
    private val userSessionProvider: UserSessionProvider,
    private val resultSyncHandler: RemoteResultSyncHandler,
    private val connectionManager: Konnection,
) : UsersRepository {

    override suspend fun createNewUserProfile(user: AppUser): UID {
        val uid = user.uid.takeIf { it.isNotBlank() } ?: randomUUID()
        val addableItem = user.copy(uid = uid)

        remoteDataSource.addOrUpdateItem(addableItem.mapToRemoteData())
        localDataSource.addOrUpdateItem(addableItem.mapToLocalData())

        return uid
    }

    override suspend fun updateCurrentUserProfile(user: AppUser) {
        localDataSource.addOrUpdateItem(user.mapToLocalData())
        resultSyncHandler.executeOrAddToQueue(
            data = user.mapToRemoteData(),
            type = OfflineChangeType.UPSERT,
            sourceKey = CURRENT_USER_SOURCE_KEY,
        ) {
            remoteDataSource.addOrUpdateItem(it)
        }
    }

    override suspend fun updateAnotherUserProfile(user: AppUser, targetUser: UID) {
        remoteDataSource.updateAnotherUser(user.mapToRemoteData(), targetUser)
    }

    override suspend fun uploadCurrentUserAvatar(oldAvatarUrl: String?, avatar: InputFile): String {
        val currentUser = userSessionProvider.getCurrentUserId()
        return remoteDataSource.uploadAvatar(oldAvatarUrl, avatar, currentUser)
    }

    override suspend fun fetchCurrentUserProfile(): Flow<AppUser?> {
        return localDataSource.fetchCurrentUserDetails().map { user -> user?.mapToDomain() }
    }

    override suspend fun fetchCurrentUserFriends(): Flow<List<AppUser>> {
        val currentUser = userSessionProvider.getCurrentUserId()
        return remoteDataSource.fetchUserFriends(targetUser = currentUser).map { users ->
            users.map { userPojo -> userPojo.mapToDomain() }
        }.retryOnReconnect(connectionManager)
    }

    override suspend fun fetchCurrentAuthUser(): AuthUser? {
        return remoteDataSource.fetchCurrentAuthUser()?.mapToDomain()
    }

    override suspend fun fetchCurrentUserOrError(): AuthUser {
        return checkNotNull(fetchCurrentAuthUser()) { "Current user is not found" }
    }

    override suspend fun fetchStateChanged(): Flow<AuthUser?> {
        return remoteDataSource.fetchStateChanged().map { it?.mapToDomain() }
    }

    override suspend fun fetchCurrentUserPaidStatus(): Flow<Boolean> {
        return subscriptionChecker.getSubscriberStatusFlow()
    }

    override suspend fun fetchExistRemoteDataStatus(): Flow<Boolean> {
        val currentUser = userSessionProvider.getCurrentUserId()
        return remoteDataSource.fetchUserDetailsById(currentUser).map {
            remoteDataSource.isExistRemoteData(currentUser)
        }.distinctUntilChanged().retryOnReconnect(connectionManager)
    }

    override suspend fun fetchUserProfileById(targetUser: UID): Flow<AppUser?> {
        return remoteDataSource.fetchUserDetailsById(targetUser).map { user ->
            user?.mapToDomain()
        }.retryOnReconnect(connectionManager)
    }

    override suspend fun fetchRealtimeUserById(targetUser: UID): AppUser? {
        return remoteDataSource.fetchRealtimeUserById(targetUser)?.mapToDomain()
    }

    override suspend fun findUsersByCode(code: String): Flow<List<AppUser>> {
        return remoteDataSource.findUsersByCode(code).map { users ->
            users.map { userPojo -> userPojo.mapToDomain() }
        }.retryOnReconnect(connectionManager)
    }

    override suspend fun reloadUser(): AuthUser? {
        return remoteDataSource.reloadUser()?.mapToDomain()
    }

    override suspend fun deleteCurrentUserAvatar(avatarUrl: String) {
        remoteDataSource.deleteAvatar(avatarUrl)
    }
}