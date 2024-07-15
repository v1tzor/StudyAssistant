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

package ru.aleshin.studyassistant.users.impl.presentation.ui.friends

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTimeDurationOrZero
import ru.aleshin.studyassistant.core.common.extensions.limitSize
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder.FRIENDS
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder.OVERVIEW_FRIEND_REQUESTS
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesOrHoursTitle
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.MediumInfoBadge
import ru.aleshin.studyassistant.users.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.users.impl.presentation.models.FriendRequestsDetailsUi
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes
import ru.aleshin.studyassistant.users.impl.presentation.ui.common.NoneUserRequestsView
import ru.aleshin.studyassistant.users.impl.presentation.ui.common.UserView
import ru.aleshin.studyassistant.users.impl.presentation.ui.common.UserViewPlaceholder
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsViewState
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.views.FriendDropdownMenu

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
@Composable
internal fun FriendsContent(
    state: FriendsViewState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onShowAllRequests: () -> Unit,
    onOpenUserProfile: (UID) -> Unit,
    onAcceptRequest: (UID) -> Unit,
    onRejectRequest: (UID) -> Unit,
    onDeleteFriend: (UID) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.fillMaxSize().padding(top = 12.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FriendsRequestsSection(
            isLoading = isLoading,
            currentTime = currentTime,
            friendRequests = requests,
            onShowAllRequests = onShowAllRequests,
            onOpenUserProfile = onOpenUserProfile,
            onAcceptRequest = onAcceptRequest,
            onRejectRequest = onRejectRequest,
        )
        HorizontalDivider()
        MyFriendsSection(
            isLoading = isLoading,
            friends = friends,
            onOpenUserProfile = onOpenUserProfile,
            onDeleteFriend = onDeleteFriend,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FriendsRequestsSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentTime: Instant,
    friendRequests: FriendRequestsDetailsUi?,
    onShowAllRequests: () -> Unit,
    onOpenUserProfile: (UID) -> Unit,
    onAcceptRequest: (UID) -> Unit,
    onRejectRequest: (UID) -> Unit,
) {
    Column(
        modifier = modifier.animateContentSize().fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FriendsRequestsSectionHeader(
            isLoading = isLoading,
            friendRequests = friendRequests,
            onShowAllRequests = onShowAllRequests,
        )
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            if (!loading) {
                FlowColumn(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachColumn = 3,
                ) {
                    val receivedRequests =
                        friendRequests?.received?.toList()?.sortedByDescending { it.second }
                    if (!receivedRequests.isNullOrEmpty()) {
                        val receivedUsers = receivedRequests.limitSize(OVERVIEW_FRIEND_REQUESTS)
                        receivedUsers.forEach { user ->
                            UserView(
                                onClick = { onOpenUserProfile(user.first.uid) },
                                name = user.first.username,
                                avatar = user.first.avatar,
                                supportText = {
                                    Text(text = UsersThemeRes.strings.receivedFriendRequestLabel)
                                },
                                trailingIcon = {
                                    val duration = dateTimeDurationOrZero(currentTime, user.second)
                                    Text(text = duration.toMinutesOrHoursTitle())
                                },
                                actions = {
                                    Button(
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        ),
                                        onClick = { onAcceptRequest(user.first.uid) },
                                    ) {
                                        Text(
                                            text = UsersThemeRes.strings.acceptRequestTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                    FilledTonalButton(
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        ),
                                        onClick = { onRejectRequest(user.first.uid) },
                                    ) {
                                        Text(
                                            text = UsersThemeRes.strings.rejectRequestTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                },
                            )
                        }
                    } else {
                        NoneUserRequestsView()
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(OVERVIEW_FRIEND_REQUESTS) {
                        UserViewPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsRequestsSectionHeader(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    friendRequests: FriendRequestsDetailsUi?,
    onShowAllRequests: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = UsersThemeRes.strings.friendRequestsSectionHeader,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MediumInfoBadge(
                containerColor = StudyAssistantRes.colors.accents.orangeContainer,
                contentColor = StudyAssistantRes.colors.accents.orange,
            ) {
                Text(text = (friendRequests?.received?.size ?: 0).toString())
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = onShowAllRequests,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(text = UsersThemeRes.strings.showAllRequestsTitle)
        }
    }
}

@Composable
private fun MyFriendsSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    friends: Map<Char, List<AppUserUi>>,
    onOpenUserProfile: (UID) -> Unit,
    onDeleteFriend: (UID) -> Unit,
) {
    Column(
        modifier = modifier.animateContentSize().fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MyFriendsSectionSectionHeader(
            isLoading = isLoading,
            friends = friends,
        )
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            if (!loading) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (friends.isNotEmpty()) {
                        val alphabeticFriends = friends.toList()
                        alphabeticFriends.forEach { friend ->
                            FriendsViewItem(
                                char = friend.first,
                                friends = friend.second,
                                onOpenUserProfile = onOpenUserProfile,
                                onDeleteFriend = onDeleteFriend,
                            )
                        }
                    } else {
                        NoneFriendsView()
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(FRIENDS) {
                        UserViewPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsViewItem(
    modifier: Modifier = Modifier,
    char: Char,
    friends: List<AppUserUi>,
    onOpenUserProfile: (UID) -> Unit,
    onDeleteFriend: (UID) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = char.toString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Column(modifier = Modifier.weight(1f)) {
            friends.forEach { friend ->
                var isExpandedFriendMenu by remember { mutableStateOf(false) }
                Row {
                    UserView(
                        onClick = { onOpenUserProfile(friend.uid) },
                        name = friend.username,
                        avatar = friend.avatar,
                        supportText = { Text(text = friend.email) },
                        trailingIcon = {
                            FriendDropdownMenu(
                                isExpand = isExpandedFriendMenu,
                                onDismiss = { isExpandedFriendMenu = false },
                                onDeleteFromFriend = {
                                    onDeleteFriend(friend.uid)
                                    isExpandedFriendMenu = false
                                },
                            )
                            IconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = { isExpandedFriendMenu = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MyFriendsSectionSectionHeader(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    friends: Map<Char, List<AppUserUi>>,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = UsersThemeRes.strings.friendRequestsSectionHeader,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MediumInfoBadge(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(text = friends.size.toString())
            }
        }
    }
}

@Composable
internal fun NoneFriendsView(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(top = 4.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = UsersThemeRes.strings.noneFriendsTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}