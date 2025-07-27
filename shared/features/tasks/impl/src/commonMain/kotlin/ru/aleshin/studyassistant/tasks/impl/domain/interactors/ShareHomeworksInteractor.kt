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

package ru.aleshin.studyassistant.tasks.impl.domain.interactors

import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.message.NotifyPushContent
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.ReceivedMediatedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SentMediatedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SentMediatedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SharedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.convertToBase
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.convertToReceived
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareHomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
internal interface ShareHomeworksInteractor {

    suspend fun fetchSharedHomeworksDetails(): FlowDomainResult<TasksFailures, SharedHomeworksDetails>
    suspend fun shareHomeworks(homeworks: SentMediatedHomeworksDetails): UnitDomainResult<TasksFailures>
    suspend fun cancelSendHomeworks(homeworks: SentMediatedHomeworks): UnitDomainResult<TasksFailures>
    suspend fun acceptOrRejectHomeworks(homeworks: ReceivedMediatedHomeworks): UnitDomainResult<TasksFailures>

    class Base(
        private val shareRepository: ShareHomeworksRepository,
        private val usersRepository: UsersRepository,
        private val messageRepository: MessageRepository,
        private val connectionManager: Konnection,
        private val eitherWrapper: TasksEitherWrapper,
    ) : ShareHomeworksInteractor {

        override suspend fun fetchSharedHomeworksDetails() = eitherWrapper.wrapFlow {
            shareRepository.fetchCurrentSharedHomeworksDetails()
        }

        override suspend fun shareHomeworks(homeworks: SentMediatedHomeworksDetails) = eitherWrapper.wrapUnit {
            if (!connectionManager.isConnected()) throw InternetConnectionException()

            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            val targetUserInfo = usersRepository.fetchCurrentUserProfile().first()
            val currentSharedHomeworks = shareRepository.fetchRealtimeSharedHomeworksByUser(currentUser)
            val updatedCurrentSharedHomeworks = currentSharedHomeworks.copy(
                sent = buildMap {
                    putAll(currentSharedHomeworks.sent)
                    put(homeworks.uid, homeworks.convertToBase())
                }
            )
            shareRepository.addOrUpdateCurrentSharedHomework(updatedCurrentSharedHomeworks)

            homeworks.recipients.forEach { recipient ->
                val recipientSharedHomeworks = shareRepository.fetchRealtimeSharedHomeworksByUser(recipient.uid)
                val updatedRecipientSharedHomeworks = recipientSharedHomeworks.copy(
                    received = buildMap {
                        putAll(recipientSharedHomeworks.received)
                        put(homeworks.uid, homeworks.convertToBase().convertToReceived(currentUser))
                    }
                )
                shareRepository.addOrUpdateSharedHomeworkForUser(updatedRecipientSharedHomeworks, recipient.uid)
            }

            val notifyContent = NotifyPushContent.ShareHomework(
                devices = homeworks.recipients.map { it.devices }.extractAllItem(),
                senderUsername = checkNotNull(targetUserInfo).username,
                senderUserId = currentUser,
                subjectNames = homeworks.homeworks.map { it.subjectName }
            )
            messageRepository.sendMessage(notifyContent.toMessageBody())
        }

        override suspend fun cancelSendHomeworks(homeworks: SentMediatedHomeworks) = eitherWrapper.wrapUnit {
            if (!connectionManager.isConnected()) throw InternetConnectionException()

            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            val currentSharedHomeworks = shareRepository.fetchRealtimeSharedHomeworksByUser(currentUser)
            val updatedCurrentSharedHomeworks = currentSharedHomeworks.copy(
                sent = buildMap {
                    putAll(currentSharedHomeworks.sent)
                    remove(homeworks.uid)
                }
            )
            shareRepository.addOrUpdateCurrentSharedHomework(updatedCurrentSharedHomeworks)

            homeworks.recipients.forEach { recipient ->
                val recipientSharedHomeworks = shareRepository.fetchRealtimeSharedHomeworksByUser(recipient)
                val updatedRecipientSharedHomeworks = recipientSharedHomeworks.copy(
                    received = buildMap {
                        putAll(recipientSharedHomeworks.received)
                        remove(homeworks.uid)
                    }
                )
                shareRepository.addOrUpdateSharedHomeworkForUser(updatedRecipientSharedHomeworks, recipient)
            }
        }

        override suspend fun acceptOrRejectHomeworks(homeworks: ReceivedMediatedHomeworks) = eitherWrapper.wrapUnit {
            if (!connectionManager.isConnected()) throw InternetConnectionException()

            val currentUser = usersRepository.fetchCurrentUserOrError().uid
            val currentSharedHomeworks = shareRepository.fetchRealtimeSharedHomeworksByUser(currentUser)
            val senderSharedHomeworks = shareRepository.fetchRealtimeSharedHomeworksByUser(homeworks.sender)

            val updatedCurrentSharedHomeworks = currentSharedHomeworks.copy(
                received = buildMap {
                    putAll(currentSharedHomeworks.received)
                    remove(homeworks.uid)
                }
            )
            val updatedSenderSharedHomeworks = senderSharedHomeworks.copy(
                sent = buildMap {
                    putAll(senderSharedHomeworks.sent)
                    remove(homeworks.uid)
                }
            )

            shareRepository.addOrUpdateCurrentSharedHomework(updatedCurrentSharedHomeworks)
            shareRepository.addOrUpdateSharedHomeworkForUser(updatedSenderSharedHomeworks, homeworks.sender)
        }
    }
}