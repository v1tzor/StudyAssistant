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

import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_priority_high as core_ic_priority_high
import ru.aleshin.studyassistant.editor.impl.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.ui.views.SegmentedButtonItem
import ru.aleshin.studyassistant.core.ui.views.SegmentedButtons

/**
 * @author Stanislav Aleshin on 23.06.2024.
 */
@Composable
internal fun TaskPriorityInfoView(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    priority: TaskPriority?,
    onChangePriority: (TaskPriority) -> Unit,
) {
    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(CoreRes.drawable.core_ic_priority_high),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TaskPriorityView(
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            selected = priority,
            onChangePriority = onChangePriority,
        )
    }
}

@Composable
internal fun TaskPriorityView(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    selected: TaskPriority?,
    onChangePriority: (TaskPriority) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.priority_view_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
            )
            SegmentedButtons(
                enabled = { enabled },
                items = TaskPrioritySegmentedItem.entries.toTypedArray(),
                selectedItem = selected?.toItem(),
                onItemClick = { onChangePriority(it.toModel()) },
            )
        }
    }
}

internal enum class TaskPrioritySegmentedItem : SegmentedButtonItem {
    STANDARD {
        override val title: String @Composable get() = stringResource(Res.string.standard_priority_item_title)
    },
    MEDIUM {
        override val title: String @Composable get() = stringResource(Res.string.medium_priority_item_title)
    },
    HIGH {
        override val title: String @Composable get() = stringResource(Res.string.high_priority_item_title)
    }
}

internal fun TaskPrioritySegmentedItem.toModel() = when (this) {
    TaskPrioritySegmentedItem.STANDARD -> TaskPriority.STANDARD
    TaskPrioritySegmentedItem.MEDIUM -> TaskPriority.MEDIUM
    TaskPrioritySegmentedItem.HIGH -> TaskPriority.HIGH
}

internal fun TaskPriority.toItem() = when (this) {
    TaskPriority.STANDARD -> TaskPrioritySegmentedItem.STANDARD
    TaskPriority.MEDIUM -> TaskPrioritySegmentedItem.MEDIUM
    TaskPriority.HIGH -> TaskPrioritySegmentedItem.HIGH
}