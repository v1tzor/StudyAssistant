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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import entities.settings.NumberOfWeek
import extensions.alphaByEnabled
import mappers.mapToSting
import ru.aleshin.studyassistant.preview.impl.presentation.models.CalendarSettingsUi
import theme.StudyAssistantRes


/**
 * @author Stanislav Aleshin on 27.04.2024
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun CalendarPageInfo(
    modifier: Modifier = Modifier,
    calendarSettings: CalendarSettingsUi,
    onUpdateCalendarSettings: (CalendarSettingsUi) -> Unit,
) {
    LazyColumn(
        modifier = modifier.padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(NumberOfWeek.entries) { week ->
            NumberOfWeekView(
                modifier = Modifier.animateItemPlacement(),
                selected = week == calendarSettings.numberOfWeek,
                week = week,
                onSelected = { onUpdateCalendarSettings(calendarSettings.copy(numberOfWeek = week)) },
            )
        }
    }
}

@Composable
internal fun NumberOfWeekView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean,
    week: NumberOfWeek,
    onSelected: () -> Unit,
) {
    Surface(
        onClick = onSelected,
        modifier = modifier.heightIn(48.dp).alphaByEnabled(enabled),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = animateColorAsState(
            animationSpec = tween(300),
            targetValue = when (selected) {
                true -> MaterialTheme.colorScheme.primaryContainer
                false -> MaterialTheme.colorScheme.surfaceContainer
            },
        ).value,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = week.mapToSting(StudyAssistantRes.strings),
                color = animateColorAsState(
                    animationSpec = tween(300),
                    targetValue = when (selected) {
                        true -> MaterialTheme.colorScheme.onPrimaryContainer
                        false -> MaterialTheme.colorScheme.onSurface
                    },
                ).value,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (selected)
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
        }
    }
}