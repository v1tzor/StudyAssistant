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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.handleLazyListScroll
import ru.aleshin.studyassistant.core.common.extensions.isNextDay
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.tokens.contentColorFor
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.DailyGoalsProgressUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.DeleteGoalWarningDialog

/**
 * @author Stanislav Aleshin on 26.03.2025.
 */
@Composable
internal fun DailyGoalsView(
    modifier: Modifier = Modifier,
    isLoadingGoals: Boolean,
    currentDate: Instant,
    selectedGoalsDate: Instant,
    dailyGoals: List<GoalDetailsUi>,
    goalsProgress: Map<Instant, DailyGoalsProgressUi>,
    onSelectDate: (Instant) -> Unit,
    onChangeGoalNumbers: (List<GoalDetailsUi>) -> Unit,
    onEditHomeworkClick: (HomeworkUi) -> Unit,
    onEditTodoClick: (TodoUi) -> Unit,
    onCompleteGoal: (GoalDetailsUi) -> Unit,
    onDeleteGoal: (GoalDetailsUi) -> Unit,
    onStartGoalTime: (GoalDetailsUi) -> Unit,
    onPauseGoalTime: (GoalDetailsUi) -> Unit,
    onResetGoalTime: (GoalDetailsUi) -> Unit,
    onChangeGoalTimeType: (GoalTime.Type, GoalDetailsUi) -> Unit,
    onChangeGoalDesiredTime: (Millis?, GoalDetailsUi) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(369.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            DailyGoalsViewHeader(
                enabled = !isLoadingGoals,
                currentDate = currentDate,
                selectedDate = selectedGoalsDate,
                onSelectDay = onSelectDate,
                onNextDay = { onSelectDate(selectedGoalsDate.shiftDay(1)) },
                onPreviousDay = { onSelectDate(selectedGoalsDate.shiftDay(-1)) },
            )
            HorizontalDivider()
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DailyGoalsViewProgressSection(
                    isLoading = isLoadingGoals,
                    currentDate = currentDate,
                    selectedGoalsDate = selectedGoalsDate,
                    goalsProgress = goalsProgress,
                )
                GoalListDivider(
                    isLoading = isLoadingGoals,
                    goalCount = dailyGoals.size,
                )
                DailyGoalsViewContent(
                    modifier = Modifier.weight(1f),
                    isLoading = isLoadingGoals,
                    dailyGoals = dailyGoals,
                    onEditHomeworkClick = onEditHomeworkClick,
                    onEditTodoClick = onEditTodoClick,
                    onCompleteGoal = onCompleteGoal,
                    onDeleteGoal = onDeleteGoal,
                    onChangeGoalNumbers = onChangeGoalNumbers,
                    onStartTime = onStartGoalTime,
                    onPauseTime = onPauseGoalTime,
                    onResetTime = onResetGoalTime,
                    onChangeTimeType = onChangeGoalTimeType,
                    onChangeDesiredTime = onChangeGoalDesiredTime,
                )
            }
        }
    }
}

