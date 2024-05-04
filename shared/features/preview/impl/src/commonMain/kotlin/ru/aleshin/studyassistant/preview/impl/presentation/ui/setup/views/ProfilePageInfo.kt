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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import entities.users.Gender
import functional.Constants
import mappers.mapToSting
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.preview.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes
import theme.StudyAssistantRes
import views.BirthdayDatePicker
import views.ExpandedIcon
import views.GenderDropdownMenu

/**
 * @author Stanislav Aleshin on 27.04.2024
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun ProfilePageInfo(
    modifier: Modifier = Modifier,
    profile: AppUserUi,
    scrollState: ScrollState = rememberScrollState(),
    onUpdateProfile: (AppUserUi) -> Unit,
    onSetAvatar: () -> Unit,
) = with(profile){
    var isOpenDatePickerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SelectableAvatarView(
                onClick = onSetAvatar,
                username = profile.username,
                imageUrl = profile.avatar,
            )
        }
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var editableUsername by remember { mutableStateOf(TextFieldValue(username)) }
            var editableDescription by remember { mutableStateOf(TextFieldValue(description ?: "")) }

            InfoTextField(
                value = editableUsername,
                onValueChange = {
                    editableUsername = it
                    onUpdateProfile(profile.copy(username = it.text))
                },
                labelText = PreviewThemeRes.strings.usernameLabel,
                infoIcon = painterResource(PreviewThemeRes.icons.name),
            )
            InfoTextField(
                value = editableDescription,
                onValueChange = {
                    editableDescription = it
                    onUpdateProfile(profile.copy(description = it.text.ifEmpty { null }))
                },
                maxLength = Constants.Text.MAX_PROFILE_DESC_LENGTH,
                labelText = PreviewThemeRes.strings.profileDescriptionLabel,
                infoIcon = painterResource(PreviewThemeRes.icons.description),
                singleLine = false,
                maxLines = 4,
            )
            InfoTextField(
                enabled = false,
                value = email,
                onValueChange = {},
                labelText = PreviewThemeRes.strings.emailLabel,
                infoIcon = painterResource(PreviewThemeRes.icons.email),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                )
            )
            InfoTextField(
                enabled = false,
                value = profile.birthday,
                onValueChange = {},
                labelText = PreviewThemeRes.strings.birthdayLabel,
                infoIcon = painterResource(PreviewThemeRes.icons.birthday),
                trailingIcon = {
                    IconButton(onClick = { isOpenDatePickerDialog = true }) {
                        Icon(
                            painter = painterResource(PreviewThemeRes.icons.selectDate),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                }
            )
            var isExpandedGenderMenu by remember { mutableStateOf(false) }
            InfoTextField(
                enabled = false,
                value = profile.gender?.mapToSting(StudyAssistantRes.strings),
                onValueChange = {},
                labelText = PreviewThemeRes.strings.genderLabel,
                infoIcon = painterResource(PreviewThemeRes.icons.gender),
                trailingIcon = {
                    IconButton(onClick = { isExpandedGenderMenu = true }) {
                        ExpandedIcon(
                            isExpanded = isExpandedGenderMenu,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
        }
    }
    BirthdayDatePicker(
        isOpenDialog = isOpenDatePickerDialog,
        label = PreviewThemeRes.strings.birthdayLabel,
        onDismiss = { isOpenDatePickerDialog = false },
        onSelectedDate = { birthday ->
            onUpdateProfile(profile.copy(birthday = birthday))
            isOpenDatePickerDialog = false
        }
    )
}