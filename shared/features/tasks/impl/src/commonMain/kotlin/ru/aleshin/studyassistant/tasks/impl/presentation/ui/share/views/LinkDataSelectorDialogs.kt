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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseSelectorDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogAddItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogNotSelectedItemView
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.NumberedClassUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 24.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SubjectSelectorDialog(
    modifier: Modifier = Modifier,
    selected: SubjectUi?,
    targetOrganization: OrganizationShortUi?,
    subjects: List<SubjectUi>,
    organizations: List<OrganizationShortUi>,
    onAddSubject: (organizationId: UID) -> Unit,
    onChangeOrganization: (OrganizationShortUi) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (SubjectUi?) -> Unit,
) {
    var selectedSubject by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedSubject,
        items = subjects,
        header = TasksThemeRes.strings.subjectSelectorHeader,
        title = TasksThemeRes.strings.subjectSelectorTitle,
        itemView = { subject ->
            SelectorDialogItemView(
                onClick = { selectedSubject = subject },
                selected = subject.uid == selectedSubject?.uid,
                title = subject.name,
                label = subject.eventType.mapToString(StudyAssistantRes.strings),
                leadingIcon = {
                    Surface(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        shape = MaterialTheme.shapes.full,
                        color = Color(subject.color),
                        content = { Box(modifier = Modifier.size(8.dp, 24.dp)) },
                    )
                },
            )
        },
        addItemView = {
            SelectorDialogAddItemView(
                enabled = targetOrganization != null,
                onClick = { if (targetOrganization != null) onAddSubject(targetOrganization.uid) }
            )
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedSubject == null,
                onClick = { selectedSubject = null },
            )
        },
        filters = {
            organizations.forEach { organization ->
                FilterChip(
                    selected = organization.uid == targetOrganization?.uid,
                    onClick = { onChangeOrganization(organization) },
                    label = { Text(text = organization.shortName) },
                )
            }
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun LinkedClassSelectorDialog(
    modifier: Modifier = Modifier,
    selected: NumberedClassUi?,
    classes: List<NumberedClassUi>,
    onDismiss: () -> Unit,
    onConfirm: (NumberedClassUi?) -> Unit,
) {
    var selectedClass by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedClass,
        items = classes,
        header = TasksThemeRes.strings.linkedClassSelectorHeader,
        title = TasksThemeRes.strings.linkedClassSelectorTitle,
        itemView = { classModel ->
            SelectorDialogItemView(
                onClick = { selectedClass = classModel },
                selected = classModel.data.uid == selectedClass?.data?.uid,
                title = classModel.data.subject?.name ?: StudyAssistantRes.strings.noneTitle,
                label = buildString {
                    append(classModel.number)
                    append(" ", TasksThemeRes.strings.numberOfClassSuffix)
                },
                leadingIcon = {
                    Surface(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        shape = MaterialTheme.shapes.full,
                        color = classModel.data.subject?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.outlineVariant,
                        content = { Box(modifier = Modifier.size(8.dp, 24.dp)) },
                    )
                },
            )
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedClass == null,
                onClick = { selectedClass = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}