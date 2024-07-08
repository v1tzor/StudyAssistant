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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.views

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.functional.Constants.Text.DEFAULT_MAX_TEXT_LENGTH
import ru.aleshin.studyassistant.core.common.functional.Constants.Text.FULL_ORG_NAME_LENGTH
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
internal fun OrganizationNameInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    shortName: String?,
    fullName: String?,
    onUpdateShortName: (String?) -> Unit,
    onUpdateFullName: (String?) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var editableShortName by remember { mutableStateOf(shortName) }
    var editableFullName by remember { mutableStateOf(fullName) }

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
            val shortNameInteraction = remember { MutableInteractionSource() }
            val fullNameInteraction = remember { MutableInteractionSource() }

            OutlinedTextField(
                modifier = modifier.fillMaxWidth(),
                enabled = !isLoading,
                value = editableShortName ?: "",
                onValueChange = { text ->
                    if (text.length <= DEFAULT_MAX_TEXT_LENGTH) {
                        editableShortName = text
                        onUpdateShortName(text)
                    }
                },
                label = { Text(text = EditorThemeRes.strings.shortNameFieldLabel) },
                trailingIcon = {
                    if (shortNameInteraction.collectIsFocusedAsState().value) {
                        IconButton(onClick = { focusManager.clearFocus() }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StudyAssistantRes.colors.accents.green,
                            )
                        }
                    }
                },
                interactionSource = shortNameInteraction,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                ),
                shape = MaterialTheme.shapes.large,
            )
            OutlinedTextField(
                modifier = modifier.fillMaxWidth(),
                enabled = !isLoading,
                value = editableFullName ?: "",
                onValueChange = { text ->
                    if (text.length <= FULL_ORG_NAME_LENGTH) {
                        editableFullName = text
                        onUpdateFullName(text)
                    }
                },
                trailingIcon = {
                    if (fullNameInteraction.collectIsFocusedAsState().value) {
                        IconButton(onClick = { focusManager.clearFocus() }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StudyAssistantRes.colors.accents.green,
                            )
                        }
                    }
                },
                interactionSource = fullNameInteraction,
                label = { Text(text = EditorThemeRes.strings.fullNameFieldLabel) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                ),
                shape = MaterialTheme.shapes.large,
            )

            LaunchedEffect(isLoading) {
                if (editableShortName != shortName) editableShortName = shortName
                if (editableFullName != fullName) editableFullName = fullName
            }
        }
    }
}