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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.share_organization_main_label
import ru.aleshin.studyassistant.schedule.impl.resources.share_organizations_empty
import ru.aleshin.studyassistant.schedule.impl.resources.share_organizations_title

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
@Composable
internal fun ShareOrganizationSelector(
    modifier: Modifier = Modifier,
    organizations: List<OrganizationShortUi>,
    selectedOrganizationIds: Set<UID>,
    isLoading: Boolean,
    onToggleOrganization: (UID) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.share_organizations_title),
            style = MaterialTheme.typography.titleMedium,
        )
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                PlaceholderBox(
                    modifier = Modifier.height(40.dp).fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )
            }
            organizations.isEmpty() -> Text(
                text = stringResource(Res.string.share_organizations_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            else -> organizations.forEach { organization ->
                key(organization.uid) {
                    ShareOrganizationCheckRow(
                        organization = organization,
                        checked = organization.uid in selectedOrganizationIds,
                        onToggle = { onToggleOrganization(organization.uid) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareOrganizationCheckRow(
    modifier: Modifier = Modifier,
    organization: OrganizationShortUi,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onToggle,
        shape = MaterialTheme.shapes.large,
        color = if (checked) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = organization.shortName,
                    color = if (checked) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (organization.isMain) {
                    Text(
                        text = stringResource(Res.string.share_organization_main_label),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Checkbox(
                modifier = Modifier.size(24.dp),
                checked = checked,
                onCheckedChange = null,
            )
        }
    }
}
