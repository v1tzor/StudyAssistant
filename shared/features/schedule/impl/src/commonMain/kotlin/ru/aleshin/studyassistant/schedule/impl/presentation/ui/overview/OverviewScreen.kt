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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import architecture.screen.ScreenContent
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import navigation.root
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewViewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.screenmodel.rememberOverviewScreenModel
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.OverviewTopBar
import views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
internal class OverviewScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberOverviewScreenModel(),
        initialState = OverviewViewState(),
    ) { state ->
        val strings = ScheduleThemeRes.strings
        val navigator = LocalNavigator.current
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                OverviewContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                )
            },
            topBar = {
                OverviewTopBar(
                    onEditClick = {},
                    onCurrentDay = { dispatchEvent(OverviewEvent.SelectedCurrentDay) },
                    onDetailsClick = { dispatchEvent(OverviewEvent.NavigateToDetails) },
                )
            },
            bottomBar = {},
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
        )

        handleEffect { effect ->
            when (effect) {
                is OverviewEffect.NavigateToLocal -> navigator?.push(effect.pushScreen)
                is OverviewEffect.NavigateToGlobal -> navigator?.root()?.push(effect.pushScreen)
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