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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.LoginCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.AuthHeaderSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginState
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.store.LoginComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.views.LoginActionsSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.views.LoginInputSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.views.SignUpButton
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.functional.Constants.App.PRIVACY_POLICY
import ru.aleshin.studyassistant.core.domain.entities.users.UserSession
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalWindowSize
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
@Composable
internal fun LoginContent(
    loginComponent: LoginComponent,
    modifier: Modifier = Modifier,
) {
    val store = loginComponent.store
    val windowSize = LocalWindowSize.current
    val strings = AuthThemeRes.strings
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            when (windowSize.heightWindowType) {
                else -> BaseLoginContent(
                    state = store.stateAsState().value,
                    modifier = Modifier.padding(paddingValues),
                    onLoginClick = { email, password ->
                        val credentials = LoginCredentialsUi(email, password)
                        store.dispatchEvent(LoginEvent.SubmitCredentials(credentials))
                    },
                    onSignUpClick = {
                        store.dispatchEvent(LoginEvent.ClickSignUp)
                    },
                    onForgotPasswordClick = {
                        store.dispatchEvent(LoginEvent.ClickForgotPassword)
                    },
                    onSuccessSocialNetworkLogin = {
                        store.dispatchEvent(LoginEvent.SocialNetworkAuthSucceeded(it))
                    },
                    onFailureSocialNetworkLogin = {
                        coroutineScope.launch {
                            snackbarState.showSnackbar(
                                message = AuthFailures.OAuthProviderError.mapToMessage(strings),
                                withDismissAction = true,
                            )
                        }
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
            is LoginEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failure.mapToMessage(strings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
internal fun BaseLoginContent(
    state: LoginState,
    modifier: Modifier,
    onLoginClick: (email: String, password: String) -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSuccessSocialNetworkLogin: (UserSession) -> Unit,
    onFailureSocialNetworkLogin: () -> Unit,
) {
    Column(
        modifier = modifier.padding(bottom = 16.dp, top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AuthHeaderSection(
            modifier = Modifier.weight(1f),
            header = AuthThemeRes.strings.loginHeadline,
            illustration = AuthThemeRes.icons.loginIllustration,
            contentDescription = AuthThemeRes.strings.loginDesc,
        )
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val focusManager = LocalFocusManager.current
            var email by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }

            LoginInputSection(
                enabled = !state.isLoading,
                email = email,
                password = password,
                emailValidError = state.emailValidError,
                passwordValidError = state.passwordValidError,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onForgotPasswordClick = onForgotPasswordClick,
                onEnterClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) onLoginClick(email, password)
                    focusManager.clearFocus()
                }
            )
            LoginActionsSection(
                enabled = !state.isLoading && email.isNotEmpty() && password.isNotEmpty(),
                enabledGoogle = state.isAvailableGoogle,
                isLoading = state.isLoading,
                onLoginClick = { onLoginClick(email, password) },
                onSuccessSocialNetworkLogin = onSuccessSocialNetworkLogin,
                onFailureSocialNetworkLogin = onFailureSocialNetworkLogin,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = buildAnnotatedString {
                    append(AuthThemeRes.strings.loginTermsAndConditionsBody)
                    withLink(
                        link = LinkAnnotation.Url(
                            url = PRIVACY_POLICY,
                            styles = TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary))
                        )
                    ) {
                        append(AuthThemeRes.strings.privacyPolicyLabel)
                    }
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        SignUpButton(
            enabled = !state.isLoading,
            onClick = onSignUpClick,
        )
    }
}