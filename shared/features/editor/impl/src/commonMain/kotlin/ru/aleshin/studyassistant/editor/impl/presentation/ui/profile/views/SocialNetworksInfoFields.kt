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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.domain.entities.users.SocialNetworkType
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableTextField
import ru.aleshin.studyassistant.core.ui.views.DialogButtons
import ru.aleshin.studyassistant.core.ui.views.DialogHeader
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorNotSelectedItemView
import ru.aleshin.studyassistant.core.ui.views.sheet.BaseSelectorBottomSheet
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.SocialNetworkUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.AddContactInfoItem
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.DeleteContactInfoItem

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
@Composable
internal fun SocialNetworksInfoFields(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    socialNetworks: List<SocialNetworkUi>,
    onUpdateSocialNetworks: (List<SocialNetworkUi>) -> Unit,
) {
    var socialNetworkEditorDialogState by remember { mutableStateOf(false) }
    var editableSocialNetwork by remember { mutableStateOf<SocialNetworkUi?>(null) }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.height(61.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.AutoMirrored.Filled.Message,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val mainSocialNetwork = socialNetworks.getOrNull(0)
            val additionalSocialNetworks = socialNetworks.filter { it != mainSocialNetwork }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ClickableTextField(
                    onClick = {
                        editableSocialNetwork = mainSocialNetwork
                        socialNetworkEditorDialogState = true
                    },
                    enabled = !isLoading,
                    modifier = Modifier.height(61.dp).weight(1f),
                    value = mainSocialNetwork?.data,
                    label = mainSocialNetwork?.type?.mapToString(
                        strings = StudyAssistantRes.strings
                    ) ?: EditorThemeRes.strings.socialNetworkLabel,
                    placeholder = EditorThemeRes.strings.socialNetworkPlaceholder,
                    trailingIcon = {
                        ExpandedIcon(
                            isExpanded = socialNetworkEditorDialogState,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    singleLine = true,
                )
                if (mainSocialNetwork != null) {
                    AddContactInfoItem(onClick = { socialNetworkEditorDialogState = true })
                }
            }
            additionalSocialNetworks.forEach { additionalSocialNetwork ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ClickableTextField(
                        onClick = {
                            editableSocialNetwork = additionalSocialNetwork
                            socialNetworkEditorDialogState = true
                        },
                        enabled = !isLoading,
                        modifier = Modifier.height(61.dp).weight(1f),
                        value = additionalSocialNetwork.data,
                        label = additionalSocialNetwork.type.mapToString(StudyAssistantRes.strings),
                        placeholder = EditorThemeRes.strings.socialNetworkPlaceholder,
                        trailingIcon = {
                            ExpandedIcon(
                                isExpanded = socialNetworkEditorDialogState,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        singleLine = true,
                    )
                    DeleteContactInfoItem(
                        onClick = {
                            val updatedEmails = socialNetworks.toMutableList().apply {
                                remove(additionalSocialNetwork)
                            }
                            onUpdateSocialNetworks(updatedEmails)
                        }
                    )
                }
            }
        }
    }

    if (socialNetworkEditorDialogState) {
        SocialNetworkEditorDialog(
            socialNetwork = editableSocialNetwork,
            onDismiss = {
                editableSocialNetwork = null
                socialNetworkEditorDialogState = false
            },
            onConfirm = { socialNetwork ->
                val updatedSocialNetwork = socialNetworks.toMutableList().apply {
                    if (editableSocialNetwork != null) {
                        set(indexOf(editableSocialNetwork), socialNetwork)
                    } else {
                        add(socialNetwork)
                    }
                }
                onUpdateSocialNetworks(updatedSocialNetwork)
                editableSocialNetwork = null
                socialNetworkEditorDialogState = false
            },
            onDelete = {
                val updatedSocialNetwork = socialNetworks.toMutableList().apply {
                    remove(editableSocialNetwork)
                }
                onUpdateSocialNetworks(updatedSocialNetwork)
                editableSocialNetwork = null
                socialNetworkEditorDialogState = false
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SocialNetworkEditorDialog(
    modifier: Modifier = Modifier,
    socialNetwork: SocialNetworkUi?,
    onDelete: () -> Unit,
    onConfirm: (SocialNetworkUi) -> Unit,
    onDismiss: () -> Unit,
) {
    var editableService by remember { mutableStateOf(socialNetwork?.type) }
    var editableServiceName by remember { mutableStateOf(socialNetwork?.otherType) }
    var editableData by remember { mutableStateOf(socialNetwork?.data) }
    var openSocialNetworkTypeSelectorSheet by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.width(312.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(header = EditorThemeRes.strings.socialNetworkLabel)
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ClickableTextField(
                        onClick = { openSocialNetworkTypeSelectorSheet = true },
                        modifier = modifier.fillMaxWidth(),
                        value = editableService?.mapToString(StudyAssistantRes.strings),
                        label = EditorThemeRes.strings.socialNetworkServiceLabel,
                        placeholder = EditorThemeRes.strings.socialNetworkServicePlaceholder,
                        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                        singleLine = true,
                    )

                    if (openSocialNetworkTypeSelectorSheet) {
                        var selectedType by remember { mutableStateOf(editableService) }
                        BaseSelectorBottomSheet(
                            modifier = modifier,
                            selected = selectedType,
                            items = SocialNetworkType.entries,
                            header = EditorThemeRes.strings.socialNetworkServiceLabel,
                            title = null,
                            itemView = { socialNetworkType ->
                                SelectorItemView(
                                    onClick = { selectedType = socialNetworkType },
                                    selected = socialNetworkType == selectedType,
                                    label = null,
                                    title = socialNetworkType.mapToString(StudyAssistantRes.strings),
                                    leadingIcon = {
                                        if (socialNetworkType != SocialNetworkType.OTHER) {
                                            Image(
                                                modifier = Modifier.size(24.dp),
                                                painter = painterResource(
                                                    socialNetworkType.mapToIcon(StudyAssistantRes.icons)
                                                ),
                                                contentDescription = null,
                                            )
                                        } else {
                                            Icon(
                                                modifier = Modifier.size(24.dp),
                                                painter = painterResource(StudyAssistantRes.icons.otherSocialNetwork),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    },
                                )
                            },
                            notSelectedItem = {
                                SelectorNotSelectedItemView(
                                    selected = selectedType == null,
                                    onClick = { selectedType = null },
                                )
                            },
                            onDismissRequest = { openSocialNetworkTypeSelectorSheet = false },
                            onConfirm = {
                                editableService = selectedType
                                openSocialNetworkTypeSelectorSheet = false
                            },
                        )
                    }

                    AnimatedVisibility(
                        visible = editableService == SocialNetworkType.OTHER,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        OutlinedTextField(
                            modifier = modifier.fillMaxWidth(),
                            value = editableServiceName ?: "",
                            onValueChange = { text ->
                                if (text.length <= Constants.Text.DEFAULT_MAX_TEXT_LENGTH) {
                                    editableServiceName = text
                                }
                            },
                            label = { Text(text = EditorThemeRes.strings.otherSocialNetworkServiceNameLabel) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large,
                        )
                    }
                    OutlinedTextField(
                        modifier = modifier.fillMaxWidth(),
                        value = editableData ?: "",
                        onValueChange = { text ->
                            if (text.length <= Constants.Text.DEFAULT_MAX_TEXT_LENGTH) {
                                editableData = text
                            }
                        },
                        label = { Text(text = StudyAssistantRes.strings.contactInfoValue) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                    )
                }
                DialogButtons(
                    enabledConfirmFirst = if (editableService == SocialNetworkType.OTHER) {
                        !editableData.isNullOrBlank() && !editableServiceName.isNullOrBlank()
                    } else {
                        !editableData.isNullOrBlank() && editableService != null
                    },
                    enabledConfirmSecond = socialNetwork != null,
                    confirmFirstTitle = StudyAssistantRes.strings.saveConfirmTitle,
                    confirmSecondTitle = StudyAssistantRes.strings.deleteConfirmTitle,
                    onCancelClick = onDismiss,
                    onConfirmFirstClick = {
                        val targetSocialNetwork = SocialNetworkUi(
                            type = checkNotNull(editableService),
                            otherType = editableServiceName,
                            data = checkNotNull(editableData)
                        )
                        onConfirm(targetSocialNetwork)
                    },
                    onConfirmSecondClick = onDelete,
                )
            }
        }
    }
}