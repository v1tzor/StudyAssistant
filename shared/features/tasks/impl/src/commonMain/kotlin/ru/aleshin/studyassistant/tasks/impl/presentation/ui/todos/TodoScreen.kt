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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoViewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.screenmodel.rememberTodoScreenModel
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.views.TodoTopBar

/**
 * @author Stanislav Aleshin on 28.07.2024
 */
internal class TodoScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberTodoScreenModel(),
        initialState = TodoViewState(),
    ) { state ->
        val strings = TasksThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                TodoContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onOpenTodoTask = { dispatchEvent(TodoEvent.NavigateToTodoEditor(it)) },
                    onChangeTodoDone = { todo, done ->
                        dispatchEvent(TodoEvent.UpdateTodoDone(todo, done))
                    },
                )
            },
            topBar = {
                Column {
                    TodoTopBar(
                        onBackClick = { dispatchEvent(TodoEvent.NavigateToBack) },
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { dispatchEvent(TodoEvent.NavigateToTodoEditor(null)) },
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

        handleEffect { effect ->
            when (effect) {
                is TodoEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is TodoEffect.NavigateToBack -> navigator.pop()
                is TodoEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings, coreStrings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}