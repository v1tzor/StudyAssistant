/*
 * Copyright 2024 Stanislav Aleshin
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

package ru.aleshin.studyassistant.chat.impl.presentation.models.schedules

import androidx.compose.runtime.Composable
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.ui.views.SegmentedButtonItem

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal enum class NumberOfWeekItem(val isoWeekNumber: Int) : SegmentedButtonItem {
    ONE(1) {
        override val title: String @Composable get() = "1"
    },
    TWO(2) {
        override val title: String @Composable get() = "2"
    },
    THREE(3) {
        override val title: String @Composable get() = "3"
    };

    fun toModel() = NumberOfRepeatWeek.valueOf(isoWeekNumber)

    companion object {
        fun valueOf(isoWeekNumber: Int) = NumberOfWeekItem.entries[isoWeekNumber - 1]
    }
}

internal fun NumberOfRepeatWeek.toItem() = NumberOfWeekItem.valueOf(isoRepeatWeekNumber)