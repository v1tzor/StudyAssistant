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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.views.dialog.WarningAlertDialog
import ru.aleshin.studyassistant.tasks.impl.resources.Res
import ru.aleshin.studyassistant.tasks.impl.resources.delete_goal_warning_text
import ru.aleshin.studyassistant.tasks.impl.resources.delete_goal_warning_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.delete_confirm_title as core_delete_confirm_title

/**
 * @author Stanislav Aleshin on 05.06.2025.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DeleteGoalWarningDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    WarningAlertDialog(
        modifier = modifier,
        icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
        title = { Text(text = stringResource(Res.string.delete_goal_warning_title)) },
        text = { Text(text = stringResource(Res.string.delete_goal_warning_text)) },
        confirmTitle = stringResource(CoreRes.string.core_delete_confirm_title),
        onDismiss = onDismiss,
        onConfirm = onDelete,
    )
}