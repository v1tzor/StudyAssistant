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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview

import androidx.compose.foundation.layout.Column
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
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewDeps
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewViewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.screenmodel.rememberOverviewScreenModel
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewBottomBar
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewTopBar
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewTopSheet

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
internal data class OverviewScreen(val firstDay: Millis?) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberOverviewScreenModel(),
        initialState = OverviewViewState(),
        dependencies = OverviewDeps(firstDay = firstDay),
    ) { state ->
        val strings = ScheduleThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                OverviewContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onEditHomework = { dispatchEvent(OverviewEvent.EditHomeworkInEditor(it)) },
                    onAddHomework = { homework, date ->
                        dispatchEvent(OverviewEvent.AddHomeworkInEditor(homework, date))
                    },
                    onAgainHomework = { dispatchEvent(OverviewEvent.CancelCompleteHomework(it)) },
                    onCompleteHomework = { dispatchEvent(OverviewEvent.CompleteHomework(it)) },
                )
            },
            topBar = {
                Column {
                    OverviewTopBar(
                        enabledEdit = state.selectedDate != null,
                        onEditClick = { dispatchEvent(OverviewEvent.NavigateToDailyScheduleEditor) },
                        onCurrentDay = { dispatchEvent(OverviewEvent.SelectedCurrentDay) },
                        onDetailsClick = { dispatchEvent(OverviewEvent.NavigateToDetails) },
                    )
                    OverviewTopSheet(
                        isLoading = state.isAnalyticsLoading,
                        selectedDate = state.selectedDate,
                        weekAnalysis = state.weekAnalysis,
                        activeClass = state.activeClass,
                    )
                }
            },
            bottomBar = {
                OverviewBottomBar(
                    currentDate = state.currentDate,
                    selectedDate = state.selectedDate,
                    onSelectedDate = { dispatchEvent(OverviewEvent.SelectedDate(it)) },
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
                is OverviewEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is OverviewEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is OverviewEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}