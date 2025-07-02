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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.snapshotListFlowGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Requests
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Users
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
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val reference = database.collection(Requests.ROOT).document(targetUser)

            return reference.set(requests)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchRequestsByUser(uid: UID): Flow<FriendRequestsDetailsPojo> {
            require(uid.isNotBlank())

            val userRoot = database.collection(Users.ROOT)

            val reference = database.collection(Requests.ROOT).document(uid)

            val requestsFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<FriendRequestsPojo?>()) ?: FriendRequestsPojo.default()
            }

            return requestsFlow.flatMapLatest { request ->
                val users = buildSet {
                    addAll(request.received.keys.toList())
                    addAll(request.send.keys.toList())
                    addAll(request.lastActions.keys.toList())
                }
                val targetUsersReference = users.let {
                    if (users.isEmpty()) return@let null
                    userRoot.where { Users.UID inArray users.toList() }
                }

                val targetUsersFlow = targetUsersReference?.snapshotListFlowGet<AppUserPojo>() ?: flowOf(null)

                targetUsersFlow.map { targetUsers ->
                    return@map FriendRequestsDetailsPojo(
                        received = targetUsers?.filter { user ->
                            request.received.containsKey(user.uid)
                        }?.associate { user ->
                            Pair(user, request.received[user.uid] ?: 0L)
                        } ?: emptyMap(),
                        send = targetUsers?.filter { user ->
                            request.send.containsKey(user.uid)
                        }?.associate { user ->
                            Pair(user, request.send[user.uid] ?: 0L)
                        } ?: emptyMap(),
                        lastActions = targetUsers?.filter { user ->
                            request.lastActions.containsKey(user.uid)
                        }?.associate { user ->
                            Pair(user, request.lastActions[user.uid] ?: false)
                        } ?: emptyMap(),
                    )
                }
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

            val friendRequests = reference.get(Source.SERVER).data(serializer<FriendRequestsPojo?>())

            return friendRequests ?: FriendRequestsPojo.default()
        }
    }
}