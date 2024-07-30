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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.touchlab.kermit.Logger
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewViewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.screenmodel.rememberOverviewScreenModel
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views.OverviewTopBar

/**
 * @author Stanislav Aleshin on 29.06.2024
 */
internal class OverviewScreen : Screen {

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() = ScreenContent(
        screenModel = rememberOverviewScreenModel(),
        initialState = OverviewViewState(),
    ) { state ->
        val strings = TasksThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                OverviewContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onOpenHomeworkTasks = { dispatchEvent(OverviewEvent.NavigateToHomeworks(it)) },
                    onOpenSharedHomeworks = { dispatchEvent(OverviewEvent.NavigateToShare) },
                    onShowAllHomeworkTasks = { dispatchEvent(OverviewEvent.NavigateToHomeworks(null)) },
                    onDoHomework = { dispatchEvent(OverviewEvent.DoHomework(it)) },
                    onSkipHomework = { dispatchEvent(OverviewEvent.SkipHomework(it)) },
                    onRepeatHomework = { dispatchEvent(OverviewEvent.RepeatHomework(it)) },
                    onShareHomeworks = { dispatchEvent(OverviewEvent.ShareHomeworks(it)) },
                    onShowAllTodoTasks = { dispatchEvent(OverviewEvent.NavigateToTodos) },
                    onOpenTodoTask = { dispatchEvent(OverviewEvent.NavigateToTodoEditor(it)) },
                    onChangeTodoDone = { task, done -> dispatchEvent(OverviewEvent.UpdateTodoDone(task, done)) },
                )
            },
            topBar = {
                OverviewTopBar()
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { dispatchEvent(OverviewEvent.AddHomeworkInEditor) },
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
        )

        handleEffect { effect ->
            when (effect) {
                is OverviewEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is OverviewEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is OverviewEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.apply {
                            Logger.e { "${effect.failures}" }
                        }.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}