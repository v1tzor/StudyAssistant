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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.format.DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.api.AppwriteApi.Common.UPDATED_AT
import ru.aleshin.studyassistant.core.api.AppwriteApi.Requests
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
import ru.aleshin.studyassistant.core.remote.mappers.users.convertToDetails
import ru.aleshin.studyassistant.core.remote.mappers.users.mapToBase
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsPojo
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojo
import ru.aleshin.studyassistant.core.remote.utils.DatabaseEvent
import ru.aleshin.studyassistant.core.remote.utils.RemoteDataSource

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
interface FriendRequestsRemoteDataSource : RemoteDataSource.FullSynced.SingleDocument<FriendRequestsDetailsPojo> {

    suspend fun addOrUpdateRequestsForUser(requests: FriendRequestsPojo, targetUser: UID)
    suspend fun fetchRequestsDetailsByUser(targetUser: UID): Flow<FriendRequestsDetailsPojo>
    suspend fun fetchShortRequestsByUser(targetUser: UID): Flow<FriendRequestsPojo>
    suspend fun fetchRealtimeShortRequestsByUser(targetUser: UID): FriendRequestsPojo

    class Base(
        private val database: DatabaseService,
        private val realtime: RealtimeService,
        private val userSessionProvider: UserSessionProvider
    ) : FriendRequestsRemoteDataSource {

        private val databaseId = Requests.DATABASE_ID

        private val collectionId = Requests.COLLECTION_ID

        override suspend fun addOrUpdateItem(item: FriendRequestsDetailsPojo) {
            val currentUser = userSessionProvider.getCurrentUserId()

            database.upsertDocument(
                databaseId = Requests.DATABASE_ID,
                collectionId = Requests.COLLECTION_ID,
                documentId = currentUser,
                data = item.mapToBase(),
                permissions = listOf(
                    Permission.read(Role.users()),
                    Permission.update(Role.users()),
                ),
                nestedType = FriendRequestsPojo.serializer(),
            )
        }

        override suspend fun addOrUpdateRequestsForUser(requests: FriendRequestsPojo, targetUser: UID) {
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

        override suspend fun fetchItem(): Flow<FriendRequestsDetailsPojo> {
            val currentUser = userSessionProvider.getCurrentUserId()
            return fetchRequestsDetailsByUser(currentUser)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchRequestsDetailsByUser(targetUser: UID): Flow<FriendRequestsDetailsPojo> {
            require(targetUser.isNotBlank())

            val requestsFlow = database.getDocumentFlow(
                databaseId = Requests.DATABASE_ID,
                collectionId = Requests.COLLECTION_ID,
                documentId = targetUser,
                nestedType = FriendRequestsPojo.serializer(),
            ).map {
                it?.data ?: FriendRequestsPojo.default(targetUser)
            }

            return requestsFlow.flatMapToDetails()
        }

        override suspend fun fetchShortRequestsByUser(targetUser: UID): Flow<FriendRequestsPojo> {
            require(targetUser.isNotBlank())

            val requestsFlow = database.getDocumentFlow(
                databaseId = Requests.DATABASE_ID,
                collectionId = Requests.COLLECTION_ID,
                documentId = targetUser,
                nestedType = FriendRequestsPojo.serializer(),
            ).map { requests ->
                requests?.data ?: FriendRequestsPojo.default(targetUser)
            }

            return requestsFlow
        }

        override suspend fun fetchRealtimeShortRequestsByUser(targetUser: UID): FriendRequestsPojo {
            require(targetUser.isNotBlank())

            val requests = database.getDocumentOrNull(
                databaseId = Requests.DATABASE_ID,
                collectionId = Requests.COLLECTION_ID,
                documentId = targetUser,
                nestedType = FriendRequestsPojo.serializer(),
            )

            return requests?.data ?: FriendRequestsPojo.default(targetUser)
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

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun observeEvents(): Flow<DatabaseEvent<FriendRequestsDetailsPojo>> {
            val currentUser = userSessionProvider.getCurrentUserId()

            return realtime.subscribe(
                channels = Channels.document(databaseId, collectionId, currentUser),
                payloadType = FriendRequestsPojo.serializer(),
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
                    val payload = checkNotNull(flowOf(response.payload).flatMapToDetails().first())
                    DatabaseEvent.Create(data = payload, documentId = currentUser)
                } else if (response.events.any { it.contains("delete") }) {
                    DatabaseEvent.Delete(documentId = currentUser)
                } else {
                    val payload = checkNotNull(flowOf(response.payload).flatMapToDetails().first())
                    DatabaseEvent.Update(data = payload, documentId = currentUser)
                }
            }
        }

        @ExperimentalCoroutinesApi
        private fun Flow<FriendRequestsPojo>.flatMapToDetails() = flatMapLatest { rawRequest ->
            val receivedMap = rawRequest.received.decodeFromString<UID, Long>()
            val sendMap = rawRequest.send.decodeFromString<UID, Long>()
            val lastActionsMap = rawRequest.lastActions.decodeFromString<UID, Boolean>()
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
                    users.map { it.data.convertToDetails() }
                }
            } ?: flowOf(null)

            targetUsersFlow.map { targetUsers ->
                return@map FriendRequestsDetailsPojo(
                    id = rawRequest.id,
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
                    updatedAt = rawRequest.updatedAt,
                )
            }
        }
    }
}