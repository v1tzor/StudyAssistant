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

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.snapshotGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirestore.Requests
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirestore.Users
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
        private val database: FirebaseFirestore,
    ) : FriendRequestsRemoteDataSource {

        override suspend fun addOrUpdateRequests(requests: FriendRequestsPojo, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()

            val reference = database.collection(Requests.ROOT).document(targetUser)

            return reference.set(requests)
        }

        override suspend fun fetchRequestsByUser(uid: UID): Flow<FriendRequestsDetailsPojo> {
            require(uid.isNotBlank())

            val reference = database.collection(Requests.ROOT).document(uid)

            val requestsPojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<FriendRequestsPojo?>()) ?: FriendRequestsPojo.default()
            }

            return requestsPojoFlow.map { request ->
                val userRoot = database.collection(Users.ROOT)

                val receivedUsersReference = request.received.keys.toList().let { received ->
                    if (received.isEmpty()) return@let null
                    userRoot.where { Users.UID inArray received }
                }
                val sendUsersReference = request.send.keys.toList().let { send ->
                    if (send.isEmpty()) return@let null
                    userRoot.where { Users.UID inArray send }
                }
                val actionsUsersReference = request.lastActions.keys.toList().let { lastActions ->
                    if (lastActions.isEmpty()) return@let null
                    userRoot.where { Users.UID inArray lastActions }
                }

                val receivedUsers = receivedUsersReference?.snapshotGet()?.map { snapshot ->
                    snapshot.data(serializer<AppUserPojo>())
                }
                val sendUsers = sendUsersReference?.snapshotGet()?.map { snapshot ->
                    snapshot.data(serializer<AppUserPojo>())
                }
                val actionsUsers = actionsUsersReference?.snapshotGet()?.map { snapshot ->
                    snapshot.data(serializer<AppUserPojo>())
                }

                return@map FriendRequestsDetailsPojo(
                    received = request.received.mapKeys { entry ->
                        checkNotNull(receivedUsers?.find { it.uid == entry.key })
                    },
                    send = request.send.mapKeys { entry ->
                        checkNotNull(sendUsers?.find { it.uid == entry.key })
                    },
                    lastActions = request.lastActions.mapKeys { entry ->
                        checkNotNull(actionsUsers?.find { it.uid == entry.key })
                    },
                )
            }
        }

        override suspend fun fetchShortRequestsByUser(uid: UID): Flow<FriendRequestsPojo> {
            require(uid.isNotBlank())

            val reference = database.collection(Requests.ROOT).document(uid)

            return reference.snapshots.map { snapshot ->
                snapshot.data(serializer<FriendRequestsPojo?>()) ?: FriendRequestsPojo.default()
            }
        }

        override suspend fun fetchRealtimeShortRequestsByUser(uid: UID): FriendRequestsPojo {
            require(uid.isNotBlank())

            val reference = database.collection(Requests.ROOT).document(uid)

            return reference.get(Source.SERVER).data(serializer<FriendRequestsPojo?>()) ?: FriendRequestsPojo.default()
        }
    }
}