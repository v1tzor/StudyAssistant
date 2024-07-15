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

package ru.aleshin.studyassistant.users.impl.presentation.ui.requests

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTimeDurationOrZero
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesOrHoursTitle
import ru.aleshin.studyassistant.users.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes
import ru.aleshin.studyassistant.users.impl.presentation.ui.common.NoneUserRequestsView
import ru.aleshin.studyassistant.users.impl.presentation.ui.common.UserView
import ru.aleshin.studyassistant.users.impl.presentation.ui.common.UserViewPlaceholder
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.views.RequestsTab
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsViewState

/**
 * @author Stanislav Aleshin on 13.07.2024.
 */
@Composable
internal fun RequestsContent(
    state: RequestsViewState,
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    onOpenUserProfile: (UID) -> Unit,
    onAcceptRequest: (UID) -> Unit,
    onRejectRequest: (UID) -> Unit,
    onDeleteHistoryRequest: (UID) -> Unit,
    onCancelSendFriendRequest: (UID) -> Unit,
) = with(state) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize().padding(top = 12.dp),
    ) { page ->
        when (RequestsTab.byIndex(page)) {
            RequestsTab.RECEIVED -> RequestsReceivedTab(
                isLoading = isLoading,
                currentTime = currentTime,
                receivedRequests = requests?.received,
                lastActions = requests?.lastActions,
                onOpenUserProfile = onOpenUserProfile,
                onAcceptRequest = onAcceptRequest,
                onRejectRequest = onRejectRequest,
                onDeleteHistoryRequest = onDeleteHistoryRequest,
            )
            RequestsTab.SENT -> RequestsSentTab(
                isLoading = isLoading,
                currentTime = currentTime,
                sentRequests = requests?.send,
                onOpenUserProfile = onOpenUserProfile,
                onCancelSendFriendRequest = onCancelSendFriendRequest,
            )
        }
    }
}

@Composable
internal fun RequestsReceivedTab(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentTime: Instant,
    receivedRequests: Map<AppUserUi, Instant>?,
    lastActions: Map<AppUserUi, Boolean>?,
    onOpenUserProfile: (UID) -> Unit,
    onAcceptRequest: (UID) -> Unit,
    onRejectRequest: (UID) -> Unit,
    onDeleteHistoryRequest: (UID) -> Unit,
) {
    Crossfade(
        modifier = modifier.padding(horizontal = 8.dp).fillMaxSize(),
        targetState = isLoading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        if (!loading) {
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!receivedRequests.isNullOrEmpty()) {
                    val sortedRequests = receivedRequests.toList().sortedByDescending { it.second }
                    items(sortedRequests, key = { it.first.uid }) { user ->
                        UserView(
                            modifier = Modifier.animateItem(),
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
                }
                if (!lastActions.isNullOrEmpty()) {
                    val sortedLastActions = lastActions.toList().sortedByDescending { it.second }
                    items(sortedLastActions, key = { it.first.uid + "action" }) { userAction ->
                        UserView(
                            modifier = Modifier.animateItem(),
                            onClick = { onOpenUserProfile(userAction.first.uid) },
                            name = userAction.first.username,
                            avatar = userAction.first.avatar,
                            supportText = {
                                Text(
                                    text = if (userAction.second) {
                                        UsersThemeRes.strings.acceptFriendRequestLabel
                                    } else {
                                        UsersThemeRes.strings.rejectFriendRequestLabel
                                    },
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    modifier = Modifier.size(32.dp),
                                    onClick = { onDeleteHistoryRequest(userAction.first.uid) },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                        )
                    }
                }
                if (receivedRequests.isNullOrEmpty() && lastActions.isNullOrEmpty()) {
                    item {
                        NoneUserRequestsView(modifier = Modifier.fillParentMaxWidth())
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(Constants.Placeholder.FULL_FRIEND_REQUESTS) {
                    UserViewPlaceholder()
                }
            }
        }
    }
}

@Composable
internal fun RequestsSentTab(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentTime: Instant,
    sentRequests: Map<AppUserUi, Instant>?,
    onOpenUserProfile: (UID) -> Unit,
    onCancelSendFriendRequest: (UID) -> Unit,
) {
    Crossfade(
        modifier = modifier.padding(horizontal = 8.dp).fillMaxSize(),
        targetState = isLoading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        if (!loading) {
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!sentRequests.isNullOrEmpty()) {
                    val sortedRequests = sentRequests.toList().sortedByDescending { it.second }
                    items(sortedRequests, key = { it.first.uid }) { user ->
                        UserView(
                            modifier = Modifier.animateItem(),
                            onClick = { onOpenUserProfile(user.first.uid) },
                            name = user.first.username,
                            avatar = user.first.avatar,
                            supportText = {
                                Text(text = UsersThemeRes.strings.sentFriendRequestLabel)
                            },
                            trailingIcon = {
                                val duration = dateTimeDurationOrZero(currentTime, user.second)
                                Text(text = duration.toMinutesOrHoursTitle())
                            },
                            actions = {
                                FilledTonalButton(
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    onClick = { onCancelSendFriendRequest(user.first.uid) },
                                ) {
                                    Text(
                                        text = UsersThemeRes.strings.cancelFriendRequestTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            },
                        )
                    }
                } else {
                    item {
                        NoneUserRequestsView(modifier = Modifier.fillParentMaxWidth())
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(Constants.Placeholder.FULL_FRIEND_REQUESTS) {
                    UserViewPlaceholder()
                }
            }
        }
    }
}