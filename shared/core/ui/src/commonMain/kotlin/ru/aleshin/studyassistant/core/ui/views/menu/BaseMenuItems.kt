/*
 * Copyright 2023 Stanislav Aleshin
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
package ru.aleshin.studyassistant.core.ui.views.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 04.08.2023.
 */
@Composable
fun CheckedMenuItem(
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    check: Boolean,
    interactionSource: MutableInteractionSource? = null,
) = DropdownMenuItem(
    modifier = modifier.alphaByEnabled(enabled),
    onClick = { onCheckedChange(!check) },
    enabled = enabled,
    leadingIcon = {
        Checkbox(
            modifier = Modifier.size(32.dp),
            enabled = enabled,
            checked = check,
            onCheckedChange = onCheckedChange,
        )
    },
    text = {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
    },
    interactionSource = interactionSource,
)

@Composable
fun BackMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String = StudyAssistantRes.strings.backTitle,
    interactionSource: MutableInteractionSource? = null,
) = DropdownMenuItem(
    modifier = modifier.alphaByEnabled(enabled),
    enabled = enabled,
    onClick = onClick,
    leadingIcon = {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    },
    text = {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
    },
    interactionSource = interactionSource,
)

@Composable
fun NavMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    interactionSource: MutableInteractionSource? = null,
) = DropdownMenuItem(
    modifier = modifier.alphaByEnabled(enabled),
    enabled = enabled,
    onClick = onClick,
    trailingIcon = {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    },
    text = {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
    },
    interactionSource = interactionSource,
)