/*
 * Copyright 2024 Stanislav Aleshin
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

package ru.aleshin.studyassistant.chat.impl.domain.interactors

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents.Formats
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ru.aleshin.studyassistant.chat.impl.domain.common.ChatEitherWrapper
import ru.aleshin.studyassistant.chat.impl.domain.entities.ChatFailures
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.parseUsingOffset
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage.Type
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChat
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChatHistory
import ru.aleshin.studyassistant.core.domain.entities.ai.ToolCall
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.AiAssistantRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.core.ui.views.TIME_SUFFIX
import ru.aleshin.studyassistant.core.ui.views.dayMonthYearFormat
import ru.aleshin.studyassistant.core.ui.views.iso8601
import ru.aleshin.studyassistant.core.ui.views.timeFormat

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
internal interface AiAssistantInteractor {

    suspend fun addChat(): DomainResult<ChatFailures, UID>
    suspend fun fetchChats(): FlowDomainResult<ChatFailures, List<AiChat>>
    suspend fun fetchChatHistory(chatId: UID): FlowDomainResult<ChatFailures, AiChatHistory>
    suspend fun clearHistory(chatId: UID): UnitDomainResult<ChatFailures>
    suspend fun sendMessage(chatId: UID, message: String?): UnitDomainResult<ChatFailures>

    class Base(
        private val aiAssistantRepository: AiAssistantRepository,
        private val todoRepository: TodoRepository,
        private val homeworksRepository: HomeworksRepository,
        private val subjectsRepository: SubjectsRepository,
        private val organizationsRepository: OrganizationsRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val employeeRepository: EmployeeRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val todoReminderManager: TodoReminderManager,
        private val usersRepository: UsersRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: ChatEitherWrapper,
    ) : AiAssistantInteractor {

        override suspend fun addChat() = eitherWrapper.wrap {
            val appUserInfo = checkNotNull(usersRepository.fetchCurrentUserProfile().first())
            val chatId = randomUUID()
            val systemMessage = AiAssistantMessage.SystemMessage(
                id = chatId,
                content = systemPromt(
                    username = appUserInfo.username,
                    birthday = appUserInfo.birthday,
                    currentDate = dateManager.fetchCurrentInstant().formatByTimeZone(
                        format = Formats.dayMonthYearFormat()
                    )
                ),
                time = dateManager.fetchCurrentInstant(),
            )
            val aiChatHistory = AiChatHistory(messages = listOf(systemMessage))
            aiAssistantRepository.addOrUpdateChat(aiChatHistory)

            return@wrap chatId
        }

        override suspend fun fetchChats() = eitherWrapper.wrapFlow {
            aiAssistantRepository.fetchAllChats()
        }

        override suspend fun fetchChatHistory(chatId: UID) = eitherWrapper.wrapFlow {
            aiAssistantRepository.fetchChatHistoryById(chatId).map { chat ->
                if (chat == null) throw NullPointerException("Chat($chatId) is not found")
                val messages = chat.messages.filter {
                    (it.type == Type.USER || it.type == Type.ASSISTANT) && !it.content.isNullOrEmpty()
                }.sortedBy { it.time }
                chat.copy(
                    messages = messages,
                    lastMessage = if (chat.lastMessage?.type == Type.USER || chat.lastMessage?.type == Type.ASSISTANT) {
                        chat.lastMessage
                    } else {
                        null
                    }
                )
            }
        }

        override suspend fun clearHistory(chatId: UID) = eitherWrapper.wrapUnit {
            val chat = aiAssistantRepository.fetchChatHistoryById(chatId).first()

            if (chat != null) {
                val appUserInfo = checkNotNull(usersRepository.fetchCurrentUserProfile().first())
                val systemMessage = AiAssistantMessage.SystemMessage(
                    id = chatId,
                    content = systemPromt(
                        username = appUserInfo.username,
                        birthday = appUserInfo.birthday,
                        currentDate = dateManager.fetchCurrentInstant().formatByTimeZone(
                            format = Formats.dayMonthYearFormat()
                        )
                    ),
                    time = dateManager.fetchCurrentInstant(),
                )
                val clearedChat = chat.copy(messages = listOf(systemMessage), lastMessage = null)
                aiAssistantRepository.addOrUpdateChat(clearedChat)
            }
        }

        override suspend fun sendMessage(chatId: UID, message: String?) = eitherWrapper.wrapUnit {
            val currentTime = dateManager.fetchCurrentInstant()
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            val lastMessage = aiAssistantRepository.fetchChatHistoryLastMessage(chatId).first()

            if (lastMessage?.time?.equalsDay(currentTime) != true) {
                val systemMessage = AiAssistantMessage.SystemMessage(
                    id = chatId,
                    content = updatedActualInfoPromt(
                        currentDate = dateManager.fetchCurrentInstant().format(
                            format = Formats.dayMonthYearFormat()
                        )
                    ),
                    time = dateManager.fetchCurrentInstant(),
                )
                aiAssistantRepository.updateSystemPromt(chatId, systemMessage)
            }
            val userMessage = message?.let { AiAssistantMessage.UserMessage(content = it, time = currentTime) }
            val response = aiAssistantRepository.sendUserMessage(chatId, userMessage)

            val assistantMessage = response.choices.firstOrNull()?.message
            handleMessage(chatId, assistantMessage, targetUser)
        }

        private suspend fun handleMessage(
            chatId: UID,
            assistantMessage: AiAssistantMessage?,
            targetUser: UID,
        ) {
            val toolCalls = (assistantMessage as AiAssistantMessage.AssistantMessage).toolCalls
            aiAssistantRepository.saveAssistantMessage(chatId, assistantMessage)

            if (toolCalls != null && toolCalls.isNotEmpty()) {
                val handleResult = handleToolCalls(toolCalls, targetUser)
                Logger.i("test") { "handleResult -> $handleResult" }
                val toolResponse = aiAssistantRepository.sendToolResponse(chatId, handleResult)
                handleMessage(chatId, toolResponse.choices.firstOrNull()?.message, targetUser)
            }
        }

        private suspend fun handleToolCalls(
            toolCalls: List<ToolCall>,
            targetUser: UID,
        ): List<AiAssistantMessage.ToolMessage> {
            return toolCalls.map { call ->
                val functionName = call.function.name
                val functionArgs = call.function.arguments ?: emptyMap()

                val resultContent = when (functionName) {
                    "create_todo" -> createTodo(functionArgs)
                    "create_homework" -> createHomeworks(functionArgs)
                    "get_homeworks" -> getHomeworks(functionArgs)
                    "get_overdue_homeworks" -> getOverdueHomeworks(functionArgs)
                    "get_subjects" -> getSubjects(functionArgs)
                    "get_employee" -> getEmployee(functionArgs)
                    "get_organizations" -> getOrganizations(functionArgs)
                    "get_classes_by_date" -> getClassesByDate(functionArgs)
                    "get_near_class" -> getNearClass(functionArgs)
                    else -> """{"error": "Функция $functionName не найдена"}"""
                }

                AiAssistantMessage.ToolMessage(
                    content = resultContent,
                    toolCallId = call.id,
                    time = dateManager.fetchCurrentInstant(),
                )
            }
        }

        private suspend fun createTodo(args: Map<String, String>): String {
            val name = args["name"] ?: "Без названия"
            val description = args["description"] ?: ""
            val deadline = args["deadline"]?.let { Instant.parseUsingOffset(it, Formats.iso8601()) }
            val priority = args["priority"]?.let { TaskPriority.valueOf(it) }
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val todo = Todo(
                uid = randomUUID(),
                name = name,
                description = description,
                deadline = deadline,
                priority = priority ?: TaskPriority.STANDARD,
                updatedAt = updatedAt,
            )
            return try {
                todoRepository.addOrUpdateTodo(todo)
                todoReminderManager.scheduleReminders(todo.uid, todo.name, todo.deadline, todo.notifications)
                """{"status": "success", "message": "Задача '${todo.name}' создана!"}"""
            } catch (e: Exception) {
                """{"error": "Произошла ошибка при создании TODO (${e.message})"}"""
            }
        }

        private suspend fun createHomeworks(args: Map<String, String>): String {
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val organization = args["organizationId"]?.let {
                organizationsRepository.fetchShortOrganizationById(it).first()
            }
            val subject = args["subjectId"]?.let {
                subjectsRepository.fetchSubjectById(it).first()
            }
            val deadline = args["deadline"]?.let {
                Instant.parseUsingOffset(it + TIME_SUFFIX, Formats.iso8601()).startThisDay()
            }
            val classId = args["classId"]
            val theoreticalTasks = args["theoreticalTasks"] ?: ""
            val practicalTasks = args["practicalTasks"] ?: ""
            val presentationTasks = args["presentationTasks"] ?: ""
            val testTopic = args["testTopic"]

            val homework = Homework(
                uid = randomUUID(),
                classId = classId,
                deadline = deadline ?: return """{"error": "Дедлайн не указан"}""",
                subject = subject ?: return """{"error": "Предмет не найден"}""",
                organization = organization ?: return """{"error": "Ораганизация не найдена"}""",
                theoreticalTasks = theoreticalTasks,
                practicalTasks = practicalTasks,
                presentationTasks = presentationTasks,
                test = testTopic,
                updatedAt = updatedAt,
            )
            Logger.i("test") { "create homework -> $homework" }
            return try {
                homeworksRepository.addOrUpdateHomework(homework)
                """{"status": "success", "message": "Домашнее задание создано!"}"""
            } catch (e: Exception) {
                """{"error": "Произошла ошибка при создании ДЗ (${e.message})"}"""
            }
        }

        private suspend fun getHomeworks(args: Map<String, String>): String {
            val from = args["from"] ?: return """{"error": "Дата периода не найдена"}"""
            val to = args["to"] ?: return """{"error": "Дата периода не найдена"}"""
            val timeRange = TimeRange(
                from = Instant.parseUsingOffset(from + TIME_SUFFIX, Formats.iso8601()).startThisDay(),
                to = Instant.parseUsingOffset(to + TIME_SUFFIX, Formats.iso8601()).endThisDay(),
            )
            val homeworks = homeworksRepository.fetchHomeworksByTimeRange(timeRange).first()
            Logger.i("test") { "getHomeworks($timeRange) -> $homeworks" }
            return buildJsonArray {
                homeworks.forEach { homework ->
                    addJsonObject {
                        put("homeworkId", homework.uid)
                        put("classId", homework.classId)
                        put("subjectId", homework.subject?.uid)
                        put("deadline", homework.deadline.formatByTimeZone(Formats.iso8601()))
                        val task = buildString {
                            if (homework.theoreticalTasks.isNotEmpty()) {
                                append("Theoretical: ${homework.theoreticalTasks} ")
                            }
                            if (homework.practicalTasks.isNotEmpty()) {
                                append("Practical: ${homework.practicalTasks} ")
                            }
                            if (homework.presentationTasks.isNotEmpty()) {
                                append("Presentations: ${homework.presentationTasks} ")
                            }
                        }
                        put("task", task)
                        put("test", homework.test)
                        put("isDone", homework.isDone)
                    }
                }
            }.toString()
        }

        private suspend fun getOverdueHomeworks(args: Map<String, String>): String {
            val currentDate = dateManager.fetchBeginningCurrentInstant()
            val homeworks = homeworksRepository.fetchOverdueHomeworks(currentDate).first()
            Logger.i("test") { "getOverdueHomeworks -> $homeworks" }
            return buildJsonArray {
                homeworks.forEach { homework ->
                    addJsonObject {
                        put("homeworkId", homework.uid)
                        put("classId", homework.classId)
                        put("subjectId", homework.subject?.uid)
                        put("deadline", homework.deadline.formatByTimeZone(Formats.iso8601()))
                        val task = buildString {
                            if (homework.theoreticalTasks.isNotEmpty()) {
                                append("Theoretical: ${homework.theoreticalTasks} ")
                            }
                            if (homework.practicalTasks.isNotEmpty()) {
                                append("Practical: ${homework.practicalTasks} ")
                            }
                            if (homework.presentationTasks.isNotEmpty()) {
                                append("Presentations: ${homework.presentationTasks} ")
                            }
                        }
                        put("task", task)
                        put("test", homework.test)
                        put("isDone", homework.isDone)
                    }
                }
            }.toString()
        }

        private suspend fun getOrganizations(args: Map<String, String>): String {
            val organizations = organizationsRepository.fetchAllShortOrganization().first()
            Logger.i("test") { "getOrganizations -> $organizations" }
            return buildJsonArray {
                organizations.forEach { organization ->
                    addJsonObject {
                        put("organizationId", organization.uid)
                        put("name", organization.shortName)
                        put("organizationType", organization.type.toString())
                    }
                }
            }.toString()
        }

        private suspend fun getSubjects(args: Map<String, String>): String {
            val organizationId = args["organizationId"] ?: return """{"error": "Организация не найдена"}"""
            val subjects = subjectsRepository.fetchAllSubjectsByOrganization(organizationId).first()
            Logger.i("test") { "getSubjects(org: $organizationId) -> $subjects" }
            return buildJsonArray {
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
        }

        private suspend fun getEmployee(args: Map<String, String>): String {
            val teacherId = args["teacherId"] ?: return """{"error": "Сотрудник не найден"}"""
            val teacher = employeeRepository.fetchEmployeeById(teacherId).first()
            if (teacher == null) return """{"error": "Сотрудник не найден"}"""
            return buildJsonObject {
                put("teacherId", teacherId)
                put("organizationId", teacher.organizationId)
                put("name", (teacher.secondName ?: "") + teacher.firstName + (teacher.patronymic ?: ""))
                put("post", teacher.post.toString())
            }.toString()
        }

        private suspend fun getClassesByDate(args: Map<String, String>): String {
            val date = args["date"]?.let {
                Instant.parseUsingOffset(it + TIME_SUFFIX, Formats.iso8601()).startThisDay()
            } ?: return """{"error": "Ошибка получения даты"}"""
            val calendarSettings = calendarSettingsRepository.fetchSettings().first()
            val currentNumberOfWeek = date.dateTime().date.numberOfRepeatWeek(calendarSettings.numberOfWeek)
            val holidays = calendarSettings.holidays

            val baseSchedule = baseScheduleRepository.fetchScheduleByDate(date, currentNumberOfWeek).first()
            val customSchedule = customScheduleRepository.fetchScheduleByDate(date).first()
            val classes = if (customSchedule != null) {
                customSchedule.classes.sortedBy { it.timeRange.from.dateTime().time }
            } else {
                val filteredClasses = baseSchedule?.classes?.filter { classModel ->
                    holidays.none {
                        val dateFilter = TimeRange(it.start, it.end).containsDate(date)
                        val organizationFilter = it.organizations.contains(classModel.organization.uid)
                        return@none dateFilter && organizationFilter
                    }
                }
                filteredClasses?.sortedBy { it.timeRange.from.dateTime().time } ?: emptyList()
            }
            Logger.i("test") { "getClassesByDate -> arg: ${args["date"]}, parseUsingOffset: $date " }
            return buildJsonArray {
                classes.forEach { classModel ->
                    addJsonObject {
                        put("classId", classModel.uid)
                        put("scheduleId", classModel.scheduleId)
                        put("organizationId", classModel.organization.uid)
                        put("eventType", classModel.eventType.toString())
                        put("subjectId", classModel.subject?.uid)
                        put("teacherId", classModel.teacher?.uid)
                        put("office", classModel.office)
                        put("location", classModel.location?.toString())
                        val timeFormat = Formats.timeFormat()
                        put("startTime", classModel.timeRange.from.formatByTimeZone(timeFormat))
                        put("endTime", classModel.timeRange.to.formatByTimeZone(timeFormat))
                    }
                }
            }.toString()
        }

        private suspend fun getNearClass(args: Map<String, String>): String {
            val subjectId = args["subjectId"] ?: return """{"error": "Ошибка получения предмета"}"""
            val currentDate = dateManager.fetchBeginningCurrentInstant()
            val maxNumberOfWeek = calendarSettingsRepository.fetchSettings().first().numberOfWeek

            val searchedTimeRange = TimeRange(
                from = currentDate.startThisDay().shiftDay(1),
                to = currentDate.endOfWeek().shiftWeek(1),
            )

            val customSchedules = customScheduleRepository.fetchSchedulesByTimeRange(
                timeRange = searchedTimeRange,
            ).first()
            val baseSchedules = baseScheduleRepository.fetchSchedulesByTimeRange(
                timeRange = searchedTimeRange,
                maxNumberOfWeek = maxNumberOfWeek,
            ).first()

            val classesMap = buildMap {
                customSchedules.forEach { customSchedule ->
                    if (customSchedule.classes.isNotEmpty()) {
                        val classesWithTargetSubject = customSchedule.classes.filter { classModel ->
                            val subjectFilter = classModel.subject?.uid == subjectId
                            return@filter subjectFilter
                        }
                        put(customSchedule.date, classesWithTargetSubject)
                    }
                }
                val availableBaseSchedules = baseSchedules.filter { !containsKey(it.key) }
                availableBaseSchedules.toList().forEach { baseScheduleEntry ->
                    val baseSchedule = baseScheduleEntry.second
                    if (baseSchedule?.classes?.isNotEmpty() == true) {
                        val classesWithTargetSubject = baseSchedule.classes.filter { classModel ->
                            val subjectFilter = classModel.subject?.uid == subjectId
                            return@filter subjectFilter
                        }
                        put(baseScheduleEntry.first, classesWithTargetSubject)
                    }
                }
            }
            val classModel = classesMap[classesMap.keys.minByOrNull { it.toEpochMilliseconds() }]?.getOrNull(0)
            Logger.i("test") { "getNearClass(sub: $subjectId) -> $classModel | all classes -> $classesMap" }
            if (classModel == null) return """{"success": "null"}"""
            return buildJsonObject {
                put("classId", classModel.uid)
                put("scheduleId", classModel.scheduleId)
                put("organizationId", classModel.organization.uid)
                put("eventType", classModel.eventType.toString())
                put("subjectId", classModel.subject?.uid)
                put("teacherId", classModel.teacher?.uid)
                put("office", classModel.office)
                put("location", classModel.location?.toString())
                val timeFormat = Formats.timeFormat()
                put("startTime", classModel.timeRange.from.formatByTimeZone(timeFormat))
                put("endTime", classModel.timeRange.to.formatByTimeZone(timeFormat))
            }.toString()
        }

        companion object {
            fun systemPromt(
                username: String,
                birthday: String?,
                currentDate: String,
            ) = "Ты — вежливый и дружелюбный учебный помощник StudyAssistant. " +
                "Помогаешь с органиизацией учёбы, решаешь задачи и объясняешь темы. " +
                "Пользователь: $username ${birthday ?: ""} — адаптируйся под возраст. " +
                "Сегодня: $currentDate, \"Завтра\" = текущая дата + 1 день. " +
                "Отвечай только по делу и вежливо, без лирических отступлений. " +
                "Предлагай и используй функции, если данных недостаточно проси уточнения" +
                "Сообщения вне тем — мягко направь к учёбе. " +
                "Отвечай ВСЕГДА на языке пользователя. Определяй язык по полследнему сообщению и переводи все на него " +
                "Используй ТОЛЬКО простой markdown ВСЕГДА а именно: заголовки, списки, жирный, курсив. " +
                "\"Не используй LaTeX (например: \\\\log_b, \\\\frac, ^, _), mathtext, //, backticks (``), HTML, код или таблицы. Формулы пиши простыми словами или обычными знаками: например 'log_b(a) = c', 'b^c = a', 'x^2', 'a/b'. " +
                "Если нужно отобразить урок/занятие отобрази название предмета (его можно найти вызвав get_subjects). " +
                "НИКОГДА НЕ ОТОБРАЖАЙ ЛЮБЫЕ ID а получай или находи по ним названия. ПОЛЬЗОВАТЕЛЬ НЕ ЗНАЕТ НИКАКИХ id итд. Расписание это уроки, задания к ним и TODO " +
                "Особые правила для некоторых функций: " +
                "1) create_homework: " +
                "1. Вызови get_organizations, найди organisationId по названию. " +
                "2. Вызови get_subjects(organisationId), найди нужный предмет. " +
                "3. Привяжи classid: 3.1 Если указана дата ДЗ вызови get_classes_by_date(deadline), выбери первый classId, где совпадает subjectId, если нет — оставь classId пустым. 3.2 Если пользователь сказал создать ДЗ на ближайший урок то вызови get_near_class(subjectId) если его нету — оставь classId пустым " +
                "4. Создай ДЗ с этими данными."
        }
        fun updatedActualInfoPromt(
            currentDate: String
        ) = "Дата обновлена, сегодня: $currentDate учитывай это при формировании функций"
    }
}