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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.dialog.WarningAlertDialog
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.hide_title as core_hide_title
import ru.aleshin.studyassistant.core.ui.resources.warning_dialog_title as core_warning_dialog_title

/**
 * @author Stanislav Aleshin on 18.08.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun HideButton(
    onHide: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    warningMessage: String?,
    interactionSource: MutableInteractionSource? = null,
) {
    var warningDialogStatus by rememberSaveable { mutableStateOf(false) }

    Button(
        onClick = {
            if (warningMessage != null) {
                warningDialogStatus = true
            } else {
                onHide()
            }
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = MaterialTheme.shapes.full,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        interactionSource = interactionSource,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = null,
            )
            Text(
                text = stringResource(CoreRes.string.core_hide_title),
                maxLines = 1,
            )
        }
    }

    if (warningDialogStatus && warningMessage != null) {
        WarningAlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(text = stringResource(CoreRes.string.core_warning_dialog_title)) },
            text = { Text(text = warningMessage) },
            confirmTitle = stringResource(CoreRes.string.core_hide_title),
            onDismiss = { warningDialogStatus = false },
            onConfirm = {
                onHide()
                warningDialogStatus = false
            },
        )
    }
}