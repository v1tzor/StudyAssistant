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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import extensions.dateTime
import extensions.formatByTimeZone
import extensions.isoWeekNumber
import functional.TimeRange
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.ScheduleViewType
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import theme.material.full

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
@Composable
internal fun DetailsBottomBar(
    modifier: Modifier = Modifier,
    currentWeek: TimeRange?,
    selectedWeek: TimeRange?,
    viewType: ScheduleViewType,
    onNextWeek: () -> Unit,
    onPreviousWeek: () -> Unit,
    onViewTypeSelected: (ScheduleViewType) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WeekPickerView(
                currentWeek = currentWeek,
                selectedWeek = selectedWeek,
                onPreviousWeek = onPreviousWeek,
                onNextWeek = onNextWeek,
            )
            Spacer(modifier = Modifier.weight(1f))
            ScheduleViewTypePicker(
                selectedType = viewType,
                onSelected = onViewTypeSelected,
            )
        }
    }
}

@Composable
internal fun WeekPickerView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    currentWeek: TimeRange?,
    selectedWeek: TimeRange?,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    val currentWeekNumber = currentWeek?.from?.dateTime()?.date?.isoWeekNumber() ?: Int.MIN_VALUE
    val selectedWeekNumber = selectedWeek?.from?.dateTime()?.date?.isoWeekNumber() ?: Int.MAX_VALUE
    val dateFormat = DateTimeComponents.Format {
        dayOfMonth()
        char('.')
        monthNumber()
    }
    Surface(
        modifier = modifier.animateContentSize().height(36.dp),
        shape = MaterialTheme.shapes.full(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onPreviousWeek,
                modifier = Modifier.size(36.dp),
                enabled = enabled,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = when {
                    currentWeek == null || selectedWeek == null -> ScheduleThemeRes.strings.currentWeekTitle
                    currentWeek == selectedWeek -> ScheduleThemeRes.strings.currentWeekTitle
                    currentWeekNumber.inc() == selectedWeekNumber -> ScheduleThemeRes.strings.nextWeekTitle
                    currentWeekNumber.dec() == selectedWeekNumber -> ScheduleThemeRes.strings.previousWeekTitle
                    else -> buildString {
                        append(selectedWeek.from.formatByTimeZone(dateFormat))
                        append(" - ")
                        append(selectedWeek.to.formatByTimeZone(dateFormat))
                    }
                },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
            )
            IconButton(
                onClick = onNextWeek,
                modifier = Modifier.size(36.dp),
                enabled = enabled,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun ScheduleViewTypePicker(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedType: ScheduleViewType,
    onSelected: (ScheduleViewType) -> Unit,
) {
    Surface(
        onClick = {
            val viewType = when (selectedType) {
                ScheduleViewType.COMMON -> ScheduleViewType.VERTICAL
                ScheduleViewType.VERTICAL -> ScheduleViewType.COMMON
            }
            onSelected(viewType)
        },
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.full(),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (selectedType) {
                    ScheduleViewType.COMMON -> ScheduleThemeRes.strings.commonScheduleViewType
                    ScheduleViewType.VERTICAL -> ScheduleThemeRes.strings.verticalScheduleViewType
                },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
            Icon(
                modifier = Modifier.size(18.dp),
                painter = when (selectedType) {
                    ScheduleViewType.COMMON -> painterResource(ScheduleThemeRes.icons.formatGrid)
                    ScheduleViewType.VERTICAL -> painterResource(ScheduleThemeRes.icons.formatColumns)
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}