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

package repositories

import dev.gitlive.firebase.auth.FirebaseUser
import entities.users.AppUser
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mappers.organizations.mapToData
import mappers.organizations.mapToDomain
import mappers.users.mapToData
import mappers.users.mapToDomain
import remote.users.UsersRemoteDataSource

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
class UsersRepositoryImpl(
    private val remoteDataSource: UsersRemoteDataSource,
) : UsersRepository {

    override fun fetchCurrentUser(): FirebaseUser? {
        return remoteDataSource.fetchCurrentFirebaseUser()
    }

    override suspend fun createOrUpdateAppUser(user: AppUser): Boolean {
        return remoteDataSource.addOrUpdateUser(user.mapToData())
    }

    override suspend fun fetchAppUserById(uid: UID): Flow<AppUser?> {
        return remoteDataSource.fetchUserById(uid).map { it?.mapToDomain() }
    }

    override suspend fun fetchAppUserByName(query: String): Flow<List<AppUser>> {
        return remoteDataSource.fetchUserByName(query).map { userList ->
            userList.map { user -> user.mapToDomain() }
        }
    }

    override suspend fun fetchAppUserByCode(code: String): Flow<List<AppUser>> {
        return remoteDataSource.fetchUserByCode(code).map { userList ->
            userList.map { user -> user.mapToDomain() }
        }
    }
}