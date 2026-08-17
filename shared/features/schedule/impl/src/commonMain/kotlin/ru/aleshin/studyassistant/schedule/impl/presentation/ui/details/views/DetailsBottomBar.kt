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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.isoWeekNumber
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.settings.WeekScheduleViewType
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.theme.material.topSide
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthFormat
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.common_schedule_view_type
import ru.aleshin.studyassistant.schedule.impl.resources.current_week_title
import ru.aleshin.studyassistant.schedule.impl.resources.ic_format_columns
import ru.aleshin.studyassistant.schedule.impl.resources.ic_format_grid
import ru.aleshin.studyassistant.schedule.impl.resources.next_week_title
import ru.aleshin.studyassistant.schedule.impl.resources.previous_week_title
import ru.aleshin.studyassistant.schedule.impl.resources.vertical_schedule_view_type

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
@Composable
internal fun DetailsBottomBar(
    modifier: Modifier = Modifier,
    currentWeek: TimeRange?,
    selectedWeek: TimeRange?,
    viewType: WeekScheduleViewType,
    useExpandedWeek: Boolean,
    onNextWeekSelected: () -> Unit,
    onPreviousWeekSelected: () -> Unit,
    onViewTypeSelected: (WeekScheduleViewType) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.topSide,
        color = if (useExpandedWeek) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (useExpandedWeek) {
                Arrangement.End
            } else {
                Arrangement.spacedBy(16.dp)
            }
        ) {
            WeekPickerView(
                modifier = if (useExpandedWeek) Modifier.padding(bottom = 12.dp, end = 12.dp) else Modifier,
                currentWeek = currentWeek,
                selectedWeek = selectedWeek,
                onPreviousWeekSelected = onPreviousWeekSelected,
                onNextWeekSelected = onNextWeekSelected,
            )
            if (!useExpandedWeek) {
                Spacer(modifier = Modifier.weight(1f))
            }
            AnimatedVisibility(
                visible = !useExpandedWeek,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
            ) {
                ScheduleViewTypePicker(
                    selectedType = viewType,
                    onSelected = onViewTypeSelected,
                )
            }
        }
    }
}

@Composable
internal fun WeekPickerView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    currentWeek: TimeRange?,
    selectedWeek: TimeRange?,
    onPreviousWeekSelected: () -> Unit,
    onNextWeekSelected: () -> Unit,
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
                onClick = onPreviousWeekSelected,
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
                    currentWeek == null || selectedWeek == null -> stringResource(Res.string.current_week_title)
                    currentWeek == selectedWeek -> stringResource(Res.string.current_week_title)
                    currentWeekNumber.inc() == selectedWeekNumber -> stringResource(Res.string.next_week_title)
                    currentWeekNumber.dec() == selectedWeekNumber -> stringResource(Res.string.previous_week_title)
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
                onClick = onNextWeekSelected,
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
        modifier = modifier.animateContentSize(),
        enabled = enabled,
        shape = MaterialTheme.shapes.full,
        color = Color.Transparent,
    ) {
        BoxWithConstraints {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (this@BoxWithConstraints.maxWidth > 158.dp) {
                    Text(
                        text = when (selectedType) {
                            WeekScheduleViewType.COMMON -> stringResource(Res.string.common_schedule_view_type)
                            WeekScheduleViewType.VERTICAL -> stringResource(Res.string.vertical_schedule_view_type)
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = when (selectedType) {
                        WeekScheduleViewType.COMMON -> painterResource(Res.drawable.ic_format_grid)
                        WeekScheduleViewType.VERTICAL -> painterResource(Res.drawable.ic_format_columns)
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}