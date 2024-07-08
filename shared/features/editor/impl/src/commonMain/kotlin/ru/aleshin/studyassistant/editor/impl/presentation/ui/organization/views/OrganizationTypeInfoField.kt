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
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseSelectorDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogNotSelectedItemView
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
    var organizationTypeSelectorState by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { organizationTypeSelectorState = true },
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = type?.mapToSting(StudyAssistantRes.strings),
        label = EditorThemeRes.strings.orgTypeFieldLabel,
        placeholder = EditorThemeRes.strings.orgTypeFieldPlaceholder,
        infoIcon = painterResource(StudyAssistantRes.icons.organizationType),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = organizationTypeSelectorState,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    if (organizationTypeSelectorState) {
        OrganizationTypeSelectorDialog(
            selected = type,
            onDismiss = { organizationTypeSelectorState = false },
            onConfirm = { selectedEventType ->
                onSelected(selectedEventType)
                organizationTypeSelectorState = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OrganizationTypeSelectorDialog(
    modifier: Modifier = Modifier,
    selected: OrganizationType?,
    onDismiss: () -> Unit,
    onConfirm: (OrganizationType?) -> Unit,
) {
    var selectedType by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedType,
        items = OrganizationType.entries,
        header = EditorThemeRes.strings.orgTypeSelectorHeader,
        title = null,
        itemView = { type ->
            SelectorDialogItemView(
                onClick = { selectedType = type },
                selected = type == selectedType,
                title = type.mapToSting(StudyAssistantRes.strings),
                label = null,
            )
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedType == null,
                onClick = { selectedType = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}