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

package ru.aleshin.studyassistant.core.remote.datasources.organizations

import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.api.AppwriteApi.Common
import ru.aleshin.studyassistant.core.api.AppwriteApi.Organizations
import ru.aleshin.studyassistant.core.api.AppwriteApi.Storage.BUCKET
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.api.databases.DatabaseService
import ru.aleshin.studyassistant.core.api.models.extractBucketIdFromFileUrl
import ru.aleshin.studyassistant.core.api.models.extractIdFromFileUrl
import ru.aleshin.studyassistant.core.api.realtime.RealtimeService
import ru.aleshin.studyassistant.core.api.storage.StorageService
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.common.extensions.getString
import ru.aleshin.studyassistant.core.common.extensions.getStringOrNull
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationPojo
import ru.aleshin.studyassistant.core.remote.utils.RemoteDataSource

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface OrganizationsRemoteDataSource : RemoteDataSource.FullSynced.MultipleDocuments<OrganizationPojo> {

    suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile): String

    suspend fun deleteAvatar(avatarUrl: String)

    class Base(
        database: DatabaseService,
        userSessionProvider: UserSessionProvider,
        realtime: RealtimeService,
        private val storage: StorageService,
    ) : OrganizationsRemoteDataSource, RemoteDataSource.FullSynced.MultipleDocuments.BaseAppwrite<OrganizationPojo>(
        database = database,
        realtime = realtime,
        userSessionProvider = userSessionProvider,
    ) {

        override val databaseId = Organizations.DATABASE_ID

        override val collectionId = Organizations.COLLECTION_ID

        override val nestedType = OrganizationPojo.serializer()

        override fun permissions(currentUser: UID) = Permission.onlyUserData(currentUser)

        override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile): String {
            val targetUser = userSessionProvider.getCurrentUserId()

            if (!oldAvatarUrl.isNullOrBlank()) {
                deleteAvatar(oldAvatarUrl)
            }

            val file = storage.createFile(
                bucketId = BUCKET,
                fileId = randomUUID(),
                file = file,
                permissions = Permission.avatarData(targetUser),
            )

            return file.getDownloadUrl()
        }

        override suspend fun deleteItemById(id: String) {
            val avatarUrl = database.getDocumentOrNull(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = id,
                queries = listOf(Query.select(listOf(Organizations.AVATAR_URL))),
                nestedType = JsonElement.serializer(),
            )?.data?.getString(Organizations.AVATAR_URL)

            database.deleteDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = id,
            )

            if (!avatarUrl.isNullOrBlank()) {
                deleteAvatar(avatarUrl)
            }
        }

        override suspend fun deleteItemsByIds(ids: List<String>) {
            val avatarUrls = database.listDocuments(
                databaseId = databaseId,
                collectionId = collectionId,
                queries = listOf(
                    Query.equal(Common.UID, ids),
                    Query.select(listOf(Organizations.AVATAR_URL))
                ),
                nestedType = JsonElement.serializer(),
            ).documents.mapNotNull { document ->
                document.data.getStringOrNull(Organizations.AVATAR_URL)?.takeIf { it.isNotEmpty() }
            }

            database.deleteDocuments(
                databaseId = databaseId,
                collectionId = collectionId,
                queries = listOf(Query.equal(Common.UID, ids)),
            )

            if (avatarUrls.isNotEmpty()) {
                avatarUrls.forEach { deleteAvatar(it) }
            }
        }

        override suspend fun deleteAvatar(avatarUrl: String) {
            require(avatarUrl.isNotBlank()) { "Organization avatar url is empty" }

            storage.deleteFile(
                bucketId = avatarUrl.extractBucketIdFromFileUrl(),
                fileId = avatarUrl.extractIdFromFileUrl(),
            )
        }
    }
}