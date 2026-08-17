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
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportDraft
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportEntry
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportRequest
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.repositories.AdRewardRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleImportRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportException
import ru.aleshin.studyassistant.schedule.impl.domain.validation.ScheduleImportValidator
import ru.aleshin.studyassistant.schedule.impl.platform.CompressedScheduleImage
import ru.aleshin.studyassistant.schedule.impl.platform.ImageCompressor

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal interface ScheduleImportInteractor {

    suspend fun prepareImage(imageBytes: ByteArray): DomainResult<ScheduleFailures, CompressedScheduleImage>
    suspend fun extractDraft(
        requestId: UID,
        image: CompressedScheduleImage,
        note: String?,
        organizationId: UID,
    ): DomainResult<ScheduleFailures, ScheduleImportDraft>
    suspend fun createImportReward(
        requestId: UID,
        draft: ScheduleImportDraft,
    ): DomainResult<ScheduleFailures, AdRewardChallenge>
    suspend fun applyDraft(
        draft: ScheduleImportDraft,
        organizationId: UID,
        rewardChallengeId: String,
    ): UnitDomainResult<ScheduleFailures>

    class Base(
        private val importRepository: ScheduleImportRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val organizationsRepository: OrganizationsRepository,
        private val subjectsRepository: SubjectsRepository,
        private val employeeRepository: EmployeeRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val adRewardRepository: AdRewardRepository,
        private val imageCompressor: ImageCompressor,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val dateManager: DateManager,
        private val validator: ScheduleImportValidator,
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
                ScheduleImportRequest(
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
            val subjects = organization.subjects
            val employees = organization.employee
            draft.copy(
                entries = draft.entries.map { entry ->
                    val matchedSubject = subjects.fuzzyMatch(entry.subject) { subject -> subject.name }
                    val matchedTeacher = employees.fuzzyMatch(entry.teacher) { employee ->
                        listOfNotNull(employee.secondName, employee.firstName, employee.patronymic).joinToString(" ")
                    }
                    entry.copy(
                        organizationId = organization.uid,
                        subjectId = matchedSubject?.uid,
                        teacherId = matchedTeacher?.uid,
                    )
                },
            )
        }

        override suspend fun createImportReward(
            requestId: UID,
            draft: ScheduleImportDraft,
        ) = eitherWrapper.wrap {
            if (!validator.isDraftValid(draft)) throw ScheduleImportException.InvalidDraft
            adRewardRepository.createChallenge(
                purpose = AdRewardPurpose.SCHEDULE_IMPORT,
                subject = requestId,
            )
        }

        override suspend fun applyDraft(
            draft: ScheduleImportDraft,
            organizationId: UID,
            rewardChallengeId: String,
        ) = eitherWrapper.wrapUnit {
            if (!validator.isDraftValid(draft)) throw ScheduleImportException.InvalidDraft
            val organization = organizationsRepository.fetchOrganizationById(organizationId).first()
                ?: throw ScheduleImportException.NoOrganization
            adRewardRepository.completeChallenge(rewardChallengeId)

            val currentTime = dateManager.fetchCurrentInstant()
            val updatedAt = currentTime.toEpochMilliseconds()
            val included = draft.entries.filter(ScheduleImportEntry::included)
            val teacherIds = linkedMapOf<String, UID>()
            included.mapNotNull { entry -> entry.teacher?.trim()?.takeIf(String::isNotEmpty) }
                .distinctBy { name -> name.normalized() }
                .forEach { name ->
                    val existingId = included.firstOrNull { entry ->
                        entry.teacher?.normalized() == name.normalized()
                    }?.teacherId
                    if (existingId != null) {
                        teacherIds[name.normalized()] = existingId
                    } else {
                        val parsed = parseTeacherName(name)
                        val employee = Employee(
                            uid = randomUUID(),
                            organizationId = organization.uid,
                            firstName = parsed.first,
                            secondName = parsed.second,
                            patronymic = parsed.third,
                            post = EmployeePost.TEACHER,
                            updatedAt = updatedAt,
                        )
                        employeeRepository.addOrUpdateEmployee(employee)
                        teacherIds[name.normalized()] = employee.uid
                    }
                }

            val subjectIds = linkedMapOf<String, UID>()
            included.mapNotNull { entry -> entry.subject?.trim()?.takeIf(String::isNotEmpty) }
                .distinctBy { name -> name.normalized() }
                .forEach { name ->
                    val sample = included.first { entry -> entry.subject?.normalized() == name.normalized() }
                    val existingId = sample.subjectId
                    val teacher = sample.teacher?.normalized()?.let(teacherIds::get)?.let { teacherId ->
                        employeeRepository.fetchEmployeeById(teacherId).first()
                    }
                    if (existingId != null) {
                        subjectIds[name.normalized()] = existingId
                    } else {
                        val subject = Subject(
                            uid = randomUUID(),
                            organizationId = organization.uid,
                            eventType = sample.eventType?.name?.let { EventType.valueOf(it) }
                                ?: EventType.LESSON,
                            name = name,
                            teacher = teacher,
                            office = sample.office.orEmpty(),
                            color = subjectColor(name),
                            location = sample.location?.let { ContactInfo(value = it) },
                            updatedAt = updatedAt,
                        )
                        subjectsRepository.addOrUpdateSubject(subject)
                        subjectIds[name.normalized()] = subject.uid
                    }
                }

            val schedules = included
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
                            val subject = entry.subject?.normalized()?.let(subjectIds::get)?.let { id ->
                                subjectsRepository.fetchSubjectById(id).first()
                            }
                            val teacher = entry.teacher?.normalized()?.let(teacherIds::get)?.let { id ->
                                employeeRepository.fetchEmployeeById(id).first()
                            }
                            val start = requireNotNull(validator.parseTime(entry.startTime))
                            val end = requireNotNull(validator.parseTime(entry.endTime))
                            Class(
                                uid = randomUUID(),
                                scheduleId = scheduleId,
                                organization = organization.convertToShort(),
                                eventType = entry.eventType?.name?.let { EventType.valueOf(it) }
                                    ?: subject?.eventType
                                    ?: EventType.CLASS,
                                subject = subject,
                                customData = entry.subject?.trim().takeIf { subject == null },
                                teacher = teacher ?: subject?.teacher,
                                office = entry.office?.trim().orEmpty(),
                                location = entry.location?.trim()?.takeIf(String::isNotEmpty)?.let { value ->
                                    ContactInfo(value = value)
                                },
                                timeRange = TimeRange(
                                    from = currentTime.startThisDay().setHoursAndMinutes(start),
                                    to = currentTime.startThisDay().setHoursAndMinutes(end),
                                ),
                                number = entry.classNumber ?: 0,
                            )
                        },
                        createdAt = updatedAt,
                    ).copy(uid = scheduleId)
                }
            baseScheduleRepository.addOrUpdateSchedulesGroup(schedules)
        }

        private fun <T> List<T>.fuzzyMatch(query: String?, selector: (T) -> String): T? {
            val normalizedQuery = query.normalized()
            if (normalizedQuery.isEmpty()) return null
            return map { item ->
                val name = selector(item).normalized()
                val distance = levenshteinDistance(name, normalizedQuery)
                val similarity = 1.0 - (distance.toDouble() / maxOf(name.length, normalizedQuery.length, 1))
                item to similarity
            }.filter { (item, similarity) ->
                val name = selector(item).normalized()
                similarity >= FUZZY_THRESHOLD || name.contains(normalizedQuery) || normalizedQuery.contains(name)
            }.maxByOrNull { it.second }?.first
        }

        private fun levenshteinDistance(first: String, second: String): Int {
            if (first == second) return 0
            if (first.isEmpty()) return second.length
            if (second.isEmpty()) return first.length
            val dp = IntArray(second.length + 1) { it }
            for (i in 1..first.length) {
                var prev = i
                for (j in 1..second.length) {
                    val current = if (first[i - 1] == second[j - 1]) {
                        dp[j - 1]
                    } else {
                        1 + minOf(dp[j - 1], dp[j], prev)
                    }
                    dp[j - 1] = prev
                    prev = current
                }
                dp[second.length] = prev
            }
            return dp[second.length]
        }

        private fun parseTeacherName(raw: String): Triple<String, String?, String?> {
            val parts = raw.trim().split(Regex("\\s+")).filter(String::isNotEmpty)
            return when {
                parts.isEmpty() -> Triple(raw, null, null)
                parts.size == 1 -> Triple(parts[0], null, null)
                parts.size == 2 -> Triple(parts[1], parts[0], null)
                else -> Triple(parts[1], parts[0], parts.drop(2).joinToString(" "))
            }
        }

        private fun subjectColor(name: String): Int {
            val palette = intArrayOf(
                0xFF1565C0.toInt(),
                0xFF2E7D32.toInt(),
                0xFF6A1B9A.toInt(),
                0xFFEF6C00.toInt(),
                0xFF00838F.toInt(),
                0xFFC62828.toInt(),
            )
            return palette[name.normalized().hashCode().mod(palette.size)]
        }

        private fun String?.normalized(): String = orEmpty().trim().lowercase()

        private companion object {
            const val FUZZY_THRESHOLD = 0.5
        }
    }
}
