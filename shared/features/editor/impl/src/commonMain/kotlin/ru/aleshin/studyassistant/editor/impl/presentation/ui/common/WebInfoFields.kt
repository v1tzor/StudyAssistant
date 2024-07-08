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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.common

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
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.dialog.ContactInfoEditorDialog
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 06.06.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun WebInfoFields(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    webs: List<ContactInfoUi>,
    onUpdate: (List<ContactInfoUi>) -> Unit,
) {
    var contactInfoEditorDialogState by remember { mutableStateOf(false) }
    var editableContactInfo by remember { mutableStateOf<ContactInfoUi?>(null) }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.height(61.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(StudyAssistantRes.icons.website),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val mainWeb = webs.getOrNull(0)
            val additionalWebs = webs.filter { it != mainWeb }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ClickableTextField(
                    onClick = {
                        editableContactInfo = mainWeb
                        contactInfoEditorDialogState = true
                    },
                    enabled = !isLoading,
                    modifier = Modifier.height(61.dp).weight(1f),
                    value = mainWeb?.value,
                    label = EditorThemeRes.strings.webFieldLabel,
                    placeholder = EditorThemeRes.strings.webFieldPlaceholder,
                    trailingIcon = {
                        ExpandedIcon(
                            isExpanded = contactInfoEditorDialogState,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    singleLine = true,
                )
                if (mainWeb != null) {
                    AddContactInfoItem(onClick = { contactInfoEditorDialogState = true })
                }
            }
            additionalWebs.forEach { additionalWeb ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ClickableTextField(
                        onClick = {
                            editableContactInfo = additionalWeb
                            contactInfoEditorDialogState = true
                        },
                        enabled = !isLoading,
                        modifier = Modifier.height(61.dp).weight(1f),
                        value = additionalWeb.value,
                        label = null,
                        placeholder = EditorThemeRes.strings.webFieldPlaceholder,
                        trailingIcon = {
                            ExpandedIcon(
                                isExpanded = contactInfoEditorDialogState,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        singleLine = true,
                    )
                    DeleteContactInfoItem(
                        onClick = {
                            val updatedWebs = webs.toMutableList().apply {
                                remove(additionalWeb)
                            }
                            onUpdate(updatedWebs)
                        }
                    )
                }
            }
        }
    }

    if (contactInfoEditorDialogState) {
        ContactInfoEditorDialog(
            header = EditorThemeRes.strings.webFieldLabel,
            label = editableContactInfo?.label,
            value = editableContactInfo?.value,
            onDismiss = {
                editableContactInfo = null
                contactInfoEditorDialogState = false
            },
            onConfirm = { label, value ->
                val web = (editableContactInfo ?: ContactInfoUi()).copy(
                    label = label,
                    value = value
                )
                val updatedWebs = webs.toMutableList().apply {
                    if (editableContactInfo != null) {
                        set(indexOf(editableContactInfo), web)
                    } else {
                        add(web)
                    }
                }
                onUpdate(updatedWebs)

                editableContactInfo = null
                contactInfoEditorDialogState = false
            },
            onDelete = {
                val updatedWebs = webs.toMutableList().apply {
                    if (editableContactInfo != null) remove(editableContactInfo)
                }
                onUpdate(updatedWebs)

                editableContactInfo = null
                contactInfoEditorDialogState = false
            }
        )
    }
}