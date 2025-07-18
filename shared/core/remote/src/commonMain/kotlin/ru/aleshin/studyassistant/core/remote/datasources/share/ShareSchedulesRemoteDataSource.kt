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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.decodeFromString
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.appwrite.databases.DatabaseService
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Permission
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Query
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Role
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.SharedSchedules
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Users
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToDetails
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToShortDetails
import ru.aleshin.studyassistant.core.remote.mappers.users.convertToDetails
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.ReceivedMediatedSchedulesShortPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SentMediatedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesShortDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesShortPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
interface ShareSchedulesRemoteDataSource {

    suspend fun addOrUpdateSharedSchedules(schedules: SharedSchedulesPojo, targetUser: UID)
    suspend fun fetchSharedSchedulesByUser(uid: UID): Flow<SharedSchedulesDetailsPojo>
    suspend fun fetchShortSharedSchedulesByUser(uid: UID): Flow<SharedSchedulesShortDetailsPojo>
    suspend fun fetchRealtimeSharedSchedulesByUser(uid: UID): SharedSchedulesDetailsPojo

    class Base(
        private val database: DatabaseService,
    ) : ShareSchedulesRemoteDataSource {

        override suspend fun addOrUpdateSharedSchedules(
            schedules: SharedSchedulesPojo,
            targetUser: UID
        ) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

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

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchSharedSchedulesByUser(uid: UID): Flow<SharedSchedulesDetailsPojo> {
            require(uid.isNotBlank())

            val sharedSchedulesPojoFlow = database.getDocumentFlow(
                databaseId = SharedSchedules.DATABASE_ID,
                collectionId = SharedSchedules.COLLECTION_ID,
                documentId = uid,
                nestedType = SharedSchedulesPojo.serializer(),
            ).map { item ->
                item ?: SharedSchedulesPojo.default()
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
                        usersList.map { it.convertToDetails() }
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
        override suspend fun fetchShortSharedSchedulesByUser(uid: UID): Flow<SharedSchedulesShortDetailsPojo> {
            require(uid.isNotBlank())

            val sharedSchedulesShortPojoFlow = database.getDocumentFlow(
                databaseId = SharedSchedules.DATABASE_ID,
                collectionId = SharedSchedules.COLLECTION_ID,
                documentId = uid,
                nestedType = SharedSchedulesShortPojo.serializer(),
            ).map { item ->
                item ?: SharedSchedulesShortPojo.default()
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
                        usersList.map { it.convertToDetails() }
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

        override suspend fun fetchRealtimeSharedSchedulesByUser(uid: UID): SharedSchedulesDetailsPojo {
            require(uid.isNotBlank())

            val sharedSchedules = database.getDocumentOrNull(
                databaseId = SharedSchedules.DATABASE_ID,
                collectionId = SharedSchedules.COLLECTION_ID,
                documentId = uid,
                nestedType = SharedSchedulesPojo.serializer(),
            )?.data ?: SharedSchedulesPojo.default()

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
    }
}