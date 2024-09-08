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

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.message.NotifyPushContent
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.ReceivedMediatedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SentMediatedHomeworks
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SentMediatedHomeworksDetails
import ru.aleshin.studyassistant.core.domain.entities.share.homeworks.SharedHomeworks
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

    suspend fun fetchSharedHomeworks(): FlowDomainResult<TasksFailures, SharedHomeworks>
    suspend fun fetchSharedHomeworksDetails(): FlowDomainResult<TasksFailures, SharedHomeworksDetails>
    suspend fun shareHomeworks(homeworks: SentMediatedHomeworksDetails): UnitDomainResult<TasksFailures>
    suspend fun cancelSendHomeworks(homeworks: SentMediatedHomeworks): UnitDomainResult<TasksFailures>
    suspend fun acceptOrRejectHomeworks(homeworks: ReceivedMediatedHomeworks): UnitDomainResult<TasksFailures>

    class Base(
        private val shareRepository: ShareHomeworksRepository,
        private val usersRepository: UsersRepository,
        private val messageRepository: MessageRepository,
        private val eitherWrapper: TasksEitherWrapper,
    ) : ShareHomeworksInteractor {
        private val currentUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchSharedHomeworks() = eitherWrapper.wrapFlow {
            shareRepository.fetchShortSharedHomeworksByUser(currentUser)
        }

        override suspend fun fetchSharedHomeworksDetails() = eitherWrapper.wrapFlow {
            shareRepository.fetchSharedHomeworksByUser(currentUser)
        }

        override suspend fun shareHomeworks(homeworks: SentMediatedHomeworksDetails) = eitherWrapper.wrapUnit {
            val currentUserInfo = usersRepository.fetchUserById(currentUser).first()
            val currentSharedHomeworks = shareRepository.fetchRealtimeSharedHomeworksByUser(currentUser)
            val updatedCurrentSharedHomeworks = currentSharedHomeworks.copy(
                sent = buildMap {
                    putAll(currentSharedHomeworks.sent)
                    put(homeworks.uid, homeworks.convertToBase())
                }
            )
            shareRepository.addOrUpdateSharedHomework(updatedCurrentSharedHomeworks, currentUser)

            homeworks.recipients.forEach { recipient ->
                val recipientSharedHomeworks = shareRepository.fetchRealtimeSharedHomeworksByUser(recipient.uid)
                val updatedRecipientSharedHomeworks = recipientSharedHomeworks.copy(
                    received = buildMap {
                        putAll(recipientSharedHomeworks.received)
                        put(homeworks.uid, homeworks.convertToBase().convertToReceived(currentUser))
                    }
                )
                shareRepository.addOrUpdateSharedHomework(updatedRecipientSharedHomeworks, recipient.uid)
            }

            val notifyContent = NotifyPushContent.ShareHomework(
                devices = homeworks.recipients.map { it.devices }.extractAllItem(),
                senderUsername = checkNotNull(currentUserInfo).username,
                senderUserId = currentUser,
                subjectNames = homeworks.homeworks.map { it.subjectName }
            )
            messageRepository.sendMessage(notifyContent.toMessageBody())
        }

        override suspend fun cancelSendHomeworks(homeworks: SentMediatedHomeworks) = eitherWrapper.wrapUnit {
            val currentSharedHomeworks = shareRepository.fetchRealtimeSharedHomeworksByUser(currentUser)
            val updatedCurrentSharedHomeworks = currentSharedHomeworks.copy(
                sent = buildMap {
                    putAll(currentSharedHomeworks.sent)
                    remove(homeworks.uid)
                }
            )
            shareRepository.addOrUpdateSharedHomework(updatedCurrentSharedHomeworks, currentUser)

            homeworks.recipients.forEach { recipient ->
                val recipientSharedHomeworks = shareRepository.fetchRealtimeSharedHomeworksByUser(recipient)
                val updatedRecipientSharedHomeworks = recipientSharedHomeworks.copy(
                    received = buildMap {
                        putAll(recipientSharedHomeworks.received)
                        remove(homeworks.uid)
                    }
                )
                shareRepository.addOrUpdateSharedHomework(updatedRecipientSharedHomeworks, recipient)
            }
        }

        override suspend fun acceptOrRejectHomeworks(homeworks: ReceivedMediatedHomeworks) = eitherWrapper.wrapUnit {
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

            shareRepository.addOrUpdateSharedHomework(updatedCurrentSharedHomeworks, currentUser)
            shareRepository.addOrUpdateSharedHomework(updatedSenderSharedHomeworks, homeworks.sender)
        }
    }
}