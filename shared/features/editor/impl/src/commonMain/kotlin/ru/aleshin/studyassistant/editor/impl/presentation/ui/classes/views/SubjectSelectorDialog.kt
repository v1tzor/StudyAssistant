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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import entities.subject.EventType
import mappers.mapToString
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import theme.material.full
import views.dialog.BaseSelectorDialog
import views.dialog.SelectorDialogAddItemView
import views.dialog.SelectorDialogItemView
import views.dialog.SelectorDialogNotSelectedItemView

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SubjectSelectorDialog(
    enabledAdd: Boolean,
    modifier: Modifier = Modifier,
    eventType: EventType?,
    selected: SubjectUi?,
    subjects: List<SubjectUi>,
    onAddSubject: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (SubjectUi?) -> Unit,
) {
    var selectedSubject by remember { mutableStateOf(selected) }
    val subjectsByEventType = subjects.filter { it.eventType == eventType }
    val otherSubjects = subjects.filter { it.eventType != eventType }.sortedBy { it.eventType }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedSubject,
        items = subjectsByEventType + otherSubjects,
        header = EditorThemeRes.strings.subjectSelectorHeader,
        title = EditorThemeRes.strings.subjectSelectorTitle,
        itemView = { subject ->
            SelectorDialogItemView(
                onClick = { selectedSubject = subject },
                selected = subject.uid == selectedSubject?.uid,
                title = subject.name,
                label = subject.eventType.mapToString(StudyAssistantRes.strings),
                leadingIcon = {
                    Surface(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        shape = MaterialTheme.shapes.full(),
                        color = Color(subject.color),
                        content = { Box(modifier = Modifier.size(8.dp, 24.dp)) },
                    )
                },
            )
        },
        addItemView = {
            SelectorDialogAddItemView(
                enabled = enabledAdd,
                onClick = onAddSubject
            )
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedSubject == null,
                onClick = { selectedSubject = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}