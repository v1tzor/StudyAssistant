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

package ru.aleshin.studyassistant.core.domain.entities.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
class DailyWorkloadTest {

    @Test
    fun calculateEmptyWorkload() {
        val workload = DailyWorkload.calculate(
            classes = emptyList(),
            homeworks = emptyList(),
            todos = emptyList(),
        )

        assertEquals(0f, workload.value)
    }

    @Test
    fun compareWorkloadWithConfiguredThreshold() {
        assertTrue(DailyWorkload(7f).isHigh(7))
        assertFalse(DailyWorkload(6.99f).isHigh(7))
    }
}
