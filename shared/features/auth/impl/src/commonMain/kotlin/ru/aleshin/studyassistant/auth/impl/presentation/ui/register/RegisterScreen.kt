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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.register

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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import navigation.root
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.auth.impl.presentation.models.RegisterCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthTheme
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterViewState
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.screenmodel.rememberRegisterScreenModel
import theme.tokens.LocalWindowSize
import views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 16.04.2024
 */
internal class RegisterScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberRegisterScreenModel(),
        initialState = RegisterViewState(),
    ) { state ->
        val strings = AuthThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val windowSize = LocalWindowSize.current
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                when (windowSize.heightWindowType) {
                    else -> RegisterContent(
                        state = state,
                        modifier = Modifier.padding(paddingValues),
                        onAlreadyHaveAccountClick = { dispatchEvent(RegisterEvent.NavigateToLogin) },
                        onRegisterClick = { name, email, password ->
                            val credentials = RegisterCredentialsUi(name, email, password)
                            dispatchEvent(RegisterEvent.RegisterNewAccount(credentials))
                        },
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
                is RegisterEffect.ReplaceGlobalScreen -> navigator.root().replaceAll(effect.screen)
                is RegisterEffect.PushScreen -> navigator.push(effect.screen)
                is RegisterEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}