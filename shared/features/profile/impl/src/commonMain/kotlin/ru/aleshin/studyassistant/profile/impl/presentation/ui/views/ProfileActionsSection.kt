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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.MediumInfoBadge
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.profile.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.ReceivedMediatedSchedulesShortUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.SentMediatedSchedulesUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.ShareSchedulesSendDataUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.SharedSchedulesShortUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.users.FriendRequestsUi
import ru.aleshin.studyassistant.profile.impl.presentation.theme.ProfileThemeRes

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
@Composable
internal fun ProfileActionsSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isLoadingShare: Boolean,
    isLoadingSend: Boolean,
    currentTime: Instant,
    profile: AppUserUi?,
    requests: FriendRequestsUi?,
    sharedSchedules: SharedSchedulesShortUi?,
    allOrganizations: List<OrganizationShortUi>,
    allFriends: List<AppUserUi>,
    onFriendsClick: () -> Unit,
    onPrivacySettingsClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onNotifySettingsClick: () -> Unit,
    onCalendarSettingsClick: () -> Unit,
    onPaymentsSettingsClick: () -> Unit,
    onShowSchedule: (ReceivedMediatedSchedulesShortUi) -> Unit,
    onCancelSentSchedule: (SentMediatedSchedulesUi) -> Unit,
    onShareSchedule: (ShareSchedulesSendDataUi) -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 24.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "Friends") {
            ProfileActionView(
                onClick = onFriendsClick,
                icon = painterResource(ProfileThemeRes.icons.friends),
                title = ProfileThemeRes.strings.friendsTitle,
                value = {
                    Crossfade(
                        targetState = isLoading,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = Spring.DefaultDisplacementThreshold,
                        )
                    ) { loading ->
                        if (loading) {
                            PlaceholderBox(
                                modifier = Modifier.size(40.dp, 28.dp),
                                shape = MaterialTheme.shapes.small,
                                highlight = null,
                            )
                        } else if (profile != null) {
                            Text(
                                text = profile.friends.size.toString(),
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                    }
                },
                badge = if (requests?.received?.isNotEmpty() == true) {
                    {
                        NewFriendBadge(count = requests.received.size)
                    }
                } else {
                    null
                }
            )
        }
        item(key = "PrivacySettings") {
            ProfileActionView(
                onClick = onPrivacySettingsClick,
                icon = painterResource(ProfileThemeRes.icons.privacySettings),
                title = ProfileThemeRes.strings.privacySettingsTitle,
            )
        }
        item(key = "GeneralSettings") {
            ProfileActionView(
                onClick = onGeneralSettingsClick,
                icon = painterResource(ProfileThemeRes.icons.generalSettings),
                title = ProfileThemeRes.strings.generalSettingsTitle,
            )
        }
        item(key = "NotifySettings") {
            ProfileActionView(
                onClick = onNotifySettingsClick,
                icon = painterResource(ProfileThemeRes.icons.notifySettings),
                title = ProfileThemeRes.strings.notifySettingsTitle,
            )
        }
        item(key = "CalendarSettings") {
            ProfileActionView(
                onClick = onCalendarSettingsClick,
                icon = painterResource(ProfileThemeRes.icons.calendarSettings),
                title = ProfileThemeRes.strings.calendarSettingsTitle,
            )
        }
        item(key = "PaymentsSettings") {
            ProfileActionView(
                onClick = onPaymentsSettingsClick,
                icon = painterResource(ProfileThemeRes.icons.paymentsSettings),
                title = ProfileThemeRes.strings.paymentsSettingsTitle,
            )
        }
        item(key = "SharedSchedules", span = { GridItemSpan(2) }) {
            ShareScheduleView(
                isLoadingShare = isLoadingShare,
                isLoadingSend = isLoadingSend,
                currentTime = currentTime,
                sharedSchedules = sharedSchedules,
                allOrganizations = allOrganizations,
                allFriends = allFriends,
                onShowSchedule = onShowSchedule,
                onCancelSentSchedule = onCancelSentSchedule,
                onShareSchedule = onShareSchedule,
            )
        }
    }
}

@Composable
internal fun ProfileActionView(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: Painter,
    title: String,
    value: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    shape: Shape = MaterialTheme.shapes.large,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = shape,
        color = backgroundColor,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.weight(1f))
                badge?.invoke()
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (value != null) 1 else 2,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                value?.invoke()
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ShareScheduleView(
    modifier: Modifier = Modifier,
    isLoadingShare: Boolean,
    isLoadingSend: Boolean,
    currentTime: Instant,
    allOrganizations: List<OrganizationShortUi>,
    allFriends: List<AppUserUi>,
    sharedSchedules: SharedSchedulesShortUi?,
    onShowSchedule: (ReceivedMediatedSchedulesShortUi) -> Unit,
    onCancelSentSchedule: (SentMediatedSchedulesUi) -> Unit,
    onShareSchedule: (ShareSchedulesSendDataUi) -> Unit,
) {
    var openSharedSchedulesSheet by remember { mutableStateOf(false) }
    var openSchedulesSenderSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.animateContentSize(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(ProfileThemeRes.icons.shareSchedule),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = ProfileThemeRes.strings.sharedSchedulesViewTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                )
                Crossfade(
                    targetState = isLoadingShare,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = Spring.DefaultDisplacementThreshold,
                    )
                ) { loading ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!loading) {
                            if (!sharedSchedules?.sent.isNullOrEmpty()) {
                                MediumInfoBadge(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ) {
                                    Text(text = (sharedSchedules?.sent?.size ?: 0).toString(), maxLines = 1)
                                }
                            }
                            MediumInfoBadge(
                                containerColor = StudyAssistantRes.colors.accents.orangeContainer,
                                contentColor = StudyAssistantRes.colors.accents.orange,
                            ) {
                                Text(text = (sharedSchedules?.received?.size ?: 0).toString(), maxLines = 1)
                            }
                        } else {
                            PlaceholderBox(
                                modifier = Modifier.size(20.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            )
                            PlaceholderBox(
                                modifier = Modifier.size(20.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = StudyAssistantRes.colors.accents.orangeContainer,
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    enabled = !isLoadingShare,
                    modifier = Modifier.weight(1f),
                    onClick = { openSharedSchedulesSheet = true },
                ) {
                    Text(
                        text = ProfileThemeRes.strings.openSentSchedulesButtonTitle,
                        maxLines = 1,
                    )
                }
                FilledTonalButton(
                    enabled = !isLoadingShare,
                    onClick = { openSchedulesSenderSheet = true }
                ) {
                    Text(
                        text = ProfileThemeRes.strings.sendScheduleButtonTitle,
                        maxLines = 1,
                    )
                }
            }
        }
    }

    if (openSharedSchedulesSheet && sharedSchedules != null) {
        SharedSchedulesBottomSheet(
            sharedSchedules = sharedSchedules,
            currentTime = currentTime,
            onDismissRequest = { openSharedSchedulesSheet = false },
            onShowSchedule = onShowSchedule,
            onCancelSentSchedule = onCancelSentSchedule,
        )
    }

    if (openSchedulesSenderSheet) {
        ScheduleSenderBottomSheet(
            isLoadingSend = isLoadingSend,
            allOrganizations = allOrganizations,
            allFriends = allFriends,
            onDismissRequest = { openSchedulesSenderSheet = false },
            onShareSchedule = onShareSchedule,
        )
    }
}

@Composable
internal fun NewFriendBadge(
    modifier: Modifier = Modifier,
    count: Int,
) {
    Surface(
        modifier = modifier.height(16.dp),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.error,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 6.dp),
            text = "+$count",
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}