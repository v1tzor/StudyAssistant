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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.todo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.screenmodel.rememberTodoScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views.TodoBottomActions
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views.TodoTopBar

/**
 * @author Stanislav Aleshin on 26.07.2024
 */
internal data class TodoScreen(private val todoId: UID?) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberTodoScreenModel(),
        initialState = TodoViewState(),
        dependencies = TodoDeps(todoId = todoId),
    ) { state ->
        val strings = EditorThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                TodoContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onTodoNameChange = { dispatchEvent(TodoEvent.UpdateTodoName(it)) },
                    onTodoDescriptionChange = { dispatchEvent(TodoEvent.UpdateTodoDescription(it)) },
                    onChangeDeadline = { dispatchEvent(TodoEvent.UpdateDeadline(it)) },
                    onChangePriority = { dispatchEvent(TodoEvent.UpdatePriority(it)) },
                    onChangeNotifications = { dispatchEvent(TodoEvent.UpdateNotifications(it)) },
                )
            },
            topBar = {
                TodoTopBar(
                    onBackClick = { dispatchEvent(TodoEvent.NavigateToBack) },
                )
            },
            bottomBar = {
                TodoBottomActions(
                    isLoadingSave = state.isLoadingSave,
                    saveEnabled = state.editableTodo?.isValid() == true,
                    showDeleteAction = state.editableTodo?.uid?.isNotBlank() == true,
                    onCancelClick = { dispatchEvent(TodoEvent.NavigateToBack) },
                    onSaveClick = { dispatchEvent(TodoEvent.SaveTodo) },
                    onDeleteClick = { dispatchEvent(TodoEvent.DeleteTodo) },
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
        )

        handleEffect { effect ->
            when (effect) {
                is TodoEffect.NavigateToBack -> navigator.nestedPop()
                is TodoEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}