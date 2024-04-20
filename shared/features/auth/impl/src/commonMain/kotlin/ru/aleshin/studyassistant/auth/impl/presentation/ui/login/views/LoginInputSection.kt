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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.login.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.auth.impl.presentation.models.EmailValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.PasswordValidError
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.EmailTextField
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.PasswordTextField

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
@Composable
internal fun LoginInputSection(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    email: String,
    password: String,
    emailValidError: EmailValidError?,
    passwordValidError: PasswordValidError?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onForgotPassword: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            ForgotButton(
                enabled = enabled,
                onClick = onForgotPassword
            )
        }
    }
}

@Composable
internal fun ForgotButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    contentPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
) {
    Text(
        modifier = modifier
            .clip(shape)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
            )
            .padding(contentPadding),
        text = AuthThemeRes.strings.forgotPasswordLabel,
        color = contentColor,
        textDecoration = TextDecoration.Underline,
        style = MaterialTheme.typography.labelMedium,
    )
}