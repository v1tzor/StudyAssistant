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

package ru.aleshin.studyassistant.chat.impl.domain.tools

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.chat.impl.domain.tools.mappers.AiToolResultMapper
import ru.aleshin.studyassistant.chat.impl.domain.tools.validation.AiToolArgumentsValidator
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.ToolCall
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal interface AiToolCallProcessor {

    fun activeCalls(messages: List<AiAssistantMessage>): List<ToolCall>
    fun pendingMutations(messages: List<AiAssistantMessage>): List<ToolCall>
    fun isMutation(call: ToolCall): Boolean
    suspend fun confirmationPreview(call: ToolCall): Map<String, String>
    suspend fun execute(call: ToolCall, mutationApproved: Boolean? = null): AiAssistantMessage.ToolMessage

    class Base(
        private val todoRepository: TodoRepository,
        private val homeworksRepository: HomeworksRepository,
        private val subjectsRepository: SubjectsRepository,
        private val organizationsRepository: OrganizationsRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val employeeRepository: EmployeeRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val todoReminderManager: TodoReminderManager,
        private val notificationSettingsRepository: NotificationSettingsRepository,
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val profileRepository: ProfileRepository,
        private val dateManager: DateManager,
        private val validator: AiToolArgumentsValidator,
        private val stateResolver: AiToolCallStateResolver,
    ) : AiToolCallProcessor {

        override fun activeCalls(messages: List<AiAssistantMessage>): List<ToolCall> {
            return stateResolver.activeCalls(messages)
        }

        override fun pendingMutations(messages: List<AiAssistantMessage>): List<ToolCall> {
            return activeCalls(messages).filter(::isMutation)
        }

        override fun isMutation(call: ToolCall): Boolean {
            return AiToolName.fromWireName(call.function.name)?.mutatesData == true
        }

        override suspend fun confirmationPreview(call: ToolCall): Map<String, String> {
            val args = call.function.arguments.orEmpty()
            val visibleArgs = resolveVisibleArguments(args)
            return when (AiToolName.fromWireName(call.function.name)) {
                AiToolName.UPDATE_TODO,
                AiToolName.COMPLETE_TODO,
                AiToolName.DELETE_TODO, -> {
                    val todo = args["todoId"]?.let { todoRepository.fetchTodoById(it).first() }
                    buildMap {
                        todo?.name?.let { put("target", it) }
                        putAll(visibleArgs)
                    }
                }
                AiToolName.UPDATE_HOMEWORK,
                AiToolName.COMPLETE_HOMEWORK,
                AiToolName.DELETE_HOMEWORK, -> {
                    val homework = args["homeworkId"]?.let {
                        homeworksRepository.fetchHomeworkById(it).first()
                    }
                    buildMap {
                        homework?.subject?.name?.let { put("target", it) }
                        putAll(visibleArgs)
                    }
                }
                AiToolName.UPDATE_CLASS,
                AiToolName.DELETE_CLASS, -> {
                    val date = validator.date(args["date"])
                    val classModel = date?.let { targetDate ->
                        classesByDate(targetDate).find { it.uid == args["classId"] }
                    }
                    buildMap {
                        (classModel?.subject?.name ?: classModel?.customData)?.let {
                            put("target", it)
                        }
                        putAll(visibleArgs)
                    }
                }
                else -> visibleArgs
            }
        }

        override suspend fun execute(
            call: ToolCall,
            mutationApproved: Boolean?,
        ): AiAssistantMessage.ToolMessage {
            val content = try {
                val tool = AiToolName.fromWireName(call.function.name)
                val args = call.function.arguments.orEmpty()
                when {
                    tool == null -> AiToolResultMapper.error("unsupported_tool")
                    tool.mutatesData && mutationApproved == false -> AiToolResultMapper.rejected()
                    tool.mutatesData && mutationApproved == null -> AiToolResultMapper.error("confirmation_required")
                    else -> execute(tool, args)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                AiToolResultMapper.error("execution_failed")
            }
            return AiAssistantMessage.ToolMessage(
                content = content,
                toolCallId = call.id,
                time = dateManager.fetchCurrentInstant(),
            )
        }

        private suspend fun execute(tool: AiToolName, args: Map<String, String>): String = when (tool) {
            AiToolName.CREATE_TODO -> createTodo(args)
            AiToolName.UPDATE_TODO -> updateTodo(args)
            AiToolName.COMPLETE_TODO -> completeTodo(args)
            AiToolName.DELETE_TODO -> deleteTodo(args)
            AiToolName.CREATE_HOMEWORK -> createHomework(args)
            AiToolName.UPDATE_HOMEWORK -> updateHomework(args)
            AiToolName.COMPLETE_HOMEWORK -> completeHomework(args)
            AiToolName.DELETE_HOMEWORK -> deleteHomework(args)
            AiToolName.CREATE_CLASS -> createClass(args)
            AiToolName.UPDATE_CLASS -> updateClass(args)
            AiToolName.DELETE_CLASS -> deleteClass(args)
            AiToolName.GET_PROFILE -> getProfile()
            AiToolName.GET_HOMEWORKS -> getHomeworks(args)
            AiToolName.GET_OVERDUE_HOMEWORKS -> getOverdueHomeworks()
            AiToolName.GET_TODOS -> getTodos(args)
            AiToolName.GET_SUBJECTS -> getSubjects(args)
            AiToolName.GET_EMPLOYEES -> getEmployees(args)
            AiToolName.GET_EMPLOYEE -> getEmployee(args)
            AiToolName.GET_ORGANIZATIONS -> getOrganizations()
            AiToolName.GET_CLASSES_BY_DATE -> getClassesByDate(args)
            AiToolName.GET_CLASSES_BY_RANGE -> getClassesByRange(args)
            AiToolName.GET_NEAR_CLASS -> getNearClass(args)
            AiToolName.GET_FREE_TIME -> getFreeTime(args)
        }

        private suspend fun createTodo(args: Map<String, String>): String {
            val name = validator.required(args, "name") ?: return AiToolResultMapper.error("todo_name_required")
            val deadlineSource = validator.optional(args, "deadline")
            val deadline = validator.instant(deadlineSource)
            if (deadlineSource != null && deadline == null) {
                return AiToolResultMapper.error("invalid_deadline")
            }
            val prioritySource = validator.optional(args, "priority")
            val priority = validator.priority(prioritySource) ?: return AiToolResultMapper.error("invalid_priority")
            val createdAt = dateManager.fetchCurrentInstant()
            val todo = Todo(
                uid = randomUUID(),
                name = name,
                description = args["description"].orEmpty().trim(),
                deadline = deadline,
                priority = priority,
                createdAt = createdAt,
                updatedAt = createdAt.toEpochMilliseconds(),
            )
            todoRepository.addOrUpdateTodo(todo)
            todoReminderManager.scheduleReminders(
                todo.uid,
                todo.name,
                todo.deadline,
                todo.notifications,
            )
            return AiToolResultMapper.success("todo_created")
        }

        private suspend fun updateTodo(args: Map<String, String>): String {
            val todoId = validator.required(args, "todoId") ?: return AiToolResultMapper.error("todo_required")
            val current = todoRepository.fetchTodoById(todoId).first() ?: return AiToolResultMapper.error("todo_not_found")
            val deadlineSource = validator.optional(args, "deadline")
            val deadline = validator.instant(deadlineSource)
            if (deadlineSource != null && deadline == null) {
                return AiToolResultMapper.error("invalid_deadline")
            }
            val prioritySource = validator.optional(args, "priority")
            val priority = prioritySource?.let(validator::priority)
            if (prioritySource != null && priority == null) {
                return AiToolResultMapper.error("invalid_priority")
            }
            val updated = current.copy(
                name = validator.optional(args, "name") ?: current.name,
                description = if ("description" in args) args["description"] else current.description,
                deadline = deadlineSource?.let { deadline } ?: current.deadline,
                priority = priority ?: current.priority,
                updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
            )
            todoRepository.addOrUpdateTodo(updated)
            todoReminderManager.clearAllReminders(todoId)
            todoReminderManager.scheduleReminders(updated.uid, updated.name, updated.deadline, updated.notifications)
            return AiToolResultMapper.success("todo_updated")
        }

        private suspend fun completeTodo(args: Map<String, String>): String {
            val todoId = validator.required(args, "todoId") ?: return AiToolResultMapper.error("todo_required")
            val completed = validator.boolean(validator.required(args, "completed")) ?: return AiToolResultMapper.error("invalid_completed_state")
            val current = todoRepository.fetchTodoById(todoId).first() ?: return AiToolResultMapper.error("todo_not_found")
            val now = dateManager.fetchCurrentInstant()
            todoRepository.addOrUpdateTodo(
                current.copy(
                    isDone = completed,
                    completeDate = now.takeIf { completed },
                    updatedAt = now.toEpochMilliseconds(),
                ),
            )
            if (completed) {
                todoReminderManager.clearAllReminders(todoId)
            } else {
                todoReminderManager.scheduleReminders(
                    current.uid,
                    current.name,
                    current.deadline,
                    current.notifications,
                )
            }
            return AiToolResultMapper.success("todo_completion_updated")
        }

        private suspend fun deleteTodo(args: Map<String, String>): String {
            val todoId = validator.required(args, "todoId") ?: return AiToolResultMapper.error("todo_required")
            if (todoRepository.fetchTodoById(todoId).first() == null) {
                return AiToolResultMapper.error("todo_not_found")
            }
            todoReminderManager.clearAllReminders(todoId)
            todoRepository.deleteTodo(todoId)
            return AiToolResultMapper.success("todo_deleted")
        }

        private suspend fun createHomework(args: Map<String, String>): String {
            val organizationId = validator.required(args, "organizationId")
                ?: return AiToolResultMapper.error("organization_required")
            val subjectId = validator.required(args, "subjectId")
                ?: return AiToolResultMapper.error("subject_required")
            val deadline = validator.date(validator.required(args, "deadline"))
                ?: return AiToolResultMapper.error("invalid_deadline")
            val organization = organizationsRepository.fetchShortOrganizationById(organizationId).first()
                ?: return AiToolResultMapper.error("organization_not_found")
            val subject = subjectsRepository.fetchSubjectById(subjectId).first()
                ?: return AiToolResultMapper.error("subject_not_found")
            if (subject.organizationId != organization.uid) {
                return AiToolResultMapper.error("subject_organization_mismatch")
            }
            val theoreticalTasks = args["theoreticalTasks"].orEmpty().trim()
            val practicalTasks = args["practicalTasks"].orEmpty().trim()
            val presentationTasks = args["presentationTasks"].orEmpty().trim()
            val testTopic = validator.optional(args, "testTopic")
            if (listOf(theoreticalTasks, practicalTasks, presentationTasks, testTopic).all { it.isNullOrEmpty() }) {
                return AiToolResultMapper.error("homework_content_required")
            }
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val homework = Homework(
                uid = randomUUID(),
                classId = validator.optional(args, "classId"),
                deadline = deadline,
                subject = subject,
                organization = organization,
                theoreticalTasks = theoreticalTasks,
                practicalTasks = practicalTasks,
                presentationTasks = presentationTasks,
                test = testTopic,
                updatedAt = updatedAt,
            )
            homeworksRepository.addOrUpdateHomework(homework)
            return AiToolResultMapper.success("homework_created")
        }

        private suspend fun updateHomework(args: Map<String, String>): String {
            val homeworkId = validator.required(args, "homeworkId")
                ?: return AiToolResultMapper.error("homework_required")
            val current = homeworksRepository.fetchHomeworkById(homeworkId).first()
                ?: return AiToolResultMapper.error("homework_not_found")
            val organizationId = validator.optional(args, "organizationId")
            val organization = organizationId?.let { id ->
                organizationsRepository.fetchShortOrganizationById(id).first()
            } ?: current.organization
            if (organizationId != null && organization.uid != organizationId) {
                return AiToolResultMapper.error("organization_not_found")
            }
            val subjectId = validator.optional(args, "subjectId")
            val subject = subjectId?.let { id -> subjectsRepository.fetchSubjectById(id).first() }
                ?: current.subject
            if (subjectId != null && subject?.uid != subjectId) {
                return AiToolResultMapper.error("subject_not_found")
            }
            if (subject != null && subject.organizationId != organization.uid) {
                return AiToolResultMapper.error("subject_organization_mismatch")
            }
            val deadlineSource = validator.optional(args, "deadline")
            val deadline = deadlineSource?.let(validator::date)
            if (deadlineSource != null && deadline == null) {
                return AiToolResultMapper.error("invalid_deadline")
            }
            val prioritySource = validator.optional(args, "priority")
            val priority = prioritySource?.let(validator::priority)
            if (prioritySource != null && priority == null) {
                return AiToolResultMapper.error("invalid_priority")
            }
            homeworksRepository.addOrUpdateHomework(
                current.copy(
                    classId = if ("classId" in args) validator.optional(args, "classId") else current.classId,
                    deadline = deadline ?: current.deadline,
                    subject = subject,
                    organization = organization,
                    theoreticalTasks = args["theoreticalTasks"] ?: current.theoreticalTasks,
                    practicalTasks = args["practicalTasks"] ?: current.practicalTasks,
                    presentationTasks = args["presentationTasks"] ?: current.presentationTasks,
                    test = if ("testTopic" in args) validator.optional(args, "testTopic") else current.test,
                    priority = priority ?: current.priority,
                    updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
                ),
            )
            return AiToolResultMapper.success("homework_updated")
        }

        private suspend fun completeHomework(args: Map<String, String>): String {
            val homeworkId = validator.required(args, "homeworkId")
                ?: return AiToolResultMapper.error("homework_required")
            val completed = validator.boolean(validator.required(args, "completed"))
                ?: return AiToolResultMapper.error("invalid_completed_state")
            val current = homeworksRepository.fetchHomeworkById(homeworkId).first()
                ?: return AiToolResultMapper.error("homework_not_found")
            val now = dateManager.fetchCurrentInstant()
            homeworksRepository.addOrUpdateHomework(
                current.copy(
                    isDone = completed,
                    completeDate = now.takeIf { completed },
                    updatedAt = now.toEpochMilliseconds(),
                ),
            )
            return AiToolResultMapper.success("homework_completion_updated")
        }

        private suspend fun deleteHomework(args: Map<String, String>): String {
            val homeworkId = validator.required(args, "homeworkId")
                ?: return AiToolResultMapper.error("homework_required")
            if (homeworksRepository.fetchHomeworkById(homeworkId).first() == null) {
                return AiToolResultMapper.error("homework_not_found")
            }
            homeworksRepository.deleteHomework(homeworkId)
            return AiToolResultMapper.success("homework_deleted")
        }

        private suspend fun createClass(args: Map<String, String>): String {
            val date = validator.date(validator.required(args, "date"))
                ?: return AiToolResultMapper.error("invalid_date")
            val startTime = validator.time(validator.required(args, "startTime"))
                ?: return AiToolResultMapper.error("invalid_start_time")
            val endTime = validator.time(validator.required(args, "endTime"))
                ?: return AiToolResultMapper.error("invalid_end_time")
            if (endTime <= startTime) return AiToolResultMapper.error("invalid_time_range")
            val organizationId = validator.required(args, "organizationId")
                ?: return AiToolResultMapper.error("organization_required")
            val organization = organizationsRepository.fetchOrganizationById(organizationId).first()
                ?: return AiToolResultMapper.error("organization_not_found")
            val subjectId = validator.optional(args, "subjectId")
            val subject = subjectId?.let { id -> subjectsRepository.fetchSubjectById(id).first() }
            if (subjectId != null && subject == null) {
                return AiToolResultMapper.error("subject_not_found")
            }
            if (subject != null && subject.organizationId != organizationId) {
                return AiToolResultMapper.error("subject_organization_mismatch")
            }
            val customData = validator.optional(args, "customData")
            if (subject == null && customData == null) {
                return AiToolResultMapper.error("class_name_required")
            }
            val employeeId = validator.optional(args, "employeeId")
            val employee = employeeId?.let { id -> employeeRepository.fetchEmployeeById(id).first() }
                ?: subject?.teacher
            if (employeeId != null && employee?.uid != employeeId) {
                return AiToolResultMapper.error("employee_not_found")
            }
            if (employee != null && employee.organizationId != organizationId) {
                return AiToolResultMapper.error("employee_organization_mismatch")
            }
            val eventTypeSource = validator.optional(args, "eventType")
            val eventType = eventTypeSource?.let { source ->
                EventType.entries.find { it.name.equals(source, ignoreCase = true) }
            }
            if (eventTypeSource != null && eventType == null) {
                return AiToolResultMapper.error("invalid_event_type")
            }
            val schedule = editableSchedule(date)
            val timeRange = TimeRange(
                from = date.setHoursAndMinutes(startTime),
                to = date.setHoursAndMinutes(endTime),
            )
            if (schedule.classes.hasConflict(timeRange)) {
                return AiToolResultMapper.error("class_time_conflict")
            }
            val classModel = Class(
                uid = randomUUID(),
                scheduleId = schedule.uid,
                organization = organization.convertToShort(),
                eventType = eventType ?: subject?.eventType ?: EventType.CLASS,
                subject = subject,
                customData = customData.takeIf { subject == null },
                teacher = employee,
                office = validator.optional(args, "office") ?: subject?.office.orEmpty(),
                location = validator.optional(args, "location")?.let { ContactInfo(value = it) }
                    ?: subject?.location,
                timeRange = timeRange,
                number = schedule.classes.count { it.organization.uid == organizationId } + 1,
            )
            saveSchedule(schedule, schedule.classes + classModel)
            return AiToolResultMapper.success("class_created")
        }

        private suspend fun updateClass(args: Map<String, String>): String {
            val classId = validator.required(args, "classId")
                ?: return AiToolResultMapper.error("class_required")
            val date = validator.date(validator.required(args, "date"))
                ?: return AiToolResultMapper.error("invalid_date")
            val schedule = editableSchedule(date)
            val current = schedule.classes.find { it.uid == classId }
                ?: return AiToolResultMapper.error("class_not_found")
            val organizationId = validator.optional(args, "organizationId")
            val organization = organizationId?.let { id ->
                organizationsRepository.fetchOrganizationById(id).first()?.convertToShort()
            } ?: current.organization
            if (organizationId != null && organization.uid != organizationId) {
                return AiToolResultMapper.error("organization_not_found")
            }
            val subjectId = validator.optional(args, "subjectId")
            val subject = subjectId?.let { id -> subjectsRepository.fetchSubjectById(id).first() }
                ?: current.subject
            if (subjectId != null && subject?.uid != subjectId) {
                return AiToolResultMapper.error("subject_not_found")
            }
            if (subject != null && subject.organizationId != organization.uid) {
                return AiToolResultMapper.error("subject_organization_mismatch")
            }
            val employeeId = validator.optional(args, "employeeId")
            val employee = employeeId?.let { id -> employeeRepository.fetchEmployeeById(id).first() }
                ?: current.teacher
            if (employeeId != null && employee?.uid != employeeId) {
                return AiToolResultMapper.error("employee_not_found")
            }
            if (employee != null && employee.organizationId != organization.uid) {
                return AiToolResultMapper.error("employee_organization_mismatch")
            }
            val startTimeSource = validator.optional(args, "startTime")
            val startTime = startTimeSource?.let(validator::time)
            if (startTimeSource != null && startTime == null) {
                return AiToolResultMapper.error("invalid_start_time")
            }
            val endTimeSource = validator.optional(args, "endTime")
            val endTime = endTimeSource?.let(validator::time)
            if (endTimeSource != null && endTime == null) {
                return AiToolResultMapper.error("invalid_end_time")
            }
            val actualStartTime = startTime ?: current.timeRange.from.dateTime().time
            val actualEndTime = endTime ?: current.timeRange.to.dateTime().time
            if (actualEndTime <= actualStartTime) {
                return AiToolResultMapper.error("invalid_time_range")
            }
            val timeRange = TimeRange(
                from = date.setHoursAndMinutes(actualStartTime),
                to = date.setHoursAndMinutes(actualEndTime),
            )
            if (schedule.classes.filterNot { it.uid == classId }.hasConflict(timeRange)) {
                return AiToolResultMapper.error("class_time_conflict")
            }
            val eventTypeSource = validator.optional(args, "eventType")
            val eventType = eventTypeSource?.let { source ->
                EventType.entries.find { it.name.equals(source, ignoreCase = true) }
            }
            if (eventTypeSource != null && eventType == null) {
                return AiToolResultMapper.error("invalid_event_type")
            }
            val customData = if ("customData" in args) {
                validator.optional(args, "customData")
            } else {
                current.customData
            }
            if (subject == null && customData == null) {
                return AiToolResultMapper.error("class_name_required")
            }
            val updated = current.copy(
                scheduleId = schedule.uid,
                organization = organization,
                eventType = eventType ?: subject?.eventType ?: current.eventType,
                subject = subject,
                customData = customData.takeIf { subject == null },
                teacher = employee,
                office = validator.optional(args, "office") ?: current.office,
                location = validator.optional(args, "location")?.let { ContactInfo(value = it) }
                    ?: current.location,
                timeRange = timeRange,
            )
            saveSchedule(
                schedule = schedule,
                classes = schedule.classes.map { classModel ->
                    updated.takeIf { classModel.uid == classId } ?: classModel
                },
            )
            return AiToolResultMapper.success("class_updated")
        }

        private suspend fun deleteClass(args: Map<String, String>): String {
            val classId = validator.required(args, "classId")
                ?: return AiToolResultMapper.error("class_required")
            val date = validator.date(validator.required(args, "date"))
                ?: return AiToolResultMapper.error("invalid_date")
            val schedule = editableSchedule(date)
            if (schedule.classes.none { it.uid == classId }) {
                return AiToolResultMapper.error("class_not_found")
            }
            saveSchedule(schedule, schedule.classes.filterNot { it.uid == classId })
            return AiToolResultMapper.success("class_deleted")
        }

        private suspend fun getProfile(): String {
            return AiToolResultMapper.profile(profileRepository.fetchProfile().first())
        }

        private suspend fun getHomeworks(args: Map<String, String>): String {
            val range = validator.range(args["from"], args["to"])
                ?: return AiToolResultMapper.error("invalid_date_range")
            return AiToolResultMapper.homeworks(
                homeworksRepository.fetchHomeworksByTimeRange(range).first(),
            )
        }

        private suspend fun getOverdueHomeworks(): String {
            val currentDate = dateManager.fetchBeginningCurrentInstant()
            return AiToolResultMapper.homeworks(
                homeworksRepository.fetchOverdueHomeworks(currentDate).first(),
            )
        }

        private suspend fun getTodos(args: Map<String, String>): String {
            val from = validator.optional(args, "fromDate")
            val to = validator.optional(args, "toDate")
            if ((from == null) != (to == null)) {
                return AiToolResultMapper.error("incomplete_date_range")
            }
            val status = validator.optional(args, "status")?.uppercase() ?: "ALL"
            if (status !in TODO_STATUSES) return AiToolResultMapper.error("invalid_status")
            val todos = if (from != null && to != null) {
                val range = validator.range(from, to)
                    ?: return AiToolResultMapper.error("invalid_date_range")
                todoRepository.fetchTodosByTimeRange(range).first().filter { todo ->
                    when (status) {
                        "ACTIVE" -> !todo.isDone
                        "COMPLETED" -> todo.isDone
                        else -> true
                    }
                }
            } else {
                when (status) {
                    "ACTIVE" -> todoRepository.fetchActiveTodos().first()
                    "COMPLETED" -> todoRepository.fetchCompletedTodos().first()
                    else -> todoRepository.fetchActiveTodos().first() +
                        todoRepository.fetchCompletedTodos().first()
                }
            }
            return AiToolResultMapper.todos(todos.distinctBy(Todo::uid))
        }

        private suspend fun getOrganizations(): String {
            return AiToolResultMapper.organizations(
                organizationsRepository.fetchAllShortOrganization().first(),
            )
        }

        private suspend fun getSubjects(args: Map<String, String>): String {
            val organizationId = validator.required(args, "organizationId")
                ?: return AiToolResultMapper.error("organization_required")
            return AiToolResultMapper.subjects(
                subjectsRepository.fetchAllSubjectsByOrganization(organizationId).first(),
            )
        }

        private suspend fun getEmployee(args: Map<String, String>): String {
            val teacherId = validator.required(args, "teacherId")
                ?: return AiToolResultMapper.error("employee_required")
            val employee = employeeRepository.fetchEmployeeById(teacherId).first()
                ?: return AiToolResultMapper.error("employee_not_found")
            return AiToolResultMapper.employee(employee)
        }

        private suspend fun getEmployees(args: Map<String, String>): String {
            val organizationId = validator.optional(args, "organizationId")
            val employees = if (organizationId != null) {
                employeeRepository.fetchAllEmployeeByOrganization(organizationId).first()
            } else {
                organizationsRepository.fetchAllShortOrganization().first().flatMap { organization ->
                    employeeRepository.fetchAllEmployeeByOrganization(organization.uid).first()
                }
            }
            val query = validator.optional(args, "query")
            val filtered = query?.let { source ->
                employees.filter { employee ->
                    listOfNotNull(employee.secondName, employee.firstName, employee.patronymic)
                        .joinToString(separator = " ")
                        .contains(source, ignoreCase = true)
                }
            } ?: employees
            return AiToolResultMapper.employees(filtered.distinctBy { it.uid })
        }

        private suspend fun getClassesByDate(args: Map<String, String>): String {
            val date = validator.date(args["date"])
                ?: return AiToolResultMapper.error("invalid_date")
            return AiToolResultMapper.classes(classesByDate(date))
        }

        private suspend fun getClassesByRange(args: Map<String, String>): String {
            val range = validator.range(args["fromDate"], args["toDate"])
                ?: return AiToolResultMapper.error("invalid_date_range")
            val classes = buildList {
                var date = range.from.startThisDay()
                while (date <= range.to) {
                    addAll(classesByDate(date))
                    date = date.shiftDay(1)
                }
            }
            return AiToolResultMapper.classes(classes)
        }

        private suspend fun getFreeTime(args: Map<String, String>): String {
            val date = validator.date(args["date"])
                ?: return AiToolResultMapper.error("invalid_date")
            val minimumMinutes = validator.optional(args, "minimumMinutes")?.toIntOrNull() ?: 1
            if (minimumMinutes !in 1..MINUTES_IN_DAY) {
                return AiToolResultMapper.error("invalid_minimum_minutes")
            }
            val classes = classesByDate(date).sortedBy { it.timeRange.from }
            val intervals = classes.zipWithNext().mapNotNull { (current, next) ->
                val from = current.timeRange.to
                val to = next.timeRange.from
                val durationMinutes = (to.toEpochMilliseconds() - from.toEpochMilliseconds()) /
                    MILLIS_IN_MINUTE
                TimeRange(from, to).takeIf { durationMinutes >= minimumMinutes }
            }
            return AiToolResultMapper.freeIntervals(intervals)
        }

        private suspend fun classesByDate(date: Instant): List<ru.aleshin.studyassistant.core.domain.entities.classes.Class> {
            val calendarSettings = calendarSettingsRepository.fetchSettings().first()
            val currentNumberOfWeek = date.dateTime().date.numberOfRepeatWeek(calendarSettings.numberOfWeek)
            val customSchedule = customScheduleRepository.fetchScheduleByDate(date).first()
            if (customSchedule != null) {
                return customSchedule.classes.sortedBy { it.timeRange.from.dateTime().time }
            }
            val baseSchedule = baseScheduleRepository.fetchScheduleByDate(date, currentNumberOfWeek).first()
            return baseSchedule?.classes.orEmpty().filter { classModel ->
                calendarSettings.holidays.none { holiday ->
                    TimeRange(holiday.start, holiday.end).containsDate(date) &&
                        holiday.organizations.contains(classModel.organization.uid)
                }
            }.sortedBy { it.timeRange.from.dateTime().time }
        }

        private suspend fun getNearClass(args: Map<String, String>): String {
            val subjectId = validator.required(args, "subjectId")
                ?: return AiToolResultMapper.error("subject_required")
            val now = dateManager.fetchCurrentInstant()
            val lastDate = now.endOfWeek().shiftWeek(1)
            val nearest = buildList {
                var date = now.startThisDay()
                while (date <= lastDate) {
                    addAll(
                        classesByDate(date).filter { classModel ->
                            classModel.subject?.uid == subjectId && classModel.timeRange.to > now
                        },
                    )
                    date = date.shiftDay(1)
                }
            }.minByOrNull { it.timeRange.from }
                ?: return AiToolResultMapper.noClass()
            return AiToolResultMapper.classModel(nearest)
        }

        private suspend fun editableSchedule(date: Instant): CustomSchedule {
            customScheduleRepository.fetchScheduleByDate(date).first()?.let { return it }
            val scheduleId = randomUUID()
            return CustomSchedule(
                uid = scheduleId,
                date = date.startThisDay(),
                classes = classesByDate(date).map { classModel ->
                    classModel.copy(scheduleId = scheduleId)
                },
                updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
            )
        }

        private suspend fun resolveVisibleArguments(args: Map<String, String>): Map<String, String> {
            return buildMap {
                putAll(args.filterKeys { key -> !key.endsWith("Id") })
                args["organizationId"]?.let { id ->
                    organizationsRepository.fetchShortOrganizationById(id).first()?.shortName?.let {
                        put("organization", it)
                    }
                }
                args["subjectId"]?.let { id ->
                    subjectsRepository.fetchSubjectById(id).first()?.name?.let {
                        put("subject", it)
                    }
                }
                args["employeeId"]?.let { id ->
                    employeeRepository.fetchEmployeeById(id).first()?.let { employee ->
                        put(
                            "employee",
                            listOfNotNull(
                                employee.secondName,
                                employee.firstName,
                                employee.patronymic,
                            ).joinToString(" "),
                        )
                    }
                }
            }
        }

        private suspend fun saveSchedule(
            schedule: CustomSchedule,
            classes: List<Class>,
        ) {
            customScheduleRepository.addOrUpdateSchedule(
                schedule.copy(
                    classes = classes.sortedBy { it.timeRange.from },
                    updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
                ),
            )
            startClassesReminderManager.startOrRetryReminderService()
            if (notificationSettingsRepository.fetchSettings().first().endOfClasses) {
                endClassesReminderManager.startOrRetryReminderService()
            }
        }

        private fun List<Class>.hasConflict(timeRange: TimeRange): Boolean {
            return any { classModel ->
                timeRange.from < classModel.timeRange.to && timeRange.to > classModel.timeRange.from
            }
        }

        private companion object {
            const val MILLIS_IN_MINUTE = 60_000L
            const val MINUTES_IN_DAY = 1_440
            val TODO_STATUSES = setOf("ALL", "ACTIVE", "COMPLETED")
        }
    }
}
