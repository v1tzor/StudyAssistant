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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.cancel_title as core_cancel_title
import ru.aleshin.studyassistant.core.ui.resources.save_confirm_title as core_save_confirm_title

/**
 * @author Stanislav Aleshin on 23.06.2024.
 */
@Composable
internal fun HomeworkBottomActions(
    modifier: Modifier = Modifier,
    isLoadingSave: Boolean,
    saveEnabled: Boolean,
    showDeleteAction: Boolean,
    contentMaxWidth: Dp? = null,
    horizontalPadding: Dp = AdaptiveLayoutDefaults.CompactHorizontalPadding,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val barModifier = if (contentMaxWidth != null) {
        Modifier
            .widthIn(max = contentMaxWidth)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = horizontalPadding, vertical = AdaptiveLayoutDefaults.SpaceLarge)
    } else {
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = horizontalPadding, vertical = AdaptiveLayoutDefaults.SpaceLarge)
    }
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopStart,
    ) {
    Row(
        modifier = barModifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showDeleteAction) {
            FilledTonalIconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error,
                )
            ) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        FilledTonalButton(onClick = onCancelClick) {
            Text(text = stringResource(CoreRes.string.core_cancel_title))
        }
        Button(onClick = onSaveClick, enabled = saveEnabled && !isLoadingSave) {
            Text(text = stringResource(CoreRes.string.core_save_confirm_title))
        }
    }
    }
}