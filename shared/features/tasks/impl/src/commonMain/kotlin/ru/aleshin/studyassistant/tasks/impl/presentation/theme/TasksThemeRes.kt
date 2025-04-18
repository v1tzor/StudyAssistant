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

package ru.aleshin.studyassistant.tasks.impl.presentation.theme

import androidx.compose.runtime.Composable
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.tokens.LocalTasksIcons
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.tokens.LocalTasksStrings
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.tokens.TasksIcons
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.tokens.TasksStrings

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal object TasksThemeRes {

    val icons: TasksIcons
        @Composable get() = LocalTasksIcons.current

    val strings: TasksStrings
        @Composable get() = LocalTasksStrings.current
}