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

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Source
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.snapshotGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.SharedSchedules
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.Users
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToDetails
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToShortDetails
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
        private val database: FirebaseFirestore,
    ) : ShareSchedulesRemoteDataSource {

        override suspend fun addOrUpdateSharedSchedules(schedules: SharedSchedulesPojo, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()

            val reference = database.collection(SharedSchedules.ROOT).document(targetUser)

            return reference.set(schedules)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchSharedSchedulesByUser(uid: UID): Flow<SharedSchedulesDetailsPojo> {
            require(uid.isNotBlank())

            val userRoot = database.collection(Users.ROOT)

            val reference = database.collection(SharedSchedules.ROOT).document(uid)

            val sharedSchedulesPojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<SharedSchedulesPojo?>()) ?: SharedSchedulesPojo.default()
            }

            return sharedSchedulesPojoFlow.flatMapLatest { sharedSchedules ->
                val users = buildList {
                    addAll(sharedSchedules.sent.map { it.value.recipient })
                    addAll(sharedSchedules.received.map { it.value.sender })
                }
                val senderAndRecipientsUsersReference = users.let {
                    if (users.isEmpty()) return@let null
                    userRoot.where { Users.UID inArray users }
                }
                val senderAndRecipientsUsersFlow = senderAndRecipientsUsersReference?.snapshots?.map { snapshot ->
                    snapshot.documents.map { it.data(serializer<AppUserPojo>()) }
                } ?: flowOf(null)

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

            val reference = database.collection(SharedSchedules.ROOT).document(uid)

            val userRoot = database.collection(Users.ROOT)

            val sharedSchedulesPojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<SharedSchedulesShortPojo?>()) ?: SharedSchedulesShortPojo.default()
            }

            return sharedSchedulesPojoFlow.flatMapLatest { sharedSchedules ->
                val users = buildList {
                    addAll(sharedSchedules.sent.map { it.value.recipient })
                    addAll(sharedSchedules.received.map { it.value.sender })
                }
                val senderAndRecipientsUsersReference = users.let {
                    if (users.isEmpty()) return@let null
                    userRoot.where { Users.UID inArray users }
                }
                val senderAndRecipientsUsersFlow = senderAndRecipientsUsersReference?.snapshots?.map { snapshot ->
                    snapshot.documents.map { it.data(serializer<AppUserPojo>()) }
                } ?: flowOf(null)

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

            val userRoot = database.collection(Users.ROOT)

            val reference = database.collection(SharedSchedules.ROOT).document(uid)

            val snapshot = reference.get(Source.SERVER)
            val sharedSchedules = snapshot.data(serializer<SharedSchedulesPojo?>()) ?: SharedSchedulesPojo.default()

            val users = buildList {
                addAll(sharedSchedules.sent.map { it.value.recipient })
                addAll(sharedSchedules.received.map { it.value.sender })
            }
            val senderAndRecipientsUsersReference = users.let {
                if (users.isEmpty()) return@let null
                userRoot.where { Users.UID inArray users }
            }
            val senderAndRecipientsUsers = senderAndRecipientsUsersReference?.snapshotGet()?.map { documentSnapshot ->
                documentSnapshot.data(serializer<AppUserPojo>())
            } ?: emptyList()

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