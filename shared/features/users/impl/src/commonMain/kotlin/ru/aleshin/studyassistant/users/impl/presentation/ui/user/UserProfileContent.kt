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

package ru.aleshin.studyassistant.users.impl.presentation.ui.user

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.domain.entities.users.SocialNetworkType
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.users.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.users.impl.presentation.models.SocialNetworkUi
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileState
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.store.UserProfileComponent
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.views.UserProfileTopBar
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.views.UserProfileTopSheet

/**
 * @author Stanislav Aleshin on 15.07.2024.
 */
@Composable
internal fun UserProfileContent(
    userProfileComponent: UserProfileComponent,
    modifier: Modifier = Modifier,
) {
    val store = userProfileComponent.store
    val state by store.stateAsState()
    val strings = UsersThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseUserProfileContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
            )
        },
        topBar = {
            Column {
                UserProfileTopBar(
                    onBackClick = { store.dispatchEvent(UserProfileEvent.ClickBack) },
                )
                UserProfileTopSheet(
                    isLoading = state.isLoading,
                    user = state.user,
                    friendStatus = state.friendStatus,
                    onAddToFriends = { store.dispatchEvent(UserProfileEvent.SendFriendRequest) },
                    onAcceptRequest = { store.dispatchEvent(UserProfileEvent.AcceptFriendRequest) },
                    onCancelSendRequest = { store.dispatchEvent(UserProfileEvent.CancelSendFriendRequest) },
                    onDeleteFromFriends = { store.dispatchEvent(UserProfileEvent.DeleteFromFriends) },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    )

    store.handleEffects { effect ->
        when (effect) {
            is UserProfileEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
internal fun BaseUserProfileContent(
    state: UserProfileState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    Column(
        modifier = modifier.padding(top = 24.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        UserSocialNetworksSection(
            isLoading = state.isLoading,
            socialNetworks = state.user?.socialNetworks ?: emptyList(),
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun UserSocialNetworksSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    socialNetworks: List<SocialNetworkUi>,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = UsersThemeRes.strings.userProfileSocialNetworksHeader,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        ) { loading ->
            if (loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(Placeholder.SOCIAL_NETWORKS) {
                        SocialNetworkPlaceholder()
                    }
                }
            } else if (socialNetworks.isNotEmpty()) {
                val clipboardManager = LocalClipboardManager.current
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    socialNetworks.forEach { socialNetwork ->
                        SocialNetworkView(
                            onClick = { clipboardManager.setText(AnnotatedString(socialNetwork.data)) },
                            type = socialNetwork.type,
                            otherTypeName = socialNetwork.otherType,
                            data = socialNetwork.data,
                        )
                    }
                }
            } else {
                NoneSocialNetworkView()
            }
        }
    }
}

@Composable
private fun SocialNetworkView(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    type: SocialNetworkType,
    otherTypeName: String?,
    data: String,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.widthIn(max = 140.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (type != SocialNetworkType.OTHER) {
                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(type.mapToIcon(StudyAssistantRes.icons)),
                    contentDescription = null,
                )
            } else {
                Icon(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(StudyAssistantRes.icons.otherSocialNetwork),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (type != SocialNetworkType.OTHER || otherTypeName == null) {
                        type.mapToString(StudyAssistantRes.strings)
                    } else {
                        otherTypeName
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = data,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun SocialNetworkPlaceholder(
    modifier: Modifier = Modifier,
) {
    PlaceholderBox(
        modifier = modifier.size(67.dp, 96.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

@Composable
private fun NoneSocialNetworkView(
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
                    imageVector = Icons.Default.Web,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = UsersThemeRes.strings.noneSocialNetworksTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}