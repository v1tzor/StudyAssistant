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

package ru.aleshin.studyassistant.core.remote.datasources.users

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.exists
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirestore.Users
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface UsersRemoteDataSource {

    /**
     * @return [Boolean] if the user is created for the first time, it returns true
     */
    suspend fun addOrUpdateUser(user: AppUserPojo): Boolean

    fun fetchCurrentFirebaseUser(): FirebaseUser?

    suspend fun fetchUserById(uid: UID): Flow<AppUserPojo?>

    suspend fun fetchUsersByName(query: String): Flow<List<AppUserPojo>>

    suspend fun fetchUsersByCode(code: String): Flow<List<AppUserPojo>>

    class Base(
        private val auth: FirebaseAuth,
        private val database: FirebaseFirestore,
    ) : UsersRemoteDataSource {

        override suspend fun addOrUpdateUser(user: AppUserPojo): Boolean {
            if (user.uid.isEmpty()) throw FirebaseUserException()

            val reference = database.collection(Users.ROOT).document(user.uid)

            return database.runTransaction {
                val isNewUser = !reference.exists()
                reference.set(user)
                return@runTransaction isNewUser
            }
        }

        override fun fetchCurrentFirebaseUser(): FirebaseUser? {
            return auth.currentUser
        }

        override suspend fun fetchUserById(uid: UID): Flow<AppUserPojo?> {
            if (uid.isEmpty()) throw FirebaseUserException()

            val reference = database.collection(Users.ROOT).document(uid)

            return reference.snapshots.map { snapshot ->
                snapshot.data(serializer<AppUserPojo?>())
            }
        }

        override suspend fun fetchUsersByName(query: String): Flow<List<AppUserPojo>> {
            val queryReference = database.collection(Users.ROOT).where {
                Users.USERNAME greaterThanOrEqualTo query
            }
            return queryReference.snapshots.map { snapshot ->
                snapshot.documents.map { documentSnapshot -> documentSnapshot.data<AppUserPojo>() }
            }
        }

        override suspend fun fetchUsersByCode(code: String): Flow<List<AppUserPojo>> {
            val queryReference = database.collection(Users.ROOT).where {
                Users.CODE equalTo code
            }
            return queryReference.snapshots.map { snapshot ->
                snapshot.documents.map { documentSnapshot -> documentSnapshot.data<AppUserPojo>() }
            }
        }
    }
}