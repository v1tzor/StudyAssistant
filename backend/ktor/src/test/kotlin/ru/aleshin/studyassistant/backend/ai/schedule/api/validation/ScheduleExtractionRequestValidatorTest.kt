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

package ru.aleshin.studyassistant.backend.ai.schedule.api.validation

import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleExtractionRequestDto
import ru.aleshin.studyassistant.backend.ai.testAiConfig
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class ScheduleExtractionRequestValidatorTest {

    private val validator = ScheduleExtractionRequestValidator(config = testAiConfig())

    @Test
    fun validRawTextRequestShouldPass() {
        validator.validate(request = request())
    }

    @Test
    fun invalidTimeZoneShouldFail() {
        assertFailsWith<InvalidRequestException> {
            validator.validate(request = request().copy(timeZone = "Unknown/Zone"))
        }
    }

    @Test
    fun unsupportedRepeatWeekCountShouldFail() {
        assertFailsWith<InvalidRequestException> {
            validator.validate(request = request().copy(numberOfWeeks = 4))
        }
    }

    private fun request(): ScheduleExtractionRequestDto {
        return ScheduleExtractionRequestDto(
            requestId = UUID.randomUUID().toString(),
            rawText = "Monday 09:00 Mathematics",
            locale = "en-US",
            timeZone = "Europe/Moscow",
            numberOfWeeks = 2,
        )
    }
}
