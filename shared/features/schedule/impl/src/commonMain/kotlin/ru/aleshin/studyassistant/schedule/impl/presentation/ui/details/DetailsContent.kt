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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.dateTimeByWeek
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.settings.WeekScheduleViewType
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.WeekScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.common.ClassBottomSheet
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
    onAddHomework: (ClassDetailsUi, Instant) -> Unit,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onAgainHomework: (HomeworkDetailsUi) -> Unit,
    onCompleteHomework: (HomeworkDetailsUi) -> Unit,
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
                onAddHomework = onAddHomework,
                onEditHomework = onEditHomework,
                onAgainHomework = onAgainHomework,
                onCompleteHomework = onCompleteHomework,
            )
            WeekScheduleViewType.VERTICAL -> DetailsVerticalSchedulesSection(
                isLoading = isLoading,
                currentDate = currentDate,
                weekSchedule = weekSchedule,
                activeClass = activeClass,
                onOpenSchedule = onOpenSchedule,
                onAddHomework = onAddHomework,
                onEditHomework = onEditHomework,
                onAgainHomework = onAgainHomework,
                onCompleteHomework = onCompleteHomework,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailsCommonSchedulesSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    weekSchedule: WeekScheduleDetailsUi?,
    activeClass: ActiveClassUi?,
    onOpenSchedule: (Instant) -> Unit,
    onAddHomework: (ClassDetailsUi, Instant) -> Unit,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onAgainHomework: (HomeworkDetailsUi) -> Unit,
    onCompleteHomework: (HomeworkDetailsUi) -> Unit,
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
                            var openClassBottomSheet by remember { mutableStateOf(false) }
                            val classSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                            var selectedSheetClass by remember { mutableStateOf<UID?>(null) }

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
                                    selectedSheetClass = it.uid
                                    openClassBottomSheet = true
                                },
                            )

                            val classModel = classes?.find { it.uid == selectedSheetClass }
                            if (openClassBottomSheet && classModel != null) {
                                ClassBottomSheet(
                                    sheetState = classSheetState,
                                    activeClass = activeClass,
                                    classModel = classModel,
                                    classDate = scheduleDate,
                                    onEditHomework = onEditHomework,
                                    onAddHomework = onAddHomework,
                                    onAgainHomework = onAgainHomework,
                                    onCompleteHomework = onCompleteHomework,
                                    onDismissRequest = {
                                        openClassBottomSheet = false
                                        selectedSheetClass = null
                                    },
                                )
                            }
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
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailsVerticalSchedulesSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    weekSchedule: WeekScheduleDetailsUi?,
    activeClass: ActiveClassUi?,
    onOpenSchedule: (Instant) -> Unit,
    onAddHomework: (ClassDetailsUi, Instant) -> Unit,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onAgainHomework: (HomeworkDetailsUi) -> Unit,
    onCompleteHomework: (HomeworkDetailsUi) -> Unit,
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
                    var openClassBottomSheet by remember { mutableStateOf(false) }
                    val classSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    var selectedSheetClass by remember { mutableStateOf<UID?>(null) }

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
                            selectedSheetClass = it.uid
                            openClassBottomSheet = true
                        },
                    )

                    val classModel = classes?.find { it.uid == selectedSheetClass }
                    if (openClassBottomSheet && classModel != null) {
                        ClassBottomSheet(
                            sheetState = classSheetState,
                            activeClass = activeClass,
                            classModel = classModel,
                            classDate = scheduleDate,
                            onEditHomework = onEditHomework,
                            onAddHomework = onAddHomework,
                            onAgainHomework = onAgainHomework,
                            onCompleteHomework = onCompleteHomework,
                            onDismissRequest = {
                                openClassBottomSheet = false
                                selectedSheetClass = null
                            },
                        )
                    }
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