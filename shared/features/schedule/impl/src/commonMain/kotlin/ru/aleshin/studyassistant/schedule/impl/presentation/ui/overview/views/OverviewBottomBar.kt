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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import extensions.dateTime
import extensions.isCurrentDay
import extensions.shiftDay
import functional.Constants.Date.OVERVIEW_FIRST_ITEM
import functional.Constants.Date.OVERVIEW_NEXT_DAYS
import functional.Constants.Date.OVERVIEW_PREVIOUS_DAYS
import functional.TimeRange
import kotlinx.datetime.Instant
import mappers.mapToSting
import theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 12.06.2024.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun OverviewBottomBar(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dateListState: LazyListState = rememberLazyListState(OVERVIEW_FIRST_ITEM),
    currentDate: Instant,
    selectedDate: Instant?,
    onSelectedDate: (Instant) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        var visibleTimeRange by remember {
            val from = currentDate.shiftDay(-OVERVIEW_PREVIOUS_DAYS)
            val to = currentDate.shiftDay(OVERVIEW_NEXT_DAYS)
            mutableStateOf(TimeRange(from, to))
        }
        val dateList by derivedStateOf { visibleTimeRange.periodDates() }
        LazyRow(
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
            state = dateListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(dateList) { date ->
                ScheduleDateItem(
                    modifier = Modifier.animateItemPlacement(),
                    enabled = enabled,
                    date = date,
                    selected = date.isCurrentDay(selectedDate),
                    isCurrentDay = date.isCurrentDay(currentDate),
                    onClick = { onSelectedDate(date) },
                )
            }
        }
        LaunchedEffect(selectedDate) {
            var index = dateList.indexOfFirst { it.isCurrentDay(selectedDate) }
            if (index != -1) {
                if (index == dateList.lastIndex || index == 0) {
                    val from = dateList[index].shiftDay(-OVERVIEW_PREVIOUS_DAYS)
                    val to = dateList[index].shiftDay(OVERVIEW_NEXT_DAYS)
                    index = OVERVIEW_PREVIOUS_DAYS
                    visibleTimeRange = TimeRange(from, to)
                }
                dateListState.animateScrollToItem(if (index > 0) index - 1 else index)
            }
        }
    }
}

@Composable
private fun ScheduleDateItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    date: Instant,
    selected: Boolean,
    isCurrentDay: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = if (isCurrentDay) {
            BorderStroke(
                width = 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            )
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = date.dateTime().dayOfMonth.toString(),
                color = if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = date.dateTime().dayOfWeek.mapToSting(StudyAssistantRes.strings),
                color = if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}