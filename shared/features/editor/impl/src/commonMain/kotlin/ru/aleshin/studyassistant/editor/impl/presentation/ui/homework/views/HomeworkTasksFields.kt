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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import functional.Constants.Text.TASK_MAX_LENGTH
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import views.InfoTextField

/**
 * @author Stanislav Aleshin on 23.06.2024.
 */
@Composable
internal fun HomeworkTasksFields(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    theoreticalTasks: String,
    practicalTasks: String,
    presentationsTasks: String,
    onTaskChange: (theory: String, practice: String, presentations: String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var editableTheory by remember { mutableStateOf(theoreticalTasks) }
    var editablePresentations by remember { mutableStateOf(practicalTasks) }
    var editablePractice by remember { mutableStateOf(presentationsTasks) }

    Column(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val theoryInteraction = remember { MutableInteractionSource() }
        val practiceInteraction = remember { MutableInteractionSource() }
        val presentationsInteraction = remember { MutableInteractionSource() }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = EditorThemeRes.strings.taskTitle,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        InfoTextField(
            enabled = !isLoading,
            value = editableTheory,
            maxLength = TASK_MAX_LENGTH,
            onValueChange = {
                editableTheory = it
                onTaskChange(it, editablePractice, editablePresentations)
            },
            label = EditorThemeRes.strings.theoryFieldLabel,
            leadingInfoIcon = painterResource(StudyAssistantRes.icons.theoreticalTasks),
            placeholder = { Text(text = EditorThemeRes.strings.theoryFieldPlaceholder) },
            trailingIcon = {
                if (theoryInteraction.collectIsFocusedAsState().value) {
                    IconButton(onClick = { focusManager.clearFocus() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.green,
                        )
                    }
                }
            },
            interactionSource = theoryInteraction,
        )

        InfoTextField(
            enabled = !isLoading,
            value = editablePractice,
            maxLength = TASK_MAX_LENGTH,
            onValueChange = {
                editablePractice = it
                onTaskChange(editableTheory, it, editablePresentations)
            },
            label = EditorThemeRes.strings.practiceFieldLabel,
            leadingInfoIcon = painterResource(StudyAssistantRes.icons.practicalTasks),
            placeholder = { Text(text = EditorThemeRes.strings.practiceFieldPlaceholder) },
            trailingIcon = {
                if (practiceInteraction.collectIsFocusedAsState().value) {
                    IconButton(onClick = { focusManager.clearFocus() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.green,
                        )
                    }
                }
            },
            interactionSource = practiceInteraction,
        )

        InfoTextField(
            enabled = !isLoading,
            value = editablePresentations,
            maxLength = TASK_MAX_LENGTH,
            onValueChange = {
                editablePresentations = it
                onTaskChange(editableTheory, editablePractice, it)
            },
            label = EditorThemeRes.strings.presentationsFieldLabel,
            leadingInfoIcon = painterResource(StudyAssistantRes.icons.presentationTasks),
            placeholder = { Text(text = EditorThemeRes.strings.presentationsFieldPlaceholder) },
            trailingIcon = {
                if (presentationsInteraction.collectIsFocusedAsState().value) {
                    IconButton(onClick = { focusManager.clearFocus() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.green,
                        )
                    }
                }
            },
            interactionSource = presentationsInteraction,
        )
    }

    LaunchedEffect(isLoading) {
        if (editableTheory != theoreticalTasks) editableTheory = theoreticalTasks
        if (editablePractice != practicalTasks) editablePractice = practicalTasks
        if (editablePresentations != presentationsTasks) editablePresentations = presentationsTasks
    }
}