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
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.appwrite.databases.DatabaseService
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Permission
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Query
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Role
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.SharedHomeworks
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Users
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToDetails
import ru.aleshin.studyassistant.core.remote.mappers.users.convertToDetails
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.ReceivedMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SentMediatedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SharedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SharedHomeworksPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
interface ShareHomeworksRemoteDataSource {

    suspend fun addOrUpdateSharedHomework(homeworks: SharedHomeworksPojo, targetUser: UID)
    suspend fun fetchSharedHomeworksByUser(uid: UID): Flow<SharedHomeworksDetailsPojo>
    suspend fun fetchShortSharedHomeworksByUser(uid: UID): Flow<SharedHomeworksPojo>
    suspend fun fetchRealtimeSharedHomeworksByUser(uid: UID): SharedHomeworksPojo

    class Base(
        private val database: DatabaseService,
    ) : ShareHomeworksRemoteDataSource {

        override suspend fun addOrUpdateSharedHomework(homeworks: SharedHomeworksPojo, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            database.upsertDocument(
                databaseId = SharedHomeworks.DATABASE_ID,
                collectionId = SharedHomeworks.COLLECTION_ID,
                documentId = targetUser,
                data = homeworks,
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.users()),
                ),
                nestedType = SharedHomeworksPojo.serializer()
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchSharedHomeworksByUser(uid: UID): Flow<SharedHomeworksDetailsPojo> {
            require(uid.isNotBlank())

            val sharedHomeworksPojoFlow = database.getDocumentFlow(
                databaseId = SharedHomeworks.DATABASE_ID,
                collectionId = SharedHomeworks.COLLECTION_ID,
                documentId = uid,
                nestedType = SharedHomeworksPojo.serializer(),
            ).map { item ->
                item ?: SharedHomeworksPojo.default()
            }

            return sharedHomeworksPojoFlow.flatMapLatest { sharedHomeworks ->
                val sentHomeworks = sharedHomeworks.sent.decodeFromString<SentMediatedHomeworksPojo>()
                val receivedHomeworks = sharedHomeworks.received.decodeFromString<ReceivedMediatedHomeworksPojo>()

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
                        usersList.map { it.convertToDetails() }
                    }
                } else {
                    flowOf(null)
                }

                senderAndRecipientsUsersFlow.map { senderAndRecipientsUsers ->
                    sharedHomeworks.convertToDetails(
                        recipientsMapper = { recipients ->
                            checkNotNull(senderAndRecipientsUsers?.filter { recipients.contains(it.uid) })
                        },
                        sendersMapper = { sender ->
                            checkNotNull(senderAndRecipientsUsers?.find { it.uid == sender })
                        },
                    )
                }
            }
        }

        override suspend fun fetchShortSharedHomeworksByUser(uid: UID): Flow<SharedHomeworksPojo> {
            require(uid.isNotBlank())

            return database.getDocumentFlow(
                databaseId = SharedHomeworks.DATABASE_ID,
                collectionId = SharedHomeworks.COLLECTION_ID,
                documentId = uid,
                nestedType = SharedHomeworksPojo.serializer(),
            ).map { item ->
                item ?: SharedHomeworksPojo.default()
            }
        }

        override suspend fun fetchRealtimeSharedHomeworksByUser(uid: UID): SharedHomeworksPojo {
            require(uid.isNotBlank())

            val sharedHomeworks = database.getDocumentOrNull(
                databaseId = SharedHomeworks.DATABASE_ID,
                collectionId = SharedHomeworks.COLLECTION_ID,
                documentId = uid,
                nestedType = SharedHomeworksPojo.serializer(),
            )

            return sharedHomeworks?.data ?: SharedHomeworksPojo.default()
        }
    }
}