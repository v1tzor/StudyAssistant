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
import kotlinx.datetime.TimeZone
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.domain.entities.common.DayOfNumberedWeek
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportDraft
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportEntry
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportRequest
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleImportRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportException
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleTextRecognitionException
import ru.aleshin.studyassistant.schedule.impl.domain.services.ScheduleTextRecognizer
import ru.aleshin.studyassistant.schedule.impl.domain.validation.ScheduleImportValidator

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal interface ScheduleImportInteractor {

    suspend fun recognizeText(imageBytes: ByteArray): DomainResult<ScheduleFailures, String>
    suspend fun extractDraft(rawText: String, numberOfWeeks: Int): DomainResult<ScheduleFailures, ScheduleImportDraft>
    suspend fun applyDraft(draft: ScheduleImportDraft): UnitDomainResult<ScheduleFailures>

    class Base(
        private val importRepository: ScheduleImportRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val organizationsRepository: OrganizationsRepository,
        private val subjectsRepository: SubjectsRepository,
        private val employeeRepository: EmployeeRepository,
        private val textRecognizer: ScheduleTextRecognizer,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val dateManager: DateManager,
        private val validator: ScheduleImportValidator,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : ScheduleImportInteractor {

        override suspend fun recognizeText(imageBytes: ByteArray) = eitherWrapper.wrap {
            if (imageBytes.isEmpty()) throw ScheduleTextRecognitionException.InvalidImage
            if (imageBytes.size > MAX_IMAGE_BYTES) {
                throw ScheduleTextRecognitionException.ImageTooLarge
            }
            textRecognizer.recognize(imageBytes).trim().also { text ->
                if (!validator.isSourceTextValid(text)) {
                    throw ScheduleTextRecognitionException.NoText
                }
            }
        }

        override suspend fun extractDraft(rawText: String, numberOfWeeks: Int) = eitherWrapper.wrap {
            if (!validator.isSourceTextValid(rawText)) throw ScheduleImportException.EmptyText
            importRepository.extractDraft(
                ScheduleImportRequest(
                    requestId = randomUUID(),
                    rawText = rawText.trim(),
                    locale = deviceInfoProvider.fetchDeviceLanguage(),
                    timeZone = TimeZone.currentSystemDefault().id,
                    numberOfWeeks = numberOfWeeks,
                ),
            )
        }

        override suspend fun applyDraft(draft: ScheduleImportDraft) = eitherWrapper.wrapUnit {
            if (!validator.isDraftValid(draft)) throw ScheduleImportException.InvalidDraft

            val organizations = organizationsRepository.fetchAllOrganization().first()
            val fallbackOrganization = organizations.firstOrNull(Organization::isMain)
                ?: organizations.firstOrNull()
                ?: throw ScheduleImportException.NoOrganization
            val subjects = organizations.associate { organization ->
                organization.uid to subjectsRepository
                    .fetchAllSubjectsByOrganization(organization.uid)
                    .first()
            }
            val employees = organizations.associate { organization ->
                organization.uid to employeeRepository
                    .fetchAllEmployeeByOrganization(organization.uid)
                    .first()
            }
            val currentTime = dateManager.fetchCurrentInstant()
            val schedules = draft.entries
                .filter(ScheduleImportEntry::included)
                .groupBy { entry -> entry.repeatWeek to entry.dayOfWeek }
                .map { (day, entries) ->
                    val scheduleId = randomUUID()
                    BaseSchedule.createActual(
                        currentDate = currentTime,
                        dayOfNumberedWeek = DayOfNumberedWeek(
                            dayOfWeek = DayOfWeek.entries[day.second - 1],
                            week = NumberOfRepeatWeek.valueOf(day.first),
                        ),
                        classes = entries.sortedBy { entry -> entry.startTime }.map { entry ->
                            val organization = organizations.matchOrganization(entry.organization) ?: fallbackOrganization
                            val subject = subjects.getValue(organization.uid).find { subject ->
                                subject.name.normalized() == entry.subject.normalized()
                            }
                            val teacher = employees.getValue(organization.uid).find { employee ->
                                listOfNotNull(
                                    employee.secondName,
                                    employee.firstName,
                                    employee.patronymic,
                                ).joinToString(" ").normalized() == entry.teacher.normalized()
                            }
                            val start = requireNotNull(validator.parseTime(entry.startTime))
                            val end = requireNotNull(validator.parseTime(entry.endTime))
                            Class(
                                uid = randomUUID(),
                                scheduleId = scheduleId,
                                organization = organization.convertToShort(),
                                eventType = entry.eventType?.name?.let(EventType::valueOf)
                                    ?: subject?.eventType
                                    ?: EventType.CLASS,
                                subject = subject,
                                customData = entry.subject?.trim().takeIf { subject == null },
                                teacher = teacher ?: subject?.teacher,
                                office = entry.office?.trim().orEmpty(),
                                location = entry.location?.trim()?.takeIf(String::isNotEmpty)?.let {
                                    ContactInfo(value = it)
                                },
                                timeRange = TimeRange(
                                    from = currentTime.startThisDay().setHoursAndMinutes(start),
                                    to = currentTime.startThisDay().setHoursAndMinutes(end),
                                ),
                                number = entry.classNumber ?: 0,
                            )
                        },
                        createdAt = currentTime.toEpochMilliseconds(),
                    ).copy(uid = scheduleId)
                }
            baseScheduleRepository.addOrUpdateSchedulesGroup(schedules)
        }

        private fun List<Organization>.matchOrganization(name: String?): Organization? {
            val normalizedName = name.normalized()
            if (normalizedName.isEmpty()) return null
            return find { organization ->
                organization.shortName.normalized() == normalizedName ||
                    organization.fullName.normalized() == normalizedName
            }
        }

        private fun String?.normalized(): String = orEmpty().trim().lowercase()

        private companion object {
            const val MAX_IMAGE_BYTES = 20 * 1024 * 1024
        }
    }
}
