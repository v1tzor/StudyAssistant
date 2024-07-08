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

package ru.aleshin.studyassistant.info.impl.presentation.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseSelectorDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogItemView
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes

/**
 * @author Stanislav Aleshin on 19.06.2024.
 */
@Composable
internal fun OrganizationPicker(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedOrganization: UID?,
    allOrganizations: List<OrganizationShortUi>,
    onSelectOrganization: (OrganizationShortUi) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val organization by derivedStateOf { allOrganizations.find { it.uid == selectedOrganization } }
    var organizationSelectorState by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .animateContentSize()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = { organizationSelectorState = true },
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(StudyAssistantRes.icons.organizationGeo),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = organization?.shortName ?: StudyAssistantRes.strings.noneTitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall,
        )
        ExpandedIcon(
            isExpanded = organizationSelectorState,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (organizationSelectorState) {
        OrganizationSelectorDialog(
            selected = organization,
            organizations = allOrganizations,
            onDismiss = { organizationSelectorState = false },
            onConfirm = {
                organizationSelectorState = false
                onSelectOrganization(it)
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OrganizationSelectorDialog(
    modifier: Modifier = Modifier,
    selected: OrganizationShortUi?,
    organizations: List<OrganizationShortUi>,
    onDismiss: () -> Unit,
    onConfirm: (OrganizationShortUi) -> Unit,
) {
    var selectedOrganization by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedOrganization,
        items = organizations,
        header = InfoThemeRes.strings.organizationSelectorHeader,
        title = InfoThemeRes.strings.organizationSelectorTitle,
        itemView = { organization ->
            SelectorDialogItemView(
                onClick = { selectedOrganization = organization },
                selected = organization.uid == selectedOrganization?.uid,
                title = organization.shortName,
                label = organization.type.mapToSting(StudyAssistantRes.strings),
            )
        },
        onDismiss = onDismiss,
        onConfirm = { organization ->
            if (organization != null) onConfirm(organization)
        },
    )
}