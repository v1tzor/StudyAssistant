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
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.FirebaseStorage
import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.exists
import ru.aleshin.studyassistant.core.common.extensions.snapshotGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.Storage
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.UserData
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.Users
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface UsersRemoteDataSource {

    /**
     * @return [Boolean] if the user is created for the first time, it returns true
     */
    suspend fun addOrUpdateUser(user: AppUserPojo): Boolean
    suspend fun uploadAvatar(uid: UID, avatar: File): String
    fun fetchCurrentAppUser(): FirebaseUser?
    suspend fun fetchAuthStateChanged(): Flow<FirebaseUser?>
    suspend fun isExistRemoteData(uid: UID): Boolean
    suspend fun fetchUserById(uid: UID): Flow<AppUserPojo?>
    suspend fun fetchRealtimeUserById(uid: UID): AppUserPojo?
    suspend fun fetchUserFriends(uid: UID): Flow<List<AppUserPojo>>
    suspend fun findUsersByCode(code: String): Flow<List<AppUserPojo>>
    suspend fun reloadUser(firebaseUser: FirebaseUser): FirebaseUser?
    suspend fun deleteAvatar(uid: UID)

    class Base(
        private val auth: FirebaseAuth,
        private val database: FirebaseFirestore,
        private val storage: FirebaseStorage,
        private val connectivityChecker: Konnection,
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

        override suspend fun uploadAvatar(uid: UID, avatar: File): String {
            if (uid.isEmpty()) throw FirebaseUserException()
            val storageRoot = storage.reference.child(uid)

            val avatarReference = storageRoot.child(Storage.USER_AVATAR).child(Storage.USER_AVATAR_FILE)
            avatarReference.putFile(avatar)

            return avatarReference.getDownloadUrl()
        }

        override fun fetchCurrentAppUser(): FirebaseUser? {
            return auth.currentUser
        }

        override suspend fun fetchAuthStateChanged(): Flow<FirebaseUser?> {
            return auth.authStateChanged
        }

        override suspend fun isExistRemoteData(uid: UID): Boolean {
            val root = database.collection(UserData.ROOT).document(uid)
            return root.collection(UserData.ORGANIZATIONS).snapshotGet().isNotEmpty()
        }

        override suspend fun fetchUserById(uid: UID): Flow<AppUserPojo?> {
            if (uid.isEmpty()) throw FirebaseUserException()

            val reference = database.collection(Users.ROOT).document(uid)

            return reference.snapshots.map { snapshot ->
                snapshot.data(serializer<AppUserPojo?>())
            }
        }

        override suspend fun fetchRealtimeUserById(uid: UID): AppUserPojo? {
            if (uid.isEmpty()) throw FirebaseUserException()

            val reference = database.collection(Users.ROOT).document(uid)

            return reference.get(Source.SERVER).data(serializer<AppUserPojo?>())
        }

        override suspend fun fetchUserFriends(uid: UID): Flow<List<AppUserPojo>> {
            require(uid.isNotEmpty())

            val queryReference = database.collection(Users.ROOT).where {
                Users.FRIENDS containsAny listOf(uid)
            }

            return queryReference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data<AppUserPojo>() }
            }
        }

        override suspend fun findUsersByCode(code: String): Flow<List<AppUserPojo>> {
            val queryReference = database.collection(Users.ROOT).where {
                Users.CODE equalTo code
            }

            return queryReference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data<AppUserPojo>() }
            }
        }

        override suspend fun reloadUser(firebaseUser: FirebaseUser): FirebaseUser? {
            return if (connectivityChecker.isConnected()) {
                firebaseUser.apply { reload() }
            } else {
                null
            }
        }

        override suspend fun deleteAvatar(uid: UID) {
            if (uid.isEmpty()) throw FirebaseUserException()
            val storageRoot = storage.reference.child(uid)

            val avatarReference = storageRoot.child(Storage.USER_AVATAR).child(Storage.USER_AVATAR_FILE)
            avatarReference.delete()
        }
    }
}