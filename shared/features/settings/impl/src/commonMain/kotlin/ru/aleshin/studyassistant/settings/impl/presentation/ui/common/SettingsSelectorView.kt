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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseSelectorDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogItemView

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun <T> SettingsSelectorView(
    onSelect: (T) -> Unit,
    modifier: Modifier,
    enabled: Boolean = true,
    selected: T?,
    allItems: List<T>,
    icon: Painter?,
    title: String,
    itemName: @Composable (T) -> String,
) {
    var settingsChooserDialogState by remember { mutableStateOf(false) }

    Surface(
        onClick = { settingsChooserDialogState = true },
        modifier = modifier.alphaByEnabled(enabled).fillMaxWidth(),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                modifier = Modifier.animateContentSize(),
                text = selected?.let { itemName(it) } ?: "",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }

    if (settingsChooserDialogState) {
        var selectedSetting by remember { mutableStateOf(selected) }
        BaseSelectorDialog(
            modifier = modifier,
            selected = selectedSetting,
            items = allItems,
            header = title,
            title = null,
            itemView = { setting ->
                SelectorDialogItemView(
                    onClick = { selectedSetting = setting },
                    selected = setting == selectedSetting,
                    title = itemName(setting),
                    label = null,
                )
            },
            onDismiss = { settingsChooserDialogState = false },
            onConfirm = { setting ->
                if (setting != null) onSelect(setting)
                settingsChooserDialogState = false
            },
        )
    }
}