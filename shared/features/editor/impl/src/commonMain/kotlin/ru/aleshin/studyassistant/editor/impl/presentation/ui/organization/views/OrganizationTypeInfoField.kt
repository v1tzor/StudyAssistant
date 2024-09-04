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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorNotSelectedItemView
import ru.aleshin.studyassistant.core.ui.views.sheet.BaseSelectorBottomSheet
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
internal fun OrganizationTypeInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    type: OrganizationType?,
    onSelected: (OrganizationType?) -> Unit,
) {
    var openOrganizationTypeSelector by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { openOrganizationTypeSelector = true },
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = type?.mapToSting(StudyAssistantRes.strings),
        label = EditorThemeRes.strings.orgTypeFieldLabel,
        placeholder = EditorThemeRes.strings.orgTypeFieldPlaceholder,
        infoIcon = painterResource(StudyAssistantRes.icons.organizationType),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = openOrganizationTypeSelector,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    if (openOrganizationTypeSelector) {
        OrganizationTypeSelectorBottomSheet(
            selected = type,
            onDismiss = { openOrganizationTypeSelector = false },
            onConfirm = { selectedEventType ->
                onSelected(selectedEventType)
                openOrganizationTypeSelector = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OrganizationTypeSelectorBottomSheet(
    modifier: Modifier = Modifier,
    selected: OrganizationType?,
    onDismiss: () -> Unit,
    onConfirm: (OrganizationType?) -> Unit,
) {
    var selectedType by remember { mutableStateOf(selected) }

    BaseSelectorBottomSheet(
        modifier = modifier,
        selected = selectedType,
        items = OrganizationType.entries,
        header = EditorThemeRes.strings.orgTypeSelectorHeader,
        title = null,
        itemView = { type ->
            SelectorItemView(
                onClick = { selectedType = type },
                selected = type == selectedType,
                title = type.mapToSting(StudyAssistantRes.strings),
                label = null,
            )
        },
        notSelectedItem = {
            SelectorNotSelectedItemView(
                selected = selectedType == null,
                onClick = { selectedType = null },
            )
        },
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
    )
}