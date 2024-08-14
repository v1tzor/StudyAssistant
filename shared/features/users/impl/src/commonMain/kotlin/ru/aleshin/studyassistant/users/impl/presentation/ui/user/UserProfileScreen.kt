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

import androidx.compose.foundation.layout.Column
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
import co.touchlab.kermit.Logger
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.users.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileDeps
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileViewState
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.screenmodel.rememberUserProfileScreenModel
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.views.UserProfileTopBar
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.views.UserProfileTopSheet

/**
 * @author Stanislav Aleshin on 15.07.2024
 */
internal class UserProfileScreen(val userId: UID) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberUserProfileScreenModel(),
        initialState = UserProfileViewState(),
        dependencies = UserProfileDeps(userId = userId),
    ) { state ->
        val strings = UsersThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                UserProfileContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                )
            },
            topBar = {
                Column {
                    UserProfileTopBar(
                        onBackClick = { dispatchEvent(UserProfileEvent.NavigateToBack) },
                    )
                    UserProfileTopSheet(
                        isLoading = state.isLoading,
                        user = state.user,
                        friendStatus = state.friendStatus,
                        onAddToFriends = { dispatchEvent(UserProfileEvent.SendFriendRequest) },
                        onCancelSendRequest = { dispatchEvent(UserProfileEvent.CancelSendFriendRequest) },
                        onDeleteFromFriends = { dispatchEvent(UserProfileEvent.DeleteFromFriends) },
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

        handleEffect { effect ->
            when (effect) {
                is UserProfileEffect.NavigateToBack -> navigator.nestedPop()
                is UserProfileEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.apply {
                            Logger.i("test") { "error -> $this" }
                        }.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}