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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.ContactInfoEditorDialog
import theme.StudyAssistantRes
import views.ClickableTextField
import views.ExpandedIcon

/**
 * @author Stanislav Aleshin on 06.06.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun PhoneInfoFields(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    phones: List<ContactInfoUi>,
    onUpdate: (List<ContactInfoUi>) -> Unit,
) {
    var isOpenContactInfoEditorDialog by remember { mutableStateOf(false) }
    var editableContactInfo by remember { mutableStateOf<ContactInfoUi?>(null) }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.height(61.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(StudyAssistantRes.icons.phone),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val mainPhone = phones.getOrNull(0)
            val additionalPhones = phones.filter { it != mainPhone }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ClickableTextField(
                    onClick = {
                        editableContactInfo = mainPhone
                        isOpenContactInfoEditorDialog = true
                    },
                    enabled = !isLoading,
                    modifier = Modifier.height(61.dp).weight(1f),
                    value = mainPhone?.value,
                    label = EditorThemeRes.strings.phoneFieldLabel,
                    placeholder = EditorThemeRes.strings.phoneFieldPlaceholder,
                    trailingIcon = {
                        ExpandedIcon(
                            isExpanded = isOpenContactInfoEditorDialog,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    singleLine = true,
                )
                if (mainPhone != null) {
                    AddContactInfoItem(onClick = { isOpenContactInfoEditorDialog = true })
                }
            }
            additionalPhones.forEach { additionalPhone ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ClickableTextField(
                        onClick = {
                            editableContactInfo = additionalPhone
                            isOpenContactInfoEditorDialog = true
                        },
                        enabled = !isLoading,
                        modifier = Modifier.height(61.dp).weight(1f),
                        value = additionalPhone.value,
                        label = null,
                        placeholder = EditorThemeRes.strings.phoneFieldPlaceholder,
                        trailingIcon = {
                            ExpandedIcon(
                                isExpanded = isOpenContactInfoEditorDialog,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        singleLine = true,
                    )
                    DeleteContactInfoItem(
                        onClick = {
                            val updatedPhones = phones.toMutableList().apply {
                                remove(additionalPhone)
                            }
                            onUpdate(updatedPhones)
                        }
                    )
                }
            }
        }
    }

    if (isOpenContactInfoEditorDialog) {
        ContactInfoEditorDialog(
            header = EditorThemeRes.strings.phoneFieldLabel,
            contactInfo = editableContactInfo,
            onDismiss = {
                editableContactInfo = null
                isOpenContactInfoEditorDialog = false
            },
            onConfirm = { phone ->
                val updatedPhones = phones.toMutableList().apply {
                    if (editableContactInfo != null) {
                        set(indexOf(editableContactInfo), phone)
                    } else {
                        add(phone)
                    }
                }
                onUpdate(updatedPhones)
                editableContactInfo = null
                isOpenContactInfoEditorDialog = false
            },
            onDelete = {
                val updatedPhones = phones.toMutableList().apply {
                    if (editableContactInfo != null) remove(editableContactInfo)
                }
                onUpdate(updatedPhones)
                editableContactInfo = null
                isOpenContactInfoEditorDialog = false
            }
        )
    }
}
