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

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.widget.presentation.utils.WidgetSizeClass
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
class WidgetSizeClassTest {

    @Test
    fun shouldMapCompactSize() {
        assertEquals(
            WidgetSizeClass(
                width = WidgetSizeClass.Width.COMPACT,
                height = WidgetSizeClass.Height.COMPACT,
            ),
            WidgetSizeClass.fetch(DpSize(179.dp, 149.dp)),
        )
    }

    @Test
    fun shouldMapExpandedSize() {
        assertEquals(
            WidgetSizeClass(
                width = WidgetSizeClass.Width.EXPANDED,
                height = WidgetSizeClass.Height.EXPANDED,
            ),
            WidgetSizeClass.fetch(DpSize(280.dp, 220.dp)),
        )
    }
}
