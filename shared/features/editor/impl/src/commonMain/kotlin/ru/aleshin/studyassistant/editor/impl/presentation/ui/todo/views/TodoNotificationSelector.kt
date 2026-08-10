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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.presentation.models.tasks.TodoNotificationsUi
import ru.aleshin.studyassistant.core.ui.views.menu.CheckedMenuItem
import ru.aleshin.studyassistant.editor.impl.resources.Res
import ru.aleshin.studyassistant.editor.impl.resources.todo_notification_label
import ru.aleshin.studyassistant.editor.impl.resources.todo_notification_title
import ru.aleshin.studyassistant.editor.impl.resources.todo_notify_params_before_fifteen_minutes
import ru.aleshin.studyassistant.editor.impl.resources.todo_notify_params_before_one_day
import ru.aleshin.studyassistant.editor.impl.resources.todo_notify_params_before_one_hour
import ru.aleshin.studyassistant.editor.impl.resources.todo_notify_params_before_one_week
import ru.aleshin.studyassistant.editor.impl.resources.todo_notify_params_before_start
import ru.aleshin.studyassistant.editor.impl.resources.todo_notify_params_before_three_hour

/**
 * @author Stanislav Aleshin on 26.07.2024.
 */
@Composable
internal fun TodoNotificationSelector(
    modifier: Modifier = Modifier,
    notifications: TodoNotificationsUi?,
    onChangeNotifications: (TodoNotificationsUi) -> Unit,
) {
    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.todo_notification_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.todo_notification_label),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Box {
            var openNotificationsMenu by remember { mutableStateOf(false) }

            IconButton(
                modifier = Modifier.size(32.dp),
                onClick = { openNotificationsMenu = true },
                enabled = notifications != null,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null
                )
            }

            if (notifications != null) {
                TodoNotificationsMenu(
                    isExpanded = openNotificationsMenu,
                    notifications = notifications,
                    onDismiss = { openNotificationsMenu = false },
                    onUpdate = onChangeNotifications,
                )
            }
        }
    }
}

@Composable
private fun TodoNotificationsMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    notifications: TodoNotificationsUi,
    onDismiss: () -> Unit,
    onUpdate: (TodoNotificationsUi) -> Unit,
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismiss,
        modifier = modifier.sizeIn(maxHeight = 200.dp),
        shape = MaterialTheme.shapes.large,
        offset = DpOffset(0.dp, 6.dp),
    ) {
        CheckedMenuItem(
            text = stringResource(Res.string.todo_notify_params_before_start),
            check = notifications.beforeStart,
            onCheckedChange = { onUpdate(notifications.copy(beforeStart = it)) },
        )
        CheckedMenuItem(
            text = stringResource(Res.string.todo_notify_params_before_fifteen_minutes),
            check = notifications.fifteenMinutesBefore,
            onCheckedChange = { onUpdate(notifications.copy(fifteenMinutesBefore = it)) },
        )
        CheckedMenuItem(
            text = stringResource(Res.string.todo_notify_params_before_one_hour),
            check = notifications.oneHourBefore,
            onCheckedChange = { onUpdate(notifications.copy(oneHourBefore = it)) },
        )
        CheckedMenuItem(
            text = stringResource(Res.string.todo_notify_params_before_three_hour),
            check = notifications.threeHourBefore,
            onCheckedChange = { onUpdate(notifications.copy(threeHourBefore = it)) },
        )
        CheckedMenuItem(
            text = stringResource(Res.string.todo_notify_params_before_one_day),
            check = notifications.oneDayBefore,
            onCheckedChange = { onUpdate(notifications.copy(oneDayBefore = it)) },
        )
        CheckedMenuItem(
            text = stringResource(Res.string.todo_notify_params_before_one_week),
            check = notifications.oneWeekBefore,
            onCheckedChange = { onUpdate(notifications.copy(oneWeekBefore = it)) },
        )
    }
}
