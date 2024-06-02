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

package ru.aleshin.studyassistant.schedule.impl.presentation.models

import androidx.compose.runtime.Composable
import entities.common.NumberOfRepeatWeek
import views.SegmentedButtonItem

/**
 * @author Stanislav Aleshin on 24.05.2024.
 */
internal enum class NumberOfWeekItem(val id: Int) : SegmentedButtonItem {
    ONE(1) {
        override val title: String @Composable get() = "1"
    },
    TWO(2) {
        override val title: String @Composable get() = "2"
    },
    THREE(3) {
        override val title: String @Composable get() = "3"
    };

    fun toModel() = when (this) {
        ONE -> NumberOfRepeatWeek.ONE
        TWO -> NumberOfRepeatWeek.TWO
        THREE -> NumberOfRepeatWeek.THREE
    }
}

internal fun NumberOfRepeatWeek.toItem() = when (this) {
    NumberOfRepeatWeek.ONE -> NumberOfWeekItem.ONE
    NumberOfRepeatWeek.TWO -> NumberOfWeekItem.TWO
    NumberOfRepeatWeek.THREE -> NumberOfWeekItem.THREE
}
