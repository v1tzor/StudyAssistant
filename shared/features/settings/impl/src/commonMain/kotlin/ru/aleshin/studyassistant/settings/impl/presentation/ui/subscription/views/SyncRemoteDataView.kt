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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.views

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.FreeOrPaidContent
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.dialog.WarningAlertDialog
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes

/**
 * @author Stanislav Aleshin on 28.08.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SyncRemoteDataView(
    modifier: Modifier = Modifier,
    isLoadingSync: Boolean,
    isPaidUser: Boolean?,
    haveRemoteData: Boolean?,
    onTransferRemoteData: () -> Unit,
    onTransferLocalData: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            var remoteDataTransferWarningDialogState by remember { mutableStateOf(false) }
            var localDataTransferWarningDialogState by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = SettingsThemeRes.strings.syncDataViewTitle,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            FreeOrPaidContent(
                isPaidUser = isPaidUser,
                placeholders = {
                    PlaceholderBox(
                        modifier = Modifier.height(34.dp).fillMaxWidth(),
                        shape = MaterialTheme.shapes.full,
                    )
                },
                paidContent = {
                    Button(
                        onClick = { localDataTransferWarningDialogState = true },
                        modifier = Modifier.height(34.dp).fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                    ) {
                        Crossfade(
                            targetState = isLoadingSync,
                            animationSpec = floatSpring(),
                        ) { loading ->
                            if (!loading) {
                                Text(
                                    text = SettingsThemeRes.strings.transferLocalDataButtonLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = LocalContentColor.current,
                                )
                            }
                        }
                    }
                },
                freeContent = {
                    Button(
                        onClick = { remoteDataTransferWarningDialogState = true },
                        enabled = haveRemoteData == true,
                        modifier = Modifier.height(34.dp).fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                    ) {
                        Crossfade(
                            targetState = isLoadingSync,
                            animationSpec = floatSpring(),
                        ) { loading ->
                            if (!loading) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = SettingsThemeRes.strings.transferRemoteDataButtonLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    Icon(
                                        modifier = Modifier.size(18.dp),
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = LocalContentColor.current,
                                    )
                                }
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 3.dp,
                                    color = LocalContentColor.current,
                                )
                            }
                        }
                    }
                },
            )

            if (remoteDataTransferWarningDialogState) {
                WarningAlertDialog(
                    icon = {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null)
                    },
                    title = {
                        Text(text = SettingsThemeRes.strings.transferDataWarningTitle)
                    },
                    text = {
                        Text(text = SettingsThemeRes.strings.transferRemoteDataWarningText)
                    },
                    confirmTitle = SettingsThemeRes.strings.transferConfirmTitle,
                    onDismiss = { remoteDataTransferWarningDialogState = false },
                    onConfirm = {
                        onTransferRemoteData()
                        remoteDataTransferWarningDialogState = false
                    },
                )
            }

            if (localDataTransferWarningDialogState) {
                WarningAlertDialog(
                    icon = {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null)
                    },
                    title = {
                        Text(text = SettingsThemeRes.strings.transferDataWarningTitle)
                    },
                    text = {
                        Text(text = SettingsThemeRes.strings.transferLocalDataWarningText)
                    },
                    confirmTitle = SettingsThemeRes.strings.transferConfirmTitle,
                    onDismiss = { localDataTransferWarningDialogState = false },
                    onConfirm = {
                        onTransferLocalData()
                        localDataTransferWarningDialogState = false
                    },
                )
            }
        }
    }
}