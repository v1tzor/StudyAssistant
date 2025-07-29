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

package ru.aleshin.studyassistant.users.impl.presentation.ui.user.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder.USER_CONTACT_INFO
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.core.domain.entities.users.UserFriendStatus
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.bottomSide
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.UserCodeView
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.users.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes

/**
 * @author Stanislav Aleshin on 16.07.2024.
 */
@Composable
internal fun UserProfileTopSheet(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    user: AppUserUi?,
    friendStatus: UserFriendStatus?,
    onAddToFriends: () -> Unit,
    onAcceptRequest: () -> Unit,
    onCancelSendRequest: () -> Unit,
    onDeleteFromFriends: () -> Unit,
) {
    Surface(
        modifier = modifier.animateContentSize().fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.bottomSide,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        ) { loading ->
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                if (!loading) {
                    UserProfileTopSheetHeader(
                        username = user?.username ?: "-",
                        avatar = user?.avatar,
                        gender = user?.gender,
                        birthday = user?.birthday,
                        city = user?.city,
                    )
                    UserProfileTopSheetContent(
                        username = user?.username ?: "-",
                        code = user?.code ?: StudyAssistantRes.strings.noneTitle,
                        email = user?.email ?: StudyAssistantRes.strings.noneTitle,
                    )
                    UserProfileTopSheetFooter(
                        description = user?.description,
                        friendStatus = friendStatus,
                        onAddToFriends = onAddToFriends,
                        onAcceptRequest = onAcceptRequest,
                        onCancelSendRequest = onCancelSendRequest,
                        onDeleteFromFriends = onDeleteFromFriends,
                    )
                } else {
                    UserProfileTopSheetHeaderPlaceholder()
                    UserProfileTopSheetContentPlaceholder()
                    UserProfileTopSheetFooterPlaceholder()
                }
            }
        }
    }
}

@Composable
private fun UserProfileTopSheetHeaderPlaceholder(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlaceholderBox(
            modifier = Modifier.size(120.dp),
            shape = MaterialTheme.shapes.full,
            color = MaterialTheme.colorScheme.primaryContainer,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PlaceholderBox(
                modifier = Modifier.size(70.dp, 14.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(68.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false,
            ) {
                items(USER_CONTACT_INFO) {
                    PlaceholderBox(
                        modifier = Modifier.size(100.dp, 68.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                }
            }
        }
    }
}

@Composable
private fun UserProfileTopSheetContentPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlaceholderBox(
            modifier = Modifier.size(94.dp, 24.dp),
            shape = MaterialTheme.shapes.full,
            color = MaterialTheme.colorScheme.secondaryContainer,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PlaceholderBox(
                modifier = Modifier.size(260.dp, 25.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            PlaceholderBox(
                modifier = Modifier.size(160.dp, 20.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
        }
    }
}

@Composable
private fun UserProfileTopSheetFooterPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlaceholderBox(
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        )
        PlaceholderBox(
            modifier = Modifier.fillMaxWidth().height(36.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
        )
    }
}

@Composable
private fun UserProfileTopSheetHeader(
    modifier: Modifier = Modifier,
    username: String,
    avatar: String?,
    city: String?,
    birthday: String?,
    gender: Gender?,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarView(
            modifier = Modifier.size(120.dp),
            firstName = username.split(' ').getOrElse(0) { "-" },
            secondName = username.split(' ').getOrNull(1),
            imageUrl = avatar,
            style = MaterialTheme.typography.headlineLarge,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (gender != null || birthday != null || city != null) {
                Text(
                    text = UsersThemeRes.strings.userProfileInfoLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                if (gender != null) {
                    item {
                        UserInfoView(
                            icon = when (gender) {
                                Gender.NONE -> Icons.Outlined.PersonOutline
                                Gender.FEMALE -> Icons.Default.Female
                                Gender.MALE -> Icons.Default.Male
                            },
                            label = UsersThemeRes.strings.userGenderLabel,
                            title = gender.mapToSting(StudyAssistantRes.strings),
                        )
                    }
                }
                if (birthday != null) {
                    item {
                        VerticalDivider(modifier = Modifier.height(56.dp))
                    }
                    item {
                        UserInfoView(
                            icon = Icons.Outlined.Cake,
                            label = UsersThemeRes.strings.userBirthdayLabel,
                            title = birthday,
                        )
                    }
                }
                if (city != null) {
                    item {
                        VerticalDivider(modifier = Modifier.height(56.dp))
                    }
                    item {
                        UserInfoView(
                            icon = Icons.Outlined.HomeWork,
                            label = UsersThemeRes.strings.userCityLabel,
                            title = city,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInfoView(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    title: String,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun UserProfileTopSheetContent(
    modifier: Modifier = Modifier,
    username: String,
    code: String,
    email: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        UserCodeView(code = code)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = username,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = email,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun UserProfileTopSheetFooter(
    modifier: Modifier = Modifier,
    description: String?,
    friendStatus: UserFriendStatus?,
    onAddToFriends: () -> Unit,
    onAcceptRequest: () -> Unit,
    onCancelSendRequest: () -> Unit,
    onDeleteFromFriends: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (description != null) {
            UserProfileDescriptionView(text = description)
        }
        when (friendStatus) {
            UserFriendStatus.NOT_FRIENDS -> Button(
                onClick = onAddToFriends,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                content = { Text(text = UsersThemeRes.strings.addToFriendsTitle) },
            )
            UserFriendStatus.REQUEST_SENT -> FilledTonalButton(
                onClick = onCancelSendRequest,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                content = { Text(text = UsersThemeRes.strings.cancelSendFriendRequestTitle) },
            )
            UserFriendStatus.REQUEST_RECEIVE -> FilledTonalButton(
                onClick = onAcceptRequest,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = StudyAssistantRes.colors.accents.greenContainer,
                    contentColor = StudyAssistantRes.colors.accents.green,
                ),
                content = { Text(text = UsersThemeRes.strings.acceptRequestTitle) },
            )
            UserFriendStatus.IN_FRIENDS -> FilledTonalButton(
                onClick = onDeleteFromFriends,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                content = { Text(text = UsersThemeRes.strings.inFriendsTitle) },
            )
            else -> Unit
        }
    }
}

@Composable
private fun UserProfileDescriptionView(
    modifier: Modifier = Modifier,
    text: String,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = UsersThemeRes.strings.userProfileDescriptionLabel,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}