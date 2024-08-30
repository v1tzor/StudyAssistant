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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.verification

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
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationViewState
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.screenmodel.rememberVerificationScreenModel
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.views.VerificationTopBar
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 29.08.2024
 */
internal class VerificationScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberVerificationScreenModel(),
        initialState = VerificationViewState(),
    ) { state ->
        val strings = AuthThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                VerificationContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onSendEmailVerification = { dispatchEvent(VerificationEvent.SendEmailVerification) },
                )
            },
            topBar = {
                VerificationTopBar(
                    onSignOut = { dispatchEvent(VerificationEvent.SignOut) },
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
                is VerificationEffect.ReplaceScreen -> navigator.replace(effect.screen)
                is VerificationEffect.ReplaceGlobalScreen -> navigator.root().replaceAll(effect.screen)
                is VerificationEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}