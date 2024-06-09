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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import functional.Constants.Text.DEFAULT_MAX_TEXT_LENGTH
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun EmployeeNameInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    firstName: String?,
    secondName: String?,
    patronymic: String?,
    onUpdateFirstName: (String?) -> Unit,
    onUpdateSecondName: (String?) -> Unit,
    onUpdatePatronymic: (String?) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var editableFirstName by remember { mutableStateOf(firstName) }
    var editablePatronymic by remember { mutableStateOf(patronymic) }
    var editableSecondName by remember { mutableStateOf(secondName) }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.height(56.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(EditorThemeRes.icons.name),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val firstNameInteraction = remember { MutableInteractionSource() }
            val patronymicInteraction = remember { MutableInteractionSource() }
            val secondNameInteraction = remember { MutableInteractionSource() }

            OutlinedTextField(
                modifier = modifier.fillMaxWidth(),
                enabled = !isLoading,
                value = editableFirstName ?: "",
                onValueChange = { text ->
                    if (text.length <= DEFAULT_MAX_TEXT_LENGTH) {
                        editableFirstName = text
                        onUpdateFirstName(text)
                    }
                },
                label = { Text(text = EditorThemeRes.strings.firstNameFieldLabel) },
                trailingIcon = {
                    if (firstNameInteraction.collectIsFocusedAsState().value) {
                        IconButton(onClick = { focusManager.clearFocus() }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StudyAssistantRes.colors.accents.green,
                            )
                        }
                    }
                },
                interactionSource = firstNameInteraction,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                ),
                shape = MaterialTheme.shapes.large,
            )
            OutlinedTextField(
                modifier = modifier.fillMaxWidth(),
                enabled = !isLoading,
                value = editablePatronymic ?: "",
                onValueChange = { text ->
                    if (text.length <= DEFAULT_MAX_TEXT_LENGTH) {
                        editablePatronymic = text
                        onUpdatePatronymic(text)
                    }
                },
                trailingIcon = {
                    if (patronymicInteraction.collectIsFocusedAsState().value) {
                        IconButton(onClick = { focusManager.clearFocus() }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StudyAssistantRes.colors.accents.green,
                            )
                        }
                    }
                },
                interactionSource = patronymicInteraction,
                label = { Text(text = EditorThemeRes.strings.patronymicFieldLabel) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                ),
                shape = MaterialTheme.shapes.large,
            )
            OutlinedTextField(
                modifier = modifier.fillMaxWidth(),
                enabled = !isLoading,
                value = editableSecondName ?: "",
                onValueChange = { text ->
                    if (text.length <= DEFAULT_MAX_TEXT_LENGTH) {
                        editableSecondName = text
                        onUpdateSecondName(text)
                    }
                },
                label = { Text(text = EditorThemeRes.strings.secondNameFieldLabel) },
                trailingIcon = {
                    if (secondNameInteraction.collectIsFocusedAsState().value) {
                        IconButton(onClick = { focusManager.clearFocus() }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StudyAssistantRes.colors.accents.green,
                            )
                        }
                    }
                },
                interactionSource = secondNameInteraction,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                ),
                shape = MaterialTheme.shapes.large,
            )
        }
    }
}
