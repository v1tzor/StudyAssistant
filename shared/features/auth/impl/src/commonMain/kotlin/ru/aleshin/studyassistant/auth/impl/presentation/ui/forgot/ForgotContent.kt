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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.ForgotCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.AuthHeaderSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.EmailTextField
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotState
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.store.ForgotComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.views.ForgotActionsSection
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsMutable
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalWindowSize
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.SpacerToKeyboard

/**
 * @author Stanislav Aleshin on 16.04.2024
 */
@Composable
internal fun ForgotContent(
    forgotComponent: ForgotComponent,
    modifier: Modifier = Modifier,
) {
    val store = forgotComponent.store
    val strings = AuthThemeRes.strings
    val windowSize = LocalWindowSize.current
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            when (windowSize.heightWindowType) {
                else -> BaseForgotContent(
                    state = store.stateAsMutable().value,
                    modifier = Modifier.padding(paddingValues),
                    onLoginClick = {
                        store.dispatchEvent(ForgotEvent.ClickLogin)
                    },
                    onResetPasswordClick = { email ->
                        val credentials = ForgotCredentialsUi(email)
                        store.dispatchEvent(ForgotEvent.ClickResetPassword(credentials))
                    }
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
            is ForgotEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
private fun BaseForgotContent(
    state: ForgotState,
    modifier: Modifier,
    onResetPasswordClick: (email: String) -> Unit,
    onLoginClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(bottom = 16.dp, top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AuthHeaderSection(
            modifier = Modifier.weight(1f),
            header = AuthThemeRes.strings.forgotHeadline,
            illustration = AuthThemeRes.icons.forgotIllustration,
            contentDescription = AuthThemeRes.strings.forgotDesc,
        )
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val focusManager = LocalFocusManager.current
            var email by rememberSaveable { mutableStateOf("") }

            Column {
                EmailTextField(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    enabled = !state.isLoading,
                    email = email,
                    onEmailChanged = { email = it },
                    isError = state.emailValidError != null,
                    errorText = state.emailValidError?.mapToMessage(),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (email.isNotEmpty()) onResetPasswordClick(email)
                            focusManager.clearFocus()
                        },
                    )
                )
                SpacerToKeyboard()
            }

            ForgotActionsSection(
                enabled = !state.isLoading && email.isNotEmpty(),
                isLoading = state.isLoading,
                onResetPasswordClick = { onResetPasswordClick(email) },
                onLoginClick = onLoginClick,
            )
        }
    }
}