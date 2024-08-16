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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.ui.mappers.toLanguageString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.MediumDragHandle
import ru.aleshin.studyassistant.core.ui.views.MediumInfoBadge
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.ReceivedMediatedSchedulesShortUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.SentMediatedSchedulesUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.SharedSchedulesShortUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.profile.impl.presentation.theme.ProfileThemeRes
import kotlin.time.Duration

/**
 * @author Stanislav Aleshin on 15.08.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SharedSchedulesBottomSheet(
    modifier: Modifier = Modifier,
    currentTime: Instant,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismissRequest: () -> Unit,
    sharedSchedules: SharedSchedulesShortUi,
    onShowSchedule: (ReceivedMediatedSchedulesShortUi) -> Unit,
    onCancelSentSchedule: (SentMediatedSchedulesUi) -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.verticalScroll(scrollState).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReceivedSchedulesSection(
                currentTime = currentTime,
                receivedSharedSchedules = sharedSchedules.received.values.toList(),
                onShowSchedule = onShowSchedule
            )
            HorizontalDivider()
            SentSchedulesSection(
                sentSharedSchedules = sharedSchedules.sent.values.toList(),
                onCancelSentSchedule = onCancelSentSchedule,
            )
        }
    }
}

@Composable
private fun ReceivedSchedulesSection(
    modifier: Modifier = Modifier,
    currentTime: Instant,
    receivedSharedSchedules: List<ReceivedMediatedSchedulesShortUi>,
    onShowSchedule: (ReceivedMediatedSchedulesShortUi) -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = ProfileThemeRes.strings.receivedSchedulesSheetHeader,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            MediumInfoBadge(
                containerColor = StudyAssistantRes.colors.accents.orangeContainer,
                contentColor = StudyAssistantRes.colors.accents.orange,
            ) {
                Text(text = (receivedSharedSchedules.size).toString(), maxLines = 1)
            }
        }
        Column(
            modifier = Modifier.animateContentSize().fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (receivedSharedSchedules.isNotEmpty()) {
                receivedSharedSchedules.forEach { receivedSchedule ->
                    ReceivedScheduleView(
                        leftTime = currentTime - receivedSchedule.sendDate,
                        sender = receivedSchedule.sender,
                        organizationNames = receivedSchedule.organizationNames,
                        onShowSchedule = { onShowSchedule(receivedSchedule) },
                    )
                }
            } else {
                NoneSharedScheduleView()
            }
        }
    }
}

@Composable
private fun ReceivedScheduleView(
    modifier: Modifier = Modifier,
    leftTime: Duration,
    sender: AppUserUi,
    organizationNames: List<String>,
    onShowSchedule: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarView(
                    modifier = Modifier.size(40.dp),
                    firstName = sender.username.split(' ').getOrElse(0) { "-" },
                    secondName = sender.username.split(' ').getOrNull(1),
                    imageUrl = sender.avatar,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildString {
                            append(ProfileThemeRes.strings.fromPrefix, " ")
                            append(sender.username)
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = sender.email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = buildString {
                        append(leftTime.toLanguageString(), " ")
                        append(ProfileThemeRes.strings.agoSuffix)
                    },
                    color = MaterialTheme.colorScheme.secondary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                organizationNames.forEach { organizationName ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                painter = painterResource(StudyAssistantRes.icons.organization),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = organizationName,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp,
                    top = 4.dp
                )
            ) {
                Button(
                    onClick = onShowSchedule,
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = ProfileThemeRes.strings.showSharedSchedulesButton,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SentSchedulesSection(
    modifier: Modifier = Modifier,
    sentSharedSchedules: List<SentMediatedSchedulesUi>,
    onCancelSentSchedule: (SentMediatedSchedulesUi) -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = ProfileThemeRes.strings.sentSchedulesSheetHeader,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            MediumInfoBadge(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(text = (sentSharedSchedules.size).toString(), maxLines = 1)
            }
        }
        Column(
            modifier = Modifier.animateContentSize().fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (sentSharedSchedules.isNotEmpty()) {
                sentSharedSchedules.forEach { sharedSchedule ->
                    SentScheduleView(
                        recipient = sharedSchedule.recipient,
                        organizationNames = sharedSchedule.organizationNames,
                        onCancelSentSchedule = { onCancelSentSchedule(sharedSchedule) },
                    )
                }
            } else {
                NoneSharedScheduleView()
            }
        }
    }
}

@Composable
private fun SentScheduleView(
    modifier: Modifier = Modifier,
    recipient: AppUserUi,
    organizationNames: List<String>,
    onCancelSentSchedule: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarView(
                    modifier = Modifier.size(40.dp),
                    firstName = recipient.username.split(' ').getOrElse(0) { "-" },
                    secondName = recipient.username.split(' ').getOrNull(1),
                    imageUrl = recipient.avatar,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildString {
                            append(ProfileThemeRes.strings.toPrefix, " ")
                            append(recipient.username)
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = recipient.email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                organizationNames.forEach { organizationName ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                painter = painterResource(StudyAssistantRes.icons.organization),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = organizationName,
                                color = MaterialTheme.colorScheme.onSurface,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp,
                    top = 4.dp
                )
            ) {
                FilledTonalButton(
                    onClick = onCancelSentSchedule,
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = ProfileThemeRes.strings.cancelSentSharedSchedulesButton,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun NoneSharedScheduleView(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(StudyAssistantRes.icons.practicalTasks),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = ProfileThemeRes.strings.noneSharedSchedulesSheetTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}