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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseCheckedDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.CheckedItemView
import ru.aleshin.studyassistant.settings.impl.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes

/**
 * @author Stanislav Aleshin on 25.08.2024.
 */
@Composable
internal fun ExceptionOrganizationsChip(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    exceptions: List<UID>,
    allOrganizations: List<OrganizationShortUi>,
    onUpdateExceptions: (List<UID>) -> Unit,
) {
    var exceptionOrganizationsSelectorState by remember { mutableStateOf(false) }

    AssistChip(
        onClick = { exceptionOrganizationsSelectorState = true },
        label = {
            Text(
                text = buildString {
                    if (exceptions.isNotEmpty()) {
                        append(SettingsThemeRes.strings.exceptionsChipLabelPrefix)
                        append(exceptions.size)
                    } else {
                        append(SettingsThemeRes.strings.addExceptionChipLabel)
                    }
                },
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        enabled = enabled,
        modifier = modifier,
        leadingIcon = {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(StudyAssistantRes.icons.organizationRemove),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        trailingIcon = if (exceptions.isNotEmpty()) {
            { ExpandedIcon(isExpanded = exceptionOrganizationsSelectorState) }
        } else {
            null
        },
    )

    if (exceptionOrganizationsSelectorState) {
        OrganizationSelectorDialog(
            selected = exceptions.map { exception ->
                allOrganizations.find { it.uid == exception } ?: OrganizationShortUi(
                    uid = exception,
                    shortName = "-",
                )
            },
            allOrganizations = allOrganizations,
            onDismiss = { exceptionOrganizationsSelectorState = false },
            onConfirm = { selectedOrganizations ->
                onUpdateExceptions(selectedOrganizations.map { it.uid })
                exceptionOrganizationsSelectorState = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OrganizationSelectorDialog(
    modifier: Modifier = Modifier,
    selected: List<OrganizationShortUi>,
    allOrganizations: List<OrganizationShortUi>,
    onDismiss: () -> Unit,
    onConfirm: (List<OrganizationShortUi>) -> Unit,
) {
    var selectedOrganization by remember { mutableStateOf(selected) }

    BaseCheckedDialog(
        modifier = modifier,
        selected = selectedOrganization,
        items = allOrganizations,
        header = SettingsThemeRes.strings.exceptionOrganizationsDialogHeader,
        title = SettingsThemeRes.strings.exceptionOrganizationsDialogTitle,
        itemView = { organization ->
            val isSelected = selectedOrganization.contains(organization)
            CheckedItemView(
                onClick = {
                    selectedOrganization = if (isSelected) {
                        selectedOrganization.toMutableList().apply { remove(organization) }
                    } else {
                        selectedOrganization.toMutableList().apply { add(organization) }
                    }
                },
                selected = isSelected,
                title = organization.shortName,
                label = organization.type.mapToSting(StudyAssistantRes.strings),
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}