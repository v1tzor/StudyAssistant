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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.core.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.VerticalInfoTextField
import ru.aleshin.studyassistant.core.ui.views.dialog.ContactInfoEditorDialog
import ru.aleshin.studyassistant.core.ui.views.menu.OrganizationTypeDropdownMenu
import ru.aleshin.studyassistant.core.ui.views.menu.SelectableAvatarView
import ru.aleshin.studyassistant.preview.impl.resources.Res
import ru.aleshin.studyassistant.preview.impl.resources.email_label
import ru.aleshin.studyassistant.preview.impl.resources.ic_textbox
import ru.aleshin.studyassistant.preview.impl.resources.organization_type_label
import ru.aleshin.studyassistant.preview.impl.resources.organization_type_placeholder
import ru.aleshin.studyassistant.preview.impl.resources.phone_number_label
import ru.aleshin.studyassistant.preview.impl.resources.short_name_label
import ru.aleshin.studyassistant.preview.impl.resources.short_name_placeholder
import ru.aleshin.studyassistant.preview.impl.resources.website_label
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_email as core_ic_email
import ru.aleshin.studyassistant.core.ui.resources.ic_organization_type as core_ic_organization_type
import ru.aleshin.studyassistant.core.ui.resources.ic_phone as core_ic_phone
import ru.aleshin.studyassistant.core.ui.resources.ic_web as core_ic_web

/**
 * @author Stanislav Aleshin on 27.04.2024
 */
