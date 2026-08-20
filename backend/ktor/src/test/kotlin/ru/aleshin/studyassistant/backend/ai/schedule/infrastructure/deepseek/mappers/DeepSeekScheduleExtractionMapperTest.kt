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

package ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek.mappers

import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleEventType
import ru.aleshin.studyassistant.backend.ai.testAiConfig
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class DeepSeekScheduleExtractionMapperTest {

    private val mapper = DeepSeekScheduleExtractionMapper(config = testAiConfig())

    @Test
    fun validJsonShouldMapToDraft() {
        val draft = mapper.mapResponse(
            content = VALID_RESPONSE,
            numberOfWeeks = 2,
        )

        requireNotNull(draft)
        assertEquals("Semester schedule", draft.title)
        assertEquals(1, draft.entries.single().dayOfWeek)
        assertEquals("09:00", draft.entries.single().startTime)
        assertEquals(ScheduleEventType.LECTURE, draft.entries.single().eventType)
        assertEquals("101", draft.entries.single().office)
        assertEquals("Корпус Б", draft.entries.single().location)
    }

    @Test
    fun numericOfficeShouldMapToString() {
        val response = VALID_RESPONSE
            .replace("\"office\":\"101\"", "\"office\":215")
            .replace("\"location\":\"Корпус Б\"", "\"location\":null")

        val draft = mapper.mapResponse(content = response, numberOfWeeks = 2)

        requireNotNull(draft)
        assertEquals("215", draft.entries.single().office)
        assertEquals(null, draft.entries.single().location)
    }

    @Test
    fun singleDigitHourShouldMapToClock() {
        val response = VALID_RESPONSE.replace("09:00", "9:00").replace("10:30", "10.30")

        val draft = mapper.mapResponse(content = response, numberOfWeeks = 2)

        requireNotNull(draft)
        assertEquals("09:00", draft.entries.single().startTime)
        assertEquals("10:30", draft.entries.single().endTime)
    }

    @Test
    fun invalidTimeShouldSkipEntry() {
        val response = VALID_RESPONSE.replace("09:00", "25:00")

        val draft = mapper.mapResponse(content = response, numberOfWeeks = 2)

        requireNotNull(draft)
        assertEquals(emptyList(), draft.entries)
    }

    @Test
    fun invalidEntryShouldNotDropTheRestOfTheDraft() {
        val response =
            "{\"title\":null,\"entries\":[" +
                "{\"repeatWeek\":1,\"dayOfWeek\":1,\"classNumber\":1," +
                "\"startTime\":\"25:00\",\"endTime\":\"10:30\"," +
                "\"subject\":\"Broken\",\"eventType\":null,\"teacher\":null,\"office\":null,\"location\":null}," +
                "{\"repeatWeek\":1,\"dayOfWeek\":1,\"classNumber\":2," +
                "\"startTime\":\"11:00\",\"endTime\":\"11:45\"," +
                "\"subject\":\"History\",\"eventType\":null,\"teacher\":null,\"office\":null,\"location\":null}" +
                "]}"

        val draft = mapper.mapResponse(content = response, numberOfWeeks = 2)

        requireNotNull(draft)
        assertEquals(listOf("History"), draft.entries.map { entry -> entry.subject })
        assertEquals("11:00", draft.entries.single().startTime)
    }

    @Test
    fun timeRangeAndStringClassNumberShouldMap() {
        val response = VALID_RESPONSE
            .replace("\"classNumber\":1", "\"classNumber\":\"3\"")
            .replace("\"startTime\":\"09:00\"", "\"startTime\":\"9:00-10:30\"")
            .replace("\"endTime\":\"10:30\"", "\"endTime\":null")

        val draft = mapper.mapResponse(content = response, numberOfWeeks = 2)

        requireNotNull(draft)
        assertEquals(3, draft.entries.single().classNumber)
        assertEquals("09:00", draft.entries.single().startTime)
        assertEquals("10:30", draft.entries.single().endTime)
    }

    @Test
    fun markdownJsonShouldMap() {
        val response = "```json\n$VALID_RESPONSE\n```"

        val draft = mapper.mapResponse(content = response, numberOfWeeks = 2)

        requireNotNull(draft)
        assertEquals("Semester schedule", draft.title)
        assertEquals(1, draft.entries.size)
    }

    @Test
    fun repeatWeekOutsideRequestShouldSkipEntry() {
        val response = VALID_RESPONSE.replace("\"repeatWeek\":1", "\"repeatWeek\":3")

        val draft = mapper.mapResponse(content = response, numberOfWeeks = 2)

        requireNotNull(draft)
        assertEquals(emptyList(), draft.entries)
    }

    private companion object {

        const val VALID_RESPONSE =
            "{\"title\":\"Semester schedule\",\"entries\":[{" +
                "\"repeatWeek\":1,\"dayOfWeek\":1,\"classNumber\":1," +
                "\"startTime\":\"09:00\",\"endTime\":\"10:30\"," +
                "\"subject\":\"Mathematics\",\"eventType\":\"LECTURE\"," +
                "\"teacher\":null,\"office\":\"101\",\"location\":\"Корпус Б\"}]}"
    }
}
