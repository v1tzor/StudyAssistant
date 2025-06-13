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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import androidx.compose.runtime.Composable
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 11.06.2025.
 */
@Composable
internal fun GoalTime.Type.mapToString() = when (this) {
    GoalTime.Type.TIMER -> TasksThemeRes.strings.goalSheetTimeTypeTimer
    GoalTime.Type.STOPWATCH -> TasksThemeRes.strings.goalSheetTimeTypeStopwatch
    GoalTime.Type.NONE -> TasksThemeRes.strings.goalSheetTimeTypeNone
}