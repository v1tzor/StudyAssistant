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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
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
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.convertToBase
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewViewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.DailyGoalsView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.HomeworksExecutionAnalysisView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.HomeworksOverview
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.OverviewTodosCompletedSection
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.OverviewTodosErrorSection
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.OverviewTodosInProgressSection
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.ScopeOfHomeworksView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.ShareHomeworksView

/**
 * @author Stanislav Aleshin on 29.06.2024
 */
internal enum class OverviewTasksTab {
    HOMEWORKS, TODO
}

@Composable
internal fun OverviewContent(
    state: OverviewViewState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    overviewTasksTab: OverviewTasksTab,
    onChangeTab: (OverviewTasksTab) -> Unit,
    onShowHomeworkTasks: (HomeworkUi?) -> Unit,
    onOpenHomeworkEditor: (HomeworkUi) -> Unit,
    onOpenSharedHomeworks: () -> Unit,
    onDoHomework: (HomeworkUi) -> Unit,
    onSkipHomework: (HomeworkUi) -> Unit,
    onRepeatHomework: (HomeworkUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
    onSelectGoalsDate: (Instant) -> Unit,
    onChangeGoalNumbers: (List<GoalDetailsUi>) -> Unit,
    onCompleteGoal: (GoalDetailsUi) -> Unit,
    onDeleteGoal: (GoalShortUi) -> Unit,
    onStartGoalTime: (GoalDetailsUi) -> Unit,
    onPauseGoalTime: (GoalDetailsUi) -> Unit,
    onResetGoalTime: (GoalDetailsUi) -> Unit,
    onChangeGoalTimeType: (GoalTime.Type, GoalDetailsUi) -> Unit,
    onChangeGoalDesiredTime: (Millis?, GoalDetailsUi) -> Unit,
    onScheduleGoal: (GoalCreateModelUi) -> Unit,
    onShowAllTodoTasks: () -> Unit,
    onOpenTodoTask: (TodoUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.fillMaxSize().padding(top = 8.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DailyGoalsSection(
            isLoadingGoals = isLoadingGoals,
            currentDate = currentDate,
            selectedGoalsDate = selectedGoalsDate,
            dailyGoals = dailyGoals,
            goalsProgress = goalsProgress,
            onSelectDate = onSelectGoalsDate,
            onChangeGoalNumbers = onChangeGoalNumbers,
            onOpenHomeworkEditor = onOpenHomeworkEditor,
            onOpenTodoEditor = onOpenTodoTask,
            onCompleteGoal = onCompleteGoal,
            onDeleteGoal = { onDeleteGoal(it.convertToShort()) },
            onStartGoalTime = onStartGoalTime,
            onPauseGoalTime = onPauseGoalTime,
            onResetGoalTime = onResetGoalTime,
            onChangeGoalTimeType = onChangeGoalTimeType,
            onChangeGoalDesiredTime = onChangeGoalDesiredTime,
        )
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OverviewTasksTabSelector(
                modifier = Modifier.padding(horizontal = 16.dp),
                currentTab = overviewTasksTab,
                onChangeTab = onChangeTab,
            )
            Crossfade(
                targetState = overviewTasksTab,
                animationSpec = tween(),
            ) { tab ->
                when (tab) {
                    OverviewTasksTab.HOMEWORKS -> HomeworksSection(
                        isLoadingHomeworks = isLoadingHomeworks,
                        isLoadingProgress = isLoadingHomeworksProgress,
                        isLoadingShare = isLoadingShare,
                        currentDate = currentDate,
                        dailyHomeworks = homeworks,
                        sharedHomeworks = sharedHomeworks,
                        homeworksScope = homeworksScope,
                        completeProgress = homeworksProgress,
                        allFriends = friends,
                        onOpenHomeworkTasks = onShowHomeworkTasks,
                        onOpenSharedHomeworks = onOpenSharedHomeworks,
                        onShowAllHomeworkTasks = { onShowHomeworkTasks(null) },
                        onDoHomework = onDoHomework,
                        onSkipHomework = onSkipHomework,
                        onRepeatHomework = onRepeatHomework,
                        onShareHomeworks = onShareHomeworks,
                    )
                    OverviewTasksTab.TODO -> TodosSection(
                        isLoadingTasks = isLoadingTasks,
                        groupedTodos = groupedTodos,
                        currentDate = currentDate,
                        onShowAllTodoTasks = onShowAllTodoTasks,
                        onOpenTodoTask = { onOpenTodoTask(it.convertToBase()) },
                        onChangeTodoDone = onChangeTodoDone,
                        onScheduleGoal = onScheduleGoal,
                        onDeleteGoal = onDeleteGoal,
                    )
                }
            }
            Spacer(modifier = Modifier.padding(48.dp))
        }
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
    onOpenHomeworkEditor: (HomeworkUi) -> Unit,
    onOpenTodoEditor: (TodoUi) -> Unit,
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
            onOpenHomeworkEditor = onOpenHomeworkEditor,
            onOpenTodoEditor = onOpenTodoEditor,
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
    onOpenSharedHomeworks: () -> Unit,
    onShowAllHomeworkTasks: () -> Unit,
    onOpenHomeworkTasks: (HomeworkUi) -> Unit,
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
            onEditHomework = onOpenHomeworkTasks,
            onDoHomework = onDoHomework,
            onSkipHomework = onSkipHomework,
        )
        HomeworksOverview(
            isLoadingHomeworks = isLoadingHomeworks,
            currentDate = currentDate,
            homeworks = dailyHomeworks,
            allFriends = allFriends,
            onOpenHomeworkTasks = { onOpenHomeworkTasks(it.convertToBase()) },
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
            onOpenSharedHomeworks = onOpenSharedHomeworks,
        )
    }
}

@Composable
private fun TodosSection(
    modifier: Modifier = Modifier,
    isLoadingTasks: Boolean,
    currentDate: Instant,
    groupedTodos: DetailsGroupedTodosUi?,
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
            currentDate = currentDate,
            todos = groupedTodos?.runningTodos ?: emptyList(),
            onOpenTodoTask = onOpenTodoTask,
            onChangeTodoDone = onChangeTodoDone,
            onScheduleGoal = onScheduleGoal,
            onDeleteGoal = onDeleteGoal,
        )
        OverviewTodosErrorSection(
            isLoading = isLoadingTasks,
            currentDate = currentDate,
            todos = groupedTodos?.errorTodos ?: emptyList(),
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