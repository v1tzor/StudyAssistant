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

package ru.aleshin.studyassistant.users.impl.presentation.ui.friends.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.Constants.Text
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.animatePadding
import ru.aleshin.studyassistant.users.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.users.impl.presentation.models.FriendRequestsDetailsUi
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes
import ru.aleshin.studyassistant.users.impl.presentation.ui.common.UserView

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun FriendsSearchTopBar(
    modifier: Modifier = Modifier,
    isLoadingSearch: Boolean,
    searchedUsers: List<AppUserUi>,
    friendRequests: FriendRequestsDetailsUi?,
    friends: List<AppUserUi>,
    onBackPress: () -> Unit,
    onSearch: (String) -> Unit,
    onOpenUserProfile: (UID) -> Unit,
    onSendFriendRequest: (UID) -> Unit,
    onCancelFriendRequest: (UID) -> Unit,
    searchInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focusManager = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    var isActive by rememberSaveable { mutableStateOf(false) }

    SearchBar(
        query = query,
        onQueryChange = {
            query = it
            if (it.length == Text.USER_CODE_LENGTH) {
                onSearch(it)
            } else {
                onSearch("")
            }
        },
        onSearch = {
            if (it.length == Text.USER_CODE_LENGTH) {
                focusManager.clearFocus()
                onSearch(it)
            } else {
                onSearch("")
            }
        },
        active = isActive,
        onActiveChange = { isActive = it },
        modifier = modifier.fillMaxWidth().animatePadding(
            targetState = !isActive,
            paddingValues = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp),
        ),
        placeholder = {
            Text(text = UsersThemeRes.strings.friendsSearchBarPlaceholder)
        },
        leadingIcon = {
            IconButton(
                onClick = {
                    if (isActive) {
                        query = ""
                        onSearch("")
                        isActive = false
                    } else {
                        onBackPress()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = StudyAssistantRes.strings.backIconDesc,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        trailingIcon = {
            AnimatedContent(
                targetState = isActive,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            ) { active ->
                if (!active) {
                    IconButton(onClick = { isActive = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            query = ""
                            onSearch("")
                            focusManager.clearFocus()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = StudyAssistantRes.strings.clearSearchBarDesk,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        },
        interactionSource = searchInteractionSource,
    ) {
        FriendsSearchTopBarContent(
            isLoading = isLoadingSearch,
            query = query,
            searchedUsers = searchedUsers,
            sentRequests = friendRequests?.send?.map { it.key.uid } ?: emptyList(),
            friends = friends,
            onOpenUserProfile = onOpenUserProfile,
            onSendFriendRequest = onSendFriendRequest,
            onCancelSendFriendRequest = onCancelFriendRequest,
        )
    }
}

@Composable
private fun FriendsSearchTopBarContent(
    isLoading: Boolean,
    query: String,
    searchedUsers: List<AppUserUi>,
    sentRequests: List<UID>,
    friends: List<AppUserUi>,
    onOpenUserProfile: (UID) -> Unit,
    onSendFriendRequest: (UID) -> Unit,
    onCancelSendFriendRequest: (UID) -> Unit,
) {
    Crossfade(targetState = isLoading) { loading ->
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                    )
                }
            } else if (searchedUsers.isNotEmpty()) {
                searchedUsers.forEach { user ->
                    UserView(
                        onClick = { onOpenUserProfile(user.uid) },
                        name = user.username,
                        avatar = user.avatar,
                        supportText = if (friends.contains(user)) {{
                            Text(text = UsersThemeRes.strings.userIsAlreadyFriendTitle)
                        }} else if(sentRequests.contains(user.uid)) {{
                            Text(text = UsersThemeRes.strings.sentFriendRequestLabel)
                        }} else {
                            null
                        },
                        actions = if (!friends.contains(user)) {{
                            if (!sentRequests.contains(user.uid)) {
                                Button(
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    onClick = { onSendFriendRequest(user.uid) },
                                ) {
                                    Text(
                                        text = UsersThemeRes.strings.sendFriendRequestTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            } else {
                                FilledTonalButton(
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    onClick = { onCancelSendFriendRequest(user.uid) },
                                ) {
                                    Text(
                                        text = UsersThemeRes.strings.cancelSendFriendRequestTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            }
                        }} else {
                            null
                        },
                    )
                }
            } else {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (query.length == Text.USER_CODE_LENGTH) {
                        UsersThemeRes.strings.notFoundUsersTitle
                    } else {
                        UsersThemeRes.strings.searchUsersRequirementsTitle
                    },
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}