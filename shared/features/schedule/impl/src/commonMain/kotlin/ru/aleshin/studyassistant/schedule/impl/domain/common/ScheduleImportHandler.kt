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

package ru.aleshin.studyassistant.schedule.impl.domain.common

import kotlinx.datetime.LocalTime
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.domain.entities.organizations.NumberedDuration
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportDraft
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportEventType
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.ui.theme.tokens.CustomColors
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportClass
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportSession
import ru.aleshin.studyassistant.schedule.impl.domain.validation.ScheduleImportValidator

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
internal interface ScheduleImportHandler {

    fun handleDraft(draft: ScheduleImportDraft, organization: Organization): ScheduleImportSession
    fun assignSubject(session: ScheduleImportSession, classId: UID, subjectId: UID?): ScheduleImportSession
    fun assignTeacher(session: ScheduleImportSession, classId: UID, teacherId: UID?): ScheduleImportSession
    fun updateClass(session: ScheduleImportSession, classModel: ScheduleImportClass): ScheduleImportSession
    fun updateSubject(session: ScheduleImportSession, subject: Subject): ScheduleImportSession
    fun updateEmployee(session: ScheduleImportSession, employee: Employee): ScheduleImportSession
    fun addSubject(session: ScheduleImportSession, subject: Subject): ScheduleImportSession
    fun addEmployee(session: ScheduleImportSession, employee: Employee): ScheduleImportSession
    fun deleteClass(session: ScheduleImportSession, classId: UID): ScheduleImportSession
    fun deleteSubject(session: ScheduleImportSession, subjectId: UID): ScheduleImportSession
    fun deleteEmployee(session: ScheduleImportSession, employeeId: UID): ScheduleImportSession
    fun reorderDayClasses(
        session: ScheduleImportSession,
        dayOfWeek: Int,
        repeatWeek: Int,
        orderedIds: List<UID>,
    ): ScheduleImportSession
    fun addClass(
        session: ScheduleImportSession,
        dayOfWeek: Int,
        repeatWeek: Int,
    ): ScheduleImportSession
    fun updateStartOfDay(
        session: ScheduleImportSession,
        repeatWeek: Int,
        dayOfWeek: Int,
        startTime: String,
    ): ScheduleImportSession
    fun updateClassesDuration(
        session: ScheduleImportSession,
        repeatWeek: Int,
        dayOfWeek: Int,
        baseDuration: Millis,
        specificDurations: List<NumberedDuration>,
    ): ScheduleImportSession
    fun updateBreaksDuration(
        session: ScheduleImportSession,
        repeatWeek: Int,
        dayOfWeek: Int,
        baseDuration: Millis,
        specificDurations: List<NumberedDuration>,
    ): ScheduleImportSession
    fun swapDays(
        session: ScheduleImportSession,
        repeatWeek: Int,
        firstDay: Int,
        secondDay: Int,
    ): ScheduleImportSession
    fun mergeOrganizationPlaces(
        organization: Organization,
        session: ScheduleImportSession,
        updatedAt: Long,
    ): Organization

