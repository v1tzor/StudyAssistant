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

package ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.ui.views.SegmentedButtonItem
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.first_week_title
import ru.aleshin.studyassistant.schedule.impl.resources.second_week_title
import ru.aleshin.studyassistant.schedule.impl.resources.thirty_week_title

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
internal enum class NumberOfWeekItem(val isoWeekNumber: Int) : SegmentedButtonItem {
    ONE(1) {
        override val title: String @Composable get() = stringResource(Res.string.first_week_title)
    },
    TWO(2) {
        override val title: String @Composable get() = stringResource(Res.string.second_week_title)
    },
    THREE(3) {
        override val title: String @Composable get() = stringResource(Res.string.thirty_week_title)
    };

    fun toModel() = NumberOfRepeatWeek.valueOf(isoWeekNumber)

    companion object {
        fun valueOf(isoWeekNumber: Int) = NumberOfWeekItem.entries[isoWeekNumber - 1]
    }
}

internal fun NumberOfRepeatWeek.toItem() = NumberOfWeekItem.valueOf(isoRepeatWeekNumber)