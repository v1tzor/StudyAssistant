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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.chat.impl.domain.entities.ChatFailures
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AiChatHistoryUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ChatQueryUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState

/**
 * @author Stanislav Aleshin on 20.06.2025
 */
@Immutable
@Parcelize
internal data class AssistantViewState(
    val isLoadingChat: Boolean = true,
    val isQuotaExpired: Boolean = false,
    val responseStatus: ResponseStatus = ResponseStatus.SUCCESS,
    val userQuery: ChatQueryUi = ChatQueryUi(),
    val chatHistory: AiChatHistoryUi? = null,
) : BaseViewState

internal sealed class AssistantEvent : BaseEvent {
    data object Init : AssistantEvent()
    data class SendMessage(val message: String) : AssistantEvent()
    data class UpdateUserQuery(val query: String) : AssistantEvent()
    data object StopResponseLoading : AssistantEvent()
    data object RetryAttempt : AssistantEvent()
    data object ClearUnsendMessage : AssistantEvent()
    data object ClearHistory : AssistantEvent()
    data object NavigateToBilling : AssistantEvent()
}

internal sealed class AssistantEffect : BaseUiEffect {
    data class NavigateToGlobal(val pushScreen: Screen) : AssistantEffect()
    data class ShowError(val failures: ChatFailures) : AssistantEffect()
}

internal sealed class AssistantAction : BaseAction {
    data class UpdateUserQuery(val query: ChatQueryUi) : AssistantAction()
    data class UpdateChatHistory(val chatHistory: AiChatHistoryUi?) : AssistantAction()
    data class UpdateLoadingChat(val isLoading: Boolean) : AssistantAction()
    data class UpdateResponseStatus(val responseStatus: ResponseStatus) : AssistantAction()
    data class UpdateQuotaExpiredStatus(val isQuotaExpired: Boolean) : AssistantAction()
}