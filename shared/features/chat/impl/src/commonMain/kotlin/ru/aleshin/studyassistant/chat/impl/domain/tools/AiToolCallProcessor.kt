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
import kotlinx.datetime.format.DateTimeComponents
import ru.aleshin.studyassistant.chat.impl.domain.tools.mappers.AiToolResultMapper
import ru.aleshin.studyassistant.chat.impl.domain.tools.validation.AiToolArgumentsValidator
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.parseUsingOffset
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.ToolCall
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.ui.theme.tokens.CustomColors
import ru.aleshin.studyassistant.core.ui.views.dayMonthYearFormat
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthTimeFormat

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
        private val dailyGoalsRepository: DailyGoalsRepository,
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
                AiToolName.COMPLETE_TODO -> {
                    val todo = args["todoId"]?.let { todoRepository.fetchTodoById(it).first() }
                    buildMap {
                        todo?.name?.let { put("target", it) }
                        putAll(visibleArgs)
                    }
                }
                AiToolName.UPDATE_HOMEWORK,
                AiToolName.COMPLETE_HOMEWORK -> {
                    val homework = args["homeworkId"]?.let {
                        homeworksRepository.fetchHomeworkById(it).first()
                    }
                    buildMap {
                        homework?.subject?.name?.let { put("target", it) }
                        putAll(visibleArgs)
                    }
                }
                AiToolName.UPDATE_CLASS -> {
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
                AiToolName.UPDATE_GOAL,
                AiToolName.COMPLETE_GOAL -> {
                    val goal = args["goalId"]?.let { dailyGoalsRepository.fetchGoalById(it).first() }
                    buildMap {
                        val target = when (goal?.contentType) {
                            GoalType.HOMEWORK -> goal.contentHomework?.subject?.name
                            GoalType.TODO -> goal.contentTodo?.name
                            null -> null
                        }
                        target?.let { put("target", it) }
                        putAll(visibleArgs)
                    }
                }
                AiToolName.UPDATE_SUBJECT -> {
                    val subject = args["subjectId"]?.let { subjectsRepository.fetchSubjectById(it).first() }
                    buildMap {
                        subject?.name?.let { put("target", it) }
                        putAll(visibleArgs)
                    }
                }
                AiToolName.UPDATE_EMPLOYEE -> {
                    val employee = args["teacherId"]?.let { employeeRepository.fetchEmployeeById(it).first() }
                    buildMap {
                        employee?.let {
                            put("target", listOfNotNull(it.secondName, it.firstName).joinToString(" "))
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
            AiToolName.CREATE_HOMEWORK -> createHomework(args)
            AiToolName.UPDATE_HOMEWORK -> updateHomework(args)
            AiToolName.COMPLETE_HOMEWORK -> completeHomework(args)
            AiToolName.CREATE_CLASS -> createClass(args)
            AiToolName.UPDATE_CLASS -> updateClass(args)
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
            AiToolName.GET_GOALS -> getGoals(args)
            AiToolName.CREATE_GOAL -> createGoal(args)
            AiToolName.UPDATE_GOAL -> updateGoal(args)
            AiToolName.COMPLETE_GOAL -> completeGoal(args)
            AiToolName.CREATE_SUBJECT -> createSubject(args)
            AiToolName.UPDATE_SUBJECT -> updateSubject(args)
            AiToolName.CREATE_EMPLOYEE -> createEmployee(args)
            AiToolName.UPDATE_EMPLOYEE -> updateEmployee(args)
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
                targetId = todo.uid,
                name = todo.name,
                deadline = todo.deadline,
                notifications = todo.notifications
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

            syncLinkedGoal(updated.uid)

            return AiToolResultMapper.success("todo_updated")
        }

        private suspend fun completeTodo(args: Map<String, String>): String {
            val todoId = validator.required(args, "todoId") ?: return AiToolResultMapper.error("todo_required")
            val isDone = validator.boolean(validator.required(args, "completed")) ?: return AiToolResultMapper.error("invalid_completed_state")
            val todo = todoRepository.fetchTodoById(todoId).first() ?: return AiToolResultMapper.error("todo_not_found")

            if (todo.isDone == isDone) return AiToolResultMapper.success("todo_already_in_state")

            val currentTime = dateManager.fetchCurrentInstant()
            val updatedAt = currentTime.toEpochMilliseconds()
            val linkedGoal = dailyGoalsRepository.fetchGoalByContentId(todo.uid).first()

            if (isDone) {
                val completedTodo = todo.copy(
                    isDone = true,
                    completeDate = currentTime,
                    updatedAt = updatedAt,
                )
                if (linkedGoal != null && !linkedGoal.isDone) completeLinkedGoal(linkedGoal)

                todoRepository.addOrUpdateTodo(completedTodo)
                todoReminderManager.clearAllReminders(todo.uid)
            } else {
                val reopenedTodo = todo.copy(
                    isDone = false,
                    completeDate = null,
                    updatedAt = updatedAt,
                )
                if (linkedGoal != null && linkedGoal.targetDate >= currentTime.startThisDay()) {
                    reopenLinkedGoal(linkedGoal)
                }

                todoRepository.addOrUpdateTodo(reopenedTodo)
                todoReminderManager.scheduleReminders(reopenedTodo.uid, reopenedTodo.name, reopenedTodo.deadline, reopenedTodo.notifications)
            }

            return AiToolResultMapper.success("todo_completion_updated")
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
            val updated = current.copy(
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
            )
            homeworksRepository.addOrUpdateHomework(updated)

            syncLinkedGoal(updated.uid)

            return AiToolResultMapper.success("homework_updated")
        }

        private suspend fun completeHomework(args: Map<String, String>): String {
            val homeworkId = validator.required(args, "homeworkId")
                ?: return AiToolResultMapper.error("homework_required")
            val isDone = validator.boolean(validator.required(args, "completed"))
                ?: return AiToolResultMapper.error("invalid_completed_state")
            val homework = homeworksRepository.fetchHomeworkById(homeworkId).first()
                ?: return AiToolResultMapper.error("homework_not_found")

            if (homework.isDone == isDone) return AiToolResultMapper.success("homework_already_in_state")

            val currentTime = dateManager.fetchCurrentInstant()
            val updatedAt = currentTime.toEpochMilliseconds()
            val linkedGoal = dailyGoalsRepository.fetchGoalByContentId(homework.uid).first()

            if (isDone) {
                val updatedHomework = homework.copy(
                    isDone = true,
                    completeDate = currentTime,
                    updatedAt = updatedAt,
                )
                if (linkedGoal != null && !linkedGoal.isDone) completeLinkedGoal(linkedGoal)

                homeworksRepository.addOrUpdateHomework(updatedHomework)
            } else {
                val updatedHomework = homework.copy(
                    isDone = false,
                    completeDate = null,
                    updatedAt = updatedAt,
                )
                if (linkedGoal != null && linkedGoal.targetDate >= currentTime.startThisDay()) {
                    reopenLinkedGoal(linkedGoal)
                }

                homeworksRepository.addOrUpdateHomework(updatedHomework)
            }

            return AiToolResultMapper.success("homework_completion_updated")
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
                location = validator.optional(args, "location")?.let { ContactInfo(value = it) } ?: current.location,
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

        private suspend fun syncLinkedGoal(contentId: UID) {
            val linkedGoal = dailyGoalsRepository.fetchGoalByContentId(contentId).first()
            if (linkedGoal != null) {
                val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
                val updatedGoal = when (linkedGoal.contentType) {
                    GoalType.HOMEWORK -> {
                        val homework = homeworksRepository.fetchHomeworkById(contentId).first()
                        linkedGoal.copy(contentHomework = homework, updatedAt = updatedAt)
                    }
                    GoalType.TODO -> {
                        val todo = todoRepository.fetchTodoById(contentId).first()
                        linkedGoal.copy(contentTodo = todo, updatedAt = updatedAt)
                    }
                }
                dailyGoalsRepository.addOrUpdateGoal(updatedGoal)
            }
        }

        private suspend fun completeLinkedGoal(linkedGoal: Goal) {
            val currentTime = dateManager.fetchCurrentInstant()
            val time = linkedGoal.time
            val updatedGoalTime = when (time) {
                is GoalTime.Stopwatch -> {
                    val stopTime = time.startTimePoint.toEpochMilliseconds()
                    val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                    time.copy(
                        pastStopTime = time.pastStopTime + timeAfterStop,
                        isActive = false,
                    )
                }
                is GoalTime.Timer -> {
                    val stopTime = time.startTimePoint.toEpochMilliseconds()
                    val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                    time.copy(
                        pastStopTime = time.pastStopTime + timeAfterStop,
                        isActive = false,
                    )
                }
                GoalTime.None -> GoalTime.None
            }
            val updatedGoal = linkedGoal.copy(
                time = updatedGoalTime,
                isDone = true,
                completeDate = currentTime,
                updatedAt = currentTime.toEpochMilliseconds(),
            )
            dailyGoalsRepository.addOrUpdateGoal(updatedGoal)
        }

        private suspend fun reopenLinkedGoal(linkedGoal: Goal) {
            val currentTime = dateManager.fetchCurrentInstant()
            val time = linkedGoal.time
            val reopenedGoalTime = when (time) {
                is GoalTime.Stopwatch -> time.copy(
                    pastStopTime = 0L,
                    startTimePoint = currentTime,
                    isActive = false,
                )
                is GoalTime.Timer -> time.copy(
                    pastStopTime = 0L,
                    startTimePoint = currentTime,
                    isActive = false,
                )
                GoalTime.None -> GoalTime.None
            }
            val reopenedGoal = linkedGoal.copy(
                time = reopenedGoalTime,
                isDone = false,
                completeDate = null,
                updatedAt = currentTime.toEpochMilliseconds(),
            )
            dailyGoalsRepository.addOrUpdateGoal(reopenedGoal)
        }

        private suspend fun getProfile(): String {
            return AiToolResultMapper.profile(profileRepository.fetchProfile().first())
        }

        private suspend fun getHomeworks(args: Map<String, String>): String {
            val range = validator.range(args["from"], args["to"]) ?: return AiToolResultMapper.error("invalid_date_range")
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
                val range = validator.range(from, to) ?: return AiToolResultMapper.error("invalid_date_range")
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
                    else -> todoRepository.fetchActiveTodos().first() + todoRepository.fetchCompletedTodos().first()
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

        private suspend fun getGoals(args: Map<String, String>): String {
            val date = validator.date(args["date"])
                ?: return AiToolResultMapper.error("invalid_date")
            return AiToolResultMapper.goals(dailyGoalsRepository.fetchDailyGoalsByDate(date).first())
        }

        private suspend fun createGoal(args: Map<String, String>): String {
            val date = validator.date(validator.required(args, "date"))
                ?: return AiToolResultMapper.error("invalid_date")
            val contentTypeSource = validator.required(args, "contentType")
                ?: return AiToolResultMapper.error("goal_type_required")
            val contentType = GoalType.entries.find { it.name.equals(contentTypeSource, ignoreCase = true) }
                ?: return AiToolResultMapper.error("invalid_goal_type")

            val homework = if (contentType == GoalType.HOMEWORK) {
                val homeworkId = validator.required(args, "homeworkId") ?: return AiToolResultMapper.error("homework_required")
                homeworksRepository.fetchHomeworkById(homeworkId).first() ?: return AiToolResultMapper.error("homework_not_found")
            } else {
                null
            }

            val todo = if (contentType == GoalType.TODO) {
                val todoId = validator.required(args, "todoId") ?: return AiToolResultMapper.error("todo_required")
                todoRepository.fetchTodoById(todoId).first() ?: return AiToolResultMapper.error("todo_not_found")
            } else {
                null
            }

            val desiredTimeSource = validator.optional(args, "desiredTime")
            val desiredTime = desiredTimeSource?.toLongOrNull()?.let { it * 60_000L }
            val dailyGoals = dailyGoalsRepository.fetchDailyGoalsByDate(date).first()
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val maxNumber = dailyGoals.maxOfOrNull { it.number } ?: 0

            val goal = Goal(
                uid = randomUUID(),
                contentType = contentType,
                contentHomework = homework,
                contentTodo = todo,
                number = maxNumber + 1,
                targetDate = date,
                desiredTime = desiredTime,
                updatedAt = updatedAt,
            )
            dailyGoalsRepository.addOrUpdateGoal(goal)
            return AiToolResultMapper.success("goal_created")
        }

        private suspend fun updateGoal(args: Map<String, String>): String {
            val goalId = validator.required(args, "goalId") ?: return AiToolResultMapper.error("goal_required")
            val current = dailyGoalsRepository.fetchGoalById(goalId).first() ?: return AiToolResultMapper.error("goal_not_found")

            val desiredTimeSource = validator.optional(args, "desiredTime")
            val desiredTime = desiredTimeSource?.toLongOrNull()?.let { it * 60_000L }

            val updated = current.copy(
                desiredTime = if (desiredTimeSource != null) desiredTime else current.desiredTime,
                updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
            )
            dailyGoalsRepository.addOrUpdateGoal(updated)
            return AiToolResultMapper.success("goal_updated")
        }

        private suspend fun completeGoal(args: Map<String, String>): String {
            val goalId = validator.required(args, "goalId") ?: return AiToolResultMapper.error("goal_required")
            val completed = validator.boolean(validator.required(args, "completed")) ?: return AiToolResultMapper.error("invalid_completed_state")
            val current = dailyGoalsRepository.fetchGoalById(goalId).first() ?: return AiToolResultMapper.error("goal_not_found")

            if (current.isDone == completed) return AiToolResultMapper.success("goal_already_in_state")

            if (completed) {
                completeLinkedGoal(current)
            } else {
                reopenLinkedGoal(current)
            }

            val currentTime = dateManager.fetchCurrentInstant()
            val updatedAt = currentTime.toEpochMilliseconds()

            if (completed) {
                when (current.contentType) {
                    GoalType.HOMEWORK -> current.contentHomework?.let { hw ->
                        homeworksRepository.fetchHomeworkById(hw.uid).first()?.let { homework ->
                            homeworksRepository.addOrUpdateHomework(
                                homework.copy(isDone = true, completeDate = currentTime, updatedAt = updatedAt),
                            )
                        }
                    }
                    GoalType.TODO -> current.contentTodo?.let { t ->
                        todoRepository.fetchTodoById(t.uid).first()?.let { todo ->
                            todoRepository.addOrUpdateTodo(
                                todo.copy(isDone = true, completeDate = currentTime, updatedAt = updatedAt),
                            )
                            todoReminderManager.clearAllReminders(todo.uid)
                        }
                    }
                }
            } else {
                when (current.contentType) {
                    GoalType.HOMEWORK -> current.contentHomework?.let { hw ->
                        homeworksRepository.fetchHomeworkById(hw.uid).first()?.let { homework ->
                            homeworksRepository.addOrUpdateHomework(
                                homework.copy(isDone = false, completeDate = null, updatedAt = updatedAt),
                            )
                        }
                    }
                    GoalType.TODO -> current.contentTodo?.let { t ->
                        todoRepository.fetchTodoById(t.uid).first()?.let { todo ->
                            val reopenedTodo = todo.copy(
                                isDone = false,
                                completeDate = null,
                                updatedAt = updatedAt,
                            )
                            todoRepository.addOrUpdateTodo(reopenedTodo)
                            todoReminderManager.scheduleReminders(
                                reopenedTodo.uid,
                                reopenedTodo.name,
                                reopenedTodo.deadline,
                                reopenedTodo.notifications,
                            )
                        }
                    }
                }
            }

            return AiToolResultMapper.success("goal_completion_updated")
        }



        private suspend fun createSubject(args: Map<String, String>): String {
            val organizationId = validator.required(args, "organizationId") ?: return AiToolResultMapper.error("organization_required")
            if (organizationsRepository.fetchShortOrganizationById(organizationId).first() == null) {
                return AiToolResultMapper.error("organization_not_found")
            }
            val name = validator.required(args, "name") ?: return AiToolResultMapper.error("name_required")
            val eventTypeSource = validator.required(args, "eventType") ?: return AiToolResultMapper.error("event_type_required")
            val eventType = EventType.entries.find {
                it.name.equals(eventTypeSource, ignoreCase = true)
            } ?: return AiToolResultMapper.error("invalid_event_type")

            val teacherId = validator.optional(args, "teacherId")
            val teacher = teacherId?.let { employeeRepository.fetchEmployeeById(it).first() }
            if (teacherId != null && teacher == null) return AiToolResultMapper.error("teacher_not_found")

            val colorName = args["color"]
            val usedColors = subjectsRepository.fetchAllSubjectsByOrganization(organizationId)
                .first()
                .map(Subject::color)
            val color = colorName?.let { name ->
                runCatching { CustomColors.valueOf(name).light.toInt() }.getOrNull()
            } ?: CustomColors.randomUnusedArgb(usedColors)

            val subject = Subject(
                uid = randomUUID(),
                organizationId = organizationId,
                eventType = eventType,
                name = name,
                teacher = teacher,
                office = validator.optional(args, "office").orEmpty(),
                color = color,
                location = validator.optional(args, "location")?.let { ContactInfo(it) },
                updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
            )
            subjectsRepository.addOrUpdateSubject(subject)
            return AiToolResultMapper.success("subject_created")
        }

        private suspend fun updateSubject(args: Map<String, String>): String {
            val subjectId = validator.required(args, "subjectId") ?: return AiToolResultMapper.error("subject_required")
            val current = subjectsRepository.fetchSubjectById(subjectId).first() ?: return AiToolResultMapper.error("subject_not_found")

            val teacherId = validator.optional(args, "teacherId")
            val teacher = teacherId?.let { employeeRepository.fetchEmployeeById(it).first() }
            if (teacherId != null && teacher == null) return AiToolResultMapper.error("teacher_not_found")

            val colorName = args["color"]
            val color = colorName?.let { name ->
                runCatching { CustomColors.valueOf(name).light.toInt() }.getOrNull()
            } ?: current.color

            val updated = current.copy(
                organizationId = validator.optional(args, "organizationId") ?: current.organizationId,
                name = validator.optional(args, "name") ?: current.name,
                eventType = validator.optional(args, "eventType")?.let { source ->
                    EventType.entries.find { it.name.equals(source, ignoreCase = true) }
                } ?: current.eventType,
                teacher = if (teacherId != null) teacher else current.teacher,
                office = validator.optional(args, "office") ?: current.office,
                color = color,
                location = validator.optional(args, "location")?.let { ContactInfo(it) } ?: current.location,
                updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
            )
            subjectsRepository.addOrUpdateSubject(updated)
            return AiToolResultMapper.success("subject_updated")
        }

        private suspend fun createEmployee(args: Map<String, String>): String {
            val organizationId = validator.required(args, "organizationId") ?: return AiToolResultMapper.error("organization_required")
            if (organizationsRepository.fetchShortOrganizationById(organizationId).first() == null) {
                return AiToolResultMapper.error("organization_not_found")
            }
            val firstName = validator.required(args, "firstName") ?: return AiToolResultMapper.error("first_name_required")
            val postSource = validator.required(args, "post") ?: return AiToolResultMapper.error("post_required")
            val post = EmployeePost.entries.find {
                it.name.equals(postSource, ignoreCase = true)
            } ?: return AiToolResultMapper.error("invalid_post")

            val employee = Employee(
                uid = randomUUID(),
                organizationId = organizationId,
                firstName = firstName,
                secondName = validator.optional(args, "secondName"),
                patronymic = validator.optional(args, "patronymic"),
                post = post,
                birthday = validator.optional(args, "birthday"),
                emails = validator.list(args["emails"]).map { ContactInfo(it) },
                phones = validator.list(args["phones"]).map { ContactInfo(it) },
                locations = validator.list(args["locations"]).map { ContactInfo(it) },
                webs = validator.list(args["webs"]).map { ContactInfo(it) },
                updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
            )
            employeeRepository.addOrUpdateEmployee(employee)
            return AiToolResultMapper.success("employee_created")
        }

        private suspend fun updateEmployee(args: Map<String, String>): String {
            val teacherId = validator.required(args, "teacherId") ?: return AiToolResultMapper.error("employee_required")
            val current = employeeRepository.fetchEmployeeById(teacherId).first() ?: return AiToolResultMapper.error("employee_not_found")

            val updated = current.copy(
                organizationId = validator.optional(args, "organizationId") ?: current.organizationId,
                firstName = validator.optional(args, "firstName") ?: current.firstName,
                secondName = validator.optional(args, "secondName") ?: current.secondName,
                patronymic = validator.optional(args, "patronymic") ?: current.patronymic,
                post = validator.optional(args, "post")?.let { source ->
                    EmployeePost.entries.find { it.name.equals(source, ignoreCase = true) }
                } ?: current.post,
                birthday = validator.optional(args, "birthday") ?: current.birthday,
                emails = if ("emails" in args) {
                    validator.list(args["emails"]).map { ContactInfo(it) }
                } else {
                    current.emails
                },
                phones = if ("phones" in args) {
                    validator.list(args["phones"]).map { ContactInfo(it) }
                } else {
                    current.phones
                },
                locations = if ("locations" in args) {
                    validator.list(args["locations"]).map { ContactInfo(it) }
                } else {
                    current.locations
                },
                webs = if ("webs" in args) validator.list(args["webs"]).map { ContactInfo(it) } else current.webs,
                updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
            )
            employeeRepository.addOrUpdateEmployee(updated)
            return AiToolResultMapper.success("employee_updated")
        }

        private suspend fun classesByDate(date: Instant): List<Class> {
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
            val subjectId = validator.required(args, "subjectId") ?: return AiToolResultMapper.error("subject_required")
            val now = dateManager.fetchCurrentInstant()
            val lastDate = now.endOfWeek().shiftWeek(1)
            val nearest = buildList {
                var date = now.startThisDay()
                while (date <= lastDate) {
                    val elements = classesByDate(date).mapNotNull { classModel ->
                        val classEnd = date.setHoursAndMinutes(classModel.timeRange.to)
                        val matchesSubject = classModel.subject?.uid == subjectId
                        if (matchesSubject && classEnd > now) {
                            classModel.copy(
                                timeRange = TimeRange(
                                    from = date.setHoursAndMinutes(classModel.timeRange.from),
                                    to = classEnd,
                                ),
                            )
                        } else {
                            null
                        }
                    }
                    addAll(elements)
                    date = date.shiftDay(1)
                }
            }.minByOrNull { it.timeRange.from } ?: return AiToolResultMapper.noClass()
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
            val dateKeys = setOf("deadline", "date", "startTime", "endTime")
            return buildMap {
                putAll(args.filterKeys { key -> !key.endsWith("Id") && key !in dateKeys })
                args["deadline"]?.let { value ->
                    val formatted = runCatching {
                        Instant.parseUsingOffset(value).formatByTimeZone(DateTimeComponents.Formats.shortDayMonthTimeFormat())
                    }.getOrNull() ?: value
                    put("deadline", formatted)
                }
                args["date"]?.let { value ->
                    val formatted = runCatching {
                        validator.date(value)?.formatByTimeZone(DateTimeComponents.Formats.dayMonthYearFormat())
                    }.getOrNull() ?: value
                    put("date", formatted)
                }
                args["startTime"]?.let { value ->
                    val formatted = runCatching {
                        validator.time(value)?.let { time ->
                            val hour = time.hour.toString().padStart(2, '0')
                            val minute = time.minute.toString().padStart(2, '0')
                            "$hour:$minute"
                        }
                    }.getOrNull() ?: value
                    put("startTime", formatted)
                }
                args["endTime"]?.let { value ->
                    val formatted = runCatching {
                        validator.time(value)?.let { time ->
                            val hour = time.hour.toString().padStart(2, '0')
                            val minute = time.minute.toString().padStart(2, '0')
                            "$hour:$minute"
                        }
                    }.getOrNull() ?: value
                    put("endTime", formatted)
                }
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
                        put("employee", listOfNotNull(employee.secondName, employee.firstName, employee.patronymic).joinToString(" "))
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
            val TODO_STATUSES = setOf("ALL", "ACTIVE", "COMPLETED")
        }
    }
}
