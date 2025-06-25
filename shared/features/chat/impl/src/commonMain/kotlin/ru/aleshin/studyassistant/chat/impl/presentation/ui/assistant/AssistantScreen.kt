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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant

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
import co.touchlab.kermit.Logger
import ru.aleshin.studyassistant.chat.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.chat.impl.presentation.theme.ChatThemeRes
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEffect
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEvent
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantViewState
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.screenmodel.rememberAssistantScreenModel
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantBottomBar
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantTopBar
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 20.06.2025
 */
internal class AssistantScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberAssistantScreenModel(),
        initialState = AssistantViewState(),
    ) { state ->
        val strings = ChatThemeRes.strings
        val navigator = LocalNavigator.current
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                AssistantContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onSendMessageSuggestion = { dispatchEvent(AssistantEvent.SendMessage(it)) },
                )
            },
            topBar = {
                AssistantTopBar(
                    isVisibleClearButton = !state.isLoadingResponse && !state.chatHistory?.messages.isNullOrEmpty(),
                    onClearChatHistory = { dispatchEvent(AssistantEvent.ClearHistory) },
                )
            },
            bottomBar = {
                AssistantBottomBar(
                    isLoadingChat = state.isLoadingChat,
                    isLoadingResponse = state.isLoadingResponse,
                    userQuery = state.userQuery.query,
                    onUpdateUserQuery = { dispatchEvent(AssistantEvent.UpdateUserQuery(it)) },
                    onSendMessage = { dispatchEvent(AssistantEvent.SendMessage(it)) },
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
                is AssistantEffect.ShowError -> {
                    dispatchEvent(AssistantEvent.StopResponseLoading)
                    snackbarState.showSnackbar(
                        message = effect.failures.apply { Logger.e("test") { effect.failures.toString() } }.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
                is AssistantEffect.NavigateToGlobal -> navigator?.root()?.push(effect.pushScreen)
            }
        }
    }
}