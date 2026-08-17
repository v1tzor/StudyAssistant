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

package ru.aleshin.studyassistant.chat.impl.domain.tools.mappers

import kotlinx.datetime.format.DateTimeComponents.Formats
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.users.Profile
import ru.aleshin.studyassistant.core.ui.views.iso8601
import ru.aleshin.studyassistant.core.ui.views.timeFormat

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal object AiToolResultMapper {

    fun error(code: String): String = buildJsonObject {
        put("status", "error")
        put("code", code)
    }.toString()

    fun success(code: String): String = buildJsonObject {
        put("status", "success")
        put("code", code)
    }.toString()

    fun rejected(): String = buildJsonObject {
        put("status", "rejected_by_user")
    }.toString()

    fun homeworks(homeworks: List<Homework>): String = buildJsonArray {
        homeworks.forEach { homework ->
            addJsonObject {
                put("homeworkId", homework.uid)
                put("classId", homework.classId)
                put("subjectId", homework.subject?.uid)
                put("deadline", homework.deadline.formatByTimeZone(Formats.iso8601()))
                put("theoreticalTasks", homework.theoreticalTasks)
                put("practicalTasks", homework.practicalTasks)
                put("presentationTasks", homework.presentationTasks)
                put("testTopic", homework.test)
                put("isDone", homework.isDone)
            }
        }
    }.toString()

    fun todos(todos: List<Todo>): String = buildJsonArray {
        todos.forEach { todo ->
            addJsonObject {
                put("todoId", todo.uid)
                put("name", todo.name)
                put("description", todo.description)
                put("deadline", todo.deadline?.formatByTimeZone(Formats.iso8601()))
                put("priority", todo.priority.toString())
                put("isDone", todo.isDone)
            }
        }
    }.toString()

    fun goals(goals: List<Goal>): String = buildJsonArray {
        goals.forEach { goal ->
            addJsonObject {
                put("goalId", goal.uid)
                put("contentType", goal.contentType.toString())
                put("homeworkId", goal.contentHomework?.uid)
                put("todoId", goal.contentTodo?.uid)
                put("targetDate", goal.targetDate.formatByTimeZone(Formats.iso8601()))
                put("desiredTime", goal.desiredTime)
                put("isDone", goal.isDone)
            }
        }
    }.toString()

    fun profile(profile: Profile?): String = profile?.let { value ->
        buildJsonObject {
            put("profileId", value.uid)
            put("name", value.username)
            put("city", value.city)
            put("birthday", value.birthday)
        }.toString()
    } ?: buildJsonObject { put("status", "not_found") }.toString()

    fun organizations(organizations: List<OrganizationShort>): String = buildJsonArray {
        organizations.forEach { organization ->
            addJsonObject {
                put("organizationId", organization.uid)
                put("name", organization.shortName)
                put("organizationType", organization.type.toString())
            }
        }
    }.toString()

    fun subjects(subjects: List<Subject>): String = buildJsonArray {
        subjects.forEach { subject ->
            addJsonObject {
                put("subjectId", subject.uid)
                put("organizationId", subject.organizationId)
                put("teacherId", subject.teacher?.uid)
                put("name", subject.name)
                put("eventType", subject.eventType.toString())
            }
        }
    }.toString()

    fun employee(employee: Employee): String = buildJsonObject {
        put("teacherId", employee.uid)
        put("organizationId", employee.organizationId)
        put("name", listOfNotNull(employee.secondName, employee.firstName, employee.patronymic).joinToString(separator = " "))
        put("post", employee.post.toString())
    }.toString()

    fun employees(employees: List<Employee>): String = buildJsonArray {
        employees.forEach { employee -> add(employeeObject(employee)) }
    }.toString()

    fun freeIntervals(intervals: List<TimeRange>): String = buildJsonArray {
        val format = Formats.timeFormat()
        intervals.forEach { interval ->
            addJsonObject {
                put("startTime", interval.from.formatByTimeZone(format))
                put("endTime", interval.to.formatByTimeZone(format))
            }
        }
    }.toString()

    fun classes(classes: List<Class>): String = buildJsonArray {
        classes.forEach { classModel -> add(classObject(classModel)) }
    }.toString()

    fun classModel(classModel: Class): String = classObject(classModel).toString()

    private fun classObject(classModel: Class): JsonObject = buildJsonObject {
        put("classId", classModel.uid)
        put("scheduleId", classModel.scheduleId)
        put("organizationId", classModel.organization.uid)
        put("eventType", classModel.eventType.toString())
        put("subjectId", classModel.subject?.uid)
        put("teacherId", classModel.teacher?.uid)
        put("office", classModel.office)
        put("location", classModel.location?.toString())
        val format = Formats.timeFormat()
        put("startTime", classModel.timeRange.from.formatByTimeZone(format))
        put("endTime", classModel.timeRange.to.formatByTimeZone(format))
    }

    private fun employeeObject(employee: Employee): JsonObject = buildJsonObject {
        put("teacherId", employee.uid)
        put("organizationId", employee.organizationId)
        put("name", listOfNotNull(employee.secondName, employee.firstName, employee.patronymic).joinToString(separator = " "))
        put("post", employee.post.toString())
    }

    fun noClass(): String = buildJsonObject {
        put("status", "not_found")
    }.toString()
}
