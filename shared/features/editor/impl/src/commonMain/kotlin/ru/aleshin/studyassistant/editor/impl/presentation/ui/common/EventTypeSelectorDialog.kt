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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import entities.subject.EventType
import mappers.mapToString
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import views.dialog.BaseSelectorDialog
import views.dialog.SelectorDialogItemView
import views.dialog.SelectorDialogNotSelectedItemView

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun EventTypeSelectorDialog(
    modifier: Modifier = Modifier,
    selected: EventType?,
    onDismiss: () -> Unit,
    onConfirm: (EventType?) -> Unit,
) {
    var selectedEventType by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedEventType,
        items = EventType.entries,
        header = EditorThemeRes.strings.eventTypeSelectorHeader,
        title = EditorThemeRes.strings.eventTypeSelectorTitle,
        itemView = { eventType ->
            SelectorDialogItemView(
                onClick = { selectedEventType = eventType },
                selected = eventType == selectedEventType,
                title = eventType.mapToString(StudyAssistantRes.strings),
                label = null,
            )
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedEventType == null,
                onClick = { selectedEventType = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}