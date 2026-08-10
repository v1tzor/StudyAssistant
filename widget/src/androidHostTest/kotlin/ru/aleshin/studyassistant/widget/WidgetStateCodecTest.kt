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

package ru.aleshin.studyassistant.widget

import ru.aleshin.studyassistant.widget.presentation.models.GoalsWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.state.WidgetContentStatusUi
import ru.aleshin.studyassistant.widget.presentation.state.WidgetStateCodec
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
class WidgetStateCodecTest {

    @Test
    fun shouldRestoreCurrentState() {
        val state = GoalsWidgetStateUi(
            status = WidgetContentStatusUi.CONTENT,
            generatedAt = 1_728_000_000_000L,
            isStale = true,
        )

        val restored = WidgetStateCodec.decodeCurrentOrDefault(
            value = WidgetStateCodec.encode(state),
            version = GoalsWidgetStateUi::version,
            defaultValue = ::GoalsWidgetStateUi,
        )

        assertEquals(state, restored)
    }

    @Test
    fun shouldFallbackForInvalidPayload() {
        val restored = WidgetStateCodec.decodeCurrentOrDefault(
            value = "not-json",
            version = GoalsWidgetStateUi::version,
            defaultValue = ::GoalsWidgetStateUi,
        )

        assertEquals(GoalsWidgetStateUi(), restored)
    }

    @Test
    fun shouldFallbackForUnsupportedVersion() {
        val state = GoalsWidgetStateUi(version = 2, status = WidgetContentStatusUi.CONTENT)
        val restored = WidgetStateCodec.decodeCurrentOrDefault(
            value = WidgetStateCodec.encode(state),
            version = GoalsWidgetStateUi::version,
            defaultValue = ::GoalsWidgetStateUi,
        )

        assertEquals(GoalsWidgetStateUi(), restored)
    }
}
