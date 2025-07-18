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

import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.message.NotifyPushContent
import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequestsDetails
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.users.impl.domain.common.UsersEitherWrapper
import ru.aleshin.studyassistant.users.impl.domain.entities.UsersFailures

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
internal interface FriendRequestsInteractor {

    suspend fun fetchUserFriendRequests(): FlowDomainResult<UsersFailures, FriendRequestsDetails>
    suspend fun sendRequest(userId: UID): UnitDomainResult<UsersFailures>
    suspend fun cancelSendRequest(userId: UID): UnitDomainResult<UsersFailures>
    suspend fun acceptRequest(userId: UID): UnitDomainResult<UsersFailures>
    suspend fun rejectRequest(userId: UID): UnitDomainResult<UsersFailures>
    suspend fun deleteHistoryRequestByUser(userId: UID): UnitDomainResult<UsersFailures>

    class Base(
        private val requestsRepository: FriendRequestsRepository,
        private val usersRepository: UsersRepository,
        private val messageRepository: MessageRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: UsersEitherWrapper,
    ) : FriendRequestsInteractor {

        override suspend fun fetchUserFriendRequests() = eitherWrapper.wrapFlow {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            requestsRepository.fetchRequestsByUser(targetUser)
        }

        override suspend fun sendRequest(userId: UID) = eitherWrapper.wrapUnit {
            val currentInstant = dateManager.fetchCurrentInstant()

            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            val currentUserRequests = requestsRepository.fetchRealtimeShortRequestsByUser(currentUser)
            val targetUserRequests = requestsRepository.fetchRealtimeShortRequestsByUser(userId)
            val currentUserInfo = usersRepository.fetchRealtimeUserById(currentUser)
            val targetUserInfo = usersRepository.fetchRealtimeUserById(userId)

            val updatedCurrentRequests = currentUserRequests.copy(
                send = buildMap {
                    putAll(currentUserRequests.send)
                    put(userId, currentInstant)
                },
            )
            val updatedTargetRequests = targetUserRequests.copy(
                received = buildMap {
                    putAll(targetUserRequests.received)
                    put(currentUser, currentInstant)
                },
            )

            requestsRepository.addOrUpdateRequests(updatedCurrentRequests, currentUser)
            requestsRepository.addOrUpdateRequests(updatedTargetRequests, userId)

            val notifyContent = NotifyPushContent.AddToFriends(
                devices = checkNotNull(targetUserInfo?.devices),
                senderUsername = checkNotNull(currentUserInfo).username,
                senderUserId = currentUser,
            )
            messageRepository.sendMessage(notifyContent.toMessageBody())
        }

        override suspend fun cancelSendRequest(userId: UID) = eitherWrapper.wrapUnit {
            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            val currentUserRequests = requestsRepository.fetchRealtimeShortRequestsByUser(currentUser)
            val targetUserRequests = requestsRepository.fetchRealtimeShortRequestsByUser(userId)

            val updatedCurrentRequests = currentUserRequests.copy(
                send = buildMap {
                    putAll(currentUserRequests.send)
                    remove(userId)
                },
            )
            val updatedTargetRequests = targetUserRequests.copy(
                received = buildMap {
                    putAll(targetUserRequests.received)
                    remove(currentUser)
                },
            )

            requestsRepository.addOrUpdateRequests(updatedCurrentRequests, currentUser)
            requestsRepository.addOrUpdateRequests(updatedTargetRequests, userId)
        }

        override suspend fun acceptRequest(userId: UID) = eitherWrapper.wrapUnit {
            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            val currentUserRequests = requestsRepository.fetchRealtimeShortRequestsByUser(currentUser)
            val targetUserRequests = requestsRepository.fetchRealtimeShortRequestsByUser(userId)
            val currentUserInfo = usersRepository.fetchRealtimeUserById(currentUser)
            val targetUserInfo = usersRepository.fetchRealtimeUserById(userId)

            val updatedCurrentRequests = currentUserRequests.copy(
                received = buildMap {
                    putAll(currentUserRequests.received)
                    remove(userId)
                },
                lastActions = buildMap {
                    putAll(currentUserRequests.lastActions)
                    put(userId, true)
                }
            )
            val updatedTargetRequests = targetUserRequests.copy(
                send = buildMap {
                    putAll(targetUserRequests.send)
                    remove(currentUser)
                },
                lastActions = buildMap {
                    putAll(targetUserRequests.lastActions)
                    put(currentUser, true)
                }
            )

            requestsRepository.addOrUpdateRequests(updatedCurrentRequests, currentUser)
            requestsRepository.addOrUpdateRequests(updatedTargetRequests, userId)

            val notifyContent = NotifyPushContent.AcceptFriendRequest(
                devices = checkNotNull(targetUserInfo?.devices),
                senderUsername = checkNotNull(currentUserInfo).username,
                senderUserId = currentUser,
            )

            messageRepository.sendMessage(notifyContent.toMessageBody())
        }

        override suspend fun rejectRequest(userId: UID) = eitherWrapper.wrapUnit {
            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            val currentUserRequests = requestsRepository.fetchRealtimeShortRequestsByUser(currentUser)
            val targetUserRequests = requestsRepository.fetchRealtimeShortRequestsByUser(userId)
            val currentUserInfo = usersRepository.fetchRealtimeUserById(currentUser)
            val targetUserInfo = usersRepository.fetchRealtimeUserById(userId)

            val updatedCurrentRequests = currentUserRequests.copy(
                received = buildMap {
                    putAll(currentUserRequests.received)
                    remove(userId)
                },
                lastActions = buildMap {
                    putAll(currentUserRequests.lastActions)
                    put(userId, false)
                }
            )
            val updatedTargetRequests = targetUserRequests.copy(
                send = buildMap {
                    putAll(targetUserRequests.send)
                    remove(currentUser)
                },
                lastActions = buildMap {
                    putAll(targetUserRequests.lastActions)
                    put(currentUser, false)
                }
            )

            requestsRepository.addOrUpdateRequests(updatedCurrentRequests, currentUser)
            requestsRepository.addOrUpdateRequests(updatedTargetRequests, userId)

            val notifyContent = NotifyPushContent.RejectFriendRequest(
                devices = checkNotNull(targetUserInfo?.devices),
                senderUsername = checkNotNull(currentUserInfo).username,
                senderUserId = currentUser,
            )

            messageRepository.sendMessage(notifyContent.toMessageBody())
        }

        override suspend fun deleteHistoryRequestByUser(userId: UID) = eitherWrapper.wrapUnit {
            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            val userRequests = requestsRepository.fetchRealtimeShortRequestsByUser(currentUser)
            val updatedUserRequests = userRequests.copy(
                lastActions = buildMap {
                    putAll(userRequests.lastActions)
                    remove(userId)
                }
            )
            requestsRepository.addOrUpdateRequests(updatedUserRequests, currentUser)
        }
    }
}