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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AiChatHistoryUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.layouts.AssistantCompactLayout
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.layouts.AssistantExpandedLayout

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun AssistantLayout(
    modifier: Modifier = Modifier,
    layoutMode: AssistantLayoutMode,
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
    when (layoutMode) {
        AssistantLayoutMode.COMPACT -> AssistantCompactLayout(
            modifier = modifier,
            isLoadingChat = isLoadingChat,
            responseStatus = responseStatus,
            isQuotaExpired = isQuotaExpired,
            quotaLimit = quotaLimit,
            rewardedResetsRemaining = rewardedResetsRemaining,
            isRewardInProgress = isRewardInProgress,
            chatHistory = chatHistory,
            onSendMessageSuggestion = onSendMessageSuggestion,
            onTryAgain = onTryAgain,
            onDeleteMessage = onDeleteMessage,
            onOpenAiSettings = onOpenAiSettings,
            onRequestQuotaReward = onRequestQuotaReward,
            onResolveToolCall = onResolveToolCall,
        )
        AssistantLayoutMode.EXPANDED -> AssistantExpandedLayout(
            modifier = modifier,
            isLoadingChat = isLoadingChat,
            responseStatus = responseStatus,
            isQuotaExpired = isQuotaExpired,
            quotaRemaining = quotaRemaining,
            quotaLimit = quotaLimit,
            rewardedResetsRemaining = rewardedResetsRemaining,
            isRewardInProgress = isRewardInProgress,
            isVisibleClearButton = isVisibleClearButton,
            isInputEnabled = isInputEnabled,
            userQuery = userQuery,
            chatHistory = chatHistory,
            onScheduleImport = onScheduleImport,
            onClearChatHistory = onClearChatHistory,
            onUpdateUserQuery = onUpdateUserQuery,
            onSendMessage = onSendMessage,
            onSendMessageSuggestion = onSendMessageSuggestion,
            onTryAgain = onTryAgain,
            onDeleteMessage = onDeleteMessage,
            onOpenAiSettings = onOpenAiSettings,
            onRequestQuotaReward = onRequestQuotaReward,
            onResolveToolCall = onResolveToolCall,
        )
    }
}
