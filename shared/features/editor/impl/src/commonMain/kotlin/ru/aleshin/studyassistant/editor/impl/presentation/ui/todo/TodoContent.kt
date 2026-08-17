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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.todo

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.presentation.models.tasks.TodoNotificationsUi
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.ui.EditorLayoutMode
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.EditorSplitPanes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.TaskPriorityInfoView
import ru.aleshin.studyassistant.editor.impl.presentation.ui.fetchEditorLayoutMode
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.store.TodoComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views.TodoBottomActions
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views.TodoDeadlineInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views.TodoInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views.TodoNotificationSelector
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views.TodoTopBar

/**
 * @author Stanislav Aleshin on 26.07.2024.
 */
@Composable
internal fun TodoContent(
    todoComponent: TodoComponent,
    modifier: Modifier = Modifier,
) {
    val store = todoComponent.store
    val state by store.stateAsState()
    val snackbarState = remember { SnackbarHostState() }
    val layoutMode = currentWindowAdaptiveInfoV2().fetchEditorLayoutMode()
    val isExpanded = layoutMode == EditorLayoutMode.EXPANDED

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            TodoLayout(
                state = state,
                modifier = Modifier.padding(paddingValues),
                layoutMode = layoutMode,
                onTodoNameChange = { store.dispatchEvent(TodoEvent.UpdateTodoName(it)) },
                onTodoDescriptionChange = { store.dispatchEvent(TodoEvent.UpdateTodoDescription(it)) },
                onChangeDeadline = { store.dispatchEvent(TodoEvent.UpdateDeadline(it)) },
                onChangePriority = { store.dispatchEvent(TodoEvent.UpdatePriority(it)) },
                onChangeNotifications = { store.dispatchEvent(TodoEvent.UpdateNotifications(it)) },
            )
        },
        topBar = {
            TodoTopBar(
                isExpanded = isExpanded,
                onBackClick = { store.dispatchEvent(TodoEvent.NavigateToBack) },
            )
        },
        bottomBar = {
            TodoBottomActions(
                isLoadingSave = state.isLoadingSave,
                saveEnabled = state.editableTodo?.isValid() == true,
                showDeleteAction = state.editableTodo?.uid?.isNotBlank() == true,
                contentMaxWidth = if (isExpanded) AdaptiveLayoutDefaults.MediumContentMaxWidth else null,
                horizontalPadding = if (isExpanded) {
                    AdaptiveLayoutDefaults.ExpandedHorizontalPadding
                } else {
                    AdaptiveLayoutDefaults.CompactHorizontalPadding
                },
                onCancelClick = { store.dispatchEvent(TodoEvent.NavigateToBack) },
                onSaveClick = { store.dispatchEvent(TodoEvent.SaveTodo) },
                onDeleteClick = { store.dispatchEvent(TodoEvent.DeleteTodo) },
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
            is TodoEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun TodoLayout(
    state: TodoState,
    modifier: Modifier = Modifier,
    layoutMode: EditorLayoutMode,
    onTodoNameChange: (String) -> Unit,
    onTodoDescriptionChange: (String) -> Unit,
    onChangeDeadline: (Instant?) -> Unit,
    onChangePriority: (TaskPriority) -> Unit,
    onChangeNotifications: (TodoNotificationsUi) -> Unit,
) {
    when (layoutMode) {
        EditorLayoutMode.COMPACT -> BaseTodoContent(
            state = state,
            modifier = modifier,
            onTodoNameChange = onTodoNameChange,
            onTodoDescriptionChange = onTodoDescriptionChange,
            onChangeDeadline = onChangeDeadline,
            onChangePriority = onChangePriority,
            onChangeNotifications = onChangeNotifications,
        )
        EditorLayoutMode.EXPANDED -> TodoExpandedLayout(
            state = state,
            modifier = modifier,
            onTodoNameChange = onTodoNameChange,
            onTodoDescriptionChange = onTodoDescriptionChange,
            onChangeDeadline = onChangeDeadline,
            onChangePriority = onChangePriority,
            onChangeNotifications = onChangeNotifications,
        )
    }
}

@Composable
private fun TodoExpandedLayout(
    state: TodoState,
    modifier: Modifier = Modifier,
    onTodoNameChange: (String) -> Unit,
    onTodoDescriptionChange: (String) -> Unit,
    onChangeDeadline: (Instant?) -> Unit,
    onChangePriority: (TaskPriority) -> Unit,
    onChangeNotifications: (TodoNotificationsUi) -> Unit,
) {
    EditorSplitPanes(
        modifier = modifier,
        startPane = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TodoInfoFields(
                    isLoading = state.isLoading,
                    todoName = state.editableTodo?.name ?: "",
                    todoDescription = state.editableTodo?.description,
                    onTodoNameChange = onTodoNameChange,
                    onTodoDescriptionChange = onTodoDescriptionChange,
                )
            }
        },
        endPane = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TodoDeadlineInfoFields(
                    isLoading = state.isLoading,
                    deadline = state.editableTodo?.deadline,
                    onChangeDeadline = onChangeDeadline,
                )
                TaskPriorityInfoView(
                    isLoading = state.isLoading,
                    priority = state.editableTodo?.priority,
                    onChangePriority = onChangePriority,
                )
                TodoNotificationSelector(
                    notifications = state.editableTodo?.notifications,
                    onChangeNotifications = onChangeNotifications,
                )
            }
        },
    )
}

@Composable
private fun BaseTodoContent(
    state: TodoState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onTodoNameChange: (String) -> Unit,
    onTodoDescriptionChange: (String) -> Unit,
    onChangeDeadline: (Instant?) -> Unit,
    onChangePriority: (TaskPriority) -> Unit,
    onChangeNotifications: (TodoNotificationsUi) -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(scrollState).padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        TodoInfoFields(
            isLoading = state.isLoading,
            todoName = state.editableTodo?.name ?: "",
            todoDescription = state.editableTodo?.description,
            onTodoNameChange = onTodoNameChange,
            onTodoDescriptionChange = onTodoDescriptionChange,
        )
        TodoDeadlineInfoFields(
            isLoading = state.isLoading,
            deadline = state.editableTodo?.deadline,
            onChangeDeadline = onChangeDeadline,
        )
        TaskPriorityInfoView(
            isLoading = state.isLoading,
            priority = state.editableTodo?.priority,
            onChangePriority = onChangePriority,
        )
        TodoNotificationSelector(
            notifications = state.editableTodo?.notifications,
            onChangeNotifications = onChangeNotifications,
        )
    }
}
