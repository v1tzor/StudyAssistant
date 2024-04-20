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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.AuthHeaderSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterViewState
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.views.RegisterActionsSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.views.RegisterInputSection

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun RegisterContent(
    modifier: Modifier,
    state: RegisterViewState,
    onRegisterClick: (name: String, email: String, password: String) -> Unit,
    onAlreadyHaveAccountClick: () -> Unit,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.padding(bottom = 16.dp, top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AuthHeaderSection(
            modifier = Modifier.weight(1f),
            text = AuthThemeRes.strings.registerHeadline,
            illustration = painterResource(AuthThemeRes.icons.registerIllustration),
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
        )
        RegisterActionsSection(
            enabled = !state.isLoading && username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty(),
            isLoading = state.isLoading,
            onRegisterClick = { onRegisterClick(username, email, password) },
            onAlreadyHaveAccountClick = onAlreadyHaveAccountClick,
        )
    }
}