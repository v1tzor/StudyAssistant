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
import mappers.mapToSting
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import views.ClickableInfoTextField
import views.ExpandedIcon
import views.dialog.BaseSelectorDialog
import views.dialog.SelectorDialogAddItemView
import views.dialog.SelectorDialogItemView
import views.dialog.SelectorDialogNotSelectedItemView

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
internal fun OrganizationInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    organization: OrganizationShortUi?,
    allOrganization: List<OrganizationShortUi>,
    onAddOrganization: () -> Unit,
    onSelected: (OrganizationShortUi?) -> Unit,
) {
    var organizationSelectorState by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { organizationSelectorState = true },
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = organization?.shortName,
        label = EditorThemeRes.strings.organizationFieldLabel,
        placeholder = EditorThemeRes.strings.organizationFieldPlaceholder,
        infoIcon = painterResource(StudyAssistantRes.icons.organization),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = organizationSelectorState,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    if (organizationSelectorState) {
        OrganizationSelectorDialog(
            selected = organization,
            organizations = allOrganization,
            onAddOrganization = onAddOrganization,
            onDismiss = { organizationSelectorState = false },
            onConfirm = {
                onSelected(it)
                organizationSelectorState = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OrganizationSelectorDialog(
    modifier: Modifier = Modifier,
    selected: OrganizationShortUi?,
    organizations: List<OrganizationShortUi>,
    onAddOrganization: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (OrganizationShortUi?) -> Unit,
) {
    var selectedOrganization by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedOrganization,
        items = organizations,
        header = EditorThemeRes.strings.organizationSelectorHeader,
        title = EditorThemeRes.strings.organizationSelectorTitle,
        itemView = { organization ->
            SelectorDialogItemView(
                onClick = { selectedOrganization = organization },
                selected = organization.uid == selectedOrganization?.uid,
                title = organization.shortName,
                label = organization.type.mapToSting(StudyAssistantRes.strings),
            )
        },
        addItemView = {
            SelectorDialogAddItemView(onClick = onAddOrganization)
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedOrganization == null,
                onClick = { selectedOrganization = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}