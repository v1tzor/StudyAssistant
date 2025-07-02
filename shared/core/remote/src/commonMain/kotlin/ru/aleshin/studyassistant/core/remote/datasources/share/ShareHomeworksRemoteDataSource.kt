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
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.SharedHomeworks
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Users
import ru.aleshin.studyassistant.core.remote.mappers.share.convertToDetails
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
        private val database: FirebaseFirestore,
    ) : ShareHomeworksRemoteDataSource {

        override suspend fun addOrUpdateSharedHomework(homeworks: SharedHomeworksPojo, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val reference = database.collection(SharedHomeworks.ROOT).document(targetUser)

            return reference.set(homeworks)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchSharedHomeworksByUser(uid: UID): Flow<SharedHomeworksDetailsPojo> {
            require(uid.isNotBlank())

            val userRoot = database.collection(Users.ROOT)

            val reference = database.collection(SharedHomeworks.ROOT).document(uid)

            val sharedHomeworksPojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<SharedHomeworksPojo?>()) ?: SharedHomeworksPojo.default()
            }

            return sharedHomeworksPojoFlow.flatMapLatest { sharedHomeworks ->
                val users = buildList {
                    addAll(sharedHomeworks.sent.map { it.value.recipients }.extractAllItem())
                    addAll(sharedHomeworks.received.map { it.value.sender })
                }
                val senderAndRecipientsUsersReference = users.let {
                    if (users.isEmpty()) return@let null
                    userRoot.where { Users.UID inArray users }
                }
                val senderAndRecipientsUsersFlow = senderAndRecipientsUsersReference?.snapshots?.map { snapshot ->
                    snapshot.documents.map { it.data(serializer<AppUserPojo>()) }
                } ?: flowOf(null)

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

            val reference = database.collection(SharedHomeworks.ROOT).document(uid)

            return reference.snapshots.map { snapshot ->
                snapshot.data(serializer<SharedHomeworksPojo?>()) ?: SharedHomeworksPojo.default()
            }
        }

        override suspend fun fetchRealtimeSharedHomeworksByUser(uid: UID): SharedHomeworksPojo {
            require(uid.isNotBlank())

            val reference = database.collection(SharedHomeworks.ROOT).document(uid)

            val sharedHomeworks = reference.get(Source.SERVER).data(serializer<SharedHomeworksPojo?>())

            return sharedHomeworks ?: SharedHomeworksPojo.default()
        }
    }
}