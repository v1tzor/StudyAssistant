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

package ru.aleshin.studyassistant.backend.ai.domain.services

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
class AiAssistantPromptFactoryTest {

    @Test
    fun promptCoversNextClassHomeworkWithoutInventingIds() {
        val prompt = requireNotNull(
            AiAssistantPromptFactory().create(
                locale = "ru-RU",
                timeZone = "Europe/Moscow",
                now = Instant.parse("2026-08-20T09:45:12Z"),
            ).content,
        )

        assertTrue("get_near_class" in prompt)
        assertTrue("create_homework" in prompt)
        assertTrue("never invent" in prompt.lowercase())
        assertTrue("get_subjects" in prompt)
        assertTrue("2026-08-20T12:00+03:00[Europe/Moscow]" in prompt)
        assertTrue(prompt.length < 2_000)
    }

    @Test
    fun promptIsIdenticalWithinSameHourForCaching() {
        val factory = AiAssistantPromptFactory()
        val prompt1 = factory.create(
            locale = "ru-RU",
            timeZone = "Europe/Moscow",
            now = Instant.parse("2026-08-20T09:10:00Z"),
        ).content
        val prompt2 = factory.create(
            locale = "ru-RU",
            timeZone = "Europe/Moscow",
            now = Instant.parse("2026-08-20T09:55:59Z"),
        ).content

        assertEquals(prompt1, prompt2)
    }
}
