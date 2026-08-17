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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AiChatHistoryUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views.AssistantChatBody

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun AssistantCompactLayout(
    modifier: Modifier = Modifier,
    isLoadingChat: Boolean,
    responseStatus: ResponseStatus,
    isQuotaExpired: Boolean,
    quotaLimit: Int,
    rewardedResetsRemaining: Int,
    isRewardInProgress: Boolean,
    chatHistory: AiChatHistoryUi?,
    onSendMessageSuggestion: (String) -> Unit,
    onTryAgain: () -> Unit,
    onDeleteMessage: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onRequestQuotaReward: () -> Unit,
    onResolveToolCall: (String, Boolean) -> Unit,
) {
    AssistantChatBody(
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
}
