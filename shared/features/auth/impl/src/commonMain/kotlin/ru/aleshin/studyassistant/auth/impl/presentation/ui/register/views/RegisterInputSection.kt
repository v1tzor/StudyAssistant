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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.register.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.auth.impl.presentation.models.EmailValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.PasswordValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.UsernameValidError
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.EmailTextField
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.UsernameTextField
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.PasswordTextField

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
@Composable
internal fun RegisterInputSection(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    username: String,
    email: String,
    password: String,
    usernameValidError: UsernameValidError?,
    emailValidError: EmailValidError?,
    passwordValidError: PasswordValidError?,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        UsernameTextField(
            enabled = enabled,
            username = username,
            onUsernameChanged = onUsernameChange,
            isError = usernameValidError != null,
            errorText = usernameValidError?.mapToMessage(),
        )
        EmailTextField(
            enabled = enabled,
            email = email,
            onEmailChanged = onEmailChange,
            isError = emailValidError != null,
            errorText = emailValidError?.mapToMessage(),
        )
        PasswordTextField(
            enabled = enabled,
            password = password,
            onPasswordChanged = onPasswordChange,
            isError = passwordValidError != null,
            errorText = passwordValidError?.mapToMessage(),
        )
    }
}