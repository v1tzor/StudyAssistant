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

package ru.aleshin.studyassistant.core.ui.views.menu

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 14.08.2023.
 */
@Composable
fun <T> CheckedDropdownMenu(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    isExpanded: Boolean,
    items: List<T>,
    selected: List<T>,
    enabledItem: (T) -> Boolean = { true },
    text: @Composable (T) -> Unit,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    onChecked: (T) -> Unit,
    onUnchecked: (T) -> Unit,
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismiss,
        modifier = modifier.sizeIn(maxHeight = 200.dp),
        scrollState = scrollState,
        shape = MaterialTheme.shapes.large,
        properties = properties,
        offset = offset,
    ) {
        BackMenuItem(onClick = onDismiss)
        items.forEach { item ->
            val checked = selected.contains(item)
            DropdownMenuItem(
                text = { text(item) },
                enabled = enabledItem(item),
                leadingIcon = {
                    Checkbox(
                        modifier = Modifier.size(32.dp),
                        enabled = enabledItem(item),
                        checked = checked || !enabledItem(item),
                        onCheckedChange = { isAdd ->
                            if (isAdd) onChecked(item) else onUnchecked(item)
                        }
                    )
                },
                onClick = {
                    if (!checked) onChecked(item) else onUnchecked(item)
                }
            )
        }
    }
}

@Composable
fun BackMenuItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        modifier = modifier.alphaByEnabled(enabled),
        enabled = enabled,
        text = { Text(text = StudyAssistantRes.strings.backTitle) },
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = StudyAssistantRes.strings.backTitle
            )
        },
        onClick = onClick,
    )
}