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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SharedHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkErrorsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkScopeUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoErrorsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewItemPlaceholder
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewNoneItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewViewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.DailyHomeworksView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.DailyHomeworksViewPlaceholder
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.HomeworkErrorsView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.HomeworkTasksChart
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.ShareHomeworksBottomSheet
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.SharedHomeworksStatusView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.TaskErrorsBottomSheet
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.TasksProgressView

/**
 * @author Stanislav Aleshin on 29.06.2024
 */
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
    Column(
        modifier = modifier.fillMaxSize().padding(top = 8.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TasksProgressControlSection(
            isLoadingHomeworks = isLoadingHomeworks,
            isLoadingErrors = isLoadingErrors,
            isLoadingShare = isLoadingShare,
            currentDate = currentDate,
            homeworks = homeworks,
            sharedHomeworks = sharedHomeworks,
            todos = todos,
            homeworkErrors = homeworkErrors,
            todoErrors = todoErrors,
            onOpenSharedHomeworks = onOpenSharedHomeworks,
            onEditHomework = onOpenHomeworkTasks,
            onDoHomework = onDoHomework,
            onSkipHomework = onSkipHomework,
            onChangeTodoDone = { todo, done -> onChangeTodoDone(todo, done) },
        )
        HomeworkAnalyticsSection(
            isLoading = isLoadingHomeworks,
            currentDate = currentDate,
            homeworksScope = homeworksScope,
        )
        HomeworksSection(
            isLoading = isLoadingHomeworks,
            currentDate = currentDate,
            homeworks = homeworks,
            allFriends = friends,
            onOpenHomeworkTasks = onOpenHomeworkTasks,
            onShowAllHomeworkTasks = onShowAllHomeworkTasks,
            onDoHomework = onDoHomework,
            onSkipHomework = onSkipHomework,
            onRepeatHomework = onRepeatHomework,
            onShareHomeworks = onShareHomeworks,
        )
        TodosSection(
            isLoading = isLoadingTasks,
            todos = todos,
            onShowAllTodoTasks = onShowAllTodoTasks,
            onOpenTodoTask = onOpenTodoTask,
            onChangeTodoDone = onChangeTodoDone,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TasksProgressControlSection(
    modifier: Modifier = Modifier,
    isLoadingHomeworks: Boolean,
    isLoadingErrors: Boolean,
    isLoadingShare: Boolean,
    currentDate: Instant,
    homeworks: Map<Instant, List<HomeworkDetailsUi>>,
    sharedHomeworks: SharedHomeworksUi?,
    todos: List<TodoDetailsUi>,
    homeworkErrors: HomeworkErrorsUi?,
    todoErrors: TodoErrorsUi?,
    onOpenSharedHomeworks: () -> Unit,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
) {
    var taskErrorsSheetState by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TasksProgressView(
            modifier = Modifier.weight(1f),
            isLoading = isLoadingHomeworks,
            currentDate = currentDate,
            homeworks = homeworks,
            todos = todos,
        )
        Column(
            modifier = Modifier.fillMaxHeight().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HomeworkErrorsView(
                modifier = Modifier.fillMaxWidth(),
                isLoading = isLoadingErrors,
                homeworkErrors = homeworkErrors,
                todoErrors = todoErrors,
                onShowErrors = { taskErrorsSheetState = true },
            )
            SharedHomeworksStatusView(
                modifier = Modifier.fillMaxWidth(),
                isLoading = isLoadingShare,
                sharedHomeworks = sharedHomeworks,
                onOpenSharedHomeworks = onOpenSharedHomeworks,
            )
        }
    }

    if (taskErrorsSheetState) {
        TaskErrorsBottomSheet(
            sheetState = sheetState,
            homeworkErrors = homeworkErrors,
            todoErrors = todoErrors,
            onDismissRequest = { taskErrorsSheetState = false },
            onEditHomework = onEditHomework,
            onDoHomework = onDoHomework,
            onSkipHomework = onSkipHomework,
            onChangeTodoDone = { todo, done -> onChangeTodoDone(todo, done) },
        )
    }
}

@Composable
private fun HomeworkAnalyticsSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    homeworksScope: HomeworkScopeUi?,
) {
    Surface(
        modifier = modifier.padding(horizontal = 16.dp).animateContentSize(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TasksThemeRes.strings.homeworkAnalyticsHeader,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            HomeworkTasksChart(
                isLoading = isLoading,
                currentDate = currentDate,
                homeworkScope = homeworksScope,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeworksSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    homeworks: Map<Instant, List<HomeworkDetailsUi>>,
    allFriends: List<AppUserUi>,
    onShowAllHomeworkTasks: () -> Unit,
    onOpenHomeworkTasks: (HomeworkDetailsUi) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
    onShareHomeworks: (SentMediatedHomeworksDetailsUi) -> Unit,
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
                text = TasksThemeRes.strings.homeworksSectionHeader,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                modifier = Modifier.size(32.dp),
                onClick = onShowAllHomeworkTasks,
            ) {
                Icon(
                    painter = painterResource(TasksThemeRes.icons.viewAllTasks),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        var isShowTargetDay by rememberSaveable { mutableStateOf(true) }
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            if (!loading) {
                val listState = rememberLazyListState()
                val homeworksMapList = homeworks.toList()

                LazyRow(
                    modifier = Modifier.height(350.dp).fillMaxWidth(),
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = true,
                ) {
                    items(homeworksMapList, key = { it.first.toString() }) { homeworksEntry ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var isShowSharedHomeworksSheet by remember { mutableStateOf(false) }
                            val isCurrent = currentDate.equalsDay(homeworksEntry.first)

                            if (isCurrent) {
                                VerticalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                )
                            }
                            DailyHomeworksView(
                                date = homeworksEntry.first,
                                isCurrent = isCurrent,
                                isPassed = homeworksEntry.first < currentDate,
                                homeworks = homeworksEntry.second,
                                onDoHomework = onDoHomework,
                                onOpenHomeworkTask = onOpenHomeworkTasks,
                                onSkipHomework = onSkipHomework,
                                onRepeatHomework = onRepeatHomework,
                                onShareHomeworks = { isShowSharedHomeworksSheet = true },
                            )
                            if (isCurrent) {
                                VerticalDivider(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                )
                            }

                            if (isShowSharedHomeworksSheet) {
                                ShareHomeworksBottomSheet(
                                    currentTime = Clock.System.now(),
                                    targetDate = homeworksEntry.first,
                                    homeworks = homeworksEntry.second,
                                    allFriends = allFriends,
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
                LaunchedEffect(true) {
                    if (isShowTargetDay) {
                        val currentDateIndex = homeworksMapList.indexOfFirst { currentDate.equalsDay(it.first) }
                        if (currentDateIndex != -1) {
                            listState.animateScrollToItem(currentDateIndex)
                            isShowTargetDay = false
                        }
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.height(350.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false,
                ) {
                    items(Placeholder.HOMEWORKS) {
                        DailyHomeworksViewPlaceholder()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodosSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
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
            targetState = isLoading,
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