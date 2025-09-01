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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.store.HomeworksComponent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.DailyHomeworksDetailsView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.DailyHomeworksDetailsViewPlaceholder
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.HomeworksTopBar
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.HomeworksTopSheet
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.ShareHomeworksBottomSheet

/**
 * @author Stanislav Aleshin on 03.07.2024
 */
@Composable
internal fun HomeworksContent(
    homeworksComponent: HomeworksComponent,
    modifier: Modifier = Modifier,
) {
    val store = homeworksComponent.store
    val state by store.stateAsState()
    val strings = TasksThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val listState = rememberLazyListState()
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseHomeworksContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                listState = listState,
                onAddHomework = {
                    store.dispatchEvent(HomeworksEvent.ClickAddHomework(it))
                },
                onEditHomework = {
                    store.dispatchEvent(HomeworksEvent.ClickEditHomework(it))
                },
                onDoHomework = {
                    store.dispatchEvent(HomeworksEvent.DoHomework(it))
                },
                onRepeatHomework = {
                    store.dispatchEvent(HomeworksEvent.RepeatHomework(it))
                },
                onSkipHomework = {
                    store.dispatchEvent(HomeworksEvent.SkipHomework(it))
                },
                onShareHomeworks = {
                    store.dispatchEvent(HomeworksEvent.ShareHomeworks(it))
                },
                onScheduleGoal = {
                    store.dispatchEvent(HomeworksEvent.ScheduleGoal(it))
                },
                onDeleteGoal = {
                    store.dispatchEvent(HomeworksEvent.DeleteGoal(it))
                },
                onPaidFunctionClick = {
                    store.dispatchEvent(HomeworksEvent.ClickPaidFunction)
                },
            )
        },
        topBar = {
            Column {
                HomeworksTopBar(
                    onCurrentTimeRangeClick = {
                        store.dispatchEvent(HomeworksEvent.ClickCurrentTimeRange)
                    },
                    onBackClick = {
                        store.dispatchEvent(HomeworksEvent.ClickBack)
                    },
                )
                HomeworksTopSheet(
                    isLoading = state.isLoading,
                    selectedTimeRange = state.selectedTimeRange,
                    progressList = remember(state.homeworks) {
                        val allHomeworks = state.homeworks.map { it.value.fetchAllHomeworks() }.extractAllItem()
                        allHomeworks.map { it.completeDate != null }
                    },
                    onNextTimeRangeClick = {
                        store.dispatchEvent(HomeworksEvent.ClickNextTimeRange)
                    },
                    onPreviousTimeRangeClick = {
                        store.dispatchEvent(HomeworksEvent.ClickPreviousTimeRange)
                    },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { store.dispatchEvent(HomeworksEvent.AddHomeworkInEditor) },
                shape = MaterialTheme.shapes.large,
                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    )

    store.handleEffects { effect ->
        when (effect) {
            is HomeworksEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
            is HomeworksEffect.ScrollToDate -> {
                delay(100L)
                val selectedDateIndex = state.homeworks.toList().indexOfFirst {
                    effect.targetDate.equalsDay(it.first)
                }
                if (selectedDateIndex != -1) {
                    listState.animateScrollToItem(selectedDateIndex)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
private fun BaseHomeworksContent(
    state: HomeworksState,
    modifier: Modifier,
    listState: LazyListState = rememberLazyListState(),
    onAddHomework: (Instant) -> Unit,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
    onScheduleGoal: (GoalCreateModelUi) -> Unit,
    onDeleteGoal: (GoalShortUi) -> Unit,
    onPaidFunctionClick: () -> Unit,
) {
    Crossfade(
        modifier = modifier.padding(top = 12.dp),
        targetState = state.isLoading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        if (loading) {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false,
            ) {
                items(Placeholder.HOMEWORKS) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DailyHomeworksDetailsViewPlaceholder()
                        DailyHomeworksDetailsVerticalDivider()
                    }
                }
            }
        } else {
            val homeworksMapList = remember(state.homeworks) { state.homeworks.toList() }
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = true,
            ) {
                items(homeworksMapList, key = { it.first.toString() }) { homeworksEntry ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        var isShowSharedHomeworksSheet by remember { mutableStateOf(false) }
                        DailyHomeworksDetailsView(
                            isPaidUser = state.isPaidUser,
                            date = homeworksEntry.first,
                            currentDate = state.currentDate,
                            isPassed = homeworksEntry.first < state.currentDate,
                            dailyHomeworks = homeworksEntry.second,
                            onAddHomework = onAddHomework,
                            onDoHomework = onDoHomework,
                            onOpenHomeworkTask = onEditHomework,
                            onSkipHomework = onSkipHomework,
                            onRepeatHomework = onRepeatHomework,
                            onShareHomeworks = { isShowSharedHomeworksSheet = true },
                            onScheduleGoal = onScheduleGoal,
                            onDeleteGoal = onDeleteGoal,
                            onOpenBillingScreen = onPaidFunctionClick,
                        )

                        DailyHomeworksDetailsVerticalDivider()

                        if (isShowSharedHomeworksSheet) {
                            ShareHomeworksBottomSheet(
                                currentTime = Clock.System.now(),
                                targetDate = homeworksEntry.first,
                                homeworks = homeworksEntry.second.fetchAllHomeworks(),
                                allFriends = state.friends,
                                onDismissRequest = { isShowSharedHomeworksSheet = false },
                                onConfirm = {
                                    onShareHomeworks(it)
                                    isShowSharedHomeworksSheet = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyHomeworksDetailsVerticalDivider(
    modifier: Modifier = Modifier,
    headerHeight: Dp = 28.dp,
    verticalArrangement: Dp = 12.dp,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    Canvas(modifier.fillMaxHeight().width(thickness)) {
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(thickness.toPx() / 2, 0f),
            end = Offset(thickness.toPx() / 2, headerHeight.toPx()),
        )
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(thickness.toPx() / 2, headerHeight.toPx() + verticalArrangement.toPx()),
            end = Offset(thickness.toPx() / 2, size.height),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 4.dp.toPx()))
        )
    }
}