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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.DailyGoalsProgressUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.convertToShort
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SharedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DailyHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DetailsGroupedTodosUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkScopeUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworksCompleteProgressUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.OverviewTasksTab
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.convertToBase
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.store.OverviewComponent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.DailyGoalsView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.HomeworksExecutionAnalysisView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.HomeworksOverview
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.OverviewTodosCompletedSection
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.OverviewTodosErrorSection
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.OverviewTodosInProgressSection
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.OverviewTopBar
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.ScopeOfHomeworksView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.ShareHomeworksView

/**
 * @author Stanislav Aleshin on 29.06.2024
 */
@Composable
internal fun OverviewContent(
    overviewComponent: OverviewComponent,
    modifier: Modifier = Modifier
) {
    val store = overviewComponent.store
    val state by store.stateAsState()
    val strings = TasksThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val overviewTasksTab = rememberSaveable { mutableStateOf(OverviewTasksTab.HOMEWORKS) }
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseOverviewContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                overviewTasksTab = overviewTasksTab.value,
                onChangeTab = { overviewTasksTab.value = it },
                onEditHomeworkClick = {
                    store.dispatchEvent(OverviewEvent.ClickEditHomework(it))
                },
                onShowAllSharedHomeworksClick = {
                    store.dispatchEvent(OverviewEvent.ClickShowAllSharedHomeworks)
                },
                onHomeworkClick = {
                    store.dispatchEvent(OverviewEvent.ClickHomework(it))
                },
                onDoHomework = {
                    store.dispatchEvent(OverviewEvent.DoHomework(it))
                },
                onSkipHomework = {
                    store.dispatchEvent(OverviewEvent.SkipHomework(it))
                },
                onRepeatHomework = {
                    store.dispatchEvent(OverviewEvent.RepeatHomework(it))
                },
                onShareHomeworks = {
                    store.dispatchEvent(OverviewEvent.ShareHomeworks(it))
                },
                onSelectGoalsDate = {
                    store.dispatchEvent(OverviewEvent.SelectedGoalsDate(it))
                },
                onChangeGoalNumbers = {
                    store.dispatchEvent(OverviewEvent.SetNewGoalNumbers(it))
                },
                onCompleteGoal = {
                    store.dispatchEvent(OverviewEvent.CompleteGoal(it))
                },
                onDeleteGoal = {
                    store.dispatchEvent(OverviewEvent.DeleteGoal(it))
                },
                onStartGoalTimeClick = {
                    store.dispatchEvent(OverviewEvent.ClickStartGoalTime(it))
                },
                onPauseGoalTimeClick = {
                    store.dispatchEvent(OverviewEvent.ClickPauseGoalTime(it))
                },
                onResetGoalTimeClick = {
                    store.dispatchEvent(OverviewEvent.ClickResetGoalTime(it))
                },
                onChangeGoalTimeType = { type, goal ->
                    store.dispatchEvent(OverviewEvent.ChangeGoalTimeType(goal, type))
                },
                onChangeGoalDesiredTime = { time, goal ->
                    store.dispatchEvent(OverviewEvent.ChangeGoalDesiredTime(goal, time))
                },
                onScheduleGoal = {
                    store.dispatchEvent(OverviewEvent.ScheduleGoal(it))
                },
                onShowAllTodoClick = {
                    store.dispatchEvent(OverviewEvent.ClickShowAllTodo)
                },
                onEditTodoClick = {
                    store.dispatchEvent(OverviewEvent.ClickEditTodo(it))
                },
                onChangeTodoDone = { task, done ->
                    store.dispatchEvent(OverviewEvent.UpdateTodoDone(task, done))
                },
                onPaidFunctionClick = {
                    store.dispatchEvent(OverviewEvent.ClickPaidFunction)
                },
            )
        },
        topBar = {
            OverviewTopBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (overviewTasksTab.value) {
                        OverviewTasksTab.HOMEWORKS -> {
                            store.dispatchEvent(OverviewEvent.AddHomeworkInEditor)
                        }
                        OverviewTasksTab.TODO -> {
                            store.dispatchEvent(OverviewEvent.ClickEditTodo(null))
                        }
                    }
                },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
            is OverviewEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseOverviewContent(
    state: OverviewState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    overviewTasksTab: OverviewTasksTab,
    onChangeTab: (OverviewTasksTab) -> Unit,
    onHomeworkClick: (HomeworkUi?) -> Unit,
    onEditHomeworkClick: (HomeworkUi) -> Unit,
    onShowAllSharedHomeworksClick: () -> Unit,
    onDoHomework: (HomeworkUi) -> Unit,
    onSkipHomework: (HomeworkUi) -> Unit,
    onRepeatHomework: (HomeworkUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
    onSelectGoalsDate: (Instant) -> Unit,
    onChangeGoalNumbers: (List<GoalDetailsUi>) -> Unit,
    onCompleteGoal: (GoalDetailsUi) -> Unit,
    onDeleteGoal: (GoalShortUi) -> Unit,
    onStartGoalTimeClick: (GoalDetailsUi) -> Unit,
    onPauseGoalTimeClick: (GoalDetailsUi) -> Unit,
    onResetGoalTimeClick: (GoalDetailsUi) -> Unit,
    onChangeGoalTimeType: (GoalTime.Type, GoalDetailsUi) -> Unit,
    onChangeGoalDesiredTime: (Millis?, GoalDetailsUi) -> Unit,
    onScheduleGoal: (GoalCreateModelUi) -> Unit,
    onShowAllTodoClick: () -> Unit,
    onEditTodoClick: (TodoUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
    onPaidFunctionClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(top = 8.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DailyGoalsSection(
            isLoadingGoals = false,
            currentDate = state.currentDate,
            selectedGoalsDate = state.selectedGoalsDate,
            dailyGoals = state.dailyGoals,
            goalsProgress = state.goalsProgress,
            onSelectDate = onSelectGoalsDate,
            onChangeGoalNumbers = onChangeGoalNumbers,
            onEditHomeworkClick = onEditHomeworkClick,
            onEditTodoClick = onEditTodoClick,
            onCompleteGoal = onCompleteGoal,
            onDeleteGoal = { onDeleteGoal(it.convertToShort()) },
            onStartGoalTime = onStartGoalTimeClick,
            onPauseGoalTime = onPauseGoalTimeClick,
            onResetGoalTime = onResetGoalTimeClick,
            onChangeGoalTimeType = onChangeGoalTimeType,
            onChangeGoalDesiredTime = onChangeGoalDesiredTime,
        )
        HorizontalDivider()
        OverviewContentDetails(
            currentTab = overviewTasksTab,
            isLoadingHomeworks = state.isLoadingHomeworks,
            isLoadingHomeworksProgress = state.isLoadingHomeworksProgress,
            isLoadingShare = state.isLoadingShare,
            isLoadingTasks = state.isLoadingTasks,
            isPaidUser = state.isPaidUser,
            currentDate = state.currentDate,
            groupedTodos = state.groupedTodos,
            dailyHomeworks = state.homeworks,
            sharedHomeworks = state.sharedHomeworks,
            homeworksScope = state.homeworksScope,
            homeworksProgress = state.homeworksProgress,
            allFriends = state.friends,
            onChangeTab = onChangeTab,
            onHomeworkClick = onHomeworkClick,
            onShowAllSharedHomeworksClick = onShowAllSharedHomeworksClick,
            onShowAllHomeworksClick = { onHomeworkClick(null) },
            onDoHomework = onDoHomework,
            onSkipHomework = onSkipHomework,
            onRepeatHomework = onRepeatHomework,
            onShareHomeworks = onShareHomeworks,
            onOpenBillingScreen = onPaidFunctionClick,
            onShowAllTodoTasks = onShowAllTodoClick,
            onOpenTodoTask = { onEditTodoClick(it.convertToBase()) },
            onChangeTodoDone = onChangeTodoDone,
            onScheduleGoal = onScheduleGoal,
            onDeleteGoal = onDeleteGoal,
        )
    }
}

@Composable
private fun OverviewContentDetails(
    modifier: Modifier = Modifier,
    currentTab: OverviewTasksTab,
    isLoadingHomeworks: Boolean,
    isLoadingHomeworksProgress: Boolean,
    isLoadingShare: Boolean,
    isLoadingTasks: Boolean,
    isPaidUser: Boolean,
    currentDate: Instant,
    groupedTodos: DetailsGroupedTodosUi?,
    dailyHomeworks: Map<Instant, DailyHomeworksUi>,
    sharedHomeworks: SharedHomeworksDetailsUi?,
    homeworksScope: HomeworkScopeUi?,
    homeworksProgress: HomeworksCompleteProgressUi?,
    allFriends: List<AppUserUi>,
    onChangeTab: (OverviewTasksTab) -> Unit,
    onShowAllSharedHomeworksClick: () -> Unit,
    onShowAllHomeworksClick: () -> Unit,
    onHomeworkClick: (HomeworkUi) -> Unit,
    onDoHomework: (HomeworkUi) -> Unit,
    onSkipHomework: (HomeworkUi) -> Unit,
    onRepeatHomework: (HomeworkUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
    onOpenBillingScreen: () -> Unit,
    onShowAllTodoTasks: () -> Unit,
    onOpenTodoTask: (TodoDetailsUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
    onScheduleGoal: (GoalCreateModelUi) -> Unit,
    onDeleteGoal: (GoalShortUi) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OverviewTasksTabSelector(
            modifier = Modifier.padding(horizontal = 16.dp),
            currentTab = currentTab,
            onChangeTab = onChangeTab,
        )
        Crossfade(
            targetState = currentTab,
            animationSpec = tween(),
        ) { tab ->
            when (tab) {
                OverviewTasksTab.HOMEWORKS -> HomeworksSection(
                    isLoadingHomeworks = isLoadingHomeworks,
                    isLoadingProgress = isLoadingHomeworksProgress,
                    isLoadingShare = isLoadingShare,
                    currentDate = currentDate,
                    dailyHomeworks = dailyHomeworks,
                    sharedHomeworks = sharedHomeworks,
                    homeworksScope = homeworksScope,
                    completeProgress = homeworksProgress,
                    allFriends = allFriends,
                    onHomeworkClick = onHomeworkClick,
                    onShowAllSharedHomeworksClick = onShowAllSharedHomeworksClick,
                    onShowAllHomeworkTasks = onShowAllHomeworksClick,
                    onDoHomework = onDoHomework,
                    onSkipHomework = onSkipHomework,
                    onRepeatHomework = onRepeatHomework,
                    onShareHomeworks = onShareHomeworks,
                )
                OverviewTasksTab.TODO -> TodosSection(
                    isLoadingTasks = isLoadingTasks,
                    isPaidUser = isPaidUser,
                    groupedTodos = groupedTodos,
                    currentDate = currentDate,
                    onOpenBillingScreen = onOpenBillingScreen,
                    onShowAllTodoTasks = onShowAllTodoTasks,
                    onOpenTodoTask = onOpenTodoTask,
                    onChangeTodoDone = onChangeTodoDone,
                    onScheduleGoal = onScheduleGoal,
                    onDeleteGoal = onDeleteGoal,
                )
            }
        }
        Spacer(modifier = Modifier.padding(48.dp))
    }
}

@Composable
private fun DailyGoalsSection(
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
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = TasksThemeRes.strings.dailyGoalsSectionHeader,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        DailyGoalsView(
            modifier = Modifier.padding(horizontal = 16.dp),
            isLoadingGoals = isLoadingGoals,
            currentDate = currentDate,
            selectedGoalsDate = selectedGoalsDate,
            dailyGoals = dailyGoals,
            goalsProgress = goalsProgress,
            onSelectDate = onSelectDate,
            onEditHomeworkClick = onEditHomeworkClick,
            onEditTodoClick = onEditTodoClick,
            onChangeGoalNumbers = onChangeGoalNumbers,
            onCompleteGoal = onCompleteGoal,
            onDeleteGoal = onDeleteGoal,
            onStartGoalTime = onStartGoalTime,
            onPauseGoalTime = onPauseGoalTime,
            onResetGoalTime = onResetGoalTime,
            onChangeGoalTimeType = onChangeGoalTimeType,
            onChangeGoalDesiredTime = onChangeGoalDesiredTime,
        )
    }
}

@Composable
private fun OverviewTasksTabSelector(
    modifier: Modifier = Modifier,
    currentTab: OverviewTasksTab,
    onChangeTab: (OverviewTasksTab) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OverviewTasksTab.entries.forEach { tab ->
            OverviewTasksTabSelectorItem(
                onClick = { onChangeTab(tab) },
                isSelected = tab == currentTab,
                text = when (tab) {
                    OverviewTasksTab.HOMEWORKS -> TasksThemeRes.strings.homeworksTasksTab
                    OverviewTasksTab.TODO -> TasksThemeRes.strings.todosTasksTab
                }
            )
        }
    }
}

@Composable
private fun OverviewTasksTabSelectorItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSelected: Boolean,
    text: String,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = when (isSelected) {
            true -> MaterialTheme.colorScheme.primaryContainer
            false -> Color.Transparent
        },
        border = when (isSelected) {
            true -> null
            false -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = text,
                color = when (isSelected) {
                    true -> MaterialTheme.colorScheme.onPrimaryContainer
                    false -> MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun HomeworksSection(
    modifier: Modifier = Modifier,
    isLoadingHomeworks: Boolean,
    isLoadingProgress: Boolean,
    isLoadingShare: Boolean,
    currentDate: Instant,
    dailyHomeworks: Map<Instant, DailyHomeworksUi>,
    sharedHomeworks: SharedHomeworksDetailsUi?,
    homeworksScope: HomeworkScopeUi?,
    completeProgress: HomeworksCompleteProgressUi?,
    allFriends: List<AppUserUi>,
    onShowAllSharedHomeworksClick: () -> Unit,
    onShowAllHomeworkTasks: () -> Unit,
    onHomeworkClick: (HomeworkUi) -> Unit,
    onDoHomework: (HomeworkUi) -> Unit,
    onSkipHomework: (HomeworkUi) -> Unit,
    onRepeatHomework: (HomeworkUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HomeworksExecutionAnalysisView(
            isLoading = isLoadingProgress,
            completeProgress = completeProgress,
            onEditHomework = onHomeworkClick,
            onDoHomework = onDoHomework,
            onSkipHomework = onSkipHomework,
        )
        HomeworksOverview(
            isLoadingHomeworks = isLoadingHomeworks,
            currentDate = currentDate,
            homeworks = dailyHomeworks,
            allFriends = allFriends,
            onOpenHomeworkTasks = { onHomeworkClick(it.convertToBase()) },
            onShowAllHomeworkTasks = onShowAllHomeworkTasks,
            onDoHomework = { onDoHomework(it.convertToBase()) },
            onSkipHomework = { onSkipHomework(it.convertToBase()) },
            onRepeatHomework = { onRepeatHomework(it.convertToBase()) },
            onShareHomeworks = onShareHomeworks,
        )
        ScopeOfHomeworksView(
            isLoading = isLoadingHomeworks,
            currentDate = currentDate,
            homeworksScope = homeworksScope,
        )
        ShareHomeworksView(
            isLoadingShare = isLoadingShare,
            sharedHomeworks = sharedHomeworks,
            onSharedHomeworkClick = onShowAllSharedHomeworksClick,
        )
    }
}

@Composable
private fun TodosSection(
    modifier: Modifier = Modifier,
    isLoadingTasks: Boolean,
    isPaidUser: Boolean,
    currentDate: Instant,
    groupedTodos: DetailsGroupedTodosUi?,
    onOpenBillingScreen: () -> Unit,
    onShowAllTodoTasks: () -> Unit,
    onOpenTodoTask: (TodoDetailsUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
    onScheduleGoal: (GoalCreateModelUi) -> Unit,
    onDeleteGoal: (GoalShortUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OverviewTodosInProgressSection(
            isLoading = isLoadingTasks,
            isPaidUser = isPaidUser,
            currentDate = currentDate,
            todos = groupedTodos?.runningTodos ?: emptyList(),
            onOpenBillingScreen = onOpenBillingScreen,
            onOpenTodoTask = onOpenTodoTask,
            onChangeTodoDone = onChangeTodoDone,
            onScheduleGoal = onScheduleGoal,
            onDeleteGoal = onDeleteGoal,
        )
        OverviewTodosErrorSection(
            isLoading = isLoadingTasks,
            isPaidUser = isPaidUser,
            currentDate = currentDate,
            todos = groupedTodos?.errorTodos ?: emptyList(),
            onOpenBillingScreen = onOpenBillingScreen,
            onOpenTodoTask = onOpenTodoTask,
            onChangeTodoDone = onChangeTodoDone,
            onScheduleGoal = onScheduleGoal,
            onDeleteGoal = onDeleteGoal,
        )
        OverviewTodosCompletedSection(
            isLoading = isLoadingTasks,
            todos = groupedTodos?.completedTodos ?: emptyList(),
            onOpenTodoTask = onOpenTodoTask,
            onChangeTodoDone = onChangeTodoDone,
        )
        Button(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            onClick = onShowAllTodoTasks
        ) {
            Text(text = TasksThemeRes.strings.showAllTodosTitle)
        }
    }
}