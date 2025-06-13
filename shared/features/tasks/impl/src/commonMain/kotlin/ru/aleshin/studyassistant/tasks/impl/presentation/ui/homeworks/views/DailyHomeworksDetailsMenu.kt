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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.ui.views.menu.ChooserDropdownMenu
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 04.06.2025.
 */
@Composable
internal fun DailyHomeworksDetailsMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
) {
    ChooserDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        items = DailyHomeworksMenuActions.entries,
        showBackItem = false,
        text = { action ->
            Text(
                text = when (action) {
                    DailyHomeworksMenuActions.SHARE -> TasksThemeRes.strings.shareHomeworksHeader
                },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        },
        leadingIcon = { action ->
            Icon(
                painter = when (action) {
                    DailyHomeworksMenuActions.SHARE -> {
                        painterResource(TasksThemeRes.icons.sharedHomeworks)
                    }
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        onDismiss = onDismiss,
        onChoose = { action ->
            when (action) {
                DailyHomeworksMenuActions.SHARE -> onShare
            }
            onDismiss()
        }
    )
}

private enum class DailyHomeworksMenuActions {
    SHARE
}