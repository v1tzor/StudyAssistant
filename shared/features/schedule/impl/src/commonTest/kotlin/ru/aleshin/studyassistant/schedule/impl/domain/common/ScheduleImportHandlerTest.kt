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

import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportDraft
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportEntry
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportEventType
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.schedule.impl.domain.validation.ScheduleImportValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
internal class ScheduleImportHandlerTest {

    private val composer = ScheduleImportHandler.Base(ScheduleImportValidator.Base())

    @Test
    fun handleDraftLinksExistingCatalogAndCreatesMissingEntities() {
        val organization = organization(
            subjects = listOf(subject(uid = "subj-math", name = "Mathematics")),
            employees = listOf(employee(uid = "emp-ivan", firstName = "Ivan", secondName = "Petrov")),
        )
        val draft = ScheduleImportDraft(
            title = "Week",
            entries = listOf(
                entry(subject = "Mathematics", teacher = "Petrov Ivan", startTime = "8.00", endTime = "8:45"),
                entry(subject = "Physics", teacher = "Sidorova Anna", startTime = "9:00", endTime = "9:45"),
            )
        )

        val session = composer.handleDraft(draft, organization)

        assertEquals(organization.uid, session.organizationId)
        assertEquals(setOf("subj-math"), session.originalSubjectIds)
        assertEquals(setOf("emp-ivan"), session.originalEmployeeIds)
        assertEquals(2, session.classes.size)
        assertEquals(2, session.subjects.size)
        assertEquals(2, session.employees.size)

        val mathClass = session.classes.first { classModel -> classModel.subjectId == "subj-math" }
        assertEquals("emp-ivan", mathClass.teacherId)
        assertEquals("08:00", mathClass.startTime)
        assertEquals("08:45", mathClass.endTime)

        val physics = session.subjects.first { subject -> subject.name == "Physics" }
        val anna = session.employees.first { employee ->
            listOfNotNull(employee.secondName, employee.firstName, employee.patronymic)
                .any { part -> part.contains("Anna", ignoreCase = true) }
        }
        assertFalse(physics.uid in session.originalSubjectIds)
        assertFalse(anna.uid in session.originalEmployeeIds)
        assertEquals(anna.uid, session.classes.first { classModel -> classModel.subjectId == physics.uid }.teacherId)
        assertEquals("101", mathClass.office)
        assertEquals("101", session.classes.first { classModel -> classModel.subjectId == physics.uid }.office)
    }

