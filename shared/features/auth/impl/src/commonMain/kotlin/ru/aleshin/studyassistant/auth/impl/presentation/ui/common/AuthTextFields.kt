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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes

/**
 * @author Stanislav Aleshin on 19.04.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun UsernameTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    username: String,
    onUsernameChanged: (String) -> Unit,
    isError: Boolean,
    errorText: String?,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(
        modifier = modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = AuthThemeRes.strings.usernameLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            modifier = Modifier.sizeIn(minHeight = 57.dp).fillMaxWidth(),
            enabled = enabled,
            value = username,
            onValueChange = onUsernameChanged,
            placeholder = { Text(text = AuthThemeRes.strings.usernamePlaceholder) },
            leadingIcon = {
                Icon(
                    imageVector = vectorResource(AuthThemeRes.icons.username),
                    contentDescription = AuthThemeRes.strings.usernameLabel,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            supportingText = if (isError) {
                {
                    errorText?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            } else {
                null
            },
            isError = isError,
            keyboardActions = keyboardActions,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                errorTextColor = MaterialTheme.colorScheme.error,
            ),
            shape = MaterialTheme.shapes.large,
        )
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun EmailTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    email: String,
    onEmailChanged: (String) -> Unit,
    isError: Boolean,
    errorText: String?,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(
        modifier = modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = AuthThemeRes.strings.emailLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            modifier = Modifier.sizeIn(minHeight = 57.dp).fillMaxWidth(),
            enabled = enabled,
            value = email,
            onValueChange = onEmailChanged,
            placeholder = { Text(text = AuthThemeRes.strings.emailPlaceholder) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = vectorResource(AuthThemeRes.icons.email),
                    contentDescription = AuthThemeRes.strings.emailLabel,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            supportingText = if (isError) {
                {
                    errorText?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            } else {
                null
            },
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            keyboardActions = keyboardActions,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                errorTextColor = MaterialTheme.colorScheme.error,
            ),
            shape = MaterialTheme.shapes.large,
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun PasswordTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    password: String,
    onPasswordChanged: (String) -> Unit,
    isError: Boolean,
    errorText: String?,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(
        modifier = modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        var isShowPassword by rememberSaveable { mutableStateOf(false) }
        Text(
            text = AuthThemeRes.strings.passwordLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            modifier = Modifier.sizeIn(minHeight = 57.dp).fillMaxWidth(),
            enabled = enabled,
            value = password,
            onValueChange = onPasswordChanged,
            placeholder = { Text(text = AuthThemeRes.strings.passwordPlaceholder) },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = vectorResource(AuthThemeRes.icons.password),
                    contentDescription = AuthThemeRes.strings.passwordLabel,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                IconButton(onClick = { isShowPassword = !isShowPassword }) {
                    if (isShowPassword) {
                        Icon(
                            painter = painterResource(AuthThemeRes.icons.visibility),
                            contentDescription = AuthThemeRes.strings.hidePasswordDesc,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Icon(
                            painter = painterResource(AuthThemeRes.icons.visibilityOff),
                            contentDescription = AuthThemeRes.strings.showPasswordDesc,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            supportingText = if (isError) {
                {
                    errorText?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            keyboardActions = keyboardActions,
            isError = isError,
            visualTransformation = if (isShowPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                errorTextColor = MaterialTheme.colorScheme.error,
            ),
            shape = MaterialTheme.shapes.large,
        )
    }
}