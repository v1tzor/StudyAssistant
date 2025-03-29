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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SharedHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DailyHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkErrorsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkScopeUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewItemPlaceholder
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewNoneItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewViewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.DailyGoalsView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.HomeworksExecutionAnalysisView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.HomeworksOverview
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
    onShowAllHomeworkTasks: () -> Unit,
    onOpenHomeworkTasks: (HomeworkDetailsUi) -> Unit,
    onOpenSharedHomeworks: () -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
    onShowAllTodoTasks: () -> Unit,
    onOpenTodoTask: (TodoDetailsUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
) = with(state) {
    var overviewTasksTab by rememberSaveable { mutableStateOf(OverviewTasksTab.HOMEWORKS) }
    Column(
        modifier = modifier.fillMaxSize().padding(top = 8.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DailyGoalsSection(
            isLoadingGoals = false,
        )
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OverviewTasksTabSelector(
                modifier = Modifier.padding(horizontal = 16.dp),
                currentTab = overviewTasksTab,
                onChangeTab = { overviewTasksTab = it }
            )
            Crossfade(
                targetState = overviewTasksTab,
                animationSpec = tween(),
            ) { tab ->
                when (tab) {
                    OverviewTasksTab.HOMEWORKS -> HomeworksSection(
                        isLoadingHomeworks = isLoadingHomeworks,
                        isLoadingErrors = isLoadingErrors,
                        isLoadingShare = isLoadingShare,
                        currentDate = currentDate,
                        dailyHomeworks = homeworks,
                        sharedHomeworks = sharedHomeworks,
                        homeworksScope = homeworksScope,
                        homeworkErrors = homeworkErrors,
                        allFriends = friends,
                        onOpenHomeworkTasks = onOpenHomeworkTasks,
                        onOpenSharedHomeworks = onOpenSharedHomeworks,
                        onShowAllHomeworkTasks = onShowAllHomeworkTasks,
                        onDoHomework = onDoHomework,
                        onSkipHomework = onSkipHomework,
                        onRepeatHomework = onRepeatHomework,
                        onShareHomeworks = onShareHomeworks,
                    )
                    OverviewTasksTab.TODO -> TodosSection(
                        isLoadingTasks = isLoadingTasks,
                        todos = todos,
                        onShowAllTodoTasks = onShowAllTodoTasks,
                        onOpenTodoTask = onOpenTodoTask,
                        onChangeTodoDone = onChangeTodoDone,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyGoalsSection(
    modifier: Modifier = Modifier,
    isLoadingGoals: Boolean,
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
    isLoadingErrors: Boolean,
    isLoadingShare: Boolean,
    currentDate: Instant,
    dailyHomeworks: Map<Instant, DailyHomeworksUi>,
    sharedHomeworks: SharedHomeworksUi?,
    homeworksScope: HomeworkScopeUi?,
    homeworkErrors: HomeworkErrorsUi?,
    allFriends: List<AppUserUi>,
    onOpenSharedHomeworks: () -> Unit,
    onShowAllHomeworkTasks: () -> Unit,
    onOpenHomeworkTasks: (HomeworkDetailsUi) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HomeworksOverview(
            isLoadingHomeworks = isLoadingHomeworks,
            currentDate = currentDate,
            homeworks = dailyHomeworks,
            allFriends = allFriends,
            onOpenHomeworkTasks = onOpenHomeworkTasks,
            onShowAllHomeworkTasks = onShowAllHomeworkTasks,
            onDoHomework = onDoHomework,
            onSkipHomework = onSkipHomework,
            onRepeatHomework = onRepeatHomework,
            onShareHomeworks = onShareHomeworks,
        )
        HomeworksExecutionAnalysisView(
            isLoadingHomeworks = isLoadingHomeworks,
            isLoadingErrors = isLoadingErrors,
            currentDate = currentDate,
            dailyHomeworks = dailyHomeworks,
            homeworkErrors = homeworkErrors,
            onEditHomework = onOpenHomeworkTasks,
            onDoHomework = onDoHomework,
            onSkipHomework = onSkipHomework,
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
    todos: List<TodoDetailsUi>,
    onShowAllTodoTasks: () -> Unit,
    onOpenTodoTask: (TodoDetailsUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = TasksThemeRes.strings.todosSectionHeader,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = buildString {
                    append(TasksThemeRes.strings.totalTodosSuffix, " ")
                    append(todos.size)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Crossfade(
            targetState = isLoadingTasks,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            if (!loading) {
                val activeTodos = todos.filter { it.completeDate == null }
                FlowRow(
                    modifier = Modifier.animateContentSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (activeTodos.isNotEmpty()) {
                        activeTodos.forEach { todo ->
                            TodoViewItem(
                                modifier = Modifier.wrapContentSize(),
                                onClick = { onOpenTodoTask(todo) },
                                isDone = todo.isDone,
                                todoText = todo.name,
                                status = todo.status,
                                deadline = todo.deadline,
                                toDeadlineDuration = todo.toDeadlineDuration,
                                priority = todo.priority,
                                onChangeDone = { onChangeTodoDone(todo, it) }
                            )
                        }
                    } else {
                        TodoViewNoneItem()
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    repeat(Placeholder.OVERVIEW_TODOS) {
                        TodoViewItemPlaceholder()
                    }
                }
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onShowAllTodoTasks
        ) {
            Text(text = TasksThemeRes.strings.showAllTodosTitle)
        }
    }
}