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

package ru.aleshin.studyassistant.profile.impl.domain.interactors

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.message.NotifyPushContent
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToMediate
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.convertToMediate
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.ReceivedMediatedSchedules
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SentMediatedSchedules
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.SharedSchedulesShort
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.profile.impl.domain.common.ProfileEitherWrapper
import ru.aleshin.studyassistant.profile.impl.domain.entities.ProfileFailures
import ru.aleshin.studyassistant.profile.impl.domain.entities.ShareSchedulesSendData

/**
 * @author Stanislav Aleshin on 14.08.2024.
 */
internal interface ShareSchedulesInteractor {

    suspend fun fetchShortSharedSchedules(): FlowDomainResult<ProfileFailures, SharedSchedulesShort>
    suspend fun shareSchedules(sendData: ShareSchedulesSendData): UnitDomainResult<ProfileFailures>
    suspend fun cancelSendSchedules(schedules: SentMediatedSchedules): UnitDomainResult<ProfileFailures>

    class Base(
        private val shareRepository: ShareSchedulesRepository,
        private val organizationRepository: OrganizationsRepository,
        private val baseSchedulesRepository: BaseScheduleRepository,
        private val usersRepository: UsersRepository,
        private val messageRepository: MessageRepository,
        private val dataManager: DateManager,
        private val eitherWrapper: ProfileEitherWrapper,
    ) : ShareSchedulesInteractor {

        override suspend fun fetchShortSharedSchedules() = eitherWrapper.wrapFlow {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            shareRepository.fetchShortSharedSchedulesByUser(targetUser)
        }

        override suspend fun shareSchedules(sendData: ShareSchedulesSendData) = eitherWrapper.wrapUnit {
            val shareId = randomUUID()
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            val currentTime = dataManager.fetchCurrentInstant()
            val targetUserInfo = usersRepository.fetchUserById(targetUser).first()
            val schedules = baseSchedulesRepository.fetchSchedulesByVersion(
                version = dataManager.fetchCurrentWeek(),
                numberOfWeek = null,
                targetUser = targetUser,
            ).let { schedulesFlow ->
                val actualVersion = DateVersion.createNewVersion(currentTime)
                val baseSchedules = schedulesFlow.first()
                val mediatedSchedules = baseSchedules.map { schedule ->
                    val filteredClasses = schedule.classes.filter { classModel ->
                        sendData.organizations.contains(classModel.organization.uid)
                    }
                    schedule.copy(classes = filteredClasses, dateVersion = actualVersion).convertToMediate()
                }.filter { mediatedSchedule ->
                    mediatedSchedule.classes.isNotEmpty()
                }
                return@let mediatedSchedules
            }
            val organizationsData = organizationRepository.fetchOrganizationsById(
                uid = sendData.organizations,
                targetUser = targetUser,
            ).let { organizationsFlow ->
                val organizations = organizationsFlow.first()
                val allClasses = schedules.map { it.classes }.extractAllItem()
                val mediatedOrganizations = organizations.map { organization ->
                    val filteredSubjects = organization.subjects.filter { subject ->
                        if (sendData.sendAllSubjects) return@filter true
                        return@filter allClasses.find { it.subjectId == subject.uid } != null
                    }
                    val filteredEmployees = organization.employee.filter { employee ->
                        if (sendData.sendAllEmployee) return@filter true
                        val inClassesUsed = allClasses.find { it.teacherId == employee.uid } != null
                        val inSubjectsUsed = filteredSubjects.find { subject ->
                            subject.teacher?.uid == employee.uid
                        } != null
                        return@filter inClassesUsed || inSubjectsUsed
                    }
                    val targetOrganization = organization.copy(
                        subjects = filteredSubjects,
                        employee = filteredEmployees,
                    )
                    return@map targetOrganization.convertToMediate()
                }
                return@let mediatedOrganizations
            }

            val sentMediatedSchedules = SentMediatedSchedules(
                uid = shareId,
                sendDate = currentTime,
                recipient = sendData.recipient,
                organizationNames = organizationsData.map { it.shortName },
            )

            val receivedMediatedSchedules = ReceivedMediatedSchedules(
                uid = shareId,
                sendDate = currentTime,
                sender = checkNotNull(targetUserInfo),
                schedules = schedules,
                organizationsData = organizationsData,
            )

            val currentSharedSchedules = shareRepository.fetchRealtimeSharedSchedulesByUser(targetUser)
            val recipientSharedSchedules = shareRepository.fetchRealtimeSharedSchedulesByUser(sendData.recipient.uid)

            val updatedCurrentSharedSchedules = currentSharedSchedules.copy(
                sent = buildMap {
                    putAll(currentSharedSchedules.sent)
                    put(shareId, sentMediatedSchedules)
                }
            )
            val updatedRecipientSharedSchedules = recipientSharedSchedules.copy(
                received = buildMap {
                    putAll(recipientSharedSchedules.received)
                    put(shareId, receivedMediatedSchedules)
                }
            )

            shareRepository.addOrUpdateSharedSchedules(updatedCurrentSharedSchedules, targetUser)
            shareRepository.addOrUpdateSharedSchedules(updatedRecipientSharedSchedules, sendData.recipient.uid)

            val notifyContent = NotifyPushContent.ShareSchedule(
                devices = sendData.recipient.devices,
                senderUsername = targetUserInfo.username,
                senderUserId = targetUser,
            )
            messageRepository.sendMessage(notifyContent.toMessageBody())
        }

        override suspend fun cancelSendSchedules(schedules: SentMediatedSchedules) = eitherWrapper.wrapUnit {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            val currentSharedSchedules = shareRepository.fetchRealtimeSharedSchedulesByUser(targetUser)
            val updatedCurrentSharedSchedules = currentSharedSchedules.copy(
                sent = buildMap {
                    putAll(currentSharedSchedules.sent)
                    remove(schedules.uid)
                }
            )
            shareRepository.addOrUpdateSharedSchedules(updatedCurrentSharedSchedules, targetUser)

            val recipientSharedSchedules = shareRepository.fetchRealtimeSharedSchedulesByUser(
                uid = schedules.recipient.uid,
            )
            val updatedRecipientSharedSchedules = recipientSharedSchedules.copy(
                received = buildMap {
                    putAll(recipientSharedSchedules.received)
                    remove(schedules.uid)
                }
            )
            shareRepository.addOrUpdateSharedSchedules(updatedRecipientSharedSchedules, schedules.recipient.uid)
        }
    }
}