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
        val anna = session.employees.first { employee -> employee.firstName == "Anna" }
        assertFalse(physics.uid in session.originalSubjectIds)
        assertFalse(anna.uid in session.originalEmployeeIds)
        assertEquals(anna.uid, session.classes.first { classModel -> classModel.subjectId == physics.uid }.teacherId)
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
    ) = Organization(
        uid = "org-1",
        isMain = true,
        shortName = "School",
        type = OrganizationType.SCHOOL,
        subjects = subjects,
        employee = employees,
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
    ) = ScheduleImportEntry(
        repeatWeek = 1,
        dayOfWeek = 1,
        classNumber = 1,
        startTime = startTime,
        endTime = endTime,
        subject = subject,
        eventType = ScheduleImportEventType.LESSON,
        teacher = teacher,
        office = "101",
        location = null,
        organization = null,
        notes = null,
        included = true,
    )
}
