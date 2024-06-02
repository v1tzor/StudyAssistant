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
 * imitations under the License.
 */

package views.menu

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

/**
 * @author Stanislav Aleshin on 14.08.2023.
 */
@Composable
fun <T> ChooserDropdownMenu(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    expanded: Boolean,
    items: List<T>,
    enabledItem: (T) -> Boolean = { true },
    text: @Composable (T) -> Unit,
    leadingIcon: @Composable ((T) -> Unit)? = null,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    onChoose: (T) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.sizeIn(maxHeight = 200.dp),
        scrollState = scrollState,
        properties = properties,
        offset = offset,
    ) {
        BackMenuItem(onClick = onDismiss)
        items.forEach { item ->
            DropdownMenuItem(
                text = { text(item) },
                enabled = enabledItem(item),
                leadingIcon = if (leadingIcon != null) { {
                    leadingIcon(item)
                } } else {
                    null
                },
                onClick = { onChoose(item) }
            )
        }
    }
}
