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

package ru.aleshin.studyassistant.core.remote.datasources.share

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.format.DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.api.AppwriteApi.Common.UPDATED_AT
import ru.aleshin.studyassistant.core.api.AppwriteApi.SharedHomeworks
import ru.aleshin.studyassistant.core.api.AppwriteApi.Users
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.api.databases.DatabaseService
import ru.aleshin.studyassistant.core.api.realtime.RealtimeService
import ru.aleshin.studyassistant.core.api.utils.Channels
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.api.utils.Role
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.decodeFromString
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.getStringOrNull
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToBase
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToDetails
import ru.aleshin.studyassistant.core.remote.mappers.users.convertToDetails
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.ReceivedMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SentMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SharedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SharedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo
import ru.aleshin.studyassistant.core.remote.utils.DatabaseEvent
import ru.aleshin.studyassistant.core.remote.utils.RemoteDataSource

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
interface SharedHomeworksRemoteDataSource : RemoteDataSource.FullSynced.SingleDocument<SharedHomeworksDetailsPojo> {

    suspend fun addOrUpdateSharedHomeworksForUser(homeworks: SharedHomeworksPojo, targetUser: UID)
    suspend fun fetchSharedHomeworksDetailsByUser(targetUser: UID): Flow<SharedHomeworksDetailsPojo>
    suspend fun fetchShortSharedHomeworksByUser(targetUser: UID): Flow<SharedHomeworksPojo>
    suspend fun fetchRealtimeSharedHomeworksByUser(targetUser: UID): SharedHomeworksPojo

