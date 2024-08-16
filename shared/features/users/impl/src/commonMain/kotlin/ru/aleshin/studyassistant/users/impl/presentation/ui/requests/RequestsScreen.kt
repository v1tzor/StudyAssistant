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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.users.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.views.RequestsTab
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsViewState
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.screenmodel.rememberRequestsScreenModel
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.views.RequestsTabsRow
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.views.RequestsTopBar

/**
 * @author Stanislav Aleshin on 13.07.2024
 */
internal class RequestsScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberRequestsScreenModel(),
        initialState = RequestsViewState(),
    ) { state ->
        val strings = UsersThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val snackbarState = remember { SnackbarHostState() }
        val pagerState = rememberPagerState { RequestsTab.entries.size }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                RequestsContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    pagerState = pagerState,
                    onOpenUserProfile = { dispatchEvent(RequestsEvent.NavigateToFriendProfile(it)) },
                    onAcceptRequest = { dispatchEvent(RequestsEvent.AcceptFriendRequest(it)) },
                    onRejectRequest = { dispatchEvent(RequestsEvent.RejectFriendRequest(it)) },
                    onDeleteHistoryRequest = { dispatchEvent(RequestsEvent.DeleteHistoryRequest(it)) },
                    onCancelSendFriendRequest = { dispatchEvent(RequestsEvent.CancelSendFriendRequest(it)) },
                )
            },
            topBar = {
                Column {
                    RequestsTopBar(
                        onBackClick = { dispatchEvent(RequestsEvent.NavigateToBack) },
                    )
                    RequestsTabsRow(
                        pagerState = pagerState,
                        selectedTab = RequestsTab.byIndex(pagerState.currentPage),
                        requests = state.requests,
                        onChangeTab = { coroutineScope.launch { pagerState.animateScrollToPage(it.index) } },
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
                is RequestsEffect.NavigateToLocal -> navigator.push(effect.pushScreen)
                is RequestsEffect.NavigateToBack -> navigator.pop()
                is RequestsEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}