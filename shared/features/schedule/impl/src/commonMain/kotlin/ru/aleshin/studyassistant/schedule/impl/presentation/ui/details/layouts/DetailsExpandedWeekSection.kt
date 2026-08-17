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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.layouts

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.dateTimeByWeek
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.WeekScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.common.ClassBottomSheet
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.CommonScheduleView
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.CommonScheduleViewPlaceholder

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DetailsExpandedWeekSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    weekSchedule: WeekScheduleDetailsUi?,
    activeClass: ActiveClassUi?,
    onAddHomeworkClick: (ClassDetailsUi, Instant) -> Unit,
    onEditHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onAgainHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onCompleteHomeworkClick: (HomeworkDetailsUi) -> Unit,
) {
    Crossfade(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        targetState = isLoading,
        animationSpec = floatSpring(),
    ) { loading ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val spacing = 8.dp
            val minDayWidth = 148.dp
            val preferredDayWidth = 180.dp
            val maxDayWidth = 200.dp
            val availableDayWidth = (maxWidth - spacing * 6) / 7
            val dayWidth = when {
                availableDayWidth < minDayWidth -> minDayWidth
                availableDayWidth > maxDayWidth -> maxDayWidth
                else -> availableDayWidth
            }
            val totalWidth = dayWidth * 7 + spacing * 6
            val useHorizontalScroll = totalWidth > maxWidth

            if (loading || weekSchedule == null) {
                DetailsExpandedWeekPlaceholder(
                    dayWidth = dayWidth,
                    spacing = spacing,
                    useHorizontalScroll = useHorizontalScroll,
                    maxHeight = maxHeight,
                )
            } else {
                val daysRow: @Composable () -> Unit = {
                    Row(
                        modifier = Modifier
                            .heightIn(max = maxHeight)
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                    ) {
                        DayOfWeek.entries.forEach { dayOfWeek ->
                            DetailsExpandedDayColumn(
                                modifier = Modifier
                                    .width(dayWidth)
                                    .fillMaxHeight(),
                                dayOfWeek = dayOfWeek,
                                currentDate = currentDate,
                                weekSchedule = weekSchedule,
                                activeClass = activeClass,
                                onAddHomeworkClick = onAddHomeworkClick,
                                onEditHomeworkClick = onEditHomeworkClick,
                                onAgainHomeworkClick = onAgainHomeworkClick,
                                onCompleteHomeworkClick = onCompleteHomeworkClick,
                            )
                        }
                    }
                }

                if (useHorizontalScroll) {
                    Row(
                        modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()),
                    ) {
                        daysRow()
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        daysRow()
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailsExpandedDayColumn(
    modifier: Modifier = Modifier,
    dayOfWeek: DayOfWeek,
    currentDate: Instant,
    weekSchedule: WeekScheduleDetailsUi,
    activeClass: ActiveClassUi?,
    onAddHomeworkClick: (ClassDetailsUi, Instant) -> Unit,
    onEditHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onAgainHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onCompleteHomeworkClick: (HomeworkDetailsUi) -> Unit,
) {
    val classSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedSheetClass by remember { mutableStateOf<UID?>(null) }
    var openClassBottomSheet by remember { mutableStateOf(false) }
    val schedule = remember(weekSchedule, dayOfWeek) {
        weekSchedule.weekDaySchedules[dayOfWeek]
    }
    val scheduleDate = remember(dayOfWeek, weekSchedule) {
        dayOfWeek.dateTimeByWeek(weekSchedule.from)
    }
    val classes = remember(schedule) {
        schedule?.mapToValue(
            onBaseSchedule = { it?.classes },
            onCustomSchedule = { it?.classes },
        )
    }

    CommonScheduleView(
        modifier = modifier,
        date = scheduleDate.dateTime().date,
        isCurrentDay = currentDate.equalsDay(scheduleDate),
        activeClass = activeClass,
        classes = classes ?: emptyList(),
        scrollableClasses = true,
        onClassClick = {
            selectedSheetClass = it.uid
            openClassBottomSheet = true
        },
    )

    val classModel = remember(classes, selectedSheetClass) {
        classes?.find { it.uid == selectedSheetClass }
    }
    if (openClassBottomSheet && classModel != null) {
        ClassBottomSheet(
            sheetState = classSheetState,
            activeClass = activeClass,
            classModel = classModel,
            classDate = scheduleDate,
            onEditHomeworkClick = onEditHomeworkClick,
            onAddHomeworkClick = onAddHomeworkClick,
            onAgainHomeworkClick = onAgainHomeworkClick,
            onCompleteHomeworkClick = onCompleteHomeworkClick,
            onDismissRequest = {
                openClassBottomSheet = false
                selectedSheetClass = null
            },
        )
    }
}

@Composable
private fun DetailsExpandedWeekPlaceholder(
    dayWidth: Dp,
    spacing: Dp,
    useHorizontalScroll: Boolean,
    maxHeight: Dp,
) {
    val placeholders: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .heightIn(max = maxHeight)
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            repeat(DayOfWeek.entries.size) {
                CommonScheduleViewPlaceholder(
                    modifier = Modifier
                        .width(dayWidth)
                        .fillMaxHeight(),
                )
            }
        }
    }
    if (useHorizontalScroll) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()),
        ) {
            placeholders()
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            placeholders()
        }
    }
}
