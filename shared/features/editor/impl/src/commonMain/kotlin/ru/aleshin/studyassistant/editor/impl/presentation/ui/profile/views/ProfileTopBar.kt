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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.DialogButtons
import ru.aleshin.studyassistant.core.ui.views.DialogHeader
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ProfileTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onChangePassword: (old: String, new: String) -> Unit,
) {
    var passwordChangerDialogState by remember { mutableStateOf(false) }
    var isExpandMoreDropdownMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(text = EditorThemeRes.strings.profileEditorHeader) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = StudyAssistantRes.strings.backIconDesc,
                )
            }
        },
        actions = {
            IconButton(onClick = { isExpandMoreDropdownMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = StudyAssistantRes.strings.backIconDesc,
                )
            }
            ProfileMoreDropdownMenu(
                isExpanded = isExpandMoreDropdownMenu,
                onDismiss = { isExpandMoreDropdownMenu = false },
                onChangePassword = {
                    passwordChangerDialogState = true
                    isExpandMoreDropdownMenu = false
                },
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        )
    )

    if (passwordChangerDialogState) {
        PasswordChangerDialog(
            onDismiss = { passwordChangerDialogState = false },
            onChangePassword = { old, new ->
                onChangePassword(old, new)
                passwordChangerDialogState = false
            }
        )
    }
}

@Composable
private fun ProfileMoreDropdownMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    onChangePassword: () -> Unit,
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismiss,
        modifier = modifier.sizeIn(maxHeight = 200.dp),
        shape = MaterialTheme.shapes.large,
        offset = DpOffset(0.dp, 6.dp),
    ) {
        DropdownMenuItem(
            onClick = onChangePassword,
            text = { Text(text = EditorThemeRes.strings.changePasswordActionLabel) },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, contentDescription = null) },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PasswordChangerDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onChangePassword: (old: String, new: String) -> Unit,
) {
    var editableOldPassword by remember { mutableStateOf("") }
    var editableNewPassword by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.width(312.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(header = EditorThemeRes.strings.passwordChangerLabel)
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        modifier = modifier.fillMaxWidth(),
                        value = editableOldPassword,
                        onValueChange = { text ->
                            editableOldPassword = text
                        },
                        label = { Text(text = EditorThemeRes.strings.oldPasswordFieldLabel) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                    )
                    OutlinedTextField(
                        modifier = modifier.fillMaxWidth(),
                        value = editableNewPassword,
                        onValueChange = { text ->
                            editableNewPassword = text
                        },
                        label = { Text(text = EditorThemeRes.strings.passwordChangerLabel) },
                        placeholder = { Text(text = EditorThemeRes.strings.passwordFieldPlaceholder) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                    )
                }
                DialogButtons(
                    enabledConfirm = editableNewPassword.isNotBlank() && editableOldPassword.isNotBlank()
                            && editableNewPassword.length >= Constants.Text.MIN_PASSWORD_LENGTH
                            && editableNewPassword.matches(Regex(Constants.Regex.PASSWORD)),
                    confirmTitle = StudyAssistantRes.strings.saveConfirmTitle,
                    onCancelClick = onDismiss,
                    onConfirmClick = { onChangePassword(editableOldPassword, editableNewPassword) },
                )
            }
        }
    }
}