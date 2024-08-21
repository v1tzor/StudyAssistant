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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details

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
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.weekTimeRange
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsViewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.screenmodel.rememberDetailsScreenModel
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.DetailsBottomBar
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views.DetailsTopBar

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
internal class DetailsScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberDetailsScreenModel(),
        initialState = DetailsViewState(),
    ) { state ->
        val strings = ScheduleThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                DetailsContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onOpenSchedule = { dispatchEvent(DetailsEvent.OpenOverviewSchedule(it)) },
                    onEditHomework = { dispatchEvent(DetailsEvent.EditHomeworkInEditor(it)) },
                    onAddHomework = { homework, date ->
                        dispatchEvent(DetailsEvent.AddHomeworkInEditor(homework, date))
                    },
                    onAgainHomework = { dispatchEvent(DetailsEvent.CancelCompleteHomework(it)) },
                    onCompleteHomework = { dispatchEvent(DetailsEvent.CompleteHomework(it)) },
                )
            },
            topBar = {
                DetailsTopBar(
                    onEditClick = { dispatchEvent(DetailsEvent.NavigateToEditor) },
                    onCurrentWeek = { dispatchEvent(DetailsEvent.SelectedCurrentWeek) },
                    onOverviewClick = { dispatchEvent(DetailsEvent.NavigateToOverview) },
                )
            },
            bottomBar = {
                DetailsBottomBar(
                    currentWeek = state.currentDate.dateTime().weekTimeRange(),
                    selectedWeek = state.selectedWeek,
                    viewType = state.scheduleView,
                    onNextWeek = { dispatchEvent(DetailsEvent.SelectedNextWeek) },
                    onPreviousWeek = { dispatchEvent(DetailsEvent.SelectedPreviousWeek) },
                    onViewTypeSelected = { dispatchEvent(DetailsEvent.SelectedViewType(it)) },
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
                is DetailsEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is DetailsEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is DetailsEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}