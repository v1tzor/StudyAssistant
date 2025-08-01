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

package ru.aleshin.studyassistant.users.impl.domain.interactors

import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.UserFriendStatus
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareHomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.users.impl.domain.common.UsersEitherWrapper
import ru.aleshin.studyassistant.users.impl.domain.entities.UsersFailures

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
internal interface UsersInteractor {

    suspend fun fetchUserById(userId: UID): FlowDomainResult<UsersFailures, AppUser>
    suspend fun fetchUserFriendStatus(userId: UID): FlowDomainResult<UsersFailures, UserFriendStatus>
    suspend fun fetchAllFriends(): FlowDomainResult<UsersFailures, List<AppUser>>
    suspend fun findUsersByCode(code: String): FlowDomainResult<UsersFailures, List<AppUser>>
    suspend fun addUserToFriends(userId: UID): UnitDomainResult<UsersFailures>
    suspend fun removeUserFromFriends(userId: UID): UnitDomainResult<UsersFailures>

    class Base(
        private val usersRepository: UsersRepository,
        private val shareSchedulesRepository: ShareSchedulesRepository,
        private val shareHomeworksRepository: ShareHomeworksRepository,
        private val requestsRepository: FriendRequestsRepository,
        private val dateManager: DateManager,
        private val connectionManager: Konnection,
        private val eitherWrapper: UsersEitherWrapper,
    ) : UsersInteractor {

        override suspend fun fetchUserById(userId: UID) = eitherWrapper.wrapFlow {
            usersRepository.fetchUserProfileById(userId).filterNotNull()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchUserFriendStatus(userId: UID) = eitherWrapper.wrapFlow {
            val currentUserInfo = usersRepository.fetchCurrentUserProfile()
            val userFriendRequests = requestsRepository.fetchCurrentRequests()

            currentUserInfo.flatMapLatest { userInfo ->
                userFriendRequests.map { friendRequests ->
                    val isFriend = userInfo?.friends?.contains(userId) ?: false
                    val isSendRequest = friendRequests.send.containsKey(userId)
                    val isReceiveRequest = friendRequests.received.containsKey(userId)

                    return@map if (isFriend) {
                        UserFriendStatus.IN_FRIENDS
                    } else if (isReceiveRequest) {
                        UserFriendStatus.REQUEST_RECEIVE
                    } else if (isSendRequest) {
                        UserFriendStatus.REQUEST_SENT
                    } else {
                        UserFriendStatus.NOT_FRIENDS
                    }
                }
            }
        }

        override suspend fun fetchAllFriends() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserFriends()
        }

        override suspend fun findUsersByCode(code: String) = eitherWrapper.wrapFlow {
            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            usersRepository.findUsersByCode(code).filter { users ->
                users.find { it.uid == currentUser } == null
            }
        }

        override suspend fun addUserToFriends(userId: UID) = eitherWrapper.wrapUnit {
            if (!connectionManager.isConnected()) throw InternetConnectionException()

            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            val currentUserInfo = usersRepository.fetchRealtimeUserById(currentUser)
            val targetUserInfo = usersRepository.fetchRealtimeUserById(userId)

            val updatedCurrentUser = checkNotNull(currentUserInfo).copy(
                updatedAt = updatedAt,
                friends = buildList {
                    addAll(currentUserInfo.friends)
                    add(userId)
                }
            )
            val updatedTargetUser = checkNotNull(targetUserInfo).copy(
                updatedAt = updatedAt,
                friends = buildList {
                    addAll(targetUserInfo.friends)
                    add(currentUser)
                }
            )
            usersRepository.updateCurrentUserProfile(updatedCurrentUser)
            usersRepository.updateAnotherUserProfile(updatedTargetUser, userId)
        }

        override suspend fun removeUserFromFriends(userId: UID) = eitherWrapper.wrapUnit {
            if (!connectionManager.isConnected()) throw InternetConnectionException()

            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            shareSchedulesRepository.fetchRealtimeSharedSchedulesByUser(currentUser).apply {
                val updatedCurrentSharedSchedules = copy(
                    updatedAt = updatedAt,
                    received = received.filter { it.value.sender.uid != userId },
                    sent = sent.filter { it.value.recipient.uid != userId },
                )
                shareSchedulesRepository.addOrUpdateCurrentSharedSchedules(updatedCurrentSharedSchedules)
            }

            shareSchedulesRepository.fetchRealtimeSharedSchedulesByUser(userId).apply {
                val updatedSenderSharedSchedules = copy(
                    updatedAt = updatedAt,
                    received = received.filter { it.value.sender.uid != currentUser },
                    sent = sent.filter { it.value.recipient.uid != currentUser },
                )
                shareSchedulesRepository.addOrUpdateSharedSchedulesForUser(updatedSenderSharedSchedules, userId)
            }

            shareHomeworksRepository.fetchRealtimeSharedHomeworksByUser(currentUser).apply {
                val updatedCurrentSharedHomeworks = copy(
                    updatedAt = updatedAt,
                    received = received.filter { it.value.sender != userId },
                    sent = sent.filter { entry ->
                        (entry.value.recipients.size == 1 && entry.value.recipients.contains(userId)).not()
                    }.mapValues { entry ->
                        entry.value.copy(recipients = entry.value.recipients.filter { it != userId })
                    }
                )
                shareHomeworksRepository.addOrUpdateCurrentSharedHomework(updatedCurrentSharedHomeworks)
            }

            shareHomeworksRepository.fetchRealtimeSharedHomeworksByUser(userId).apply {
                val updatedSenderSharedHomeworks = copy(
                    updatedAt = updatedAt,
                    received = received.filter { it.value.sender != currentUser },
                    sent = sent.filter { entry ->
                        (entry.value.recipients.size == 1 && entry.value.recipients.contains(currentUser)).not()
                    }.mapValues { entry ->
                        entry.value.copy(recipients = entry.value.recipients.filter { it != currentUser })
                    },
                )
                shareHomeworksRepository.addOrUpdateSharedHomeworkForUser(updatedSenderSharedHomeworks, userId)
            }

            val currentUserInfo = usersRepository.fetchRealtimeUserById(currentUser)
            val targetUserInfo = usersRepository.fetchRealtimeUserById(userId)

            val updatedCurrentUser = checkNotNull(currentUserInfo).copy(
                updatedAt = updatedAt,
                friends = buildList {
                    addAll(currentUserInfo.friends)
                    remove(userId)
                }
            )
            val updatedTargetUser = checkNotNull(targetUserInfo).copy(
                updatedAt = updatedAt,
                friends = buildList {
                    addAll(targetUserInfo.friends)
                    remove(currentUser)
                }
            )
            usersRepository.updateCurrentUserProfile(updatedCurrentUser)
            usersRepository.updateAnotherUserProfile(updatedTargetUser, userId)
        }
    }
}