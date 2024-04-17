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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import architecture.screen.ScreenContent
import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.preview.impl.presentation.mappers.mapToString
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewTheme
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupDeps
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupViewState
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel.rememberSetupScreenModel
import views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal class SetupScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberSetupScreenModel(),
        dependencies = SetupDeps("test"),
        initialState = SetupViewState(),
    ) { state ->
        PreviewTheme {
            val strings = PreviewThemeRes.strings
            val snackbarState = remember { SnackbarHostState() }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                content = { paddingValues ->
                    SetupContent(
                        state = state,
                        modifier = Modifier.padding(paddingValues),
                    )
                },
                topBar = {},
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
                    is SetupEffect.ShowError -> {
                        snackbarState.showSnackbar(
                            message = effect.failures.mapToString(strings),
                            withDismissAction = true,
                        )
                    }
                    is SetupEffect.ReplaceGlobalScreen -> TODO()
                    is SetupEffect.ReplacePage -> TODO()
                }
            }
        }
    }
}