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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mappers.mapToSting
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.preview.impl.presentation.models.ContactInfoUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.OrganizationUi
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes
import theme.StudyAssistantRes
import views.ExpandedIcon
import views.OrganizationTypeDropdownMenu

/**
 * @author Stanislav Aleshin on 27.04.2024
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun OrganizationPageInfo(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    organization: OrganizationUi,
    onUpdateOrganization: (OrganizationUi) -> Unit,
    onSetAvatar: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelectableAvatarView(
                onClick = onSetAvatar,
                username = organization.type.mapToSting(StudyAssistantRes.strings),
                imageUrl = organization.avatar,
                shape = RoundedCornerShape(32.dp),
            )
            VerticalInfoTextField(
                value = organization.shortName,
                onValueChange = { onUpdateOrganization(organization.copy(shortName = it)) },
                labelText = PreviewThemeRes.strings.shortNameLabel,
                placeholder = PreviewThemeRes.strings.shortNamePlaceholder,
                infoIcon = painterResource(PreviewThemeRes.icons.name),
            )
        }
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var isExpandedTypeMenu by remember { mutableStateOf(false) }

            InfoTextField(
                enabled = false,
                value = organization.type.mapToSting(StudyAssistantRes.strings),
                onValueChange = {},
                labelText = PreviewThemeRes.strings.organizationTypeLabel,
                infoIcon = painterResource(PreviewThemeRes.icons.organization),
                trailingIcon = {
                    IconButton(onClick = { isExpandedTypeMenu = true }) {
                        ExpandedIcon(
                            isExpanded = isExpandedTypeMenu,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OrganizationTypeDropdownMenu(
                        isExpanded = isExpandedTypeMenu,
                        selected = organization.type,
                        onDismiss = { isExpandedTypeMenu = false },
                        onSelect = { type ->
                            onUpdateOrganization(organization.copy(type = type))
                            isExpandedTypeMenu = false
                        }
                    )
                }
            )
            InfoTextField(
                value = organization.emails?.getOrElse(0) { ContactInfoUi() }?.value,
                onValueChange = {
                    val currentEmail = organization.emails.getOrElse(0) { ContactInfoUi() }
                    val emails = organization.emails.toMutableList().apply {
                        if (size != 0) set(0, currentEmail.copy(value = it)) else add(currentEmail)
                    }
                    onUpdateOrganization(organization.copy(emails = emails))
                },
                labelText = PreviewThemeRes.strings.emailLabel,
                infoIcon = painterResource(PreviewThemeRes.icons.email),
            )
            InfoTextField(
                value = organization.phones.getOrElse(0) { ContactInfoUi() }.value,
                onValueChange = {
                    val currentPhone = organization.phones.getOrElse(0) { ContactInfoUi() }
                    val phones = organization.phones.toMutableList().apply {
                        if (size != 0) set(0, currentPhone.copy(value = it)) else add(currentPhone)
                    }
                    onUpdateOrganization(organization.copy(phones = phones))
                },
                labelText = PreviewThemeRes.strings.phoneNumberLabel,
                infoIcon = painterResource(PreviewThemeRes.icons.phone),
            )
            InfoTextField(
                value = organization.webs.getOrElse(0) { ContactInfoUi() }.value,
                onValueChange = {
                    val currentWeb = organization.webs.getOrElse(0) { ContactInfoUi() }
                    val webs = organization.webs.toMutableList().apply {
                        if (size != 0) set(0, currentWeb.copy(value = it)) else add(currentWeb)
                    }
                    onUpdateOrganization(organization.copy(webs = webs))
                },
                labelText = PreviewThemeRes.strings.websiteLabel,
                infoIcon = painterResource(PreviewThemeRes.icons.website),
            )
        }
    }
}