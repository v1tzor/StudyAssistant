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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.general

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralViewState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.screenmodel.rememberGeneralScreenModel

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
internal class GeneralScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberGeneralScreenModel(),
        initialState = GeneralViewState(),
    ) { state ->
        val strings = SettingsThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                GeneralContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onSelectedLanguage = { dispatchEvent(GeneralEvent.ChangeLanguage(it)) },
                    onSelectedTheme = { dispatchEvent(GeneralEvent.ChangeTheme(it)) },
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
            contentWindowInsets = WindowInsets.navigationBars
        )

        handleEffect { effect ->
            when (effect) {
                is GeneralEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings, coreStrings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}