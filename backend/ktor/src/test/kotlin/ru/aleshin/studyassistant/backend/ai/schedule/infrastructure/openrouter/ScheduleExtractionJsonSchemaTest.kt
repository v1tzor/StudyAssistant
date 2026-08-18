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

import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleEventType
import ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.openrouter.dto.OpenRouterResponseFormatDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
class ScheduleExtractionJsonSchemaTest {

    @Test
    fun responseFormatShouldFollowOpenRouterStructuredOutputContract() {
        val root = OpenRouterJson.encodeToJsonElement(
            serializer = OpenRouterResponseFormatDto.serializer(),
            value = ScheduleExtractionJsonSchema.responseFormat(),
        ).jsonObject

        assertEquals("json_schema", root.getValue("type").jsonPrimitive.content)

        val jsonSchema = root.getValue("json_schema").jsonObject
        assertEquals(ScheduleExtractionJsonSchema.NAME, jsonSchema.getValue("name").jsonPrimitive.content)
        assertTrue(jsonSchema.getValue("strict").jsonPrimitive.boolean)

        val schema = jsonSchema.getValue("schema").jsonObject
        assertEquals("object", schema.getValue("type").jsonPrimitive.content)
        assertFalse(schema.getValue("additionalProperties").jsonPrimitive.boolean)
        assertEquals(
            listOf("title", "entries"),
            schema.getValue("required").jsonArray.map { it.jsonPrimitive.content },
        )

        val properties = schema.getValue("properties").jsonObject
        assertTrue(properties.containsKey("title"))
        assertTrue(properties.containsKey("entries"))

        val entry = properties.getValue("entries").jsonObject.getValue("items").jsonObject
        assertEquals("object", entry.getValue("type").jsonPrimitive.content)
        assertFalse(entry.getValue("additionalProperties").jsonPrimitive.boolean)
        assertEquals(
            listOf(
                "repeatWeek",
                "dayOfWeek",
                "classNumber",
                "startTime",
                "endTime",
                "subject",
                "eventType",
                "teacher",
                "office",
            ),
            entry.getValue("required").jsonArray.map { it.jsonPrimitive.content },
        )
        assertFalse(entry.getValue("properties").jsonObject.containsKey("unparsedLines"))
        assertFalse(entry.getValue("properties").jsonObject.containsKey("organization"))
        assertFalse(entry.getValue("properties").jsonObject.containsKey("notes"))
        assertFalse(entry.getValue("properties").jsonObject.containsKey("location"))

        val eventTypeValues = entry
            .getValue("properties")
            .jsonObject
            .getValue("eventType")
            .jsonObject
            .getValue("anyOf")
            .jsonArray
            .first()
            .jsonObject
            .getValue("enum")
            .jsonArray
            .map { it.jsonPrimitive.content }

        assertEquals(ScheduleEventType.entries.map { it.name }, eventTypeValues)
    }
}
