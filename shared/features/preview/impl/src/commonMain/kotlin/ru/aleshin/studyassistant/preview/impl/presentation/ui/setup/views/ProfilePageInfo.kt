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
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.GenderDropdownMenu
import ru.aleshin.studyassistant.core.ui.views.InfoTextField
import ru.aleshin.studyassistant.core.ui.views.dialog.BirthdayDatePicker
import ru.aleshin.studyassistant.core.ui.views.menu.SelectableAvatarView
import ru.aleshin.studyassistant.preview.impl.resources.Res
import ru.aleshin.studyassistant.preview.impl.resources.birthday_label
import ru.aleshin.studyassistant.preview.impl.resources.birthday_placeholder
import ru.aleshin.studyassistant.preview.impl.resources.gender_label
import ru.aleshin.studyassistant.preview.impl.resources.gender_placeholder
import ru.aleshin.studyassistant.preview.impl.resources.ic_textbox
import ru.aleshin.studyassistant.preview.impl.resources.username_label
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_birthday as core_ic_birthday
import ru.aleshin.studyassistant.core.ui.resources.ic_gender as core_ic_gender
import ru.aleshin.studyassistant.core.ui.resources.ic_select_date as core_ic_select_date

/**
 * @author Stanislav Aleshin on 27.04.2024
 */
@Composable
internal fun ProfilePageInfo(
    modifier: Modifier = Modifier,
    profile: ProfileUi,
    avatar: String?,
    scrollState: ScrollState = rememberScrollState(),
    onUpdateProfile: (ProfileUi) -> Unit,
    onUpdateAvatar: (PlatformFile) -> Unit,
    onDeleteAvatar: () -> Unit,
    onExceedingLimit: (Int) -> Unit,
) = with(profile) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
        }
        Column(
            modifier = Modifier.verticalScroll(state = scrollState, overscrollEffect = null),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val focusManager = LocalFocusManager.current
            var isExpandedGenderMenu by remember { mutableStateOf(false) }
            var datePickerDialogState by remember { mutableStateOf(false) }
            var editableUsername by remember { mutableStateOf(TextFieldValue(username)) }
            val usernameInteraction = remember { MutableInteractionSource() }

            InfoTextField(
                value = editableUsername,
                onValueChange = {
                    editableUsername = it
                    onUpdateProfile(profile.copy(username = it.text))
                },
                label = stringResource(Res.string.username_label),
                leadingInfoIcon = painterResource(Res.drawable.ic_textbox),
                trailingIcon = if (usernameInteraction.collectIsFocusedAsState().value) {
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
                interactionSource = usernameInteraction,
            )
            ClickableInfoTextField(
                value = profile.birthday,
                onClick = { datePickerDialogState = true },
                label = stringResource(Res.string.birthday_label),
                placeholder = stringResource(Res.string.birthday_placeholder),
                infoIcon = painterResource(CoreRes.drawable.core_ic_birthday),
                trailingIcon = {
                    Icon(
                        painter = painterResource(CoreRes.drawable.core_ic_select_date),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            )
            if (datePickerDialogState) {
                BirthdayDatePicker(
                    label = stringResource(Res.string.birthday_label),
                    onDismiss = { datePickerDialogState = false },
                    onSelectedDate = { birthday ->
                        onUpdateProfile(profile.copy(birthday = birthday))
                        datePickerDialogState = false
                    }
                )
            }

            ClickableInfoTextField(
                value = profile.gender?.mapToSting(),
                onClick = { isExpandedGenderMenu = true },
                label = stringResource(Res.string.gender_label),
                placeholder = stringResource(Res.string.gender_placeholder),
                infoIcon = painterResource(CoreRes.drawable.core_ic_gender),
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
