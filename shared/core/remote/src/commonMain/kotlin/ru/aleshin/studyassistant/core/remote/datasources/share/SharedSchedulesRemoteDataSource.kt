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
import ru.aleshin.studyassistant.core.api.AppwriteApi.SharedSchedules
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
import ru.aleshin.studyassistant.core.common.extensions.getStringOrNull
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToBase
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToDetails
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToShortDetails
import ru.aleshin.studyassistant.core.remote.mappers.users.convertToDetails
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SharedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesShortPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SentMediatedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesShortDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesShortPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo
import ru.aleshin.studyassistant.core.remote.utils.DatabaseEvent
import ru.aleshin.studyassistant.core.remote.utils.RemoteDataSource

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
interface SharedSchedulesRemoteDataSource : RemoteDataSource.FullSynced.SingleDocument<SharedSchedulesDetailsPojo> {

    suspend fun addOrUpdateSharedSchedulesForUser(schedules: SharedSchedulesPojo, targetUser: UID)
    suspend fun fetchSharedSchedulesDetailsByUser(targetUser: UID): Flow<SharedSchedulesDetailsPojo>
    suspend fun fetchShortSharedSchedulesByUser(targetUser: UID): Flow<SharedSchedulesShortDetailsPojo>
    suspend fun fetchRealtimeSharedSchedulesByUser(targetUser: UID): SharedSchedulesDetailsPojo

    class Base(
        private val database: DatabaseService,
        private val realtime: RealtimeService,
        private val userSessionProvider: UserSessionProvider
    ) : SharedSchedulesRemoteDataSource {

        private val databaseId = SharedSchedules.DATABASE_ID

        private val collectionId = SharedSchedules.COLLECTION_ID

        override suspend fun addOrUpdateItem(item: SharedSchedulesDetailsPojo) {
            val currentUser = userSessionProvider.getCurrentUserId()

            database.upsertDocument(
                databaseId = SharedSchedules.DATABASE_ID,
                collectionId = SharedSchedules.COLLECTION_ID,
                documentId = currentUser,
                data = item.convertToBase(),
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.users()),
                ),
                nestedType = SharedSchedulesPojo.serializer()
            )
        }

        override suspend fun addOrUpdateSharedSchedulesForUser(schedules: SharedSchedulesPojo, targetUser: UID) {
            database.upsertDocument(
                databaseId = SharedSchedules.DATABASE_ID,
                collectionId = SharedSchedules.COLLECTION_ID,
                documentId = targetUser,
                data = schedules,
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.users()),
                ),
                nestedType = SharedSchedulesPojo.serializer()
            )
        }

        override suspend fun fetchItem(): Flow<SharedSchedulesDetailsPojo?> {
            val currentUser = userSessionProvider.getCurrentUserId()
            return fetchSharedSchedulesDetailsByUser(currentUser)
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

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchSharedSchedulesDetailsByUser(targetUser: UID): Flow<SharedSchedulesDetailsPojo> {
            require(targetUser.isNotBlank())

            val sharedSchedulesPojoFlow = database.getDocumentFlow(
                databaseId = SharedSchedules.DATABASE_ID,
                collectionId = SharedSchedules.COLLECTION_ID,
                documentId = targetUser,
                nestedType = SharedSchedulesPojo.serializer(),
            ).map { item ->
                item?.data ?: SharedSchedulesPojo.default(targetUser)
            }

            return sharedSchedulesPojoFlow.flatMapLatest { sharedSchedules ->
                val sentSchedules = sharedSchedules.sent.decodeFromString<SentMediatedSchedulesPojo>()
                val receivedSchedules = sharedSchedules.received.decodeFromString<ReceivedMediatedSchedulesPojo>()

                val users = buildList {
                    addAll(sentSchedules.map { it.value.recipient })
                    addAll(receivedSchedules.map { it.value.sender })
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
                    flowOf(null)
                }

                senderAndRecipientsUsersFlow.map { senderAndRecipientsUsers ->
                    sharedSchedules.convertToDetails(
                        recipientMapper = { recipient ->
                            checkNotNull(senderAndRecipientsUsers?.find { it.uid == recipient })
                        },
                        senderMapper = { sender ->
                            checkNotNull(senderAndRecipientsUsers?.find { it.uid == sender })
                        },
                    )
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchShortSharedSchedulesByUser(targetUser: UID): Flow<SharedSchedulesShortDetailsPojo> {
            require(targetUser.isNotBlank())

            val sharedSchedulesShortPojoFlow = database.getDocumentFlow(
                databaseId = SharedSchedules.DATABASE_ID,
                collectionId = SharedSchedules.COLLECTION_ID,
                documentId = targetUser,
                nestedType = SharedSchedulesShortPojo.serializer(),
            ).map { item ->
                item?.data ?: SharedSchedulesShortPojo.default(targetUser)
            }

            return sharedSchedulesShortPojoFlow.flatMapLatest { sharedSchedules ->
                val sentSchedules = sharedSchedules.sent.decodeFromString<SentMediatedSchedulesPojo>()
                val receivedSchedules = sharedSchedules.received.decodeFromString<ReceivedMediatedSchedulesShortPojo>()

                val users = buildList {
                    addAll(sentSchedules.map { it.value.recipient })
                    addAll(receivedSchedules.map { it.value.sender })
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
                    flowOf(null)
                }

                senderAndRecipientsUsersFlow.map { senderAndRecipientsUsers ->
                    sharedSchedules.convertToShortDetails(
                        recipientMapper = { recipient ->
                            checkNotNull(senderAndRecipientsUsers?.find { it.uid == recipient })
                        },
                        senderMapper = { sender ->
                            checkNotNull(senderAndRecipientsUsers?.find { it.uid == sender })
                        },
                    )
                }
            }
        }

        override suspend fun fetchRealtimeSharedSchedulesByUser(targetUser: UID): SharedSchedulesDetailsPojo {
            require(targetUser.isNotBlank())

            val sharedSchedules = database.getDocumentOrNull(
                databaseId = SharedSchedules.DATABASE_ID,
                collectionId = SharedSchedules.COLLECTION_ID,
                documentId = targetUser,
                nestedType = SharedSchedulesPojo.serializer(),
            )?.data ?: SharedSchedulesPojo.default(targetUser)

            val sentSchedules = sharedSchedules.sent.decodeFromString<SentMediatedSchedulesPojo>()
            val receivedSchedules = sharedSchedules.received.decodeFromString<ReceivedMediatedSchedulesPojo>()

            val users = buildList {
                addAll(sentSchedules.map { it.value.recipient })
                addAll(receivedSchedules.map { it.value.sender })
            }

            val senderAndRecipientsUsers = if (users.isNotEmpty()) {
                database.listDocuments(
                    databaseId = Users.DATABASE_ID,
                    collectionId = Users.COLLECTION_ID,
                    queries = listOf(Query.equal(Users.UID, users)),
                    nestedType = AppUserPojo.serializer(),
                ).documents.map { document -> document.data.convertToDetails() }
            } else {
                emptyList()
            }

            return sharedSchedules.convertToDetails(
                recipientMapper = { recipient ->
                    checkNotNull(senderAndRecipientsUsers.find { it.uid == recipient })
                },
                senderMapper = { sender ->
                    checkNotNull(senderAndRecipientsUsers.find { it.uid == sender })
                },
            )
        }

        override suspend fun deleteItem() {
            val currentUser = userSessionProvider.getCurrentUserId()

            database.deleteDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = currentUser,
            )
        }

        override suspend fun observeEvents(): Flow<DatabaseEvent<SharedSchedulesDetailsPojo>> {
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