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

package ru.aleshin.studyassistant.schedule.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import studyassistant.shared.features.schedule.impl.generated.resources.Res
import studyassistant.shared.features.schedule.impl.generated.resources.ic_calendar_today
import studyassistant.shared.features.schedule.impl.generated.resources.ic_list_edit
import studyassistant.shared.features.schedule.impl.generated.resources.ic_open_table
import studyassistant.shared.features.schedule.impl.generated.resources.ic_table_edit

/**
 * @author Stanislav Aleshin on 21.06.2023.
 */
@OptIn(ExperimentalResourceApi::class)
internal data class ScheduleIcons(
    val openTable: DrawableResource,
    val openOverview: DrawableResource,
    val editList: DrawableResource,
    val editTable: DrawableResource,
) {
    companion object {
        val LIGHT = ScheduleIcons(
            openTable = Res.drawable.ic_open_table,
            openOverview = Res.drawable.ic_calendar_today,
            editList = Res.drawable.ic_list_edit,
            editTable = Res.drawable.ic_table_edit,
        )
        val DARK = ScheduleIcons(
            openTable = Res.drawable.ic_open_table,
            openOverview = Res.drawable.ic_calendar_today,
            editList = Res.drawable.ic_list_edit,
            editTable = Res.drawable.ic_table_edit,
        )
    }
}

internal val LocalScheduleIcons = staticCompositionLocalOf<ScheduleIcons> {
    error("Schedule Icons is not provided")
}

internal fun fetchScheduleIcons(isDark: Boolean) = when (isDark) {
    true -> ScheduleIcons.DARK
    false -> ScheduleIcons.LIGHT
}