    class Base(
        private val validator: ScheduleImportValidator,
        private val timeNormalizer: ScheduleImportTimeNormalizer = ScheduleImportTimeNormalizer(validator),
    ) : ScheduleImportHandler {

        override fun handleDraft(
            draft: ScheduleImportDraft,
            organization: Organization,
        ): ScheduleImportSession {
            val employees = organization.employee.toMutableList()
            val subjects = organization.subjects.toMutableList()
            val createdEmployeeIds = linkedMapOf<String, UID>()
            val createdSubjectIds = linkedMapOf<String, UID>()

            val classes = timeNormalizer.normalize(
                classes = draft.entries.map { entry ->
                    val isDashSubject = entry.subject.isDash()
                    val teacherId = if (isDashSubject) null else resolveTeacherId(
                        entryTeacherId = entry.teacherId,
                        teacherName = entry.teacher,
                        employees = employees,
                        createdEmployeeIds = createdEmployeeIds,
                        organizationId = organization.uid,
                    )
                    val subjectId = if (isDashSubject) null else resolveSubjectId(
                        entrySubjectId = entry.subjectId,
                        subjectName = entry.subject,
                        eventType = entry.eventType.toEventType(),
                        teacherId = teacherId,
                        office = entry.office,
                        location = entry.location,
                        employees = employees,
                        subjects = subjects,
                        createdSubjectIds = createdSubjectIds,
                        organizationId = organization.uid,
                    )
                    val start = validator.parseTime(entry.startTime)
                    val end = validator.parseTime(entry.endTime)
                    ScheduleImportClass(
                        uid = randomUUID(),
                        repeatWeek = entry.repeatWeek,
                        dayOfWeek = entry.dayOfWeek,
                        number = entry.classNumber,
                        startTime = start?.formatClock().orEmpty(),
                        endTime = end?.formatClock().orEmpty(),
                        subjectId = subjectId,
                        teacherId = teacherId,
                        office = entry.office.orEmpty(),
                        location = entry.location,
                        eventType = entry.eventType.toEventType(),
                        included = entry.included && !isDashSubject,
                    )
                }
            )

            return ScheduleImportSession(
                title = draft.title,
                organizationId = organization.uid,
                classes = classes,
                subjects = subjects.toList(),
                employees = employees.toList(),
                originalSubjectIds = organization.subjects.map(Subject::uid).toSet(),
                originalEmployeeIds = organization.employee.map(Employee::uid).toSet()
            )
        }

        override fun assignSubject(
            session: ScheduleImportSession,
            classId: UID,
            subjectId: UID?,
        ): ScheduleImportSession {
            val subject = subjectId?.let { id -> session.subjects.firstOrNull { item -> item.uid == id } }
            return session.copy(
                classes = session.classes.map { classModel ->
                    if (classModel.uid != classId) {
                        classModel
                    } else {
                        classModel.copy(
                            subjectId = subjectId,
                            eventType = classModel.eventType ?: subject?.eventType,
                            office = classModel.office.ifBlank { subject?.office.orEmpty() },
                        )
                    }
                },
            )
        }

        override fun assignTeacher(
            session: ScheduleImportSession,
            classId: UID,
            teacherId: UID?,
        ): ScheduleImportSession {
            return session.copy(
                classes = session.classes.map { classModel ->
                    if (classModel.uid == classId) classModel.copy(teacherId = teacherId) else classModel
                },
            )
        }

        override fun updateClass(
            session: ScheduleImportSession,
            classModel: ScheduleImportClass,
        ): ScheduleImportSession {
            return session.withNormalizedClasses(
                classes = session.classes.map { item ->
                    if (item.uid == classModel.uid) classModel else item
                },
            )
        }

        override fun updateSubject(
            session: ScheduleImportSession,
            subject: Subject,
        ): ScheduleImportSession {
            return session.copy(
                subjects = session.subjects.map { item ->
                    if (item.uid == subject.uid) subject else item
                },
                dirtySubjectIds = session.dirtySubjectIds + subject.uid,
            )
        }

        override fun updateEmployee(
            session: ScheduleImportSession,
            employee: Employee,
        ): ScheduleImportSession {
            val updatedEmployees = session.employees.map { item ->
                if (item.uid == employee.uid) employee else item
            }
            val updatedSubjects = session.subjects.map { subject ->
                if (subject.teacher?.uid == employee.uid) subject.copy(teacher = employee) else subject
            }
            return session.copy(
                employees = updatedEmployees,
                subjects = updatedSubjects,
                dirtyEmployeeIds = session.dirtyEmployeeIds + employee.uid,
                dirtySubjectIds = session.dirtySubjectIds + updatedSubjects
                    .filter { item -> item.teacher?.uid == employee.uid }
                    .map(Subject::uid),
            )
        }

        override fun addSubject(
            session: ScheduleImportSession,
            subject: Subject,
        ): ScheduleImportSession {
            if (session.subjects.any { item -> item.uid == subject.uid }) return session
            return session.copy(subjects = session.subjects + subject)
        }

        override fun addEmployee(
            session: ScheduleImportSession,
            employee: Employee,
        ): ScheduleImportSession {
            if (session.employees.any { item -> item.uid == employee.uid }) return session
            return session.copy(employees = session.employees + employee)
        }

        override fun deleteClass(
            session: ScheduleImportSession,
            classId: UID,
        ): ScheduleImportSession {
            return session.withNormalizedClasses(
                classes = session.classes.filter { classModel -> classModel.uid != classId },
            )
        }

        override fun deleteSubject(
            session: ScheduleImportSession,
            subjectId: UID,
        ): ScheduleImportSession {
            return session.copy(
                subjects = session.subjects.filter { subject -> subject.uid != subjectId },
                classes = session.classes.map { classModel ->
                    if (classModel.subjectId == subjectId) classModel.copy(subjectId = null) else classModel
                },
            )
        }

        override fun deleteEmployee(
            session: ScheduleImportSession,
            employeeId: UID,
        ): ScheduleImportSession {
            return session.copy(
                employees = session.employees.filter { employee -> employee.uid != employeeId },
                classes = session.classes.map { classModel ->
                    if (classModel.teacherId == employeeId) classModel.copy(teacherId = null) else classModel
                },
                subjects = session.subjects.map { subject ->
                    if (subject.teacher?.uid == employeeId) subject.copy(teacher = null) else subject
                },
            )
        }

        override fun reorderDayClasses(
            session: ScheduleImportSession,
            dayOfWeek: Int,
            repeatWeek: Int,
            orderedIds: List<UID>,
        ): ScheduleImportSession {
            val dayClasses = session.classes.filter { classModel ->
                classModel.dayOfWeek == dayOfWeek && classModel.repeatWeek == repeatWeek
            }
            if (dayClasses.isEmpty() || orderedIds.toSet() != dayClasses.map(ScheduleImportClass::uid).toSet()) {
                return session
            }
            if (dayClasses.map(ScheduleImportClass::uid) == orderedIds) return session
            val slots = dayClasses.map { classModel ->
                Triple(classModel.number, classModel.startTime, classModel.endTime)
            }
            val slotById = orderedIds.mapIndexed { index, classId -> classId to slots[index] }.toMap()
            return session.copy(
                classes = session.classes.map { classModel ->
                    val slot = slotById[classModel.uid] ?: return@map classModel
                    classModel.copy(
                        number = slot.first,
                        startTime = slot.second,
                        endTime = slot.third,
                    )
                },
            )
        }

        override fun addClass(
            session: ScheduleImportSession,
            dayOfWeek: Int,
            repeatWeek: Int,
        ): ScheduleImportSession {
            val dayClasses = session.classes.filter { classModel ->
                classModel.dayOfWeek == dayOfWeek && classModel.repeatWeek == repeatWeek
            }.sortedBy { classModel -> classModel.startTime }
            val last = dayClasses.lastOrNull()
            val lastEnd = last?.let { classModel -> validator.parseTime(classModel.endTime) }
            val start = lastEnd?.plusMinutes(DEFAULT_BREAK_MINUTES) ?: LocalTime(8, 0)
            val end = start.plusMinutes(DEFAULT_CLASS_MINUTES) ?: start
            val created = ScheduleImportClass(
                uid = randomUUID(),
                repeatWeek = repeatWeek,
                dayOfWeek = dayOfWeek,
                number = (last?.number ?: dayClasses.size) + 1,
                startTime = start.formatClock(),
                endTime = end.formatClock(),
                subjectId = last?.subjectId ?: session.subjects.firstOrNull()?.uid,
                teacherId = last?.teacherId,
                office = last?.office.orEmpty(),
                location = last?.location,
                eventType = last?.eventType ?: EventType.LESSON,
                included = true,
            )
            return session.withNormalizedClasses(classes = session.classes + created)
        }

        override fun updateStartOfDay(
            session: ScheduleImportSession,
            repeatWeek: Int,
            dayOfWeek: Int,
            startTime: String,
        ): ScheduleImportSession {
            val targetStart = validator.parseTime(startTime) ?: return session
            val updatedClasses = session.classes.groupBy { classModel ->
                classModel.repeatWeek to classModel.dayOfWeek
            }.flatMap { (key, dayClasses) ->
                if (key.first != repeatWeek || key.second != dayOfWeek) return@flatMap dayClasses
                val sorted = dayClasses.sortedBy { classModel -> classModel.startTime }
                val firstStart = sorted.firstOrNull()?.let { classModel ->
                    validator.parseTime(classModel.startTime)
                } ?: return@flatMap dayClasses
                val shift = targetStart.toMinutes() - firstStart.toMinutes()
                sorted.map { classModel ->
                    val start = validator.parseTime(classModel.startTime)?.plusMinutes(shift)
                    val end = validator.parseTime(classModel.endTime)?.plusMinutes(shift)
                    if (start == null || end == null) {
                        classModel
                    } else {
                        classModel.copy(
                            startTime = start.formatClock(),
                            endTime = end.formatClock(),
                        )
                    }
                }
            }
            return session.copy(classes = timeNormalizer.normalize(updatedClasses))
        }

        override fun updateClassesDuration(
            session: ScheduleImportSession,
            repeatWeek: Int,
            dayOfWeek: Int,
            baseDuration: Millis,
            specificDurations: List<NumberedDuration>,
        ): ScheduleImportSession {
            val updatedClasses = session.classes.groupBy { classModel ->
                classModel.repeatWeek to classModel.dayOfWeek
            }.flatMap { (key, dayClasses) ->
                if (key.first != repeatWeek || key.second != dayOfWeek) return@flatMap dayClasses
                val sorted = dayClasses.sortedBy { classModel -> classModel.startTime }
                var previousEnd: LocalTime? = null
                var previousOriginalEnd: LocalTime? = null
                sorted.mapIndexed { index, classModel ->
                    val durationMinutes = (
                        specificDurations.find { duration -> duration.number == index + 1 }?.duration
                            ?: baseDuration
                        ) / MILLIS_IN_MINUTE
                    val originalStart = validator.parseTime(classModel.startTime)
                    val originalEnd = validator.parseTime(classModel.endTime)
                    val start = if (previousEnd == null || originalStart == null) {
                        originalStart
                    } else {
                        val originalGap = if (previousOriginalEnd != null) {
                            originalStart.toMinutes() - previousOriginalEnd.toMinutes()
                        } else {
                            0
                        }
                        previousEnd.plusMinutes(originalGap.coerceAtLeast(0))
                    }
                    val end = start?.plusMinutes(durationMinutes.toInt())
                    previousOriginalEnd = originalEnd
                    previousEnd = end
                    if (start == null || end == null) {
                        classModel
                    } else {
                        classModel.copy(
                            startTime = start.formatClock(),
                            endTime = end.formatClock(),
                        )
                    }
                }
            }
            return session.copy(classes = timeNormalizer.normalize(updatedClasses))
        }

        override fun updateBreaksDuration(
            session: ScheduleImportSession,
            repeatWeek: Int,
            dayOfWeek: Int,
            baseDuration: Millis,
            specificDurations: List<NumberedDuration>,
        ): ScheduleImportSession {
            val updatedClasses = session.classes.groupBy { classModel ->
                classModel.repeatWeek to classModel.dayOfWeek
            }.flatMap { (key, dayClasses) ->
                if (key.first != repeatWeek || key.second != dayOfWeek) return@flatMap dayClasses
                val sorted = dayClasses.sortedBy { classModel -> classModel.startTime }
                var previousEnd: LocalTime? = null
                sorted.mapIndexed { index, classModel ->
                    val originalStart = validator.parseTime(classModel.startTime)
                    val originalEnd = validator.parseTime(classModel.endTime)
                    val durationMinutes = originalStart?.let { start ->
                        originalEnd?.toMinutes()?.minus(start.toMinutes())
                    } ?: DEFAULT_CLASS_MINUTES
                    if (index == 0 || previousEnd == null) {
                        previousEnd = originalEnd
                        classModel
                    } else {
                        val breakMinutes = (specificDurations.find { duration -> duration.number == index }?.duration ?: baseDuration) / MILLIS_IN_MINUTE
                        val start = previousEnd.plusMinutes(breakMinutes.toInt().coerceAtLeast(0))
                        val end = start?.plusMinutes(durationMinutes)
                        previousEnd = end
                        if (start == null || end == null) {
                            classModel
                        } else {
                            classModel.copy(
                                startTime = start.formatClock(),
                                endTime = end.formatClock(),
                            )
                        }
                    }
                }
            }
            return session.copy(classes = timeNormalizer.normalize(updatedClasses))
        }

        override fun swapDays(
            session: ScheduleImportSession,
            repeatWeek: Int,
            firstDay: Int,
            secondDay: Int,
        ): ScheduleImportSession {
            if (firstDay == secondDay) return session
            if (firstDay !in 1..7 || secondDay !in 1..7) return session
            return session.withNormalizedClasses(
                classes = session.classes.map { classModel ->
                    if (classModel.repeatWeek != repeatWeek) {
                        classModel
                    } else when (classModel.dayOfWeek) {
                        firstDay -> classModel.copy(dayOfWeek = secondDay)
                        secondDay -> classModel.copy(dayOfWeek = firstDay)
                        else -> classModel
                    }
                },
            )
        }

        private fun ScheduleImportSession.withNormalizedClasses(
            classes: List<ScheduleImportClass>,
        ): ScheduleImportSession {
            return copy(classes = timeNormalizer.normalize(classes))
        }

        override fun mergeOrganizationPlaces(
            organization: Organization,
            session: ScheduleImportSession,
            updatedAt: Long,
        ): Organization {
            val importedOffices = buildList {
                session.classes.filter(ScheduleImportClass::included).forEach { classModel ->
                    classModel.office.trim().takeIf(String::isNotEmpty)?.let(::add)
                }
                session.subjects.forEach { subject ->
                    subject.office.trim().takeIf(String::isNotEmpty)?.let(::add)
                }
            }.distinct()
            val importedLocationValues = buildList {
                session.classes.filter(ScheduleImportClass::included).forEach { classModel ->
                    classModel.location?.trim()?.takeIf(String::isNotEmpty)?.let(::add)
                }
                session.subjects.forEach { subject ->
                    subject.location?.value?.trim()?.takeIf(String::isNotEmpty)?.let(::add)
                }
            }.distinct()
            val existingOffices = organization.offices.map(String::trim).toSet()
            val existingLocationValues = organization.locations.map { location ->
                location.value.trim()
            }.toSet()
            val offices = organization.offices + importedOffices.filter { office -> office !in existingOffices }
            val locations = organization.locations + importedLocationValues
                .filter { value -> value !in existingLocationValues }
                .map { value -> ContactInfo(label = value, value = value) }
            if (offices == organization.offices && locations == organization.locations) {
                return organization
            }
            return organization.copy(
                offices = offices,
                locations = locations,
                updatedAt = updatedAt,
            )
        }

        private fun resolveTeacherId(
            entryTeacherId: UID?,
            teacherName: String?,
            employees: MutableList<Employee>,
            createdEmployeeIds: MutableMap<String, UID>,
            organizationId: UID,
        ): UID? {
            employees.firstOrNull { employee -> employee.uid == entryTeacherId }?.let { return it.uid }
            val matched = employees.fuzzyMatch(teacherName) { employee -> employee.officialName() }
            if (matched != null) return matched.uid
            val normalized = teacherName.normalized()
            if (normalized.isEmpty()) return null
            createdEmployeeIds[normalized]?.let { return it }
            val parsed = parseTeacherName(teacherName.orEmpty())
            val employee = Employee(
                uid = randomUUID(),
                organizationId = organizationId,
                firstName = parsed.first.capitalized(),
                secondName = parsed.second?.capitalized(),
                patronymic = parsed.third?.capitalized(),
                post = EmployeePost.TEACHER,
                updatedAt = 0L,
            )
            employees.add(employee)
            createdEmployeeIds[normalized] = employee.uid
            return employee.uid
        }

        private fun resolveSubjectId(
            entrySubjectId: UID?,
            subjectName: String?,
            eventType: EventType,
            teacherId: UID?,
            office: String?,
            location: String?,
            employees: List<Employee>,
            subjects: MutableList<Subject>,
            createdSubjectIds: MutableMap<String, UID>,
            organizationId: UID,
        ): UID? {
            subjects.firstOrNull { subject -> subject.uid == entrySubjectId }?.let { return it.uid }
            val matched = subjects.fuzzyMatch(subjectName, Subject::name)
            if (matched != null) return matched.uid
            val normalized = subjectName.normalized()
            if (normalized.isEmpty()) return null
            createdSubjectIds[normalized]?.let { return it }
            val teacher = teacherId?.let { id -> employees.firstOrNull { employee -> employee.uid == id } }
            val subject = Subject(
                uid = randomUUID(),
                organizationId = organizationId,
                eventType = eventType,
                name = subjectName?.trim().orEmpty().capitalized(),
                teacher = teacher,
                office = office.orEmpty(),
                color = nextSubjectColor(subjects),
                location = location?.trim()?.takeIf(String::isNotEmpty)?.let { value -> ContactInfo(value = value) },
                updatedAt = 0L,
            )
            subjects.add(subject)
            createdSubjectIds[normalized] = subject.uid
            return subject.uid
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
                parts.size == 2 -> Triple(parts[0], null, parts[1])
                else -> Triple(parts[1], parts[0], parts.drop(2).joinToString(" "))
            }
        }

