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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.dateTimeByWeek
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.domain.entities.settings.WeekScheduleViewType
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.ScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.WeekScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsViewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.CommonScheduleView
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.CommonScheduleViewPlaceholder

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
@Composable
internal fun DetailsContent(
    state: DetailsViewState,
    modifier: Modifier = Modifier,
    onOpenSchedule: (Instant) -> Unit,
    onShowClassInfo: (ClassDetailsUi, ScheduleDetailsUi, Instant) -> Unit,
) = with(state) {
    Crossfade(
        modifier = modifier,
        targetState = scheduleView,
        animationSpec = tween(),
    ) { scheduleViewType ->
        when (scheduleViewType) {
            WeekScheduleViewType.COMMON -> DetailsCommonSchedulesSection(
                isLoading = isLoading,
                currentDate = currentDate,
                weekSchedule = weekSchedule,
                activeClass = activeClass,
                onOpenSchedule = onOpenSchedule,
                onShowClassInfo = onShowClassInfo,
            )
            WeekScheduleViewType.VERTICAL -> DetailsVerticalSchedulesSection(
                isLoading = isLoading,
                currentDate = currentDate,
                weekSchedule = weekSchedule,
                activeClass = activeClass,
                onOpenSchedule = onOpenSchedule,
                onShowClassInfo = onShowClassInfo,
            )
        }
    }
}

@Composable
private fun DetailsCommonSchedulesSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    weekSchedule: WeekScheduleDetailsUi?,
    activeClass: ActiveClassUi?,
    onOpenSchedule: (Instant) -> Unit,
    onShowClassInfo: (ClassDetailsUi, ScheduleDetailsUi, Instant) -> Unit,
) {
    Crossfade(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        targetState = isLoading,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = Spring.DefaultDisplacementThreshold,
        )
    ) { loading ->
        if (!loading && weekSchedule != null) {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                columnsWithDaysOfWeek.forEach { daysOfWeek ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        daysOfWeek.forEach { dayOfWeek ->
                            val schedule = weekSchedule.weekDaySchedules[dayOfWeek]
                            val scheduleDate = dayOfWeek.dateTimeByWeek(weekSchedule.from)
                            val classes = schedule?.mapToValue(
                                onBaseSchedule = { it?.classes },
                                onCustomSchedule = { it?.classes },
                            )

                            CommonScheduleView(
                                date = scheduleDate.dateTime().date,
                                isCurrentDay = currentDate.equalsDay(scheduleDate),
                                activeClass = activeClass,
                                classes = classes ?: emptyList(),
                                onOpenSchedule = { onOpenSchedule(scheduleDate) },
                                onClassClick = {
                                    if (schedule != null) onShowClassInfo(it, schedule, scheduleDate)
                                },
                            )
                        }
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(DayOfWeek.entries.size) {
                    CommonScheduleViewPlaceholder()
                }
            }
        }
    }
}

@Composable
private fun DetailsVerticalSchedulesSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    weekSchedule: WeekScheduleDetailsUi?,
    activeClass: ActiveClassUi?,
    onOpenSchedule: (Instant) -> Unit,
    onShowClassInfo: (ClassDetailsUi, ScheduleDetailsUi, Instant) -> Unit,
) {
    Crossfade(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        targetState = isLoading,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = Spring.DefaultDisplacementThreshold,
        )
    ) { loading ->
        if (!loading && weekSchedule != null) {
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(DayOfWeek.entries.toTypedArray(), key = { it.name }) { dayOfWeek ->
                    val schedule = weekSchedule.weekDaySchedules[dayOfWeek]
                    val scheduleDate = dayOfWeek.dateTimeByWeek(weekSchedule.from)
                    val classes = schedule?.mapToValue(
                        onBaseSchedule = { it?.classes },
                        onCustomSchedule = { it?.classes },
                    )

                    CommonScheduleView(
                        date = scheduleDate.dateTime().date,
                        isCurrentDay = currentDate.equalsDay(scheduleDate),
                        activeClass = activeClass,
                        classes = classes ?: emptyList(),
                        onOpenSchedule = { onOpenSchedule(scheduleDate) },
                        onClassClick = {
                            if (schedule != null) onShowClassInfo(it, schedule, scheduleDate)
                        },
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(DayOfWeek.entries.size) {
                    CommonScheduleViewPlaceholder()
                }
            }
        }
    }
}

private val columnsWithDaysOfWeek: List<List<DayOfWeek>>
    get() = listOf(
        listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
        listOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
    )