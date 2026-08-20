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

package ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.openrouter

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleEventType
import ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.openrouter.dto.OpenRouterJsonSchemaDto
import ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.openrouter.dto.OpenRouterResponseFormatDto

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
internal object ScheduleExtractionJsonSchema {

    const val NAME = "schedule_draft"

    fun responseFormat(): OpenRouterResponseFormatDto {
        return OpenRouterResponseFormatDto(
            type = "json_schema",
            jsonSchema = OpenRouterJsonSchemaDto(
                name = NAME,
                strict = true,
                schema = schema(),
            ),
        )
    }

    private fun schema(): JsonObject {
        return buildJsonObject {
            put("type", JsonPrimitive("object"))
            put(
                "properties",
                buildJsonObject {
                    put(
                        "title",
                        nullableString("Optional short title of the timetable if clearly visible"),
                    )
                    put(
                        "entries",
                        buildJsonObject {
                            put("type", JsonPrimitive("array"))
                            put("description", JsonPrimitive("Extracted classes and events in chronological order within each week and day"))
                            put("items", entrySchema())
                        },
                    )
                },
            )
            put("required", stringArray("title", "entries"))
            put("additionalProperties", JsonPrimitive(false))
        }
    }

    private fun entrySchema(): JsonObject {
        return buildJsonObject {
            put("type", JsonPrimitive("object"))
            put(
                "properties",
                buildJsonObject {
                    put(
                        "repeatWeek",
                        integer(
                            description = "Numbered week in 1..numberOfWeeks. One visible week uses 1",
                            minimum = 1,
                            maximum = 3,
                        ),
                    )
                    put(
                        "dayOfWeek",
                        integer(
                            description = "Monday=1 ... Sunday=7",
                            minimum = 1,
                            maximum = 7,
                        ),
                    )
                    put(
                        "classNumber",
                        nullableInteger(
                            description = "Lesson/period index in the day (1, 2, 3, 4, ...). Null if no period number is visible. Never a school grade or group such as 9А",
                            minimum = 1,
                            maximum = 30,
                        ),
                    )
                    put(
                        "startTime",
                        nullableString("Start time in HH:mm if visible. Null if only a lesson number is shown"),
                    )
                    put(
                        "endTime",
                        nullableString("End time in HH:mm if visible. Null if only a lesson number is shown"),
                    )
                    put("subject", nullableString("Subject or event name from the same visual block"))
                    put("eventType", eventTypeSchema())
                    put("teacher", nullableString("Teacher name if clearly shown for this event. Formats: LastName FirstName Patronymic OR FirstName Patronymic OR FirstName"))
                    put(
                        "office",
                        nullableString(
                            "Classroom, room, cabinet or auditorium number from the same block or a каб/ауд/room column (101, каб. 215). Null if not visible",
                        ),
                    )
                    put(
                        "location",
                        nullableString(
                            "Building, campus, address or named site if shown. Null when only a room number exists",
                        ),
                    )
                },
            )
            put(
                "required",
                stringArray(
                    "repeatWeek",
                    "dayOfWeek",
                    "classNumber",
                    "startTime",
                    "endTime",
                    "subject",
                    "eventType",
                    "teacher",
                    "office",
                    "location",
                ),
            )
            put("additionalProperties", JsonPrimitive(false))
        }
    }

    private fun eventTypeSchema(): JsonObject {
        return buildJsonObject {
            put(
                "anyOf",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("type", JsonPrimitive("string"))
                            put(
                                "enum",
                                buildJsonArray {
                                    ScheduleEventType.entries.forEach { type ->
                                        add(JsonPrimitive(type.name))
                                    }
                                },
                            )
                        },
                    )
                    add(buildJsonObject { put("type", JsonPrimitive("null")) })
                },
            )
            put(
                "description",
                JsonPrimitive("Event type only when clearly indicated. Otherwise null"),
            )
        }
    }

    private fun integer(
        description: String,
        minimum: Int,
        maximum: Int,
    ): JsonObject {
        return buildJsonObject {
            put("type", JsonPrimitive("integer"))
            put("description", JsonPrimitive(description))
            put("minimum", JsonPrimitive(minimum))
            put("maximum", JsonPrimitive(maximum))
        }
    }

    private fun nullableInteger(
        description: String,
        minimum: Int,
        maximum: Int,
    ): JsonObject {
        return buildJsonObject {
            put("type", JsonArray(listOf(JsonPrimitive("integer"), JsonPrimitive("null"))))
            put("description", JsonPrimitive(description))
            put("minimum", JsonPrimitive(minimum))
            put("maximum", JsonPrimitive(maximum))
        }
    }

    private fun nullableString(description: String): JsonObject {
        return buildJsonObject {
            put("type", JsonArray(listOf(JsonPrimitive("string"), JsonPrimitive("null"))))
            put("description", JsonPrimitive(description))
        }
    }

    private fun stringArray(vararg values: String): JsonArray {
        return buildJsonArray {
            values.forEach { value -> add(JsonPrimitive(value)) }
        }
    }
}
