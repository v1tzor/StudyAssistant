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

package ru.aleshin.studyassistant.tasks.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource
import studyassistant.shared.features.tasks.impl.generated.resources.Res
import studyassistant.shared.features.tasks.impl.generated.resources.ic_account_file_outline
import studyassistant.shared.features.tasks.impl.generated.resources.ic_alert_triangular
import studyassistant.shared.features.tasks.impl.generated.resources.ic_calendar_clock_outline
import studyassistant.shared.features.tasks.impl.generated.resources.ic_timeline_in_progress
import studyassistant.shared.features.tasks.impl.generated.resources.ic_view_array_outline
import studyassistant.shared.features.tasks.impl.generated.resources.ic_view_day_outline

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal data class TasksIcons(
    val tomorrowTime: DrawableResource,
    val weekTime: DrawableResource,
    val homeworkError: DrawableResource,
    val sharedHomeworks: DrawableResource,
    val viewAllTasks: DrawableResource,
    val viewShortTasks: DrawableResource,
) {

    companion object {
        val LIGHT = TasksIcons(
            tomorrowTime = Res.drawable.ic_timeline_in_progress,
            weekTime = Res.drawable.ic_calendar_clock_outline,
            homeworkError = Res.drawable.ic_alert_triangular,
            sharedHomeworks = Res.drawable.ic_account_file_outline,
            viewAllTasks = Res.drawable.ic_view_day_outline,
            viewShortTasks = Res.drawable.ic_view_array_outline,
        )
        val DARK = TasksIcons(
            tomorrowTime = Res.drawable.ic_timeline_in_progress,
            weekTime = Res.drawable.ic_calendar_clock_outline,
            homeworkError = Res.drawable.ic_alert_triangular,
            sharedHomeworks = Res.drawable.ic_account_file_outline,
            viewAllTasks = Res.drawable.ic_view_day_outline,
            viewShortTasks = Res.drawable.ic_view_array_outline,
        )
    }
}

internal val LocalTasksIcons = staticCompositionLocalOf<TasksIcons> {
    error("Tasks Icons is not provided")
}

internal fun fetchTasksIcons(isDark: Boolean) = when (isDark) {
    true -> TasksIcons.DARK
    false -> TasksIcons.LIGHT
}