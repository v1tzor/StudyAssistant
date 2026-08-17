/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorAddItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorItemView
import ru.aleshin.studyassistant.core.ui.views.sheet.BaseSelectorBottomSheet
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_organization_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_organization_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_organization_selector_header
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_organization_selector_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_organization as core_ic_organization

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
internal fun ImportOrganizationField(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    selectedOrganization: OrganizationShortUi?,
    organizations: List<OrganizationShortUi>,
    onAddOrganization: () -> Unit,
    onSelected: (OrganizationShortUi?) -> Unit,
) {
    var isSelectorOpen by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { isSelectorOpen = true },
        modifier = modifier,
        enabled = enabled,
        value = selectedOrganization?.shortName,
        label = stringResource(Res.string.schedule_import_organization_label),
        placeholder = stringResource(Res.string.schedule_import_organization_placeholder),
        infoIcon = painterResource(CoreRes.drawable.core_ic_organization),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = isSelectorOpen,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    if (isSelectorOpen) {
        ImportOrganizationSelectorSheet(
            selected = selectedOrganization,
            organizations = organizations,
            onAddOrganization = onAddOrganization,
            onDismiss = { isSelectorOpen = false },
            onConfirm = { selected ->
                onSelected(selected)
                isSelectorOpen = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ImportOrganizationSelectorSheet(
    modifier: Modifier = Modifier,
    selected: OrganizationShortUi?,
    organizations: List<OrganizationShortUi>,
    onAddOrganization: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (OrganizationShortUi?) -> Unit,
) {
    var selectedOrganization by remember { mutableStateOf(selected) }

    BaseSelectorBottomSheet(
        modifier = modifier,
        selected = selectedOrganization,
        items = organizations,
        header = stringResource(Res.string.schedule_import_organization_selector_header),
        title = stringResource(Res.string.schedule_import_organization_selector_title),
        itemView = { item ->
            SelectorItemView(
                onClick = { selectedOrganization = item },
                selected = item.uid == selectedOrganization?.uid,
                title = item.shortName,
                label = item.type.mapToSting(),
            )
        },
        addItemView = {
            SelectorAddItemView(onClick = onAddOrganization)
        },
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
    )
}
