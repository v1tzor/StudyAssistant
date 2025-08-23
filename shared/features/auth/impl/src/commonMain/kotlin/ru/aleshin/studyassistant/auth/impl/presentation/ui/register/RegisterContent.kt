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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.RegisterCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.AuthHeaderSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterState
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.store.RegisterComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.views.RegisterActionsSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.views.RegisterInputSection
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsMutable
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalWindowSize
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
@Composable
internal fun RegisterContent(
    registerComponent: RegisterComponent,
    modifier: Modifier = Modifier,
) {
    val store = registerComponent.store
    val strings = AuthThemeRes.strings
    val windowSize = LocalWindowSize.current
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            when (windowSize.heightWindowType) {
                else -> BaseRegisterContent(
                    state = store.stateAsMutable().value,
                    modifier = Modifier.padding(paddingValues),
                    onLoginClick = {
                        store.dispatchEvent(RegisterEvent.ClickLogin)
                    },
                    onRegisterClick = { name, email, password ->
                        val credentials = RegisterCredentialsUi(name, email, password)
                        store.dispatchEvent(RegisterEvent.SubmitCredentials(credentials))
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

    store.handleEffects { effect ->
        when (effect) {
            is RegisterEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseRegisterContent(
    modifier: Modifier,
    state: RegisterState,
    onRegisterClick: (name: String, email: String, password: String) -> Unit,
    onLoginClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(bottom = 16.dp, top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val focusManager = LocalFocusManager.current
        var username by rememberSaveable { mutableStateOf("") }
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }

        AuthHeaderSection(
            modifier = Modifier.weight(1f),
            header = AuthThemeRes.strings.registerHeadline,
            illustration = AuthThemeRes.icons.registerIllustration,
            contentDescription = AuthThemeRes.strings.registerDesc,
        )
        RegisterInputSection(
            enabled = !state.isLoading,
            email = email,
            username = username,
            password = password,
            usernameValidError = state.usernameValidError,
            emailValidError = state.emailValidError,
            passwordValidError = state.passwordValidError,
            onUsernameChange = { username = it },
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onEnterClick = {
                if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    onRegisterClick(username, email, password)
                }
                focusManager.clearFocus()
            }
        )
        RegisterActionsSection(
            enabled = !state.isLoading && username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty(),
            isLoading = state.isLoading,
            onRegisterClick = { onRegisterClick(username, email, password) },
            onLogin = onLoginClick,
        )
    }
}