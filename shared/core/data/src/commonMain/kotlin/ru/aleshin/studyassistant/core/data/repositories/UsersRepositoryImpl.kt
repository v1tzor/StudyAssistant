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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemote
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.core.remote.datasources.users.UsersRemoteDataSource

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
class UsersRepositoryImpl(
    private val remoteDataSource: UsersRemoteDataSource,
) : UsersRepository {

    override fun fetchCurrentUser(): FirebaseUser? {
        return remoteDataSource.fetchCurrentFirebaseUser()
    }

    override suspend fun addOrUpdateAppUser(user: AppUser): Boolean {
        return remoteDataSource.addOrUpdateUser(user.mapToRemote())
    }

    override suspend fun fetchAppUserById(uid: UID): Flow<AppUser?> {
        return remoteDataSource.fetchUserById(uid).map { userPojo -> userPojo?.mapToDomain() }
    }

    override suspend fun fetchRealtimeAppUserById(uid: UID): AppUser? {
        return remoteDataSource.fetchRealtimeAppUserById(uid)?.mapToDomain()
    }

    override suspend fun fetchAppUserFriends(uid: UID): Flow<List<AppUser>> {
        return remoteDataSource.fetchAppUserFriends(uid).map { users ->
            users.map { userPojo -> userPojo.mapToDomain() }
        }
    }

    override suspend fun findAppUsersByCode(code: String): Flow<List<AppUser>> {
        return remoteDataSource.findUsersByCode(code).map { users ->
            users.map { userPojo -> userPojo.mapToDomain() }
        }
    }
}