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
import kotlin.test.assertNull

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
    }

    @Test
    fun invalidTimeShouldFailMapping() {
        val response = VALID_RESPONSE.replace("09:00", "25:00")

        assertNull(mapper.mapResponse(content = response, numberOfWeeks = 2))
    }

    @Test
    fun repeatWeekOutsideRequestShouldFailMapping() {
        val response = VALID_RESPONSE.replace("\"repeatWeek\":1", "\"repeatWeek\":3")

        assertNull(mapper.mapResponse(content = response, numberOfWeeks = 2))
    }

    private companion object {

        const val VALID_RESPONSE =
            "{\"title\":\"Semester schedule\",\"entries\":[{" +
                "\"repeatWeek\":1,\"dayOfWeek\":1,\"classNumber\":1," +
                "\"startTime\":\"09:00\",\"endTime\":\"10:30\"," +
                "\"subject\":\"Mathematics\",\"eventType\":\"LECTURE\"," +
                "\"teacher\":null,\"office\":\"101\",\"location\":null," +
                "\"organization\":null,\"notes\":null}],\"unparsedLines\":[]}"
    }
}
