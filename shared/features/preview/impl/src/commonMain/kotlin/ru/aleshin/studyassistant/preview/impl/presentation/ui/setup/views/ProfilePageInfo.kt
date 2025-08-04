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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.core.PlatformFile
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.FreeOrPaidContent
import ru.aleshin.studyassistant.core.ui.views.GenderDropdownMenu
import ru.aleshin.studyassistant.core.ui.views.InfoTextField
import ru.aleshin.studyassistant.core.ui.views.dialog.BirthdayDatePicker
import ru.aleshin.studyassistant.core.ui.views.menu.ClickableAvatarView
import ru.aleshin.studyassistant.core.ui.views.menu.SelectableAvatarView
import ru.aleshin.studyassistant.preview.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes

/**
 * @author Stanislav Aleshin on 27.04.2024
 */
@Composable
internal fun ProfilePageInfo(
    modifier: Modifier = Modifier,
    profile: AppUserUi,
    isPaidUser: Boolean,
    avatar: String?,
    scrollState: ScrollState = rememberScrollState(),
    onUpdateProfile: (AppUserUi) -> Unit,
    onUpdateAvatar: (PlatformFile) -> Unit,
    onDeleteAvatar: () -> Unit,
    onExceedingLimit: (Int) -> Unit,
    onOpenBillingScreen: () -> Unit,
) = with(profile) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FreeOrPaidContent(
                isPaidUser = isPaidUser,
                modifier = modifier,
                paidContent = {
                    SelectableAvatarView(
                        onSelect = onUpdateAvatar,
                        onDelete = onDeleteAvatar,
                        onExceedingLimit = onExceedingLimit,
                        modifier = Modifier.size(90.dp),
                        firstName = profile.username.split(' ').getOrNull(0) ?: "-",
                        secondName = profile.username.split(' ').getOrNull(1),
                        imageUrl = avatar,
                        style = MaterialTheme.typography.displaySmall,
                    )
                },
                freeContent = {
                    ClickableAvatarView(
                        onClick = onOpenBillingScreen,
                        modifier = Modifier.size(90.dp),
                        imageUrl = avatar,
                        sideIcon = {
                            Icon(
                                modifier = Modifier.clip(MaterialTheme.shapes.full),
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        firstName = username.split(' ').getOrNull(0) ?: "-",
                        secondName = username.split(' ').getOrNull(1),
                        style = MaterialTheme.typography.displaySmall,
                        iconOffset = DpOffset((-4).dp, (-4).dp),
                    )
                },
            )
        }
        Column(
            modifier = Modifier.verticalScroll(state = scrollState, overscrollEffect = null),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val focusManager = LocalFocusManager.current
            var isExpandedGenderMenu by remember { mutableStateOf(false) }
            var datePickerDialogState by remember { mutableStateOf(false) }
            var editableUsername by remember { mutableStateOf(TextFieldValue(username)) }
            var editableDescription by remember { mutableStateOf(TextFieldValue(description ?: "")) }
            val usernameInteraction = remember { MutableInteractionSource() }
            val descriptionInteraction = remember { MutableInteractionSource() }

            InfoTextField(
                value = editableUsername,
                onValueChange = {
                    editableUsername = it
                    onUpdateProfile(profile.copy(username = it.text))
                },
                label = PreviewThemeRes.strings.usernameLabel,
                leadingInfoIcon = painterResource(PreviewThemeRes.icons.name),
                trailingIcon = if (usernameInteraction.collectIsFocusedAsState().value) { {
                    IconButton(onClick = { focusManager.clearFocus() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.green,
                        )
                    }
                } } else {
                    null
                },
                interactionSource = usernameInteraction,
            )
            InfoTextField(
                value = editableDescription,
                onValueChange = {
                    editableDescription = it
                    onUpdateProfile(profile.copy(description = it.text.ifEmpty { null }))
                },
                maxLength = Constants.Text.MAX_PROFILE_DESC_LENGTH,
                label = PreviewThemeRes.strings.profileDescriptionLabel,
                leadingInfoIcon = painterResource(StudyAssistantRes.icons.userDescription),
                trailingIcon = if (descriptionInteraction.collectIsFocusedAsState().value) {{
                    IconButton(onClick = { focusManager.clearFocus() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.green,
                        )
                    }
                } } else {
                    null
                },
                singleLine = false,
                maxLines = 4,
                interactionSource = descriptionInteraction,
            )
            InfoTextField(
                enabled = false,
                value = email,
                onValueChange = {},
                label = PreviewThemeRes.strings.emailLabel,
                leadingInfoIcon = painterResource(StudyAssistantRes.icons.email),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                )
            )

            ClickableInfoTextField(
                value = profile.birthday,
                onClick = { datePickerDialogState = true },
                label = PreviewThemeRes.strings.birthdayLabel,
                placeholder = PreviewThemeRes.strings.birthdayPlaceholder,
                infoIcon = painterResource(StudyAssistantRes.icons.birthday),
                trailingIcon = {
                    Icon(
                        painter = painterResource(StudyAssistantRes.icons.selectDate),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            )
            if (datePickerDialogState) {
                BirthdayDatePicker(
                    label = PreviewThemeRes.strings.birthdayLabel,
                    onDismiss = { datePickerDialogState = false },
                    onSelectedDate = { birthday ->
                        onUpdateProfile(profile.copy(birthday = birthday))
                        datePickerDialogState = false
                    }
                )
            }

            ClickableInfoTextField(
                value = profile.gender?.mapToSting(StudyAssistantRes.strings),
                onClick = { isExpandedGenderMenu = true },
                label = PreviewThemeRes.strings.genderLabel,
                placeholder = PreviewThemeRes.strings.genderPlaceholder,
                infoIcon = painterResource(StudyAssistantRes.icons.gender),
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isExpandedGenderMenu,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GenderDropdownMenu(
                        isExpanded = isExpandedGenderMenu,
                        selected = profile.gender ?: Gender.NONE,
                        onDismiss = { isExpandedGenderMenu = false },
                        onSelect = { gender ->
                            onUpdateProfile(profile.copy(gender = gender))
                            isExpandedGenderMenu = false
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}