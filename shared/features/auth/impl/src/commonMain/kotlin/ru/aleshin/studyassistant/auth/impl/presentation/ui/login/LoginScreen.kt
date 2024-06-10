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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.login

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
import cafe.adriel.voyager.navigator.currentOrThrow
import navigation.root
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.LoginCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginViewState
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.screenmodel.rememberLoginScreenModel
import theme.tokens.LocalWindowSize
import views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 16.04.2024
 */
internal class LoginScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberLoginScreenModel(),
        initialState = LoginViewState(),
    ) { state ->
        val navigator = LocalNavigator.currentOrThrow
        val windowSize = LocalWindowSize.current
        val strings = AuthThemeRes.strings
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                when (windowSize.heightWindowType) {
                    else -> LoginContent(
                        state = state,
                        modifier = Modifier.padding(paddingValues),
                        onLoginViaGoogleClick = { dispatchEvent(LoginEvent.LoginViaGoogle(it)) },
                        onForgotPassword = { dispatchEvent(LoginEvent.NavigateToForgot) },
                        onNotAccountClick = { dispatchEvent(LoginEvent.NavigateToRegister) },
                        onLoginClick = { email, password ->
                            val credentials = LoginCredentialsUi(email, password)
                            dispatchEvent(LoginEvent.LoginWithEmail(credentials))
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
                is LoginEffect.PushScreen -> navigator.push(effect.screen)
                is LoginEffect.ReplaceGlobalScreen -> navigator.root().replaceAll(effect.screen)
                is LoginEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failure.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}