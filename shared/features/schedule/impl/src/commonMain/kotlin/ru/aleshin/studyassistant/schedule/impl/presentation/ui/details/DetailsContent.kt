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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.dateTimeByWeek
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.extensions.weekTimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.settings.WeekScheduleViewType
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.WeekScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.common.ClassBottomSheet
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.store.DetailsComponent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.CommonScheduleView
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.CommonScheduleViewPlaceholder
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.DetailsBottomBar
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.DetailsTopBar

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
@Composable
internal fun DetailsContent(
    detailsComponent: DetailsComponent,
    modifier: Modifier = Modifier,
) {
    val store = detailsComponent.store
    val state by store.stateAsState()
    val strings = ScheduleThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseDetailsContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onEditHomeworkClick = {
                    store.dispatchEvent(DetailsEvent.ClickEditHomework(it))
                },
                onAddHomeworkClick = { homework, date ->
                    store.dispatchEvent(DetailsEvent.ClickAddHomework(homework, date))
                },
                onAgainHomeworkClick = {
                    store.dispatchEvent(DetailsEvent.ClickAgainHomework(it))
                },
                onCompleteHomework = {
                    store.dispatchEvent(DetailsEvent.CompleteHomework(it))
                },
            )
        },
        topBar = {
            DetailsTopBar(
                onEditClick = {
                    store.dispatchEvent(DetailsEvent.ClickEdit)
                },
                onCurrentWeekSelected = {
                    store.dispatchEvent(DetailsEvent.SelectedCurrentWeek)
                },
                onOverviewClick = {
                    store.dispatchEvent(DetailsEvent.ClickOverview)
                },
            )
        },
        bottomBar = {
            DetailsBottomBar(
                currentWeek = state.currentDate.dateTime().weekTimeRange(),
                selectedWeek = state.selectedWeek,
                viewType = state.scheduleView,
                onNextWeekSelected = {
                    store.dispatchEvent(DetailsEvent.SelectedNextWeek)
                },
                onPreviousWeekSelected = {
                    store.dispatchEvent(DetailsEvent.SelectedPreviousWeek)
                },
                onViewTypeSelected = {
                    store.dispatchEvent(DetailsEvent.SelectedViewType(it))
                },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    )

    store.handleEffects { effect ->
        when (effect) {
            is DetailsEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseDetailsContent(
    state: DetailsState,
    modifier: Modifier = Modifier,
    onAddHomeworkClick: (ClassDetailsUi, Instant) -> Unit,
    onEditHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onAgainHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onCompleteHomework: (HomeworkDetailsUi) -> Unit,
) {
    Crossfade(
        modifier = modifier,
        targetState = state.scheduleView,
        animationSpec = tween(),
    ) { scheduleViewType ->
        when (scheduleViewType) {
            WeekScheduleViewType.COMMON -> DetailsCommonSchedulesSection(
                isLoading = state.isLoading,
                currentDate = state.currentDate,
                weekSchedule = state.weekSchedule,
                activeClass = state.activeClass,
                onAddHomeworkClick = onAddHomeworkClick,
                onEditHomeworkClick = onEditHomeworkClick,
                onAgainHomeworkClick = onAgainHomeworkClick,
                onCompleteHomeworkClick = onCompleteHomework,
            )

            WeekScheduleViewType.VERTICAL -> DetailsVerticalSchedulesSection(
                isLoading = state.isLoading,
                currentDate = state.currentDate,
                weekSchedule = state.weekSchedule,
                activeClass = state.activeClass,
                onAddHomeworkClick = onAddHomeworkClick,
                onEditHomeworkClick = onEditHomeworkClick,
                onAgainHomeworkClick = onAgainHomeworkClick,
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
    onAddHomeworkClick: (ClassDetailsUi, Instant) -> Unit,
    onEditHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onAgainHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onCompleteHomeworkClick: (HomeworkDetailsUi) -> Unit,
) {
    Crossfade(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        targetState = isLoading,
        animationSpec = floatSpring(),
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
                                date = scheduleDate.dateTime().date,
                                isCurrentDay = currentDate.equalsDay(scheduleDate),
                                activeClass = activeClass,
                                classes = classes ?: emptyList(),
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
    onAddHomeworkClick: (ClassDetailsUi, Instant) -> Unit,
    onEditHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onAgainHomeworkClick: (HomeworkDetailsUi) -> Unit,
    onCompleteHomework: (HomeworkDetailsUi) -> Unit,
) {
    Crossfade(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        targetState = isLoading,
        animationSpec = floatSpring(),
    ) { loading ->
        if (!loading && weekSchedule != null) {
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(DayOfWeek.entries.toTypedArray(), key = { it.name }) { dayOfWeek ->
                    val classSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    var selectedSheetClass by remember { mutableStateOf<UID?>(null) }
                    var openClassBottomSheet by remember { mutableStateOf(false) }

                    val schedule = remember(weekSchedule, dayOfWeek) {
                        weekSchedule.weekDaySchedules[dayOfWeek]
                    }
                    val scheduleDate = remember(weekSchedule, dayOfWeek) {
                        dayOfWeek.dateTimeByWeek(weekSchedule.from)
                    }
                    val classes = remember(schedule) {
                        schedule?.mapToValue(
                            onBaseSchedule = { it?.classes },
                            onCustomSchedule = { it?.classes },
                        )
                    }

                    CommonScheduleView(
                        date = scheduleDate.dateTime().date,
                        isCurrentDay = currentDate.equalsDay(scheduleDate),
                        activeClass = activeClass,
                        classes = classes ?: emptyList(),
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
                            onCompleteHomeworkClick = onCompleteHomework,
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