    class Base(
        private val database: DatabaseService,
        private val realtime: RealtimeService,
        private val userSessionProvider: UserSessionProvider
    ) : SharedHomeworksRemoteDataSource {

        private val databaseId = SharedHomeworks.DATABASE_ID

        private val collectionId = SharedHomeworks.COLLECTION_ID

        override suspend fun addOrUpdateItem(item: SharedHomeworksDetailsPojo) {
            val currentUser = userSessionProvider.getCurrentUserId()

            database.upsertDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = currentUser,
                data = item.convertToBase(),
                nestedType = SharedHomeworksPojo.serializer(),
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.users()),
                ),
            )
        }

        override suspend fun addOrUpdateSharedHomeworksForUser(homeworks: SharedHomeworksPojo, targetUser: UID) {
            database.upsertDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = targetUser,
                data = homeworks,
                nestedType = SharedHomeworksPojo.serializer(),
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.users()),
                ),
            )
        }

        override suspend fun fetchItem(): Flow<SharedHomeworksDetailsPojo?> {
            val currentUser = userSessionProvider.getCurrentUserId()
            return fetchSharedHomeworksDetailsByUser(currentUser)
        }

        override suspend fun fetchOnceItem(): SharedHomeworksDetailsPojo? {
            val currentUser = userSessionProvider.getCurrentUserId()

            val sharedHomeworks = database.getDocumentOrNull(
                databaseId = SharedHomeworks.DATABASE_ID,
                collectionId = SharedHomeworks.COLLECTION_ID,
                documentId = currentUser,
                nestedType = SharedHomeworksPojo.serializer(),
            )?.data ?: SharedHomeworksPojo.default(currentUser)

            val sentHomeworks = sharedHomeworks.sent.decodeFromString<UID, SentMediatedHomeworksPojo>()
            val receivedHomeworks = sharedHomeworks.received.decodeFromString<UID, ReceivedMediatedHomeworksPojo>()

            val users = buildList {
                addAll(sentHomeworks.map { it.value.recipients }.extractAllItem())
                addAll(receivedHomeworks.map { it.value.sender })
            }

            val senderAndRecipientsUsers = if (users.isNotEmpty()) {
                database.listDocuments(
                    databaseId = Users.DATABASE_ID,
                    collectionId = Users.COLLECTION_ID,
                    queries = listOf(Query.equal(Users.UID, users)),
                    nestedType = AppUserPojo.serializer(),
                ).documents.map {
                    it.data.convertToDetails()
                }
            } else {
                emptyList()
            }

            return sharedHomeworks.convertToDetails(
                recipientsMapper = { recipients ->
                    checkNotNull(senderAndRecipientsUsers.filter { recipients.contains(it.uid) })
                },
                sendersMapper = { sender ->
                    checkNotNull(senderAndRecipientsUsers.find { it.uid == sender })
                },
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchSharedHomeworksDetailsByUser(targetUser: UID): Flow<SharedHomeworksDetailsPojo> {
            require(targetUser.isNotBlank())

            val sharedHomeworksPojoFlow = database.getDocumentFlow(
                databaseId = SharedHomeworks.DATABASE_ID,
                collectionId = SharedHomeworks.COLLECTION_ID,
                documentId = targetUser,
                nestedType = SharedHomeworksPojo.serializer(),
            ).map { item ->
                item?.data ?: SharedHomeworksPojo.default(targetUser)
            }

            return sharedHomeworksPojoFlow.flatMapLatest { sharedHomeworks ->
                val sentHomeworks = sharedHomeworks.sent.decodeFromString<UID, SentMediatedHomeworksPojo>()
                val receivedHomeworks = sharedHomeworks.received.decodeFromString<UID, ReceivedMediatedHomeworksPojo>()

                val users = buildList {
                    addAll(sentHomeworks.map { it.value.recipients }.extractAllItem())
                    addAll(receivedHomeworks.map { it.value.sender })
                }

                val senderAndRecipientsUsersFlow = if (users.isNotEmpty()) {
                    database.listDocumentsFlow(
                        databaseId = Users.DATABASE_ID,
                        collectionId = Users.COLLECTION_ID,
                        queries = listOf(Query.equal(Users.UID, users)),
                        nestedType = AppUserPojo.serializer(),
                    ).map { usersList ->
                        usersList.map { it.data.convertToDetails() }
                    }
                } else {
                    flowOf(emptyList())
                }

                senderAndRecipientsUsersFlow.map { senderAndRecipientsUsers ->
                    sharedHomeworks.convertToDetails(
                        recipientsMapper = { recipients ->
                            checkNotNull(senderAndRecipientsUsers.filter { recipients.contains(it.uid) })
                        },
                        sendersMapper = { sender ->
                            checkNotNull(senderAndRecipientsUsers.find { it.uid == sender })
                        },
                    )
                }
            }
        }

        override suspend fun fetchShortSharedHomeworksByUser(targetUser: UID): Flow<SharedHomeworksPojo> {
            require(targetUser.isNotBlank())

            return database.getDocumentFlow(
                databaseId = SharedHomeworks.DATABASE_ID,
                collectionId = SharedHomeworks.COLLECTION_ID,
                documentId = targetUser,
                nestedType = SharedHomeworksPojo.serializer(),
            ).map { item ->
                item?.data ?: SharedHomeworksPojo.default(targetUser)
            }
        }

        override suspend fun fetchRealtimeSharedHomeworksByUser(targetUser: UID): SharedHomeworksPojo {
            require(targetUser.isNotBlank())

            val sharedHomeworks = database.getDocumentOrNull(
                databaseId = SharedHomeworks.DATABASE_ID,
                collectionId = SharedHomeworks.COLLECTION_ID,
                documentId = targetUser,
                nestedType = SharedHomeworksPojo.serializer(),
            )

            return sharedHomeworks?.data ?: SharedHomeworksPojo.default(targetUser)
        }

        override suspend fun fetchMetadata(): MetadataModel? {
            val currentUser = userSessionProvider.getCurrentUserId()

            val document = database.getDocumentOrNull(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = currentUser,
                nestedType = JsonElement.serializer(),
            )

            return if (document != null) {
                val documentUpdatedAt = ISO_DATE_TIME_OFFSET.parse(document.updatedAt)
                val updatedAt = document.data.getStringOrNull(UPDATED_AT).let {
                    it?.toLongOrNull() ?: documentUpdatedAt.toInstantUsingOffset().toEpochMilliseconds()
                }
                MetadataModel(id = currentUser, updatedAt = updatedAt)
            } else {
                null
            }
        }

        override suspend fun deleteItem() {
            val currentUser = userSessionProvider.getCurrentUserId()

            database.deleteDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = currentUser,
            )
        }

        override suspend fun observeEvents(): Flow<DatabaseEvent<SharedHomeworksDetailsPojo>> {
            val currentUser = userSessionProvider.getCurrentUserId()

            return realtime.subscribe(
                channels = Channels.document(databaseId, collectionId, currentUser),
                payloadType = SharedHomeworksPojo.serializer(),
            ).map { response ->
                response.copy(
                    events = response.events.filter {
                        it.contains(databaseId) &&
                            it.contains(collectionId) &&
                            it.contains(currentUser)
                    }
                )
            }.filter { response ->
                response.events.isNotEmpty()
            }.map { response ->
                if (response.events.any { it.contains("create") }) {
                    val payload = checkNotNull(fetchItem().first())
                    DatabaseEvent.Create(data = payload, documentId = currentUser)
                } else if (response.events.any { it.contains("delete") }) {
                    DatabaseEvent.Delete(documentId = currentUser)
                } else {
                    val payload = checkNotNull(fetchItem().first())
                    DatabaseEvent.Update(data = payload, documentId = currentUser)
                }
            }
        }
    }
}