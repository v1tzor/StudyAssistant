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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.users.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsViewState
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.screenmodel.rememberFriendsScreenModel
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.views.FriendsSearchTopBar

/**
 * @author Stanislav Aleshin on 12.07.2024
 */
internal class FriendsScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberFriendsScreenModel(),
        initialState = FriendsViewState(),
    ) { state ->
        val strings = UsersThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                FriendsContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onShowAllRequests = { dispatchEvent(FriendsEvent.NavigateToRequests) },
                    onOpenUserProfile = { dispatchEvent(FriendsEvent.NavigateToFriendProfile(it)) },
                    onAcceptRequest = { dispatchEvent(FriendsEvent.AcceptFriendRequest(it)) },
                    onRejectRequest = { dispatchEvent(FriendsEvent.RejectFriendRequest(it)) },
                    onDeleteFriend = { dispatchEvent(FriendsEvent.DeleteFriend(it)) },
                )
            },
            topBar = {
                FriendsSearchTopBar(
                    isLoadingSearch = state.isLoadingSearch,
                    searchedUsers = state.searchedUsers,
                    friendRequests = state.requests,
                    friends = state.friends.values.toList().extractAllItem(),
                    onBackPress = { dispatchEvent(FriendsEvent.NavigateToBack) },
                    onSearch = { dispatchEvent(FriendsEvent.SearchUsers(it)) },
                    onOpenUserProfile = { dispatchEvent(FriendsEvent.NavigateToFriendProfile(it)) },
                    onSendFriendRequest = { dispatchEvent(FriendsEvent.SendFriendRequest(it)) },
                    onCancelFriendRequest = { dispatchEvent(FriendsEvent.CancelSendFriendRequest(it)) },
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
        )

        handleEffect { effect ->
            when (effect) {
                is FriendsEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is FriendsEffect.NavigateToBack -> navigator.nestedPop()
                is FriendsEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings, coreStrings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}