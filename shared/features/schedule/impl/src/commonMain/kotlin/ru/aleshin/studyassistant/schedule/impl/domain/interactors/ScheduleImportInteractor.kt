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

import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardChallenge
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardPurpose
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.domain.entities.common.DayOfNumberedWeek
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportRequest
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.AdRewardRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleImportRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleImportHandler
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportClass
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportException
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportSession
import ru.aleshin.studyassistant.schedule.impl.domain.validation.ScheduleImportValidator
import ru.aleshin.studyassistant.schedule.impl.platform.CompressedScheduleImage
import ru.aleshin.studyassistant.schedule.impl.platform.ImageCompressor

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal interface ScheduleImportInteractor {

    suspend fun prepareImage(
        imageBytes: ByteArray
    ): DomainResult<ScheduleFailures, CompressedScheduleImage>

    suspend fun extractDraft(
        requestId: UID,
        image: CompressedScheduleImage,
        note: String?,
        organizationId: UID,
    ): DomainResult<ScheduleFailures, ScheduleImportSession>

    suspend fun createImportReward(
        requestId: UID,
        session: ScheduleImportSession,
    ): DomainResult<ScheduleFailures, AdRewardChallenge>

    suspend fun applySession(
        session: ScheduleImportSession,
        rewardChallengeId: String,
    ): UnitDomainResult<ScheduleFailures>

    class Base(
        private val importRepository: ScheduleImportRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val organizationsRepository: OrganizationsRepository,
        private val subjectsRepository: SubjectsRepository,
        private val employeeRepository: EmployeeRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val notificationSettingsRepository: NotificationSettingsRepository,
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val adRewardRepository: AdRewardRepository,
        private val imageCompressor: ImageCompressor,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val dateManager: DateManager,
        private val validator: ScheduleImportValidator,
        private val importHandler: ScheduleImportHandler,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : ScheduleImportInteractor {

        override suspend fun prepareImage(imageBytes: ByteArray) = eitherWrapper.wrap {
            imageCompressor.compress(imageBytes)
        }

        override suspend fun extractDraft(
            requestId: UID,
            image: CompressedScheduleImage,
            note: String?,
            organizationId: UID,
        ) = eitherWrapper.wrap {
            val organization = organizationsRepository.fetchOrganizationById(organizationId).first()
                ?: throw ScheduleImportException.NoOrganization

            val settings = calendarSettingsRepository.fetchSettings().first()
            val timeZone = TimeZone.currentSystemDefault()
            val todayDate = dateManager.fetchCurrentInstant().toLocalDateTime(timeZone).date.toString()
            val draft = importRepository.extractDraft(
                request = ScheduleImportRequest(
                    requestId = requestId,
                    imageBytes = image.bytes,
                    imageMimeType = image.mimeType,
                    note = note?.trim()?.takeIf(String::isNotEmpty),
                    locale = deviceInfoProvider.fetchDeviceLanguage(),
                    timeZone = timeZone.id,
                    numberOfWeeks = settings.numberOfWeek.isoRepeatWeekNumber,
                    todayDate = todayDate,
                ),
            )
            importHandler.handleDraft(draft, organization)
        }

        override suspend fun createImportReward(
            requestId: UID,
            session: ScheduleImportSession,
        ) = eitherWrapper.wrap {
            if (!validator.isSessionValid(session)) throw ScheduleImportException.InvalidDraft
            adRewardRepository.createChallenge(
                purpose = AdRewardPurpose.SCHEDULE_IMPORT,
                subject = requestId,
            )
        }

        override suspend fun applySession(
            session: ScheduleImportSession,
            rewardChallengeId: String,
        ) = eitherWrapper.wrapUnit {
            if (!validator.isSessionValid(session)) throw ScheduleImportException.InvalidDraft
            adRewardRepository.completeChallenge(rewardChallengeId)
            val organization = organizationsRepository.fetchOrganizationById(session.organizationId).first()
                ?: throw ScheduleImportException.NoOrganization

            val currentTime = dateManager.fetchCurrentInstant()
            val updatedAt = currentTime.toEpochMilliseconds()
            val updatedOrganization = importHandler.mergeOrganizationPlaces(
                organization = organization,
                session = session,
                updatedAt = updatedAt,
            )
            if (updatedOrganization != organization) {
                organizationsRepository.addOrUpdateOrganization(updatedOrganization)
            }
            val employeesToPersist = session.employees.filter { employee ->
                employee.uid !in session.originalEmployeeIds || employee.uid in session.dirtyEmployeeIds
            }
            employeesToPersist.forEach { employee ->
                employeeRepository.addOrUpdateEmployee(employee.copy(updatedAt = updatedAt))
            }
            val persistedEmployees = session.employees.associateBy(Employee::uid)
            val subjectsToPersist = session.subjects.filter { subject ->
                subject.uid !in session.originalSubjectIds || subject.uid in session.dirtySubjectIds
            }
            subjectsToPersist.forEach { subject ->
                val teacher = subject.teacher?.uid?.let(persistedEmployees::get)
                subjectsRepository.addOrUpdateSubject(subject.copy(teacher = teacher, updatedAt = updatedAt))
            }

            val included = session.classes.filter(ScheduleImportClass::included)
            val schedules = included
                .groupBy { classModel -> classModel.repeatWeek to classModel.dayOfWeek }
                .map { (day, classes) ->
                    val scheduleId = randomUUID()
                    BaseSchedule.createActual(
                        currentDate = currentTime,
                        dayOfNumberedWeek = DayOfNumberedWeek(
                            dayOfWeek = DayOfWeek.entries[day.second - 1],
                            week = NumberOfRepeatWeek.valueOf(day.first),
                        ),
                        classes = classes.sortedBy { classModel -> classModel.startTime }.map { classModel ->
                            val subject = classModel.subjectId?.let { id ->
                                session.subjects.firstOrNull { item -> item.uid == id }
                            }
                            val teacher = classModel.teacherId?.let { id -> persistedEmployees[id] } ?: subject?.teacher
                            val start = requireNotNull(validator.parseTime(classModel.startTime))
                            val end = requireNotNull(validator.parseTime(classModel.endTime))
                            Class(
                                uid = randomUUID(),
                                scheduleId = scheduleId,
                                organization = updatedOrganization.convertToShort(),
                                eventType = classModel.eventType ?: subject?.eventType ?: EventType.CLASS,
                                subject = subject,
                                customData = null,
                                teacher = teacher,
                                office = classModel.office.trim(),
                                location = classModel.location?.trim()?.takeIf(String::isNotEmpty)?.let { value ->
                                    ContactInfo(label = value, value = value)
                                },
                                timeRange = TimeRange(
                                    from = currentTime.startThisDay().setHoursAndMinutes(start),
                                    to = currentTime.startThisDay().setHoursAndMinutes(end),
                                ),
                                number = classModel.number ?: 0,
                            )
                        },
                        createdAt = updatedAt,
                    ).copy(uid = scheduleId)
                }
            deprecateCurrentSchedules(currentTime)
            baseScheduleRepository.addOrUpdateSchedulesGroup(schedules)
            restartClassReminders()
        }

        private suspend fun deprecateCurrentSchedules(currentInstant: Instant) {
            val currentWeek = dateManager.fetchCurrentWeek()
            val currentSchedules = baseScheduleRepository.fetchSchedulesByVersion(currentWeek, null).first()
            if (currentSchedules.isEmpty()) return
            val deprecatedSchedules = currentSchedules.map { schedule ->
                schedule.copy(
                    dateVersion = schedule.dateVersion.makeDeprecated(currentInstant),
                    classes = schedule.classes.map { classModel -> classModel.copy(uid = randomUUID()) },
                    updatedAt = currentInstant.toEpochMilliseconds(),
                )
            }
            baseScheduleRepository.addOrUpdateSchedulesGroup(deprecatedSchedules)
        }

        private suspend fun restartClassReminders() {
            val notificationSettings = notificationSettingsRepository.fetchSettings().first()
            startClassesReminderManager.startOrRetryReminderService()
            if (notificationSettings.endOfClasses) {
                endClassesReminderManager.startOrRetryReminderService()
            }
        }
    }
}
