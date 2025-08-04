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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.billing.api.navigation.BillingScreen
import ru.aleshin.studyassistant.chat.impl.di.holder.ChatFeatureDIHolder
import ru.aleshin.studyassistant.chat.impl.navigation.ChatScreenProvider
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantAction
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEffect
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEvent
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantViewState
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager

/**
 * @author Stanislav Aleshin on 20.06.2025
 */
internal class AssistantScreenModel(
    private val workProcessor: AssistantWorkProcessor,
    private val screenProvider: ChatScreenProvider,
    stateCommunicator: AssistantStateCommunicator,
    effectCommunicator: AssistantEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<AssistantViewState, AssistantEvent, AssistantAction, AssistantEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(AssistantEvent.Init)
        }
    }

    override suspend fun WorkScope<AssistantViewState, AssistantAction, AssistantEffect>.handleEvent(
        event: AssistantEvent,
    ) {
        when (event) {
            is AssistantEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_MESSAGES) {
                    val command = AssistantWorkCommand.LoadMessages
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_QUOTA_EXPIRED_STATUS) {
                    val command = AssistantWorkCommand.LoadQuotaExpiredStatus
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AssistantEvent.SendMessage -> with(event) {
                launchBackgroundWork(BackgroundKey.SEND_MESSAGE) {
                    val command = AssistantWorkCommand.SendMessage(state().chatHistory?.uid, message)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AssistantEvent.RetryAttempt -> with(state()) {
                launchBackgroundWork(BackgroundKey.SEND_MESSAGE) {
                    val command = AssistantWorkCommand.RetryAttempt(chatHistory?.uid)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AssistantEvent.ClearUnsendMessage -> with(state()) {
                launchBackgroundWork(BackgroundKey.MESSAGE_ACTION) {
                    val command = AssistantWorkCommand.ClearUnsendMessage(chatHistory?.uid)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AssistantEvent.ClearHistory -> with(state()) {
                val chatId = chatHistory?.uid
                if (chatId != null) {
                    launchBackgroundWork(BackgroundKey.MESSAGE_ACTION) {
                        val command = AssistantWorkCommand.ClearChatHistory(chatId)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is AssistantEvent.UpdateUserQuery -> with(event) {
                val query = state().userQuery.copy(query)
                sendAction(AssistantAction.UpdateUserQuery(query))
            }
            is AssistantEvent.NavigateToBilling -> {
                val screen = screenProvider.provideBillingScreen(BillingScreen.Subscription)
                sendEffect(AssistantEffect.NavigateToGlobal(screen))
            }
            is AssistantEvent.StopResponseLoading -> {
                sendAction(AssistantAction.UpdateResponseStatus(ResponseStatus.FAILURE))
            }
        }
    }

    override suspend fun reduce(
        action: AssistantAction,
        currentState: AssistantViewState,
    ) = when (action) {
        is AssistantAction.UpdateLoadingChat -> currentState.copy(
            isLoadingChat = action.isLoading,
        )
        is AssistantAction.UpdateUserQuery -> currentState.copy(
            userQuery = action.query,
        )
        is AssistantAction.UpdateResponseStatus -> currentState.copy(
            responseStatus = action.responseStatus,
        )
        is AssistantAction.UpdateChatHistory -> currentState.copy(
            chatHistory = action.chatHistory,
            isLoadingChat = false,
        )
        is AssistantAction.UpdateQuotaExpiredStatus -> currentState.copy(
            isQuotaExpired = action.isQuotaExpired,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_MESSAGES, LOAD_QUOTA_EXPIRED_STATUS, SEND_MESSAGE, MESSAGE_ACTION
    }
}

@Composable
internal fun Screen.rememberAssistantScreenModel(): AssistantScreenModel {
    val di = ChatFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<AssistantScreenModel>() }
}