    @Test
    fun handleDraftKeepsOfficeAndLocationOnCreatedSubjectAndClass() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = "Week",
                entries = listOf(
                    entry(
                        subject = "Chemistry",
                        startTime = "08:00",
                        endTime = "08:45",
                        office = "215",
                        location = "Корпус Б",
                    ),
                ),
            ),
            organization = organization(),
        )

        val classModel = session.classes.single()
        val subject = session.subjects.single()
        assertEquals("215", classModel.office)
        assertEquals("Корпус Б", classModel.location)
        assertEquals("215", subject.office)
        assertEquals("Корпус Б", subject.location?.value)
    }

    @Test
    fun mergeOrganizationPlacesAddsImportedOfficesAndLocationsToOrganization() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = "Week",
                entries = listOf(
                    entry(
                        subject = "Chemistry",
                        startTime = "08:00",
                        endTime = "08:45",
                        office = "215",
                        location = "Корпус Б",
                    ),
                    entry(
                        subject = "Physics",
                        startTime = "09:00",
                        endTime = "09:45",
                        office = "101",
                        location = "Корпус Б",
                    ),
                ),
            ),
            organization = organization(offices = listOf("12"), locations = listOf(ContactInfo(value = "Старый корпус"))),
        )

        val merged = composer.mergeOrganizationPlaces(
            organization = organization(offices = listOf("12"), locations = listOf(ContactInfo(value = "Старый корпус"))),
            session = session,
            updatedAt = 10L,
        )

        assertEquals(listOf("12", "215", "101"), merged.offices)
        assertEquals(listOf("Старый корпус", "Корпус Б"), merged.locations.map(ContactInfo::value))
        assertEquals(10L, merged.updatedAt)
    }

    @Test
    fun createdSubjectsPreferUnusedLightAndDarkColors() {
        val draft = ScheduleImportDraft(
            title = "Week",
            entries = listOf(
                entry(subject = "Algebra", startTime = "08:00", endTime = "08:45"),
                entry(subject = "Painting", startTime = "09:00", endTime = "09:45"),
                entry(subject = "Geography", startTime = "10:00", endTime = "10:45"),
                entry(subject = "Robotics", startTime = "11:00", endTime = "11:45"),
                entry(subject = "Choir", startTime = "12:00", endTime = "12:45"),
            ),
        )

        val session = composer.handleDraft(draft, organization())
        val createdColors = session.subjects.map(Subject::color)

        assertEquals(5, createdColors.size)
        assertEquals(createdColors.toSet().size, createdColors.size)
    }

    @Test
    fun handleDraftRenumbersIdenticalClassNumbersByStartTime() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = "Week",
                entries = listOf(
                    entry(subject = "Math", startTime = "08:00", endTime = "08:45", classNumber = 9),
                    entry(subject = "Physics", startTime = "09:00", endTime = "09:45", classNumber = 9),
                    entry(subject = "History", startTime = "10:00", endTime = "10:45", classNumber = 9),
                ),
            ),
            organization = organization(),
        )

        assertEquals(listOf(1, 2, 3), session.classes.map { classModel -> classModel.number })
    }

    @Test
    fun handleDraftKeepsPrintedPeriodNumbersWithGaps() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = "Week",
                entries = listOf(
                    entry(subject = "Physics", startTime = "08:00", endTime = "08:40", classNumber = 1, dayOfWeek = 5),
                    entry(subject = "Geography", startTime = "09:50", endTime = "10:30", classNumber = 3, dayOfWeek = 5),
                    entry(subject = "Chemistry", startTime = "11:40", endTime = "12:20", classNumber = 5, dayOfWeek = 5),
                ),
            ),
            organization = organization(),
        )

        assertEquals(listOf(1, 3, 5), session.classes.map { classModel -> classModel.number })
        assertEquals("11:40", session.classes.first { classModel -> classModel.number == 5 }.startTime)
        assertEquals("12:20", session.classes.first { classModel -> classModel.number == 5 }.endTime)
    }

    @Test
    fun handleDraftSplitsClonedMegaTimesUsingWeekGridAndKeepsPrintedNumbers() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = "Week",
                entries = listOf(
                    entry(subject = "Math", startTime = "08:00", endTime = "08:40", classNumber = 1, dayOfWeek = 1),
                    entry(subject = "PE", startTime = "08:50", endTime = "09:30", classNumber = 2, dayOfWeek = 1),
                    entry(subject = "English", startTime = "09:50", endTime = "10:30", classNumber = 3, dayOfWeek = 1),
                    entry(subject = "Physics", startTime = "10:40", endTime = "11:20", classNumber = 4, dayOfWeek = 1),
                    entry(subject = "Algebra", startTime = "11:40", endTime = "12:20", classNumber = 5, dayOfWeek = 1),
                    entry(subject = "Physics", startTime = "08:00", endTime = "15:40", classNumber = 1, dayOfWeek = 5),
                    entry(subject = "Geography", startTime = "08:00", endTime = "15:40", classNumber = 3, dayOfWeek = 5),
                    entry(subject = "Chemistry", startTime = "08:00", endTime = "15:40", classNumber = 5, dayOfWeek = 5),
                ),
            ),
            organization = organization(),
        )

        val friday = session.classes.filter { classModel -> classModel.dayOfWeek == 5 }
            .sortedBy { classModel -> classModel.number }
        assertEquals(listOf(1, 3, 5), friday.map { classModel -> classModel.number })
        assertEquals("08:00", friday[0].startTime)
        assertEquals("08:40", friday[0].endTime)
        assertEquals("09:50", friday[1].startTime)
        assertEquals("10:30", friday[1].endTime)
        assertEquals("11:40", friday[2].startTime)
        assertEquals("12:20", friday[2].endTime)
    }

    @Test
    fun handleDraftRestoresPrintedNumbersFromTimeGridWhenModelCompactedThem() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = "Week",
                entries = listOf(
                    entry(subject = "Math", startTime = "08:00", endTime = "08:40", classNumber = 1, dayOfWeek = 1),
                    entry(subject = "PE", startTime = "08:50", endTime = "09:30", classNumber = 2, dayOfWeek = 1),
                    entry(subject = "English", startTime = "09:50", endTime = "10:30", classNumber = 3, dayOfWeek = 1),
                    entry(subject = "Algebra", startTime = "11:40", endTime = "12:20", classNumber = 5, dayOfWeek = 1),
                    entry(subject = "Physics", startTime = "08:00", endTime = "08:40", classNumber = 1, dayOfWeek = 5),
                    entry(subject = "Geography", startTime = "09:50", endTime = "10:30", classNumber = 2, dayOfWeek = 5),
                    entry(subject = "Chemistry", startTime = "11:40", endTime = "12:20", classNumber = 3, dayOfWeek = 5),
                ),
            ),
            organization = organization(),
        )

        val friday = session.classes.filter { classModel -> classModel.dayOfWeek == 5 }
            .sortedBy { classModel -> classModel.startTime }
        assertEquals(listOf(1, 3, 5), friday.map { classModel -> classModel.number })
    }

    @Test
    fun addClassAppendsLessonOnSelectedDay() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = "Week",
                entries = listOf(entry(subject = "Math", startTime = "08:00", endTime = "08:40")),
            ),
            organization = organization(),
        )

        val updated = composer.addClass(session, dayOfWeek = 1, repeatWeek = 1)

        assertEquals(2, updated.classes.size)
        assertEquals("08:50", updated.classes.last().startTime)
        assertEquals("09:35", updated.classes.last().endTime)
    }

    @Test
    fun handleDraftKeepsUniqueSequentialClassNumbers() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = "Week",
                entries = listOf(
                    entry(subject = "Math", startTime = "08:00", endTime = "08:45", classNumber = 1),
                    entry(subject = "Physics", startTime = "09:00", endTime = "09:45", classNumber = 2),
                    entry(subject = "History", startTime = "10:00", endTime = "10:45", classNumber = 3),
                ),
            ),
            organization = organization(),
        )

        assertEquals(listOf(1, 2, 3), session.classes.map { classModel -> classModel.number })
    }

    @Test
    fun handleDraftKeepsSingleClassNumber() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = "Week",
                entries = listOf(
                    entry(subject = "Math", startTime = "14:00", endTime = "14:45", classNumber = 9),
                ),
            ),
            organization = organization(),
        )

        assertEquals(9, session.classes.single().number)
    }

    @Test
    fun assignExistingCatalogSubjectToClass() {
        val organization = organization(
            subjects = listOf(
                subject(uid = "subj-math", name = "Mathematics"),
                subject(uid = "subj-history", name = "History"),
            ),
        )
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = null,
                entries = listOf(entry(subject = "Mathematics", startTime = "08:00", endTime = "08:45")),
            ),
            organization = organization,
        )
        val classId = session.classes.single().uid

        val updated = composer.assignSubject(session, classId, "subj-history")

        assertEquals("subj-history", updated.classes.single().subjectId)
        assertEquals(2, updated.subjects.size)
        assertTrue(updated.subjects.any { subject -> subject.uid == "subj-history" })
    }

    @Test
    fun assignExistingCatalogTeacherToClass() {
        val organization = organization(
            employees = listOf(
                employee(uid = "emp-ivan", firstName = "Ivan", secondName = "Petrov"),
                employee(uid = "emp-anna", firstName = "Anna", secondName = "Sidorova"),
            ),
        )
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = null,
                entries = listOf(entry(subject = "Math", teacher = "Petrov Ivan", startTime = "08:00", endTime = "08:45")),
            ),
            organization = organization,
        )
        val classId = session.classes.single().uid
        assertEquals("emp-ivan", session.classes.single().teacherId)

        val updated = composer.assignTeacher(session, classId, "emp-anna")

        assertEquals("emp-anna", updated.classes.single().teacherId)
        assertEquals(2, updated.employees.size)
        assertTrue(updated.employees.any { employee -> employee.uid == "emp-anna" })
    }

    @Test
    fun updateExistingEmployeeKeepsOriginId() {
        val organization = organization(
            employees = listOf(employee(uid = "emp-ivan", firstName = "Ivan", secondName = "Petrov")),
        )
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = null,
                entries = listOf(entry(subject = "Math", teacher = "Petrov Ivan", startTime = "08:00", endTime = "08:45")),
            ),
            organization = organization,
        )
        val stored = session.employees.single { employee -> employee.uid == "emp-ivan" }

        val updated = composer.updateEmployee(
            session,
            stored.copy(secondName = "Sidorov"),
        )

        val employee = updated.employees.single { item -> item.uid == "emp-ivan" }
        assertEquals("Ivan", employee.firstName)
        assertEquals("Sidorov", employee.secondName)
        assertEquals("emp-ivan", employee.uid)
        assertTrue("emp-ivan" in updated.dirtyEmployeeIds)
        assertEquals("emp-ivan", updated.classes.single().teacherId)
        assertNotEquals("Petrov", employee.secondName)
    }

    @Test
    fun handleDraftCapitalizesImportedSubjectAndTeacherNames() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = null,
                entries = listOf(
                    entry(subject = "физика", teacher = "сидорова анна ивановна", startTime = "08:00", endTime = "08:45"),
                )
            ),
            organization = organization(),
        )

        val subject = session.subjects.single()
        val teacher = session.employees.single()
        assertEquals("Физика", subject.name)
        assertEquals("Анна", teacher.firstName)
        assertEquals("Сидорова", teacher.secondName)
        assertEquals("Ивановна", teacher.patronymic)
    }

    @Test
    fun reorderDayClassesSwapsTimeSlots() {
        val organization = organization()
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = null,
                entries = listOf(
                    entry(subject = "Math", startTime = "08:00", endTime = "08:45"),
                    entry(subject = "Physics", startTime = "09:00", endTime = "09:45"),
                )
            ),
            organization = organization,
        )
        val firstId = session.classes[0].uid
        val secondId = session.classes[1].uid

        val updated = composer.reorderDayClasses(session, dayOfWeek = 1, repeatWeek = 1, orderedIds = listOf(secondId, firstId))

        val first = updated.classes.first { classModel -> classModel.uid == firstId }
        val second = updated.classes.first { classModel -> classModel.uid == secondId }
        assertEquals("09:00", first.startTime)
        assertEquals("08:00", second.startTime)
        assertEquals(session.classes[1].number, first.number)
        assertEquals(session.classes[0].number, second.number)
    }

    @Test
    fun deleteClassRemovesOnlyTargetClass() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = null,
                entries = listOf(
                    entry(subject = "Math", startTime = "08:00", endTime = "08:45"),
                    entry(subject = "Physics", startTime = "09:00", endTime = "09:45"),
                ),
            ),
            organization = organization(),
        )
        val removedId = session.classes.first().uid

        val updated = composer.deleteClass(session, removedId)

        assertEquals(1, updated.classes.size)
        assertTrue(updated.classes.none { classModel -> classModel.uid == removedId })
    }

    @Test
    fun deleteSubjectUnassignsClasses() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = null,
                entries = listOf(entry(subject = "Physics", startTime = "08:00", endTime = "08:45")),
            ),
            organization = organization(),
        )
        val subjectId = session.subjects.single().uid

        val updated = composer.deleteSubject(session, subjectId)

        assertTrue(updated.subjects.none { subject -> subject.uid == subjectId })
        assertEquals(null, updated.classes.single().subjectId)
    }

    @Test
    fun deleteEmployeeUnassignsClassesAndSubjects() {
        val session = composer.handleDraft(
            draft = ScheduleImportDraft(
                title = null,
                entries = listOf(
                    entry(subject = "Physics", teacher = "Sidorova Anna", startTime = "08:00", endTime = "08:45"),
                ),
            ),
            organization = organization(),
        )
        val employeeId = session.employees.single().uid

        val updated = composer.deleteEmployee(session, employeeId)

        assertTrue(updated.employees.none { employee -> employee.uid == employeeId })
        assertEquals(null, updated.classes.single().teacherId)
        assertTrue(updated.subjects.all { subject -> subject.teacher == null })
    }

    private fun organization(
        subjects: List<Subject> = emptyList(),
        employees: List<Employee> = emptyList(),
        offices: List<String> = emptyList(),
        locations: List<ContactInfo> = emptyList(),
    ) = Organization(
        uid = "org-1",
        isMain = true,
        shortName = "School",
        type = OrganizationType.SCHOOL,
        subjects = subjects,
        employee = employees,
        locations = locations,
        offices = offices,
        updatedAt = 1L,
    )

    private fun subject(uid: String, name: String) = Subject(
        uid = uid,
        organizationId = "org-1",
        eventType = EventType.LESSON,
        name = name,
        teacher = null,
        office = "",
        color = 1,
        location = null,
        updatedAt = 1L,
    )

    private fun employee(
        uid: String,
        firstName: String,
        secondName: String?,
    ) = Employee(
        uid = uid,
        organizationId = "org-1",
        firstName = firstName,
        secondName = secondName,
        patronymic = null,
        post = EmployeePost.TEACHER,
        updatedAt = 1L,
    )

    private fun entry(
        subject: String,
        teacher: String? = null,
        startTime: String,
        endTime: String,
        classNumber: Int? = 1,
        dayOfWeek: Int = 1,
        office: String? = "101",
        location: String? = null,
    ) = ScheduleImportEntry(
        repeatWeek = 1,
        dayOfWeek = dayOfWeek,
        classNumber = classNumber,
        startTime = startTime,
        endTime = endTime,
        subject = subject,
        eventType = ScheduleImportEventType.LESSON,
        teacher = teacher,
        office = office,
        location = location,
        organization = null,
        notes = null,
        included = true,
    )
}
