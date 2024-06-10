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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import entities.subject.EventType
import mappers.mapToString
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.EventTypeSelectorDialog
import theme.StudyAssistantRes
import views.ClickableInfoTextField
import views.ExpandedIcon

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun EventTypeInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    eventType: EventType?,
    onSelected: (EventType?) -> Unit,
) {
    var isOpenEventTypeSelector by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { isOpenEventTypeSelector = true },
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = eventType?.mapToString(StudyAssistantRes.strings),
        label = EditorThemeRes.strings.eventTypeFieldLabel,
        placeholder = EditorThemeRes.strings.eventTypeFieldPlaceholder,
        infoIcon = painterResource(EditorThemeRes.icons.classes),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = isOpenEventTypeSelector,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    if (isOpenEventTypeSelector) {
        EventTypeSelectorDialog(
            selected = eventType,
            onDismiss = { isOpenEventTypeSelector = false },
            onConfirm = { selectedEventType ->
                onSelected(selectedEventType)
                isOpenEventTypeSelector = false
            },
        )
    }
}