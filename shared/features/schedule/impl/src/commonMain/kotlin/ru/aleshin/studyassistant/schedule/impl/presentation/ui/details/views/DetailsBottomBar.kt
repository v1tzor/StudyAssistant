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
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.isoWeekNumber
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.settings.WeekScheduleViewType
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.theme.material.topSide
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthFormat
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
@Composable
internal fun DetailsBottomBar(
    modifier: Modifier = Modifier,
    currentWeek: TimeRange?,
    selectedWeek: TimeRange?,
    viewType: WeekScheduleViewType,
    onNextWeek: () -> Unit,
    onPreviousWeek: () -> Unit,
    onViewTypeSelected: (WeekScheduleViewType) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.topSide,
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
    val shortDateFormat = DateTimeComponents.Formats.shortDayMonthFormat()
    Surface(
        modifier = modifier.animateContentSize().height(36.dp),
        shape = MaterialTheme.shapes.full,
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
                        append(selectedWeek.from.formatByTimeZone(shortDateFormat))
                        append(" - ")
                        append(selectedWeek.to.formatByTimeZone(shortDateFormat))
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
    selectedType: WeekScheduleViewType,
    onSelected: (WeekScheduleViewType) -> Unit,
) {
    Surface(
        onClick = {
            val viewType = when (selectedType) {
                WeekScheduleViewType.COMMON -> WeekScheduleViewType.VERTICAL
                WeekScheduleViewType.VERTICAL -> WeekScheduleViewType.COMMON
            }
            onSelected(viewType)
        },
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.full,
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (selectedType) {
                    WeekScheduleViewType.COMMON -> ScheduleThemeRes.strings.commonScheduleViewType
                    WeekScheduleViewType.VERTICAL -> ScheduleThemeRes.strings.verticalScheduleViewType
                },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
            Icon(
                modifier = Modifier.size(18.dp),
                painter = when (selectedType) {
                    WeekScheduleViewType.COMMON -> painterResource(ScheduleThemeRes.icons.formatGrid)
                    WeekScheduleViewType.VERTICAL -> painterResource(ScheduleThemeRes.icons.formatColumns)
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}