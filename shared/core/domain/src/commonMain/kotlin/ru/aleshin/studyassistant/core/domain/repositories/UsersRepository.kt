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

package ru.aleshin.studyassistant.core.domain.repositories

import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface UsersRepository {
    suspend fun addOrUpdateAppUser(user: AppUser): Boolean
    fun fetchCurrentAppUser(): FirebaseUser?
    fun fetchCurrentUserOrError() = fetchCurrentAppUser() ?: throw FirebaseUserException()
    suspend fun fetchAuthStateChanged(): Flow<FirebaseUser?>
    suspend fun fetchCurrentUserPaidStatus(): Flow<Boolean>
    suspend fun fetchExistRemoteDataStatus(uid: UID): Flow<Boolean>
    suspend fun fetchUserById(uid: UID): Flow<AppUser?>
    suspend fun fetchRealtimeUserById(uid: UID): AppUser?
    suspend fun fetchUserFriends(uid: UID): Flow<List<AppUser>>
    suspend fun findUsersByCode(code: String): Flow<List<AppUser>>
    suspend fun uploadUserAvatar(uid: UID, avatar: File): String
    suspend fun reloadUser(firebaseUser: FirebaseUser): FirebaseUser?
    suspend fun deleteUserAvatar(uid: UID)
}