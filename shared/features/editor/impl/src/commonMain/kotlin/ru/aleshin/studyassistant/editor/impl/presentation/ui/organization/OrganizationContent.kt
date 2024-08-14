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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.organization

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.core.PlatformFile
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.AvatarSection
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.EmailInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.LocationsInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.PhoneInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.WebInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.views.OrganizationNameInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.views.OrganizationStatusChooser
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.views.OrganizationTypeInfoField

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
@Composable
internal fun OrganizationContent(
    state: OrganizationViewState,
    modifier: Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onUpdateAvatar: (PlatformFile) -> Unit,
    onDeleteAvatar: () -> Unit,
    onExceedingAvatarSizeLimit: (Int) -> Unit,
    onSelectedType: (OrganizationType?) -> Unit,
    onUpdateName: (short: String?, full: String?) -> Unit,
    onUpdateEmails: (List<ContactInfoUi>) -> Unit,
    onUpdatePhones: (List<ContactInfoUi>) -> Unit,
    onUpdateWebs: (List<ContactInfoUi>) -> Unit,
    onUpdateLocations: (List<ContactInfoUi>) -> Unit,
    onStatusChange: (Boolean) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.fillMaxSize().padding(top = 24.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        AvatarSection(
            isLoading = isLoading,
            shortName = editableOrganization?.shortName,
            avatar = when (actionWithAvatar) {
                is ActionWithAvatar.None -> actionWithAvatar.uri
                is ActionWithAvatar.Set -> actionWithAvatar.uri
                is ActionWithAvatar.Delete -> null
            },
            onUpdateAvatar = onUpdateAvatar,
            onDeleteAvatar = onDeleteAvatar,
            onExceedingLimit = onExceedingAvatarSizeLimit,
        )
        OrganizationTypeInfoField(
            isLoading = isLoading,
            type = editableOrganization?.type,
            onSelected = onSelectedType,
        )
        OrganizationNameInfoField(
            isLoading = isLoading,
            shortName = editableOrganization?.shortName,
            fullName = editableOrganization?.fullName,
            onUpdateShortName = { onUpdateName(it, editableOrganization?.fullName) },
            onUpdateFullName = { onUpdateName(editableOrganization?.shortName, it) },
        )
        Column(
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = EditorThemeRes.strings.contactInfoSectionHeader,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall,
                )
                HorizontalDivider()
            }
            EmailInfoFields(
                isLoading = isLoading,
                emails = editableOrganization?.emails ?: emptyList(),
                onUpdate = onUpdateEmails,
            )
            PhoneInfoFields(
                isLoading = isLoading,
                phones = editableOrganization?.phones ?: emptyList(),
                onUpdate = onUpdatePhones,
            )
            WebInfoFields(
                isLoading = isLoading,
                webs = editableOrganization?.webs ?: emptyList(),
                onUpdate = onUpdateWebs,
            )
            LocationsInfoFields(
                isLoading = isLoading,
                locations = editableOrganization?.locations ?: emptyList(),
                onUpdate = onUpdateLocations,
            )
        }
        OrganizationStatusChooser(
            isLoading = isLoading,
            isMain = editableOrganization?.isMain ?: false,
            onStatusChange = onStatusChange,
        )
        Spacer(modifier = Modifier.height(56.dp))
    }
}