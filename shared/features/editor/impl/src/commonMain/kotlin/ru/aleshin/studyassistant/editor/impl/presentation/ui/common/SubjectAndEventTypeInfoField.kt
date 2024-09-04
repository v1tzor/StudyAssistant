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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.ClickableTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.SubjectSelectorBottomSheet

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
internal fun SubjectAndEventTypeInfoField(
    enabledAddSubject: Boolean = true,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    subject: SubjectUi?,
    eventType: EventType?,
    allSubjects: List<SubjectUi>,
    onAddSubject: () -> Unit,
    onEditSubject: (SubjectUi) -> Unit,
    onSelectedSubject: (SubjectUi?) -> Unit,
    onSelectedEventType: (EventType?) -> Unit,
) {
    var openEventTypeSelectorSheet by remember { mutableStateOf(false) }
    var openSubjectSelectorSheet by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.height(61.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(StudyAssistantRes.icons.classes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ClickableTextField(
                onClick = { openSubjectSelectorSheet = true },
                enabled = !isLoading,
                value = subject?.name,
                label = EditorThemeRes.strings.subjectFieldLabel,
                placeholder = EditorThemeRes.strings.subjectFieldPlaceholder,
                leadingIcon = {
                    Surface(
                        shape = MaterialTheme.shapes.full,
                        color = subject?.color?.let { Color(it) }
                            ?: MaterialTheme.colorScheme.outlineVariant,
                        content = { Box(modifier = Modifier.size(8.dp, 24.dp)) },
                    )
                },
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = openSubjectSelectorSheet,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = false,
                maxLines = 3,
            )
            ClickableTextField(
                onClick = { openEventTypeSelectorSheet = true },
                enabled = !isLoading,
                value = eventType?.mapToString(StudyAssistantRes.strings),
                label = EditorThemeRes.strings.eventTypeFieldLabel,
                placeholder = EditorThemeRes.strings.eventTypeFieldPlaceholder,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = openEventTypeSelectorSheet,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }

    if (openEventTypeSelectorSheet) {
        EventTypeSelectorBottomSheet(
            selected = eventType,
            onDismiss = { openEventTypeSelectorSheet = false },
            onConfirm = {
                onSelectedEventType(it)
                openEventTypeSelectorSheet = false
            },
        )
    }

    if (openSubjectSelectorSheet) {
        SubjectSelectorBottomSheet(
            enabledAdd = enabledAddSubject,
            selected = subject,
            eventType = eventType,
            subjects = allSubjects,
            onAddSubject = onAddSubject,
            onEditSubject = onEditSubject,
            onDismiss = { openSubjectSelectorSheet = false },
            onConfirm = {
                onSelectedSubject(it)
                openSubjectSelectorSheet = false
            },
        )
    }
}

@Composable
internal fun SubjectInfoField(
    enabledAddSubject: Boolean = true,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    subject: SubjectUi?,
    allSubjects: List<SubjectUi>,
    onAddSubject: () -> Unit,
    onEditSubject: (SubjectUi) -> Unit,
    onSelectedSubject: (SubjectUi?) -> Unit,
) {
    var subjectSelectorState by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { subjectSelectorState = true },
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = subject?.name,
        label = EditorThemeRes.strings.subjectFieldLabel,
        infoIcon = painterResource(StudyAssistantRes.icons.classes),
        placeholder = EditorThemeRes.strings.subjectFieldPlaceholder,
        leadingIcon = {
            Surface(
                shape = MaterialTheme.shapes.full,
                color = subject?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.outlineVariant,
                content = { Box(modifier = Modifier.size(8.dp, 24.dp)) },
            )
        },
        trailingIcon = {
            ExpandedIcon(
                isExpanded = subjectSelectorState,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = false,
        maxLines = 3,
    )

    if (subjectSelectorState) {
        SubjectSelectorBottomSheet(
            enabledAdd = enabledAddSubject,
            selected = subject,
            eventType = EventType.CLASS,
            subjects = allSubjects,
            onAddSubject = onAddSubject,
            onEditSubject = onEditSubject,
            onDismiss = { subjectSelectorState = false },
            onConfirm = {
                onSelectedSubject(it)
                subjectSelectorState = false
            },
        )
    }
}