        private fun nextSubjectColor(subjects: List<Subject>): Int {
            return CustomColors.randomUnusedArgb(subjects.map(Subject::color))
        }

        private fun Employee.officialName(): String {
            return listOfNotNull(secondName, firstName, patronymic).joinToString(" ")
        }

        private fun ScheduleImportEventType?.toEventType(): EventType {
            return EventType.entries.firstOrNull { entry -> entry.name == this?.name } ?: EventType.LESSON
        }

        private fun LocalTime.formatClock(): String {
            val hours = hour.toString().padStart(2, '0')
            val minutes = minute.toString().padStart(2, '0')
            return "$hours:$minutes"
        }

        private fun LocalTime.toMinutes(): Int = hour * 60 + minute

        private fun LocalTime.plusMinutes(amount: Int): LocalTime? {
            val total = toMinutes() + amount
            if (total !in 0 until MINUTES_IN_DAY) return null
            return LocalTime(hour = total / 60, minute = total % 60)
        }

        private fun String?.normalized(): String = orEmpty().trim().lowercase()

        private fun String?.isDash(): Boolean {
            val normalized = this?.trim().orEmpty()
            return normalized.all { char -> char == '-' || char == '—' || char == '–' || char == '.' } || normalized.isEmpty()
        }

        private fun String.capitalized(): String {
            return trim().split(Regex("\\s+")).filter(String::isNotEmpty).joinToString(" ") { part ->
                part.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() }
            }
        }

        private companion object {
            const val FUZZY_THRESHOLD = 0.7
            const val DEFAULT_CLASS_MINUTES = 45
            const val DEFAULT_BREAK_MINUTES = 10
            const val MINUTES_IN_DAY = 24 * 60
            const val MILLIS_IN_MINUTE = 60_000L
        }
    }
}
