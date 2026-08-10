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

package ru.aleshin.studyassistant.core.ui.mappers

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.high_priority_title as core_high_priority_title
import ru.aleshin.studyassistant.core.ui.resources.medium_priority_title as core_medium_priority_title
import ru.aleshin.studyassistant.core.ui.resources.standard_priority_title as core_standard_priority_title

/**
 * @author Stanislav Aleshin on 21.06.2024.
 */
@Composable
fun TaskPriority.mapToString() = when (this) {
    TaskPriority.STANDARD -> stringResource(CoreRes.string.core_standard_priority_title)
    TaskPriority.MEDIUM -> stringResource(CoreRes.string.core_medium_priority_title)
    TaskPriority.HIGH -> stringResource(CoreRes.string.core_high_priority_title)
}