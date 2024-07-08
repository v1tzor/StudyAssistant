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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksDeps
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksViewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.screenmodel.rememberHomeworksScreenModel
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.HomeworksTopBar
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views.HomeworksTopSheet

/**
 * @author Stanislav Aleshin on 03.07.2024
 */
internal data class HomeworksScreen(val targetDate: Long?) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberHomeworksScreenModel(),
        initialState = HomeworksViewState(),
        dependencies = HomeworksDeps(
            targetDate = targetDate,
        ),
    ) { state ->
        val strings = TasksThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                HomeworksContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    targetDate = targetDate?.mapEpochTimeToInstant() ?: state.currentDate,
                    onEditHomework = { dispatchEvent(HomeworksEvent.NavigateToHomeworkEditor(it)) },
                    onDoHomework = { dispatchEvent(HomeworksEvent.DoHomework(it)) },
                    onRepeatHomework = { dispatchEvent(HomeworksEvent.RepeatHomework(it)) },
                    onSkipHomework = { dispatchEvent(HomeworksEvent.SkipHomework(it)) },
                )
            },
            topBar = {
                Column {
                    HomeworksTopBar(
                        onOverviewClick = { dispatchEvent(HomeworksEvent.NavigateToOverview) },
                    )
                    HomeworksTopSheet(
                        isLoading = state.isLoading,
                        selectedTimeRange = state.selectedTimeRange,
                        progressList = state.homeworks.map { entry ->
                            entry.value.map { it.completeDate != null }
                        }.extractAllItem(),
                        onNextTimeRange = { dispatchEvent(HomeworksEvent.NextTimeRange) },
                        onPreviousTimeRange = { dispatchEvent(HomeworksEvent.PreviousTimeRange) },
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { dispatchEvent(HomeworksEvent.AddHomeworkInEditor) },
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
                is HomeworksEffect.NavigateToBack -> navigator.pop()
                is HomeworksEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is HomeworksEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is HomeworksEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}