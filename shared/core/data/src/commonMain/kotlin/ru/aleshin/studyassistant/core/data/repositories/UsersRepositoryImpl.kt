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

import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.core.remote.datasources.users.UsersRemoteDataSource

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
class UsersRepositoryImpl(
    private val remoteDataSource: UsersRemoteDataSource,
) : UsersRepository {

    override suspend fun addOrUpdateAppUser(user: AppUser): Boolean {
        return remoteDataSource.addOrUpdateUser(user.mapToRemoteData())
    }

    override fun fetchCurrentAppUser(): FirebaseUser? {
        return remoteDataSource.fetchCurrentAppUser()
    }

    override suspend fun fetchAuthStateChanged(): Flow<FirebaseUser?> {
        return remoteDataSource.fetchAuthStateChanged()
    }

    override suspend fun fetchUserById(uid: UID): Flow<AppUser?> {
        return remoteDataSource.fetchUserById(uid).map { userPojo -> userPojo?.mapToDomain() }
    }

    override suspend fun fetchRealtimeUserById(uid: UID): AppUser? {
        return remoteDataSource.fetchRealtimeUserById(uid)?.mapToDomain()
    }

    override suspend fun fetchUserFriends(uid: UID): Flow<List<AppUser>> {
        return remoteDataSource.fetchUserFriends(uid).map { users ->
            users.map { userPojo -> userPojo.mapToDomain() }
        }
    }

    override suspend fun findUsersByCode(code: String): Flow<List<AppUser>> {
        return remoteDataSource.findUsersByCode(code).map { users ->
            users.map { userPojo -> userPojo.mapToDomain() }
        }
    }

    override suspend fun uploadUserAvatar(uid: UID, avatar: File): String {
        return remoteDataSource.uploadAvatar(uid, avatar)
    }

    override suspend fun deleteUserAvatar(uid: UID) {
        return remoteDataSource.deleteAvatar(uid)
    }
}