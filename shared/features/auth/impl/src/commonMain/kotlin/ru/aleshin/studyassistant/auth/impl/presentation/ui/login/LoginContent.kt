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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.AuthHeaderSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginViewState
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.views.LoginActionsSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.views.LoginInputSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.views.NotAccountButton
import ru.aleshin.studyassistant.core.common.functional.Constants.App.PRIVACY_POLICY

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
@Composable
internal fun LoginContent(
    state: LoginViewState,
    modifier: Modifier,
    onForgotPassword: () -> Unit,
    onLoginClick: (email: String, password: String) -> Unit,
    onLoginViaGoogleClick: (idToken: String?) -> Unit,
    onNotAccountClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(bottom = 16.dp, top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AuthHeaderSection(
            modifier = Modifier.weight(1f),
            header = AuthThemeRes.strings.loginHeadline,
            illustration = painterResource(AuthThemeRes.icons.loginIllustration),
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
                onForgotPassword = onForgotPassword,
                onCompleteEnter = {
                    if (email.isNotEmpty() && password.isNotEmpty()) onLoginClick(email, password)
                    focusManager.clearFocus()
                }
            )
            LoginActionsSection(
                enabled = !state.isLoading && email.isNotEmpty() && password.isNotEmpty(),
                enabledGoogle = state.isAvailableGoogle,
                isLoading = state.isLoading,
                onLoginClick = { onLoginClick(email, password) },
                onLoginViaGoogleClick = { onLoginViaGoogleClick(it) },
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
        NotAccountButton(
            enabled = !state.isLoading,
            onClick = onNotAccountClick,
        )
    }
}