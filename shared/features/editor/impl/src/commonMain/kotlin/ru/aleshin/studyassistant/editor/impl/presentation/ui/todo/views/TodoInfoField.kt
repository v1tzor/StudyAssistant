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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.InfoTextField
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 26.07.2024.
 */
@Composable
internal fun TodoInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    todoName: String,
    onTodoNameChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var editableTodoName by remember { mutableStateOf(todoName) }
    val todoNameInteraction = remember { MutableInteractionSource() }

    InfoTextField(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = editableTodoName,
        maxLength = Constants.Text.TODO_MAX_LENGTH,
        onValueChange = {
            editableTodoName = it
            onTodoNameChange(it)
        },
        label = EditorThemeRes.strings.todoNameFieldLabel,
        leadingInfoIcon = painterResource(StudyAssistantRes.icons.practicalTasks),
        placeholder = { Text(text = EditorThemeRes.strings.todoNameFieldPlaceholder) },
        trailingIcon = {
            if (todoNameInteraction.collectIsFocusedAsState().value) {
                IconButton(onClick = { focusManager.clearFocus() }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = StudyAssistantRes.colors.accents.green,
                    )
                }
            }
        },
        interactionSource = todoNameInteraction,
    )

    LaunchedEffect(isLoading) {
        if (editableTodoName != todoName) editableTodoName = todoName
    }
}