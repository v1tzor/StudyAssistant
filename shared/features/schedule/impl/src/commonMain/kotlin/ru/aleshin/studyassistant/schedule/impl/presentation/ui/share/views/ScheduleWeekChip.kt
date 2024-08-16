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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.NumberOfWeekItem

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
@Composable
internal fun ScheduleWeekChip(
    modifier: Modifier = Modifier,
    selected: NumberOfWeekItem,
    maxNumberOfWeek: Int = NumberOfWeekItem.THREE.isoWeekNumber,
    onSelect: (NumberOfWeekItem) -> Unit,
) = Box {
    var isExpandedScheduleWeekMenu by remember { mutableStateOf(false) }
    AssistChip(
        modifier = modifier.animateContentSize(),
        onClick = { isExpandedScheduleWeekMenu = true },
        label = { Text(text = selected.title) },
        trailingIcon = { ExpandedIcon(isExpanded = isExpandedScheduleWeekMenu) }
    )

    ScheduleWeekDropdownMenu(
        isExpanded = isExpandedScheduleWeekMenu,
        selected = selected,
        maxNumberOfWeek = maxNumberOfWeek,
        onDismiss = { isExpandedScheduleWeekMenu = false },
        onSelect = { numberOfWeek ->
            onSelect(numberOfWeek)
            isExpandedScheduleWeekMenu = false
        },
    )
}

@Composable
internal fun ScheduleWeekDropdownMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    selected: NumberOfWeekItem,
    maxNumberOfWeek: Int = NumberOfWeekItem.THREE.isoWeekNumber,
    onDismiss: () -> Unit,
    onSelect: (NumberOfWeekItem) -> Unit,
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismiss,
        modifier = modifier.sizeIn(maxHeight = 200.dp),
        shape = MaterialTheme.shapes.large,
        offset = DpOffset(0.dp, 6.dp),
    ) {
        NumberOfWeekItem.entries.filter { it.isoWeekNumber <= maxNumberOfWeek }.forEach { item ->
            val isSelected = selected == item
            DropdownMenuItem(
                modifier = Modifier.alphaByEnabled(!isSelected),
                onClick = { onSelect(item) },
                enabled = !isSelected,
                text = {
                    Text(
                        text = item.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
            )
        }
    }
}