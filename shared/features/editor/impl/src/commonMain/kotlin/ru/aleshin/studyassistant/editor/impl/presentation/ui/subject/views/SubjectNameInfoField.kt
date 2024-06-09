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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.views

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import functional.Constants
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import views.InfoTextField

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun SubjectNameInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    name: String?,
    onNameChange: (String) -> Unit,
) {
    var editableName by remember { mutableStateOf(name) }
    val focusManager = LocalFocusManager.current
    val nameInteraction = remember { MutableInteractionSource() }

    InfoTextField(
        modifier = modifier,
        enabled = !isLoading,
        paddingValues = PaddingValues(start = 16.dp, end = 24.dp),
        value = editableName,
        onValueChange = {
            editableName = it
            onNameChange(it)
        },
        trailingIcon = {
            if (nameInteraction.collectIsFocusedAsState().value) {
                IconButton(onClick = { focusManager.clearFocus() }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = StudyAssistantRes.colors.accents.green,
                    )
                }
            }
        },
        interactionSource = nameInteraction,
        maxLength = Constants.Text.SUBJECT_TEXT_LENGTH,
        label = EditorThemeRes.strings.subjectNameFieldLabel,
        placeholder = { Text(text = EditorThemeRes.strings.subjectNameFieldPlaceholder) },
        leadingInfoIcon = painterResource(EditorThemeRes.icons.name),
    )
}