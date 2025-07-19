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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.api.AppwriteApi.Organizations
import ru.aleshin.studyassistant.core.api.AppwriteApi.Storage.BUCKET
import ru.aleshin.studyassistant.core.api.AppwriteApi.Users
import ru.aleshin.studyassistant.core.api.auth.AccountApi
import ru.aleshin.studyassistant.core.api.databases.DatabaseApi
import ru.aleshin.studyassistant.core.api.models.AuthUserPojo
import ru.aleshin.studyassistant.core.api.models.extractBucketIdFromFileUrl
import ru.aleshin.studyassistant.core.api.models.extractIdFromFileUrl
import ru.aleshin.studyassistant.core.api.realtime.RealtimeApi
import ru.aleshin.studyassistant.core.api.storage.StorageApi
import ru.aleshin.studyassistant.core.api.utils.Channels
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.api.utils.Role
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.tryFromJson
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.remote.mappers.users.convertToBase
import ru.aleshin.studyassistant.core.remote.mappers.users.convertToDetails
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojoDetails

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface UsersRemoteDataSource {

    suspend fun addUser(user: AppUserPojoDetails): UID
    suspend fun updateUser(user: AppUserPojoDetails)
    suspend fun uploadAvatar(oldAvatarUrl: String?, avatar: InputFile, targetUser: UID): String
    suspend fun fetchCurrentAppUser(): AuthUserPojo?
    suspend fun fetchStateChanged(): Flow<AuthUserPojo?>
    suspend fun isExistRemoteData(uid: UID): Boolean
    suspend fun fetchUserById(uid: UID): Flow<AppUserPojoDetails?>
    suspend fun fetchRealtimeUserById(uid: UID): AppUserPojoDetails?
    suspend fun fetchUserFriends(uid: UID): Flow<List<AppUserPojoDetails>>
    suspend fun findUsersByCode(code: String): Flow<List<AppUserPojoDetails>>
    suspend fun reloadUser(): AuthUserPojo?
    suspend fun deleteAvatar(avatarUrl: String, targetUser: UID)

    class Base(
        private val account: AccountApi,
        private val database: DatabaseApi,
        private val realtime: RealtimeApi,
        private val storage: StorageApi,
        private val connectivityChecker: Konnection,
    ) : UsersRemoteDataSource {

        override suspend fun addUser(user: AppUserPojoDetails): UID {
            if (user.uid.isEmpty()) throw AppwriteUserException()

            database.createDocument(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = user.uid,
                data = user.convertToBase(),
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.users()),
                    Permission.delete(Role.user(user.uid)),
                ),
                nestedType = AppUserPojo.serializer(),
            )

            return user.uid
        }

        override suspend fun updateUser(user: AppUserPojoDetails) {
            if (user.uid.isEmpty()) throw AppwriteUserException()

            database.updateDocument(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = user.uid,
                data = user.convertToBase(),
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.users()),
                    Permission.delete(Role.user(user.uid)),
                ),
                nestedType = AppUserPojo.serializer(),
            )
        }

        override suspend fun uploadAvatar(oldAvatarUrl: String?, avatar: InputFile, targetUser: UID): String {
            if (targetUser.isBlank()) throw AppwriteUserException()

            if (!oldAvatarUrl.isNullOrBlank()) {
                deleteAvatar(oldAvatarUrl, targetUser)
            }

            val file = storage.createFile(
                bucketId = BUCKET,
                fileId = randomUUID(),
                file = avatar,
                permissions = Permission.avatarData(targetUser),
            )

            return file.getDownloadUrl()
        }

        override suspend fun fetchCurrentAppUser(): AuthUserPojo? {
            return account.getCurrentUser()
        }

        @Suppress("UNCHECKED_CAST")
        override suspend fun fetchStateChanged(): Flow<AuthUserPojo?> = channelFlow {
            send(account.getCurrentUser())

            realtime.subscribe(channels = Channels.account()).collect { event ->
                val payload = event.payload.tryFromJson(AuthUserPojo.serializer())
                trySend(payload ?: account.getCurrentUser(true))
            }
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

        override suspend fun fetchUserById(uid: UID): Flow<AppUserPojoDetails?> {
            if (uid.isEmpty()) throw AppwriteUserException()

            return database.getDocumentFlow(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = uid,
                nestedType = AppUserPojo.serializer(),
            ).map { user ->
                user?.convertToDetails()
            }
        }

        override suspend fun fetchRealtimeUserById(uid: UID): AppUserPojoDetails? {
            require(uid.isNotEmpty())

            val document = database.getDocumentOrNull(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = uid,
                nestedType = AppUserPojo.serializer(),
            )

            return document?.data?.convertToDetails()
        }

        override suspend fun fetchUserFriends(uid: UID): Flow<List<AppUserPojoDetails>> {
            require(uid.isNotEmpty())

            val usersFlow = database.listDocumentsFlow(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                queries = listOf(Query.contains(Users.FRIENDS, uid)),
                nestedType = AppUserPojo.serializer(),
            )

            return usersFlow.map { usersList ->
                usersList.map { it.convertToDetails() }
            }
        }

        override suspend fun findUsersByCode(code: String): Flow<List<AppUserPojoDetails>> {
            require(code.isNotEmpty())

            val usersFlow = database.listDocumentsFlow(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                queries = listOf(Query.equal(Users.CODE, code)),
                nestedType = AppUserPojo.serializer(),
            )

            return usersFlow.map { usersList ->
                usersList.map { it.convertToDetails() }
            }
        }

        override suspend fun reloadUser(): AuthUserPojo? {
            return if (connectivityChecker.isConnected()) {
                account.updateSession("current")
                account.getCurrentUser()
            } else {
                null
            }
        }

        override suspend fun deleteAvatar(avatarUrl: String, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            require(avatarUrl.isNotBlank()) { "User avatar url is empty" }

            storage.deleteFile(
                bucketId = avatarUrl.extractBucketIdFromFileUrl(),
                fileId = avatarUrl.extractIdFromFileUrl(),
            )
        }
    }
}