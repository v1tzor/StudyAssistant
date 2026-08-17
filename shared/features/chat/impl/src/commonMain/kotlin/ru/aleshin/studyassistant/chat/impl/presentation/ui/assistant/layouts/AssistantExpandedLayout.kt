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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AiChatHistoryUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantChatBody
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantExpandedTopBar
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantInputIsland

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun AssistantExpandedLayout(
    modifier: Modifier = Modifier,
    isLoadingChat: Boolean,
    responseStatus: ResponseStatus,
    isQuotaExpired: Boolean,
    quotaRemaining: Int,
    quotaLimit: Int,
    rewardedResetsRemaining: Int,
    isRewardInProgress: Boolean,
    isVisibleClearButton: Boolean,
    isInputEnabled: Boolean,
    userQuery: String,
    chatHistory: AiChatHistoryUi?,
    onScheduleImport: () -> Unit,
    onClearChatHistory: () -> Unit,
    onUpdateUserQuery: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSendMessageSuggestion: (String) -> Unit,
    onTryAgain: () -> Unit,
    onDeleteMessage: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onRequestQuotaReward: () -> Unit,
    onResolveToolCall: (String, Boolean) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AssistantExpandedTopBar(
            modifier = Modifier.fillMaxWidth(),
            quotaRemaining = quotaRemaining,
            quotaLimit = quotaLimit,
            isVisibleClearButton = isVisibleClearButton,
            onScheduleImport = onScheduleImport,
            onClearChatHistory = onClearChatHistory,
        )
        AssistantChatBody(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            isLoadingChat = isLoadingChat,
            responseStatus = responseStatus,
            isQuotaExpired = isQuotaExpired,
            quotaLimit = quotaLimit,
            rewardedResetsRemaining = rewardedResetsRemaining,
            isRewardInProgress = isRewardInProgress,
            chatHistory = chatHistory,
            contentMaxWidth = ASSISTANT_CONTENT_MAX_WIDTH,
            onSendMessageSuggestion = onSendMessageSuggestion,
            onTryAgain = onTryAgain,
            onDeleteMessage = onDeleteMessage,
            onOpenAiSettings = onOpenAiSettings,
            onRequestQuotaReward = onRequestQuotaReward,
            onResolveToolCall = onResolveToolCall,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    insets = WindowInsets.ime.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom)
                )
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            AssistantInputIsland(
                modifier = Modifier
                    .widthIn(max = ASSISTANT_CONTENT_MAX_WIDTH)
                    .fillMaxWidth(),
                isLoadingChat = isLoadingChat,
                responseStatus = responseStatus,
                isQuotaExpired = isQuotaExpired,
                isInputEnabled = isInputEnabled,
                userQuery = userQuery,
                onUpdateUserQuery = onUpdateUserQuery,
                onSendMessage = onSendMessage,
            )
        }
    }
}

internal val ASSISTANT_CONTENT_MAX_WIDTH = 720.dp
