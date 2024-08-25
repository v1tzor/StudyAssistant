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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.menu.ChooserDropdownMenu

/**
 * @author Stanislav Aleshin on 25.08.2024.
 */
@Composable
internal fun WorkloadRateChip(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxRate: Int,
    onRateChange: (Int) -> Unit,
) {
    Box {
        var isExpandedRateMenu by remember { mutableStateOf(false) }

        AssistChip(
            onClick = { isExpandedRateMenu = true },
            label = {
                Text(
                    text = maxRate.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            enabled = enabled,
            modifier = modifier,
            trailingIcon = { ExpandedIcon(isExpanded = isExpandedRateMenu) },
        )
        ChooserDropdownMenu(
            onDismiss = { isExpandedRateMenu = false },
            expanded = isExpandedRateMenu,
            items = (1..10).toList(),
            enabledItem = { it != maxRate },
            text = { Text(text = it.toString(), color = MaterialTheme.colorScheme.onSurface) },
            onChoose = {
                onRateChange(it)
                isExpandedRateMenu = false
            },
        )
    }
}