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

import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.remote.appwrite.auth.AppwriteAuth
import ru.aleshin.studyassistant.core.remote.appwrite.auth.AuthUserPojo
import ru.aleshin.studyassistant.core.remote.appwrite.databases.AppwriteDatabase
import ru.aleshin.studyassistant.core.remote.appwrite.databases.AppwriteRealtime
import ru.aleshin.studyassistant.core.remote.appwrite.databases.getDocumentFlow
import ru.aleshin.studyassistant.core.remote.appwrite.databases.getDocumentOrNull
import ru.aleshin.studyassistant.core.remote.appwrite.databases.listDocumentsFlow
import ru.aleshin.studyassistant.core.remote.appwrite.storage.AppwriteStorage
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Channels
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Permission
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Query
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Role
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Organizations
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Storage.BUCKET
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Users
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface UsersRemoteDataSource {

    /**
     * @return [Boolean] if the user is created for the first time, it returns true
     */
    suspend fun addOrUpdateUser(user: AppUserPojo): Boolean
    suspend fun uploadAvatar(uid: UID, avatar: InputFile): String
    fun fetchCurrentAppUser(): AuthUserPojo?
    suspend fun fetchAuthStateChanged(): Flow<AuthUserPojo?>
    suspend fun isExistRemoteData(uid: UID): Boolean
    suspend fun fetchUserById(uid: UID): Flow<AppUserPojo?>
    suspend fun fetchRealtimeUserById(uid: UID): AppUserPojo?
    suspend fun fetchUserFriends(uid: UID): Flow<List<AppUserPojo>>
    suspend fun findUsersByCode(code: String): Flow<List<AppUserPojo>>
    suspend fun reloadUser(): AuthUserPojo?
    suspend fun deleteAvatar(uid: UID)

    class Base(
        private val auth: AppwriteAuth,
        private val database: AppwriteDatabase,
        private val realtime: AppwriteRealtime,
        private val storage: AppwriteStorage,
        private val connectivityChecker: Konnection,
    ) : UsersRemoteDataSource {

        override suspend fun addOrUpdateUser(user: AppUserPojo): Boolean {
            if (user.uid.isEmpty()) throw AppwriteUserException()

            val existDocument = database.getDocumentOrNull(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = user.uid,
            )

            database.upsertDocument(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = user.uid,
                data = user,
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.users()),
                    Permission.delete(Role.user(user.uid)),
                ),
                nestedType = AppUserPojo::class,
            )

            return existDocument == null
        }

        override suspend fun uploadAvatar(uid: UID, avatar: InputFile): String {
            require(uid.isNotEmpty())

            val file = storage.createFile(
                bucketId = BUCKET,
                fileId = randomUUID(),
                fileBytes = avatar.fileBytes,
                filename = avatar.filename,
                mimeType = avatar.mimeType,
                permissions = Permission.onlyUsersVisibleData(uid),
            )

            return file.getDownloadUrl()
        }

        override fun fetchCurrentAppUser(): AuthUserPojo? {
            return auth.fetchCurrentUser()
        }

        override suspend fun fetchAuthStateChanged(): Flow<AuthUserPojo?> = channelFlow {
            send(auth.fetchCurrentUser())

            var job: Job? = null
            val subscribe = realtime.subscribe(channels = Channels.account()) { event ->
                job?.cancel()
                job = launch { send(auth.fetchCurrentUser()) }
            }

            awaitClose { subscribe.close() }
        }

        override suspend fun isExistRemoteData(uid: UID): Boolean {
            require(uid.isNotEmpty())

            val documentList = database.listDocuments(
                databaseId = Organizations.DATABASE_ID,
                collectionId = Organizations.COLLECTION_ID,
                queries = listOf(Query.contains(Organizations.USER_ID, uid))
            )
            return documentList.documents.isNotEmpty()
        }

        override suspend fun fetchUserById(uid: UID): Flow<AppUserPojo?> {
            if (uid.isEmpty()) throw AppwriteUserException()

            return database.getDocumentFlow(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = uid,
                realtime = realtime,
            )
        }

        override suspend fun fetchRealtimeUserById(uid: UID): AppUserPojo? {
            require(uid.isNotEmpty())

            val document = database.getDocumentOrNull(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = uid,
                nestedType = AppUserPojo::class,
            )

            return document?.data
        }

        override suspend fun fetchUserFriends(uid: UID): Flow<List<AppUserPojo>> {
            require(uid.isNotEmpty())

            val usersFlow = database.listDocumentsFlow<AppUserPojo>(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                queries = listOf(Query.contains(Users.FRIENDS, uid)),
                realtime = realtime,
            )

            return usersFlow
        }

        override suspend fun findUsersByCode(code: String): Flow<List<AppUserPojo>> {
            require(code.isNotEmpty())

            val usersFlow = database.listDocumentsFlow<AppUserPojo>(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                queries = listOf(Query.equal(Users.CODE, code)),
                realtime = realtime,
            )

            return usersFlow
        }

        override suspend fun reloadUser(): AuthUserPojo? {
            return if (connectivityChecker.isConnected()) auth.reloadUser() else null
        }

        override suspend fun deleteAvatar(uid: UID) {
            if (uid.isEmpty()) throw AppwriteUserException()
            storage.deleteFile(BUCKET, uid)
        }
    }
}