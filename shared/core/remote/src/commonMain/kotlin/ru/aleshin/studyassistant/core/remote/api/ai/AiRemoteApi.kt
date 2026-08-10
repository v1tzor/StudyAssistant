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

package ru.aleshin.studyassistant.core.remote.api.ai

import dev.tmapps.konnection.Konnection
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.delay
import kotlinx.io.IOException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceException
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.remote.ktor.StudyAssistantKtor
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionResponsePojo
import ru.aleshin.studyassistant.core.remote.models.ai.DeepSeekException
import ru.aleshin.studyassistant.core.remote.models.ai.FunctionRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ai.ToolCallTypePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ToolPojo
import ru.aleshin.studyassistant.core.remote.models.ai.bodyOrAiError
import kotlin.random.Random

/**
 * @author Stanislav Aleshin on 14.06.2025.
 */
interface AiRemoteApi {
    suspend fun chatCompletion(
        request: ChatCompletionRequestPojo,
        credential: String,
        requestKey: String? = null,
    ): AiCompletionResult

    class DeepSeek(
        private val httpClient: HttpClient,
        private val connectionManager: Konnection,
    ) : AiRemoteApi {

        override suspend fun chatCompletion(
            request: ChatCompletionRequestPojo,
            credential: String,
            requestKey: String?,
        ): AiCompletionResult {
            if (!connectionManager.isConnected()) throw InternetConnectionException()
            repeat(MAX_ATTEMPTS) { attempt ->
                try {
                    val response = httpClient.post(StudyAssistantKtor.DeepSeek.CHAT_COMPLETIONS) {
                        bearerAuth(credential)
                        setBody(request)
                    }
                    return AiCompletionResult(
                        response = response.bodyOrAiError<ChatCompletionResponsePojo>(),
                    )
                } catch (error: DeepSeekException) {
                    if (error.statusCode !in RETRYABLE_STATUS_CODES || attempt == MAX_ATTEMPTS - 1) {
                        throw error.mapToServiceException()
                    }
                    delay(error.retryDelayMillis(attempt))
                } catch (_: IOException) {
                    if (attempt == MAX_ATTEMPTS - 1) throw InternetConnectionException()
                    delay(retryDelayMillis(attempt))
                }
            }
            throw AiServiceException.ServerUnavailable()
        }

        private fun DeepSeekException.retryDelayMillis(attempt: Int): Long {
            val retryAfterSeconds = headers[HttpHeaders.RetryAfter]?.toLongOrNull()
            return retryAfterSeconds?.times(1_000L) ?: retryDelayMillis(attempt)
        }

        private fun retryDelayMillis(attempt: Int): Long {
            val exponential = 500L * (1L shl attempt)
            return exponential + Random.nextLong(100L, 400L)
        }

        private fun DeepSeekException.mapToServiceException(): Throwable = when (statusCode) {
            401, 403 -> AiServiceException.InvalidKey()
            402 -> AiServiceException.InsufficientBalance()
            429 -> AiServiceException.RateLimited()
            500, 503 -> AiServiceException.ServerUnavailable()
            else -> this
        }

        companion object Companion {
            private const val MAX_ATTEMPTS = 3
            private val RETRYABLE_STATUS_CODES = setOf(429, 500, 503)

            val createTodoTool = ToolPojo(
                type = ToolCallTypePojo.FUNCTION,
                function = FunctionRequestPojo(
                    name = "create_todo",
                    description = "Создать задачу (TODO)",
                    parameters = JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("object"),
                            "properties" to JsonObject(
                                mapOf(
                                    "name" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Короткий заголовок задачи"),
                                        )
                                    ),
                                    "description" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Подробное описание задачи")
                                        )
                                    ),
                                    "priority" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "enum" to JsonArray(
                                                TaskPriority.entries.map { JsonPrimitive(it.toString()) }
                                            ),
                                            "description" to JsonPrimitive("Приоритет/Важность"),
                                        )
                                    ),
                                    "deadline" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Дедлайн ISO 8601 (н.п: 2025-07-29T00:00:00)")
                                        )
                                    )
                                )
                            ),
                            "required" to JsonArray(listOf(JsonPrimitive("name"))),
                        )
                    )
                )
            )
            val createHomework = ToolPojo(
                type = ToolCallTypePojo.FUNCTION,
                function = FunctionRequestPojo(
                    name = "create_homework",
                    description = "Создать домашнее задание. Нужен хотя бы один блок задач",
                    parameters = JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("object"),
                            "properties" to JsonObject(
                                mapOf(
                                    "organizationId" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("ID организации"),
                                        )
                                    ),
                                    "subjectId" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("ID предмета"),
                                        )
                                    ),
                                    "classId" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("ID привязанного урока"),
                                        )
                                    ),
                                    "deadline" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Дата дедлайна (YYYY-MM-DD)"),
                                        )
                                    ),
                                    "theoreticalTasks" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Задания на теорию: изучение, прочтение, итд"),
                                        )
                                    ),
                                    "practicalTasks" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Задачи на практику: решить, начеритить, итд")
                                        )
                                    ),
                                    "presentationTasks" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Задачи на создание докладов, презенатаций, проктов итд"),
                                        )
                                    ),
                                    "testTopic" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Заголовок теста, экзамена, контрольной итд (если есть)")
                                        )
                                    )
                                )
                            ),
                            "required" to JsonArray(
                                listOf(
                                    JsonPrimitive("organizationId"),
                                    JsonPrimitive("subjectId"),
                                    JsonPrimitive("deadline")
                                )
                            ),
                        )
                    )
                )
            )
            val getHomeworksTool = ToolPojo(
                type = ToolCallTypePojo.FUNCTION,
                function = FunctionRequestPojo(
                    name = "get_homeworks",
                    description = "Получить ДЗ (homeworkId, classId, subjectId, deadline, task, test, isDone) за период (макс. 2 недели)",
                    parameters = JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("object"),
                            "properties" to JsonObject(
                                mapOf(
                                    "from" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Начало периода (YYYY-MM-DD)"),
                                        )
                                    ),
                                    "to" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Конец периода (YYYY-MM-DD)"),
                                        )
                                    ),
                                )
                            ),
                            "required" to JsonArray(
                                listOf(
                                    JsonPrimitive("from"),
                                    JsonPrimitive("to")
                                )
                            ),
                        )
                    )
                )
            )
            val getOverdueHomeworksTool = ToolPojo(
                type = ToolCallTypePojo.FUNCTION,
                function = FunctionRequestPojo(
                    name = "get_overdue_homeworks",
                    description = "Получить просроченные ДЗ",
                    parameters = null,
                )
            )
            val getSubjectsTool = ToolPojo(
                type = ToolCallTypePojo.FUNCTION,
                function = FunctionRequestPojo(
                    name = "get_subjects",
                    description = "Получить предметы (subjectId, organizationId, teacherId, name, eventType) по организации",
                    parameters = JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("object"),
                            "properties" to JsonObject(
                                mapOf(
                                    "organizationId" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("ID организации"),
                                        )
                                    ),
                                )
                            ),
                            "required" to JsonArray(listOf(JsonPrimitive("organizationId"))),
                        )
                    )
                )
            )

            val getEmployeeTool = ToolPojo(
                type = ToolCallTypePojo.FUNCTION,
                function = FunctionRequestPojo(
                    name = "get_employee",
                    description = "Получить данные учителя/сотрудника (teacherId, organizationId, name, post)",
                    parameters = JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("object"),
                            "properties" to JsonObject(
                                mapOf(
                                    "teacherId" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("id учителя или сотрудника"),
                                        )
                                    ),
                                )
                            ),
                            "required" to JsonArray(listOf(JsonPrimitive("teacherId"))),
                        )
                    )
                )
            )
            val getOrganizationsTool = ToolPojo(
                type = ToolCallTypePojo.FUNCTION,
                function = FunctionRequestPojo(
                    name = "get_organizations",
                    description = "Получить список организаций (organizationId, name, organizationType)",
                    parameters = null,
                )
            )
            val getClassesByDateTool = ToolPojo(
                type = ToolCallTypePojo.FUNCTION,
                function = FunctionRequestPojo(
                    name = "get_classes_by_date",
                    description = "Позволяет получить все уроки (classId, scheduleId, organizationId, eventType, subjectId, teacherId, office, location, startTime, endTime) в указанный день",
                    parameters = JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("object"),
                            "properties" to JsonObject(
                                mapOf(
                                    "date" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("Дата за который день нужно получить уроки. Формат: YYYY-MM-DD"),
                                        )
                                    ),
                                )
                            ),
                            "required" to JsonArray(listOf(JsonPrimitive("date"))),
                        )
                    )
                )
            )
            val getNearClassTool = ToolPojo(
                type = ToolCallTypePojo.FUNCTION,
                function = FunctionRequestPojo(
                    name = "get_near_class",
                    description = "Позволяет получить ближайший в расписании урок по subjectId",
                    parameters = JsonObject(
                        mapOf(
                            "type" to JsonPrimitive("object"),
                            "properties" to JsonObject(
                                mapOf(
                                    "subjectId" to JsonObject(
                                        mapOf(
                                            "type" to JsonPrimitive("string"),
                                            "description" to JsonPrimitive("id предмета"),
                                        )
                                    ),
                                )
                            ),
                            "required" to JsonArray(listOf(JsonPrimitive("subjectId"))),
                        )
                    )
                )
            )
        }
    }
}
