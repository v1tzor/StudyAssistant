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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import extensions.dateByWeek
import extensions.isCurrentDay
import kotlinx.datetime.DayOfWeek
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.ScheduleViewType
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsViewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.CommonScheduleView
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.CommonScheduleViewPlaceholder

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DetailsContent(
    state: DetailsViewState,
    modifier: Modifier = Modifier,
) = with(state) {
    AnimatedContent(
        modifier = modifier,
        targetState = scheduleView,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)).togetherWith(
                fadeOut(animationSpec = tween(400))
            )
        },
    ) { scheduleViewType ->
        if (scheduleViewType == ScheduleViewType.COMMON) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!isLoading && weekSchedule != null) {
                    items(DayOfWeek.entries.toTypedArray()) { dayOfWeek ->
                        val schedule = weekSchedule.weekDaySchedules[dayOfWeek]
                        val scheduleDate = dayOfWeek.dateByWeek(weekSchedule.from)
                        CommonScheduleView(
                            modifier = Modifier.animateItemPlacement(),
                            date = scheduleDate,
                            isCurrentDay = currentDate.isCurrentDay(scheduleDate),
                            activeClass = activeClass,
                            classes = schedule?.mapToValue(
                                onBaseSchedule = { it?.classes },
                                onCustomSchedule = { it?.classes },
                            ) ?: emptyList(),
                            onClassClick = {},
                        )
                    }
                } else {
                    items(DayOfWeek.entries.size) {
                        CommonScheduleViewPlaceholder(
                            modifier = Modifier.animateItemPlacement(),
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!isLoading && weekSchedule != null) {
                    items(DayOfWeek.entries.toTypedArray()) { dayOfWeek ->
                        val schedule = weekSchedule.weekDaySchedules[dayOfWeek]
                        val scheduleDate = dayOfWeek.dateByWeek(weekSchedule.from)
                        CommonScheduleView(
                            modifier = Modifier.animateItemPlacement(),
                            date = scheduleDate,
                            isCurrentDay = currentDate.isCurrentDay(scheduleDate),
                            activeClass = activeClass,
                            classes = schedule?.mapToValue(
                                onBaseSchedule = { it?.classes },
                                onCustomSchedule = { it?.classes },
                            ) ?: emptyList(),
                            onClassClick = {},
                        )
                    }
                } else {
                    items(DayOfWeek.entries.size) {
                        CommonScheduleViewPlaceholder(
                            modifier = Modifier.animateItemPlacement(),
                        )
                    }
                }
            }
        }
    }
}
