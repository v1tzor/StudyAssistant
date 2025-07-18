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

package ru.aleshin.studyassistant.core.remote.datasources.requests

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
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Requests
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Users
import ru.aleshin.studyassistant.core.remote.mappers.users.convertToDetails
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
interface FriendRequestsRemoteDataSource {

    suspend fun addOrUpdateRequests(requests: FriendRequestsPojo, targetUser: UID)
    suspend fun fetchRequestsByUser(uid: UID): Flow<FriendRequestsDetailsPojo>
    suspend fun fetchShortRequestsByUser(uid: UID): Flow<FriendRequestsPojo>
    suspend fun fetchRealtimeShortRequestsByUser(uid: UID): FriendRequestsPojo

    class Base(
        private val database: DatabaseService,
    ) : FriendRequestsRemoteDataSource {

        override suspend fun addOrUpdateRequests(requests: FriendRequestsPojo, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            database.upsertDocument(
                databaseId = Requests.DATABASE_ID,
                collectionId = Requests.COLLECTION_ID,
                documentId = targetUser,
                data = requests,
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.users()),
                ),
                nestedType = FriendRequestsPojo.serializer(),
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchRequestsByUser(uid: UID): Flow<FriendRequestsDetailsPojo> {
            require(uid.isNotBlank())

            val requestsFlow = database.getDocumentFlow(
                databaseId = Requests.DATABASE_ID,
                collectionId = Requests.COLLECTION_ID,
                documentId = uid,
                nestedType = FriendRequestsPojo.serializer(),
            ).map {
                it ?: FriendRequestsPojo.default()
            }

            return requestsFlow.flatMapLatest { rawRequest ->
                val receivedMap = rawRequest.received.decodeFromString<Long>()
                val sendMap = rawRequest.send.decodeFromString<Long>()
                val lastActionsMap = rawRequest.lastActions.decodeFromString<Boolean>()
                val users = buildSet {
                    addAll(receivedMap.keys.toList())
                    addAll(sendMap.keys.toList())
                    addAll(lastActionsMap.keys.toList())
                }

                val targetUsersFlow = users.toList().takeIf { it.isNotEmpty() }?.let { users ->
                    database.listDocumentsFlow(
                        databaseId = Users.DATABASE_ID,
                        collectionId = Users.COLLECTION_ID,
                        queries = listOf(Query.equal(Users.UID, users)),
                        nestedType = AppUserPojo.serializer(),
                    ).map { users ->
                        users.map { it.convertToDetails() }
                    }
                } ?: flowOf(null)

                targetUsersFlow.map { targetUsers ->
                    return@map FriendRequestsDetailsPojo(
                        received = targetUsers?.filter { user ->
                            receivedMap.containsKey(user.uid)
                        }?.associate { user ->
                            Pair(user, receivedMap[user.uid] ?: 0L)
                        } ?: emptyMap(),
                        send = targetUsers?.filter { user ->
                            sendMap.containsKey(user.uid)
                        }?.associate { user ->
                            Pair(user, sendMap[user.uid] ?: 0L)
                        } ?: emptyMap(),
                        lastActions = targetUsers?.filter { user ->
                            lastActionsMap.containsKey(user.uid)
                        }?.associate { user ->
                            Pair(user, lastActionsMap[user.uid] ?: false)
                        } ?: emptyMap(),
                    )
                }
            }
        }

        override suspend fun fetchShortRequestsByUser(uid: UID): Flow<FriendRequestsPojo> {
            require(uid.isNotBlank())

            val requestsFlow = database.getDocumentFlow(
                databaseId = Requests.DATABASE_ID,
                collectionId = Requests.COLLECTION_ID,
                documentId = uid,
                nestedType = FriendRequestsPojo.serializer(),
            ).map { requests ->
                requests ?: FriendRequestsPojo.default()
            }

            return requestsFlow
        }

        override suspend fun fetchRealtimeShortRequestsByUser(uid: UID): FriendRequestsPojo {
            require(uid.isNotBlank())

            val requests = database.getDocumentOrNull(
                databaseId = Requests.DATABASE_ID,
                collectionId = Requests.COLLECTION_ID,
                documentId = uid,
                nestedType = FriendRequestsPojo.serializer(),
            )

            return requests?.data ?: FriendRequestsPojo.default()
        }
    }
}