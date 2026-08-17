/*
 * Copyright 2026 Stanislav Aleshin
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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.chat.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEffect
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEvent
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.store.ChatFeatureComponent
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantBottomBar
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantTopBar
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.ads.LocalAdsConfiguration
import ru.aleshin.studyassistant.core.ui.ads.YandexRewardedAdHost
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 20.06.2025
 */
@Composable
internal fun AssistantContent(
    assistantComponent: ChatFeatureComponent,
    modifier: Modifier = Modifier,
) {
    val store = assistantComponent.store
    val state by store.stateAsState()
    val snackbarState = remember { SnackbarHostState() }
    val adsConfiguration = LocalAdsConfiguration.current
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val layoutMode = adaptiveInfo.fetchAssistantLayoutMode()
    val chatHistory = state.chatHistory
    val isVisibleClearButton = state.responseStatus != ResponseStatus.LOADING && !state.chatHistory?.messages.isNullOrEmpty()
    val isInputEnabled = chatHistory != null && chatHistory.pendingMutations.isEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(),
        topBar = {
            if (layoutMode.showScaffoldChrome) {
                AssistantTopBar(
                    isVisibleClearButton = isVisibleClearButton,
                    onClearChatHistory = { store.dispatchEvent(AssistantEvent.ClearHistory) },
                    onScheduleImport = {
                        store.dispatchEvent(AssistantEvent.OpenScheduleImport)
                    },
                )
            }
        },
        bottomBar = {
            if (layoutMode.showScaffoldChrome) {
                AssistantBottomBar(
                    isLoadingChat = state.isLoadingChat,
                    responseStatus = state.responseStatus,
                    isQuotaExpired = state.isQuotaExpired,
                    isInputEnabled = isInputEnabled,
                    userQuery = state.userQuery.query,
                    onUpdateUserQuery = { store.dispatchEvent(AssistantEvent.UpdateUserQuery(it)) },
                    onSendMessage = { store.dispatchEvent(AssistantEvent.SendMessage(it)) },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    ) { paddingValues ->
        AssistantLayout(
            modifier = Modifier.padding(paddingValues),
            layoutMode = layoutMode,
            isLoadingChat = state.isLoadingChat,
            responseStatus = state.responseStatus,
            isQuotaExpired = state.isQuotaExpired,
            quotaRemaining = state.quotaRemaining,
            quotaLimit = state.quotaLimit,
            rewardedResetsRemaining = state.rewardedResetsRemaining,
            isRewardInProgress = state.isRewardInProgress,
            isVisibleClearButton = isVisibleClearButton,
            isInputEnabled = isInputEnabled,
            userQuery = state.userQuery.query,
            chatHistory = chatHistory,
            onScheduleImport = { store.dispatchEvent(AssistantEvent.OpenScheduleImport) },
            onClearChatHistory = { store.dispatchEvent(AssistantEvent.ClearHistory) },
            onUpdateUserQuery = { store.dispatchEvent(AssistantEvent.UpdateUserQuery(it)) },
            onSendMessage = { store.dispatchEvent(AssistantEvent.SendMessage(it)) },
            onSendMessageSuggestion = { store.dispatchEvent(AssistantEvent.SendMessage(it)) },
            onTryAgain = { store.dispatchEvent(AssistantEvent.RetryAttempt) },
            onDeleteMessage = { store.dispatchEvent(AssistantEvent.ClearUnsendMessage) },
            onOpenAiSettings = { store.dispatchEvent(AssistantEvent.OpenAiSettings) },
            onRequestQuotaReward = { store.dispatchEvent(AssistantEvent.RequestQuotaReward) },
            onResolveToolCall = { toolCallId, approved ->
                store.dispatchEvent(AssistantEvent.ResolveToolCall(toolCallId, approved))
            },
        )
    }

    YandexRewardedAdHost(
        adUnitId = adsConfiguration?.aiQuotaRewardedId.orEmpty(),
        requestKey = state.rewardChallengeId,
        onRewarded = { challengeId ->
            store.dispatchEvent(AssistantEvent.RewardedAdGranted(challengeId))
        },
        onUnavailable = { store.dispatchEvent(AssistantEvent.RewardedAdUnavailable) },
    )

    store.handleEffects { effect ->
        when (effect) {
            is AssistantEffect.ShowError -> {
                if (state.responseStatus == ResponseStatus.LOADING) {
                    store.dispatchEvent(AssistantEvent.StopResponseLoading)
                }
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}