@Composable
internal fun OrganizationPageInfo(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    organization: OrganizationUi,
    avatar: String?,
    onUpdateOrganization: (OrganizationUi) -> Unit,
    onUpdateAvatar: (PlatformFile) -> Unit,
    onDeleteAvatar: () -> Unit,
    onExceedingLimit: (Int) -> Unit,
) = with(organization) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val focusManager = LocalFocusManager.current
            var editableShortName by remember { mutableStateOf(TextFieldValue(shortName)) }
            val shortNameInteraction = remember { MutableInteractionSource() }

            SelectableAvatarView(
                onSelect = onUpdateAvatar,
                onDelete = onDeleteAvatar,
                onExceedingLimit = onExceedingLimit,
                modifier = Modifier.size(90.dp),
                firstName = organization.shortName.split(' ').getOrNull(0) ?: "*",
                secondName = organization.shortName.split(' ').getOrNull(1),
                imageUrl = avatar,
                shape = RoundedCornerShape(32.dp),
                style = MaterialTheme.typography.displaySmall,
            )
            VerticalInfoTextField(
                value = editableShortName,
                onValueChange = {
                    editableShortName = it
                    onUpdateOrganization(organization.copy(shortName = it.text))
                },
                labelText = stringResource(Res.string.short_name_label),
                placeholder = stringResource(Res.string.short_name_placeholder),
                infoIcon = painterResource(Res.drawable.ic_textbox),
                trailingIcon = if (shortNameInteraction.collectIsFocusedAsState().value) {
                    {
                        IconButton(onClick = { focusManager.clearFocus() }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StudyAssistantRes.colors.accents.green,
                            )
                        }
                    }
                } else {
                    null
                },
                interactionSource = shortNameInteraction,
            )
        }
        Column(
            modifier = Modifier.verticalScroll(state = scrollState, overscrollEffect = null),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var isExpandedTypeMenu by remember { mutableStateOf(false) }
            var emailInfoEditorDialogState by remember { mutableStateOf(false) }
            var phoneInfoEditorDialogState by remember { mutableStateOf(false) }
            var webInfoEditorDialogState by remember { mutableStateOf(false) }

            ClickableInfoTextField(
                onClick = { isExpandedTypeMenu = true },
                value = type.mapToSting(),
                label = stringResource(Res.string.organization_type_label),
                placeholder = stringResource(Res.string.organization_type_placeholder),
                infoIcon = painterResource(CoreRes.drawable.core_ic_organization_type),
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isExpandedTypeMenu,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OrganizationTypeDropdownMenu(
                        isExpanded = isExpandedTypeMenu,
                        selected = type,
                        onDismiss = { isExpandedTypeMenu = false },
                        onSelect = { type ->
                            onUpdateOrganization(copy(type = type))
                            isExpandedTypeMenu = false
                        }
                    )
                }
            )

            ClickableInfoTextField(
                onClick = { emailInfoEditorDialogState = true },
                value = emails.getOrNull(0)?.value,
                label = stringResource(Res.string.email_label),
                placeholder = stringResource(Res.string.email_label),
                infoIcon = painterResource(CoreRes.drawable.core_ic_email),
            )
            if (emailInfoEditorDialogState) {
                ContactInfoEditorDialog(
                    header = stringResource(Res.string.email_label),
                    label = emails.getOrNull(0)?.label,
                    value = emails.getOrNull(0)?.value,
                    onDismiss = { emailInfoEditorDialogState = false },
                    onConfirm = { label, value ->
                        val email = (emails.getOrNull(0) ?: ContactInfoUi()).copy(
                            label = label,
                            value = value
                        )
                        val updatedEmails = emails.toMutableList().apply {
                            if (size != 0) set(0, email) else add(email)
                        }
                        onUpdateOrganization(organization.copy(emails = updatedEmails))

                        emailInfoEditorDialogState = false
                    },
                    onDelete = {
                        val updatedEmails = emails.toMutableList().apply {
                            if (size != 0) removeAt(0)
                        }
                        onUpdateOrganization(organization.copy(emails = updatedEmails))

                        emailInfoEditorDialogState = false
                    }
                )
            }

            ClickableInfoTextField(
                onClick = { phoneInfoEditorDialogState = true },
                value = phones.getOrNull(0)?.value,
                label = stringResource(Res.string.phone_number_label),
                placeholder = stringResource(Res.string.phone_number_label),
                infoIcon = painterResource(CoreRes.drawable.core_ic_phone),
            )
            if (phoneInfoEditorDialogState) {
                ContactInfoEditorDialog(
                    header = stringResource(Res.string.phone_number_label),
                    label = phones.getOrNull(0)?.label,
                    value = phones.getOrNull(0)?.value,
                    onDismiss = { phoneInfoEditorDialogState = false },
                    onConfirm = { label, value ->
                        val phone = (phones.getOrNull(0) ?: ContactInfoUi()).copy(
                            label = label,
                            value = value
                        )
                        val updatedPhones = phones.toMutableList().apply {
                            if (size != 0) set(0, phone) else add(phone)
                        }
                        onUpdateOrganization(organization.copy(phones = updatedPhones))

                        phoneInfoEditorDialogState = false
                    },
                    onDelete = {
                        val updatedPhones = phones.toMutableList().apply {
                            if (size != 0) removeAt(0)
                        }
                        onUpdateOrganization(organization.copy(phones = updatedPhones))

                        phoneInfoEditorDialogState = false
                    }
                )
            }

            ClickableInfoTextField(
                onClick = { webInfoEditorDialogState = true },
                value = webs.getOrNull(0)?.value,
                label = stringResource(Res.string.website_label),
                placeholder = stringResource(Res.string.website_label),
                infoIcon = painterResource(CoreRes.drawable.core_ic_web),
            )
            if (webInfoEditorDialogState) {
                ContactInfoEditorDialog(
                    header = stringResource(Res.string.website_label),
                    label = webs.getOrNull(0)?.label,
                    value = webs.getOrNull(0)?.value,
                    onDismiss = { webInfoEditorDialogState = false },
                    onConfirm = { label, value ->
                        val web = (webs.getOrNull(0) ?: ContactInfoUi()).copy(
                            label = label,
                            value = value
                        )
                        val updatedWebs = webs.toMutableList().apply {
                            if (size != 0) set(0, web) else add(web)
                        }
                        onUpdateOrganization(organization.copy(webs = updatedWebs))

                        webInfoEditorDialogState = false
                    },
                    onDelete = {
                        val updatedWebs = webs.toMutableList().apply {
                            if (size != 0) removeAt(0)
                        }
                        onUpdateOrganization(organization.copy(webs = updatedWebs))

                        webInfoEditorDialogState = false
                    }
                )
            }
        }
    }
}