@Composable
private fun DailyGoalsViewHeader(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    currentDate: Instant,
    selectedDate: Instant,
    onNextDay: () -> Unit,
    onPreviousDay: () -> Unit,
    onSelectDay: (Instant) -> Unit,
) {
    var datePickerDialogState by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier.size(36.dp),
                enabled = enabled,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Text(
            modifier = Modifier
                .alphaByEnabled(enabled)
                .weight(1f)
                .clickable(enabled = enabled) { datePickerDialogState = true },
            text = when {
                currentDate.equalsDay(selectedDate) -> StudyAssistantRes.strings.todayTitle
                currentDate.isNextDay(selectedDate) -> StudyAssistantRes.strings.tomorrowTitle
                else -> selectedDate.formatByTimeZone(
                    format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat(StudyAssistantRes.strings)
                )
            },
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onNextDay,
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

    if (datePickerDialogState) {
        GoalDatePicker(
            initSelectedDate = selectedDate,
            onDismiss = { datePickerDialogState = false },
            onSelectedDate = { date ->
                datePickerDialogState = false
                onSelectDay(date)
            }
        )
    }
}

@Composable
private fun DailyGoalsViewProgressSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    selectedGoalsDate: Instant,
    goalsProgress: Map<Instant, DailyGoalsProgressUi>,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = TasksThemeRes.strings.goalsViewProgressTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val currentGoalsProgress = remember(selectedGoalsDate, goalsProgress) {
                    goalsProgress[selectedGoalsDate]
                }
                Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                    val progressValue by animateFloatAsState(
                        targetValue = currentGoalsProgress?.progress.takeIf { !isLoading } ?: 0f
                    )
                    CircularProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 6.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = buildString {
                            append((progressValue * 100).fastRoundToInt())
                            append('%')
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GoalsByTypeProgressView(
                        isLoading = isLoading,
                        typeLabel = TasksThemeRes.strings.goalsViewProgressHomeworkLabel,
                        progress = currentGoalsProgress?.homeworkGoals ?: emptyList(),
                    )
                    GoalsByTypeProgressView(
                        isLoading = isLoading,
                        typeLabel = TasksThemeRes.strings.goalsViewProgressTodoLabel,
                        progress = currentGoalsProgress?.todoGoals ?: emptyList(),
                    )
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = TasksThemeRes.strings.goalsViewClosestTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            Crossfade(
                targetState = isLoading,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
            ) { loading ->
                if (!loading) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DateGoalRateView(
                            goalCount = goalsProgress[currentDate.shiftDay(1)]?.goalsCount,
                            date = currentDate.shiftDay(1),
                        )
                        DateGoalRateView(
                            goalCount = goalsProgress[currentDate.shiftDay(2)]?.goalsCount,
                            date = currentDate.shiftDay(2),
                        )
                        DateGoalRateView(
                            goalCount = goalsProgress[currentDate.shiftDay(3)]?.goalsCount,
                            date = currentDate.shiftDay(3),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DateGoalRateViewPlaceholder()
                        DateGoalRateViewPlaceholder()
                        DateGoalRateViewPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsByTypeProgressView(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    typeLabel: String,
    progress: List<Boolean>,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = typeLabel,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 2,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Crossfade(
                modifier = Modifier.animateContentSize(),
                targetState = isLoading,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
            ) { loading ->
                if (!loading) {
                    Text(
                        text = buildString {
                            append(progress.count { it })
                            append('/')
                            append(progress.size)
                        },
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                } else {
                    PlaceholderBox(
                        modifier = Modifier.size(26.dp, 19.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DateGoalRateView(
    modifier: Modifier = Modifier,
    goalCount: Int?,
    date: Instant,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val rateBackgroundColor = when (goalCount) {
            null -> StudyAssistantRes.colors.accents.greenContainer
            in 0..2 -> StudyAssistantRes.colors.accents.greenContainer
            in 3..5 -> StudyAssistantRes.colors.accents.orangeContainer
            else -> StudyAssistantRes.colors.accents.redContainer
        }
        Surface(
            modifier = Modifier.size(20.dp),
            shape = RoundedCornerShape(6.dp),
            color = rateBackgroundColor
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = (goalCount ?: 0).toString(),
                    color = StudyAssistantRes.colors.accents.contentColorFor(rateBackgroundColor),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Surface(
            modifier = Modifier.height(20.dp).weight(1f),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date.formatByTimeZone(
                        format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat(
                            StudyAssistantRes.strings
                        )
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun DateGoalRateViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaceholderBox(
            modifier = Modifier.size(20.dp),
            shape = RoundedCornerShape(6.dp),
        )
        PlaceholderBox(
            modifier = Modifier.height(20.dp).weight(1f),
            shape = MaterialTheme.shapes.small,
        )
    }
}

@Composable
private fun GoalListDivider(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    goalCount: Int?,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = TasksThemeRes.strings.goalsViewListTitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
        Crossfade(
            modifier = Modifier.animateContentSize(),
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
        ) { loading ->
            if (!loading) {
                Text(
                    text = (goalCount ?: 0).toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                )
            } else {
                PlaceholderBox(
                    modifier = Modifier.size(20.dp, 17.dp),
                    shape = RoundedCornerShape(6.dp),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DailyGoalsViewContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    dailyGoals: List<GoalDetailsUi>,
    onEditHomeworkClick: (HomeworkUi) -> Unit,
    onEditTodoClick: (TodoUi) -> Unit,
    onCompleteGoal: (GoalDetailsUi) -> Unit,
    onDeleteGoal: (GoalDetailsUi) -> Unit,
    onStartTime: (GoalDetailsUi) -> Unit,
    onPauseTime: (GoalDetailsUi) -> Unit,
    onResetTime: (GoalDetailsUi) -> Unit,
    onChangeTimeType: (GoalTime.Type, GoalDetailsUi) -> Unit,
    onChangeDesiredTime: (Millis?, GoalDetailsUi) -> Unit,
    onChangeGoalNumbers: (List<GoalDetailsUi>) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val reorderState = rememberReorderState<GoalDetailsUi>()
    var goalItems by remember { mutableStateOf(dailyGoals) }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(dailyGoals) {
        if (reorderState.draggedItem == null) goalItems = dailyGoals
    }

    Crossfade(
        targetState = isLoading,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
    ) { loading ->
        if (!loading) {
            ReorderContainer(
                state = reorderState,
                modifier = modifier,
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (goalItems.isNotEmpty()) {
                        items(goalItems, key = { it.uid }, contentType = { it.contentType }) { goal ->
                            var goalBottomSheetViewerStatus by remember { mutableStateOf(false) }
                            var deleteWarningDialogState by remember { mutableStateOf(false) }

                            ReorderableItem(
                                state = reorderState,
                                key = goal,
                                data = goal,
                                requireFirstDownUnconsumed = true,
                                onDrop = { onChangeGoalNumbers(goalItems) },
                                onDragEnter = { state ->
                                    goalItems = goalItems.toMutableList().apply {
                                        val index = indexOf(goal)
                                        if (index == -1) return@ReorderableItem
                                        remove(state.data)
                                        add(index, state.data)

                                        scope.launch {
                                            handleLazyListScroll(
                                                lazyListState = lazyListState,
                                                dropIndex = index,
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.animateItem(),
                            ) {
                                GoalViewItem(
                                    goal = goal,
                                    onClick = { goalBottomSheetViewerStatus = true },
                                    onDelete = { deleteWarningDialogState = true },
                                    onComplete = { onCompleteGoal(goal) },
                                )
                            }

                            if (deleteWarningDialogState) {
                                DeleteGoalWarningDialog(
                                    onDismiss = { deleteWarningDialogState = false },
                                    onDelete = {
                                        onDeleteGoal(goal)
                                        deleteWarningDialogState = false
                                    },
                                )
                            }

                            if (goalBottomSheetViewerStatus) {
                                GoalBottomSheet(
                                    goal = goal,
                                    onEditTodoClick = onEditTodoClick,
                                    onEditHomeworkClick = onEditHomeworkClick,
                                    onStartTime = { onStartTime(goal) },
                                    onPauseTime = { onPauseTime(goal) },
                                    onResetTime = { onResetTime(goal) },
                                    onChangeTimeType = { onChangeTimeType(it, goal) },
                                    onChangeDesiredTime = { onChangeDesiredTime(it, goal) },
                                    onComplete = { onCompleteGoal(goal) },
                                    onDelete = {
                                        goalBottomSheetViewerStatus = false
                                        onDeleteGoal(goal)
                                   },
                                    onDismissRequest = { goalBottomSheetViewerStatus = false }
                                )
                            }
                        }
                    } else {
                        item {
                            GoalViewEmptyItem(
                                modifier = Modifier.fillParentMaxSize(),
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
            ) {
                items(Constants.Placeholder.GOALS) {
                    PlaceholderBox(
                        modifier = Modifier.height(44.dp).fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GoalDatePicker(
    modifier: Modifier = Modifier,
    initSelectedDate: Instant,
    onDismiss: () -> Unit,
    onSelectedDate: (Instant) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initSelectedDate.toEpochMilliseconds()
    )
    val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = confirmEnabled,
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis ?: return@TextButton
                    onSelectedDate.invoke(selectedDate.mapEpochTimeToInstant().startThisDay())
                },
                content = { Text(text = StudyAssistantRes.strings.selectConfirmTitle) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = StudyAssistantRes.strings.cancelTitle)
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp),
                    text = StudyAssistantRes.strings.datePickerDialogHeader,
                )
            },
        )
    }
}