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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.profile.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.FriendRequestsUi
import ru.aleshin.studyassistant.profile.impl.presentation.theme.ProfileThemeRes

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun ProfileActionsSection(
    modifier: Modifier = Modifier,
    profile: AppUserUi?,
    requests: FriendRequestsUi?,
    onFriendsClick: () -> Unit,
    onPrivacySettingsClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onNotifySettingsClick: () -> Unit,
    onCalendarSettingsClick: () -> Unit,
    onPaymentsSettingsClick: () -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 24.dp),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ProfileActionView(
                onClick = onFriendsClick,
                icon = painterResource(ProfileThemeRes.icons.friends),
                title = ProfileThemeRes.strings.friendsTitle,
                value = {
                    AnimatedContent(
                        targetState = profile == null,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500, delayMillis = 180)).togetherWith(
                                fadeOut(animationSpec = tween(500))
                            )
                        },
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
        item {
            ProfileActionView(
                onClick = onPrivacySettingsClick,
                icon = painterResource(ProfileThemeRes.icons.privacySettings),
                title = ProfileThemeRes.strings.privacySettingsTitle,
            )
        }
        item {
            ProfileActionView(
                onClick = onGeneralSettingsClick,
                icon = painterResource(ProfileThemeRes.icons.generalSettings),
                title = ProfileThemeRes.strings.generalSettingsTitle,
            )
        }
        item {
            ProfileActionView(
                onClick = onNotifySettingsClick,
                icon = painterResource(ProfileThemeRes.icons.notifySettings),
                title = ProfileThemeRes.strings.notifySettingsTitle,
            )
        }
        item {
            ProfileActionView(
                onClick = onCalendarSettingsClick,
                icon = painterResource(ProfileThemeRes.icons.calendarSettings),
                title = ProfileThemeRes.strings.calendarSettingsTitle,
            )
        }
        item {
            ProfileActionView(
                onClick = onPaymentsSettingsClick,
                icon = painterResource(ProfileThemeRes.icons.paymentsSettings),
                title = ProfileThemeRes.strings.paymentsSettingsTitle,
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