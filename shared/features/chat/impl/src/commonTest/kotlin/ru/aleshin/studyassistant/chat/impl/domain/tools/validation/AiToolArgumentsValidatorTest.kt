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

package ru.aleshin.studyassistant.chat.impl.domain.tools.validation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AiToolArgumentsValidatorTest {

    private val validator = AiToolArgumentsValidator.Base()

    @Test
    fun `date is parsed in current local time zone`() {
        val date = validator.date("2026-08-12")

        assertEquals(
            LocalDate(2026, 8, 12),
            date?.toLocalDateTime(TimeZone.currentSystemDefault())?.date,
        )
    }

    @Test
    fun `time accepts ISO local time`() {
        assertEquals(LocalTime(9, 30), validator.time("09:30"))
        assertNull(validator.time("25:00"))
    }

    @Test
    fun `range rejects more than two weeks`() {
        assertNull(validator.range("2026-08-01", "2026-08-16"))
    }
}
