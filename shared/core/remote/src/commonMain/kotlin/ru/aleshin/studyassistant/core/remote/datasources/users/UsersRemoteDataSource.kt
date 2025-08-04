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
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.api.AppwriteApi.Organizations
import ru.aleshin.studyassistant.core.api.AppwriteApi.Storage.BUCKET
import ru.aleshin.studyassistant.core.api.AppwriteApi.Users
import ru.aleshin.studyassistant.core.api.auth.AccountService
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.api.databases.DatabaseService
import ru.aleshin.studyassistant.core.api.models.AuthUserPojo
import ru.aleshin.studyassistant.core.api.models.extractBucketIdFromFileUrl
import ru.aleshin.studyassistant.core.api.models.extractIdFromFileUrl
import ru.aleshin.studyassistant.core.api.realtime.RealtimeService
import ru.aleshin.studyassistant.core.api.storage.StorageService
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.api.utils.Role
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.remote.mappers.users.convertToDetails
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojoDetails
import ru.aleshin.studyassistant.core.remote.utils.RemoteDataSource

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface UsersRemoteDataSource : RemoteDataSource.FullSynced.SingleDocument<AppUserPojo> {

    suspend fun updateAnotherUser(user: AppUserPojo, targetUser: UID)
    suspend fun uploadAvatar(oldAvatarUrl: String?, avatar: InputFile, targetUser: UID): String
    suspend fun fetchCurrentAuthUser(): AuthUserPojo?
    suspend fun fetchStateChanged(): Flow<AuthUserPojo?>
    suspend fun isExistRemoteData(uid: UID): Boolean
    suspend fun fetchUserDetailsById(targetUser: UID): Flow<AppUserPojoDetails?>
    suspend fun fetchRealtimeUserById(targetUser: UID): AppUserPojoDetails?
    suspend fun fetchUserFriends(targetUser: UID): Flow<List<AppUserPojoDetails>>
    suspend fun findUsersByCode(code: String): Flow<List<AppUserPojoDetails>>
    suspend fun reloadUser(): AuthUserPojo?
    suspend fun deleteAvatar(avatarUrl: String)

    class Base(
        database: DatabaseService,
        realtime: RealtimeService,
        userSessionProvider: UserSessionProvider,
        private val account: AccountService,
        private val storage: StorageService,
        private val connectivityChecker: Konnection,
    ) : UsersRemoteDataSource, RemoteDataSource.FullSynced.SingleDocument.BaseAppwrite<AppUserPojo>(
        database = database,
        realtime = realtime,
        userSessionProvider = userSessionProvider,
    ) {

        override val databaseId = Users.DATABASE_ID

        override val collectionId = Users.COLLECTION_ID

        override val nestedType = AppUserPojo.serializer()

        override fun permissions(currentUser: UID) = listOf(
            Permission.read(Role.users()),
            Permission.update(Role.users()),
            Permission.delete(Role.user(currentUser)),
        )

        override suspend fun updateAnotherUser(user: AppUserPojo, targetUser: UID) {
            database.updateDocument(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = targetUser,
                data = user,
                nestedType = AppUserPojo.serializer(),
            )
        }

        override suspend fun uploadAvatar(oldAvatarUrl: String?, avatar: InputFile, targetUser: UID): String {
            if (targetUser.isBlank()) throw AppwriteUserException()

            if (!oldAvatarUrl.isNullOrBlank()) {
                deleteAvatar(oldAvatarUrl)
            }

            val file = storage.createFile(
                bucketId = BUCKET,
                fileId = randomUUID(),
                file = avatar,
                permissions = Permission.avatarData(targetUser),
            )

            return file.getDownloadUrl()
        }

        override suspend fun fetchCurrentAuthUser(): AuthUserPojo? {
            return account.getCurrentUser()
        }

        override suspend fun fetchStateChanged(): Flow<AuthUserPojo?> {
            return account.fetchStateChanged()
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

        override suspend fun fetchUserDetailsById(targetUser: UID): Flow<AppUserPojoDetails?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            return database.getDocumentFlow(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = targetUser,
                nestedType = AppUserPojo.serializer(),
            ).map { user ->
                user?.data?.convertToDetails()
            }
        }

        override suspend fun fetchRealtimeUserById(targetUser: UID): AppUserPojoDetails? {
            require(targetUser.isNotEmpty())

            val document = database.getDocumentOrNull(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                documentId = targetUser,
                nestedType = AppUserPojo.serializer(),
            )

            return document?.data?.convertToDetails()
        }

        override suspend fun fetchUserFriends(targetUser: UID): Flow<List<AppUserPojoDetails>> {
            require(targetUser.isNotEmpty())

            val usersFlow = database.listDocumentsFlow(
                databaseId = Users.DATABASE_ID,
                collectionId = Users.COLLECTION_ID,
                queries = listOf(Query.contains(Users.FRIENDS, targetUser)),
                nestedType = AppUserPojo.serializer(),
            )

            return usersFlow.map { usersList ->
                usersList.map { it.data.convertToDetails() }
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
                usersList.map { it.data.convertToDetails() }
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

        override suspend fun deleteAvatar(avatarUrl: String) {
            require(avatarUrl.isNotBlank()) { "User avatar url is empty" }

            storage.deleteFile(
                bucketId = avatarUrl.extractBucketIdFromFileUrl(),
                fileId = avatarUrl.extractIdFromFileUrl(),
            )
        }
    }
}