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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.general.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.views.DialogAlertButtons
import ru.aleshin.studyassistant.core.ui.views.dialog.CheckedItemView
import ru.aleshin.studyassistant.settings.impl.resources.Res
import ru.aleshin.studyassistant.settings.impl.resources.delete_schedule_empty_organizations
import ru.aleshin.studyassistant.settings.impl.resources.delete_schedule_main_label
import ru.aleshin.studyassistant.settings.impl.resources.delete_schedule_organizations_title
import ru.aleshin.studyassistant.settings.impl.resources.delete_schedule_select_all
import ru.aleshin.studyassistant.settings.impl.resources.delete_schedule_warning_text
import ru.aleshin.studyassistant.settings.impl.resources.delete_schedule_warning_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.delete_confirm_title as core_delete_confirm_title

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DeleteScheduleDialog(
    modifier: Modifier = Modifier,
    organizations: List<OrganizationShortUi>,
    onDismiss: () -> Unit,
    onConfirm: (Set<UID>) -> Unit,
) {
    var selectedIds by remember(organizations) {
        mutableStateOf(organizations.map(OrganizationShortUi::uid).toSet())
    }

    BasicAlertDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Surface(
            modifier = Modifier
                .widthIn(min = 280.dp, max = 420.dp)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = stringResource(Res.string.delete_schedule_warning_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = stringResource(Res.string.delete_schedule_warning_text),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.delete_schedule_organizations_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    if (organizations.size > 1) {
                        TextButton(
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            onClick = {
                                selectedIds = if (selectedIds.size == organizations.size) {
                                    emptySet()
                                } else {
                                    organizations.map(OrganizationShortUi::uid).toSet()
                                }
                            },
                        ) {
                            Text(text = stringResource(Res.string.delete_schedule_select_all))
                        }
                    }
                    if (organizations.isEmpty()) {
                        Text(
                            modifier = Modifier.padding(vertical = 12.dp),
                            text = stringResource(Res.string.delete_schedule_empty_organizations),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                items = organizations,
                                key = OrganizationShortUi::uid,
                            ) { organization ->
                                val isSelected = organization.uid in selectedIds
                                CheckedItemView(
                                    onClick = {
                                        selectedIds = if (isSelected) {
                                            selectedIds - organization.uid
                                        } else {
                                            selectedIds + organization.uid
                                        }
                                    },
                                    selected = isSelected,
                                    title = organization.shortName,
                                    label = if (organization.isMain) {
                                        stringResource(Res.string.delete_schedule_main_label)
                                    } else {
                                        organization.type.mapToSting()
                                    },
                                    leadingIcon = {
                                        Surface(
                                            modifier = Modifier.size(40.dp),
                                            shape = MaterialTheme.shapes.medium,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.surface
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainerHigh
                                            },
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    modifier = Modifier.size(20.dp),
                                                    painter = painterResource(organization.type.mapToIcon()),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
                DialogAlertButtons(
                    enabledConfirm = selectedIds.isNotEmpty(),
                    confirmTitle = stringResource(CoreRes.string.core_delete_confirm_title),
                    onCancelClick = onDismiss,
                    onConfirmClick = { onConfirm(selectedIds) },
                )
            }
        }
    }
}
