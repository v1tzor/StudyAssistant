/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.schedule.impl.domain.interactors

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardChallenge
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardPurpose
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.organizations.MediatedOrganization
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToMediate
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.MediatedBaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.convertToMediate
import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShare
import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShareClaim
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.domain.entities.share.ShareLink
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.repositories.AdRewardRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleShareRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleLinkResult
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleOrganizationLink
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleSharePreview

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal interface ShareSchedulesInteractor {

    suspend fun createShare(): DomainResult<ScheduleFailures, ShareLink>

    suspend fun claimShare(code: String): DomainResult<ScheduleFailures, ScheduleSharePreview>

    suspend fun releaseShare(claim: ScheduleShareClaim): UnitDomainResult<ScheduleFailures>

    suspend fun createImportReward(claim: ScheduleShareClaim): DomainResult<ScheduleFailures, AdRewardChallenge>

    suspend fun linkOrganization(
        links: List<ScheduleOrganizationLink>,
        schedules: List<MediatedBaseSchedule>,
        sharedOrganizationId: UID,
        targetOrganizationId: UID?,
    ): DomainResult<ScheduleFailures, ScheduleLinkResult>

    suspend fun updateLinkedSubjects(
        links: List<ScheduleOrganizationLink>,
        schedules: List<MediatedBaseSchedule>,
        sharedOrganizationId: UID,
        subjects: Map<UID, Subject>,
    ): DomainResult<ScheduleFailures, ScheduleLinkResult>

    suspend fun updateLinkedEmployees(
        links: List<ScheduleOrganizationLink>,
        schedules: List<MediatedBaseSchedule>,
        sharedOrganizationId: UID,
        employees: Map<UID, Employee>,
    ): DomainResult<ScheduleFailures, ScheduleLinkResult>

    suspend fun importShare(
        rewardChallengeId: String,
        claim: ScheduleShareClaim,
        links: List<ScheduleOrganizationLink>,
    ): UnitDomainResult<ScheduleFailures>

    class Base(
        private val shareRepository: ScheduleShareRepository,
        private val profileRepository: ProfileRepository,
        private val organizationRepository: OrganizationsRepository,
        private val baseSchedulesRepository: BaseScheduleRepository,
        private val adRewardRepository: AdRewardRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : ShareSchedulesInteractor {

        override suspend fun createShare() = eitherWrapper.wrap {
            val currentTime = dateManager.fetchCurrentInstant()
            val profile = profileRepository.fetchProfile().filterNotNull().first()
            val schedules = baseSchedulesRepository.fetchSchedulesByVersion(
                version = dateManager.fetchCurrentWeek(),
                numberOfWeek = null,
            ).first().map { schedule ->
                schedule.copy(dateVersion = DateVersion.createNewVersion(currentTime)).convertToMediate()
            }.filter { schedule -> schedule.classes.isNotEmpty() }
            if (schedules.isEmpty() || schedules.size > MAX_SCHEDULES) {
                throw ShareException.ItemLimit()
            }

            val organizationIds = schedules
                .map { schedule -> schedule.classes.map { it.organizationId } }
                .extractAllItem()
                .distinct()
            val organizations = organizationRepository.fetchOrganizationsById(organizationIds)
                .first()
                .map { it.convertToMediate() }

            shareRepository.createShare(
                ScheduleShare(
                    senderName = profile.username,
                    schedules = schedules,
                    organizations = organizations,
                )
            )
        }

        override suspend fun claimShare(code: String) = eitherWrapper.wrap {
            val claim = shareRepository.claimShare(code)
            val links = claim.share.organizations.map { organization ->
                ScheduleOrganizationLink(sharedOrganization = organization)
            }
            ScheduleSharePreview(
                claim = claim,
                organizations = organizationRepository.fetchAllShortOrganization().first()
                    .sortedByDescending { it.isMain },
                links = links,
                schedules = convertSchedules(claim.share.schedules, links),
                maxNumberOfWeek = claim.share.schedules.maxOfOrNull { schedule ->
                    schedule.week.isoRepeatWeekNumber
                } ?: 1,
            )
        }

        override suspend fun releaseShare(claim: ScheduleShareClaim) = eitherWrapper.wrapUnit {
            shareRepository.releaseShare(claim)
        }

        override suspend fun createImportReward(claim: ScheduleShareClaim) = eitherWrapper.wrap {
            adRewardRepository.createChallenge(
                purpose = AdRewardPurpose.SCHEDULE_IMPORT,
                subject = claim.claimId,
            )
        }

        override suspend fun linkOrganization(
            links: List<ScheduleOrganizationLink>,
            schedules: List<MediatedBaseSchedule>,
            sharedOrganizationId: UID,
            targetOrganizationId: UID?,
        ) = eitherWrapper.wrap {
            val targetOrganization = targetOrganizationId?.let { organizationId ->
                checkNotNull(organizationRepository.fetchOrganizationById(organizationId).first())
            }
            val updatedLinks = links.map { link ->
                if (link.sharedOrganization.uid != sharedOrganizationId) return@map link
                link.copy(
                    linkedOrganization = targetOrganization,
                    linkedSubjects = buildMap {
                        link.sharedOrganization.subjects.forEach { sharedSubject ->
                            targetOrganization?.subjects?.find { subject ->
                                subject.name.contains(sharedSubject.name, ignoreCase = true)
                            }?.let { subject -> put(sharedSubject.uid, subject) }
                        }
                    },
                    linkedTeachers = buildMap {
                        link.sharedOrganization.employee.forEach { sharedEmployee ->
                            targetOrganization?.employee?.find { employee ->
                                employee.firstName.equals(sharedEmployee.firstName, ignoreCase = true) &&
                                    employee.secondName.equals(sharedEmployee.secondName, ignoreCase = true)
                            }?.let { employee -> put(sharedEmployee.uid, employee) }
                        }
                    },
                )
            }
            ScheduleLinkResult(updatedLinks, convertSchedules(schedules, updatedLinks))
        }

        override suspend fun updateLinkedSubjects(
            links: List<ScheduleOrganizationLink>,
            schedules: List<MediatedBaseSchedule>,
            sharedOrganizationId: UID,
            subjects: Map<UID, Subject>,
        ) = eitherWrapper.wrap {
            val updatedLinks = links.map { link ->
                if (link.sharedOrganization.uid == sharedOrganizationId) {
                    link.copy(linkedSubjects = subjects)
                } else {
                    link
                }
            }
            ScheduleLinkResult(updatedLinks, convertSchedules(schedules, updatedLinks))
        }

        override suspend fun updateLinkedEmployees(
            links: List<ScheduleOrganizationLink>,
            schedules: List<MediatedBaseSchedule>,
            sharedOrganizationId: UID,
            employees: Map<UID, Employee>,
        ) = eitherWrapper.wrap {
            val updatedLinks = links.map { link ->
                if (link.sharedOrganization.uid == sharedOrganizationId) {
                    link.copy(linkedTeachers = employees)
                } else {
                    link
                }
            }
            ScheduleLinkResult(updatedLinks, convertSchedules(schedules, updatedLinks))
        }

        override suspend fun importShare(
            rewardChallengeId: String,
            claim: ScheduleShareClaim,
            links: List<ScheduleOrganizationLink>,
        ) = eitherWrapper.wrapUnit {
            adRewardRepository.completeChallenge(rewardChallengeId)
            var imported = false
            try {
                val (organizations, idMappings) = buildOrganizations(links)
                val schedules = buildSchedules(
                    schedules = claim.share.schedules,
                    organizations = organizations,
                    organizationIds = idMappings.organizationIds,
                    subjectIds = idMappings.subjectIds,
                    teacherIds = idMappings.teacherIds,
                )
                shareRepository.importShare(organizations, schedules)
                imported = true
                withContext(NonCancellable) {
                    shareRepository.confirmShare(claim)
                }
            } catch (error: Throwable) {
                if (!imported) {
                    withContext(NonCancellable) {
                        runCatching { shareRepository.releaseShare(claim) }
                    }
                }
                throw error
            }
        }

        private fun convertSchedules(
            schedules: List<MediatedBaseSchedule>,
            links: List<ScheduleOrganizationLink>,
        ) = schedules.map { schedule ->
            val organizationNumbers = mutableMapOf<UID, Int>()
            val classes = schedule.classes.sortedBy { classModel ->
                classModel.timeRange.from.dateTime().time
            }.mapNotNull { classModel ->
                val link = links.find {
                    it.sharedOrganization.uid == classModel.organizationId
                } ?: return@mapNotNull null
                val sharedOrganization = link.sharedOrganization.toOrganization()
                val organization = link.linkedOrganization ?: sharedOrganization
                val organizationId = organization.uid
                val number = organizationNumbers.getOrElse(organizationId) { 0 } + 1
                organizationNumbers[organizationId] = number
                Class(
                    uid = classModel.uid,
                    scheduleId = classModel.scheduleId,
                    organization = organization.convertToShort(),
                    eventType = classModel.eventType,
                    subject = link.linkedSubjects[classModel.subjectId]
                        ?: sharedOrganization.subjects.find { it.uid == classModel.subjectId },
                    customData = classModel.customData,
                    teacher = link.linkedTeachers[classModel.teacherId]
                        ?: sharedOrganization.employee.find { it.uid == classModel.teacherId },
                    office = classModel.office,
                    location = classModel.location,
                    timeRange = classModel.timeRange,
                    number = number,
                )
            }
            BaseSchedule(
                uid = schedule.uid,
                dateVersion = schedule.dateVersion,
                dayOfWeek = schedule.dayOfWeek,
                week = schedule.week,
                classes = classes,
            )
        }

        private suspend fun buildOrganizations(
            links: List<ScheduleOrganizationLink>,
        ): Pair<List<Organization>, ScheduleIdMappings> {
            val currentTime = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val organizationIds = mutableMapOf<UID, UID>()
            val subjectIds = mutableMapOf<UID, UID>()
            val teacherIds = mutableMapOf<UID, UID>()
            val organizations = links.map { link ->
                val sharedOrganization = link.sharedOrganization.toOrganization(currentTime)
                val linkedOrganization = link.linkedOrganization
                if (linkedOrganization != null) {
                    organizationIds[sharedOrganization.uid] = linkedOrganization.uid
                    link.linkedTeachers.forEach { (sharedId, employee) ->
                        teacherIds[sharedId] = employee.uid
                    }
                    link.linkedSubjects.forEach { (sharedId, subject) ->
                        subjectIds[sharedId] = subject.uid
                    }
                    val newTeachers = sharedOrganization.employee
                        .filterNot { link.linkedTeachers.containsKey(it.uid) }
                        .map { employee ->
                            employee.copy(
                                uid = randomUUID().also { teacherIds[employee.uid] = it },
                                organizationId = linkedOrganization.uid,
                                updatedAt = currentTime,
                            )
                        }
                    val newSubjects = sharedOrganization.subjects
                        .filterNot { link.linkedSubjects.containsKey(it.uid) }
                        .map { subject ->
                            val teacher = subject.teacher
                            subject.copy(
                                uid = randomUUID().also { subjectIds[subject.uid] = it },
                                organizationId = linkedOrganization.uid,
                                teacher = teacher?.copy(
                                    uid = teacherIds[teacher.uid] ?: teacher.uid,
                                    organizationId = linkedOrganization.uid,
                                    updatedAt = currentTime,
                                ),
                                updatedAt = currentTime,
                            )
                        }
                    linkedOrganization.copy(
                        subjects = linkedOrganization.subjects + newSubjects,
                        employee = linkedOrganization.employee + newTeachers,
                        offices = (linkedOrganization.offices + sharedOrganization.offices).distinct(),
                        locations = (linkedOrganization.locations + sharedOrganization.locations)
                            .distinctBy { it.value },
                        updatedAt = currentTime,
                    )
                } else {
                    val organizationId = randomUUID().also {
                        organizationIds[sharedOrganization.uid] = it
                    }
                    val employees = sharedOrganization.employee.map { employee ->
                        employee.copy(
                            uid = randomUUID().also { teacherIds[employee.uid] = it },
                            organizationId = organizationId,
                            updatedAt = currentTime,
                        )
                    }
                    val subjects = sharedOrganization.subjects.map { subject ->
                        val teacher = subject.teacher
                        subject.copy(
                            uid = randomUUID().also { subjectIds[subject.uid] = it },
                            organizationId = organizationId,
                            teacher = teacher?.copy(
                                uid = teacherIds[teacher.uid] ?: teacher.uid,
                                organizationId = organizationId,
                                updatedAt = currentTime,
                            ),
                            updatedAt = currentTime,
                        )
                    }
                    sharedOrganization.copy(
                        uid = organizationId,
                        subjects = subjects,
                        employee = employees,
                        updatedAt = currentTime,
                    )
                }
            }
            return organizations to ScheduleIdMappings(organizationIds, subjectIds, teacherIds)
        }

        private fun buildSchedules(
            schedules: List<MediatedBaseSchedule>,
            organizations: List<Organization>,
            organizationIds: Map<UID, UID>,
            subjectIds: Map<UID, UID>,
            teacherIds: Map<UID, UID>,
        ) = schedules.map { schedule ->
            val scheduleId = randomUUID()
            val classes = schedule.classes.mapNotNull { classModel ->
                val organizationId = organizationIds[classModel.organizationId]
                    ?: classModel.organizationId
                val organization = organizations.find { it.uid == organizationId }
                    ?: return@mapNotNull null
                Class(
                    uid = randomUUID(),
                    scheduleId = scheduleId,
                    organization = organization.convertToShort(),
                    eventType = classModel.eventType,
                    subject = classModel.subjectId?.let { subjectId ->
                        organization.subjects.find { it.uid == (subjectIds[subjectId] ?: subjectId) }
                    },
                    customData = classModel.customData,
                    teacher = classModel.teacherId?.let { teacherId ->
                        organization.employee.find { it.uid == (teacherIds[teacherId] ?: teacherId) }
                    },
                    office = classModel.office,
                    location = classModel.location,
                    timeRange = classModel.timeRange,
                )
            }
            BaseSchedule(
                uid = scheduleId,
                dateVersion = schedule.dateVersion,
                dayOfWeek = schedule.dayOfWeek,
                week = schedule.week,
                classes = classes,
            )
        }

        private fun MediatedOrganization.toOrganization(updatedAt: Long = 0L): Organization {
            val employees = employee.map { source ->
                Employee(
                    uid = source.uid,
                    organizationId = source.organizationId,
                    firstName = source.firstName,
                    secondName = source.secondName,
                    patronymic = source.patronymic,
                    post = source.post,
                    birthday = source.birthday,
                    workTime = source.workTime,
                    emails = source.emails,
                    phones = source.phones,
                    locations = source.locations,
                    webs = source.webs,
                    updatedAt = updatedAt,
                )
            }
            return Organization(
                uid = uid,
                isMain = isMain,
                shortName = shortName,
                fullName = fullName,
                type = type,
                scheduleTimeIntervals = scheduleTimeIntervals,
                subjects = subjects.map { source ->
                    Subject(
                        uid = source.uid,
                        organizationId = source.organizationId,
                        eventType = source.eventType,
                        name = source.name,
                        teacher = employees.find { it.uid == source.teacherId },
                        office = source.office,
                        color = source.color,
                        location = source.location,
                        updatedAt = updatedAt,
                    )
                },
                employee = employees,
                emails = emails,
                phones = phones,
                locations = locations,
                webs = webs,
                offices = offices,
                updatedAt = updatedAt,
            )
        }

        private data class ScheduleIdMappings(
            val organizationIds: Map<UID, UID>,
            val subjectIds: Map<UID, UID>,
            val teacherIds: Map<UID, UID>,
        )

        private companion object {
            const val MAX_SCHEDULES = 20
        }
    }
}
