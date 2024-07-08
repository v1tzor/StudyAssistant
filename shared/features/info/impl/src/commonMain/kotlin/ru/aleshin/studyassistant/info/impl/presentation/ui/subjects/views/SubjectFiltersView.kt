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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseSelectorDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogItemView
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToString
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectSortedType
import ru.aleshin.studyassistant.info.impl.presentation.ui.common.OrganizationPicker
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes

/**
 * @author Stanislav Aleshin on 18.06.2024.
 */
@Composable
internal fun SubjectFiltersView(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    sortedType: SubjectSortedType,
    selectedOrganization: UID?,
    allOrganizations: List<OrganizationShortUi>,
    onSelectOrganization: (OrganizationShortUi) -> Unit,
    onSelectSortedType: (SubjectSortedType) -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrganizationPicker(
                enabled = !isLoading,
                selectedOrganization = selectedOrganization,
                allOrganizations = allOrganizations,
                onSelectOrganization = onSelectOrganization,
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                SubjectSortedTypePicker(
                    enabled = !isLoading,
                    sortedType = sortedType,
                    onSelectSortedType = onSelectSortedType,
                )
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun SubjectSortedTypePicker(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    sortedType: SubjectSortedType,
    onSelectSortedType: (SubjectSortedType) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var sortedTypeSelectorState by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .animateContentSize()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = { sortedTypeSelectorState = true },
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = sortedType.mapToString(InfoThemeRes.strings),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall,
        )
        Icon(
            painter = painterResource(StudyAssistantRes.icons.sortedType),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (sortedTypeSelectorState) {
        SortedTypePickerDialog(
            selected = sortedType,
            onDismiss = { sortedTypeSelectorState = false },
            onConfirm = {
                sortedTypeSelectorState = false
                onSelectSortedType(it)
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SortedTypePickerDialog(
    modifier: Modifier = Modifier,
    selected: SubjectSortedType,
    onConfirm: (SubjectSortedType) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedSortedType by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedSortedType,
        items = SubjectSortedType.entries,
        header = InfoThemeRes.strings.subjectSortedTypeSelectorHeader,
        title = InfoThemeRes.strings.subjectSortedTypeSelectorTitle,
        itemView = { sortedType ->
            SelectorDialogItemView(
                onClick = { selectedSortedType = sortedType },
                selected = sortedType == selectedSortedType,
                title = sortedType.mapToString(InfoThemeRes.strings),
                label = null,
            )
        },
        onDismiss = onDismiss,
        onConfirm = { sortedType ->
            if (sortedType != null) onConfirm(sortedType)
        },
    )
}