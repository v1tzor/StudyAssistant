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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseSelectorDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.ContactInfoEditorDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogAddItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogNotSelectedItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogSwipeItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogTextField
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
internal fun LocationInfoField(
    modifier: Modifier = Modifier,
    enabledAdd: Boolean,
    isLoading: Boolean,
    location: ContactInfoUi?,
    office: String?,
    allLocations: List<ContactInfoUi>,
    allOffices: List<String>,
    onUpdateLocations: (List<ContactInfoUi>) -> Unit,
    onUpdateOffices: (List<String>) -> Unit,
    onSelectedLocation: (ContactInfoUi?) -> Unit,
    onSelectedOffice: (String?) -> Unit,
) {
    var isOpenLocationSelector by remember { mutableStateOf(false) }
    var isOpenOfficeSelector by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(StudyAssistantRes.icons.location),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClickableTextField(
                enabled = !isLoading,
                modifier = Modifier.weight(0.6f),
                onClick = { isOpenLocationSelector = true },
                value = (location?.label ?: location?.value)?.ifEmpty { null },
                label = EditorThemeRes.strings.locationFieldLabel,
                placeholder = EditorThemeRes.strings.locationFieldPlaceholder,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isOpenLocationSelector,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            ClickableTextField(
                enabled = !isLoading,
                modifier = Modifier.weight(0.4f),
                onClick = { isOpenOfficeSelector = true },
                value = office,
                label = EditorThemeRes.strings.officeFieldLabel,
                placeholder = EditorThemeRes.strings.officeFieldPlaceholder,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isOpenOfficeSelector,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
    }

    if (isOpenLocationSelector) {
        LocationSelectorDialog(
            enabledAdd = enabledAdd,
            selected = location,
            locations = allLocations,
            onUpdateLocations = onUpdateLocations,
            onDismiss = { isOpenLocationSelector = false },
            onConfirm = {
                onSelectedLocation(it)
                isOpenLocationSelector = false
            },
        )
    }

    if (isOpenOfficeSelector) {
        OfficeSelectorDialog(
            enabledAdd = enabledAdd,
            selected = office,
            offices = allOffices,
            onUpdateOffices = onUpdateOffices,
            onDismiss = { isOpenOfficeSelector = false },
            onConfirm = {
                onSelectedOffice(it)
                isOpenOfficeSelector = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun LocationSelectorDialog(
    enabledAdd: Boolean,
    modifier: Modifier = Modifier,
    selected: ContactInfoUi?,
    locations: List<ContactInfoUi>,
    onUpdateLocations: (List<ContactInfoUi>) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (ContactInfoUi?) -> Unit,
) {
    var contactInfoEditorDialogState by remember { mutableStateOf(false) }
    var editableContactInfo by remember { mutableStateOf<ContactInfoUi?>(null) }
    var selectedLocation by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedLocation,
        items = locations,
        header = EditorThemeRes.strings.locationSelectorHeader,
        title = EditorThemeRes.strings.locationSelectorTitle,
        itemView = { location ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissBoxValue ->
                    if (dismissBoxValue == SwipeToDismissBoxValue.StartToEnd) {
                        val updatedLocations = locations.toMutableList().apply { remove(location) }
                        onUpdateLocations(updatedLocations)
                        true
                    } else {
                        false
                    }
                },
                positionalThreshold = { it * .60f },
            )
            SelectorDialogSwipeItemView(
                onClick = { selectedLocation = location },
                state = dismissState,
                selected = location == selectedLocation,
                title = location.value,
                label = location.label,
                enableDismissFromEndToStart = false,
                backgroundContent = {
                    SwipeToDismissBackground(
                        dismissState = dismissState,
                        startToEndContent = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                            )
                        },
                        startToEndColor = MaterialTheme.colorScheme.errorContainer,
                    )
                },
            )
        },
        addItemView = {
            SelectorDialogAddItemView(
                enabled = enabledAdd,
                onClick = { contactInfoEditorDialogState = true }
            )
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedLocation == null,
                onClick = { selectedLocation = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )

    if (contactInfoEditorDialogState) {
        ContactInfoEditorDialog(
            header = EditorThemeRes.strings.locationFieldLabel,
            label = editableContactInfo?.label,
            value = editableContactInfo?.value,
            onDismiss = {
                editableContactInfo = null
                contactInfoEditorDialogState = false
            },
            onConfirm = { label, value ->
                val location = (editableContactInfo ?: ContactInfoUi()).copy(
                    label = label,
                    value = value
                )
                val updatedLocations = locations.toMutableList().apply {
                    if (editableContactInfo != null) {
                        set(indexOf(editableContactInfo), location)
                    } else {
                        add(location)
                    }
                }
                onUpdateLocations(updatedLocations)

                editableContactInfo = null
                contactInfoEditorDialogState = false
            },
            onDelete = {
                val updatedLocations = locations.toMutableList().apply {
                    if (editableContactInfo != null) remove(editableContactInfo)
                }
                onUpdateLocations(updatedLocations)

                editableContactInfo = null
                contactInfoEditorDialogState = false
            }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OfficeSelectorDialog(
    modifier: Modifier = Modifier,
    enabledAdd: Boolean = true,
    selected: String?,
    offices: List<String>,
    onUpdateOffices: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    var selectedOffice by remember { mutableStateOf(selected) }
    var editableOffice by remember { mutableStateOf("") }
    var isEdited by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedOffice,
        items = offices,
        header = EditorThemeRes.strings.officeSelectorHeader,
        title = EditorThemeRes.strings.officeSelectorTitle,
        itemView = { office ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissBoxValue ->
                    if (dismissBoxValue == SwipeToDismissBoxValue.StartToEnd) {
                        val updatedOffices = offices.toMutableList().apply { remove(office) }
                        onUpdateOffices(updatedOffices)
                        true
                    } else {
                        false
                    }
                },
                positionalThreshold = { it * .60f },
            )
            SelectorDialogSwipeItemView(
                onClick = { selectedOffice = office },
                state = dismissState,
                selected = office == selectedOffice,
                title = office,
                label = null,
                enableDismissFromEndToStart = false,
                backgroundContent = {
                    SwipeToDismissBackground(
                        dismissState = dismissState,
                        startToEndContent = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                            )
                        },
                        startToEndColor = MaterialTheme.colorScheme.errorContainer,
                    )
                },
            )
        },
        addItemView = {
            AnimatedContent(targetState = isEdited) { edit ->
                if (edit) {
                    SelectorDialogTextField(
                        modifier = Modifier.focusRequester(focusRequester),
                        value = editableOffice,
                        onValueChange = {
                            if (it.length < Constants.Text.DEFAULT_MAX_TEXT_LENGTH) {
                                editableOffice = it
                            }
                        },
                        onDismiss = {
                            editableOffice = ""
                            isEdited = false
                        },
                        onConfirm = {
                            val updatedOffices = offices.toMutableList().apply { add(editableOffice) }
                            onUpdateOffices(updatedOffices)
                            editableOffice = ""
                            isEdited = false
                        }
                    )
                    SideEffect { focusRequester.requestFocus() }
                } else {
                    SelectorDialogAddItemView(
                        enabled = enabledAdd,
                        onClick = { isEdited = true }
                    )
                }
            }
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedOffice == null,
                onClick = { selectedOffice = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}