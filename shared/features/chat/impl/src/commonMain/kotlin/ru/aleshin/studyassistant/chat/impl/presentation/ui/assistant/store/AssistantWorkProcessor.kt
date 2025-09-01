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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.store

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.chat.impl.domain.interactors.AiAssistantInteractor
import ru.aleshin.studyassistant.chat.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.ResponseStatus
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantAction
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantEffect
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantOutput
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.functional.handleAndGet
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage

/**
 * @author Stanislav Aleshin on 20.06.2025.
 */
internal interface AssistantWorkProcessor :
    FlowWorkProcessor<AssistantWorkCommand, AssistantAction, AssistantEffect, AssistantOutput> {

    class Base(
        private val aiAssistantInteractor: AiAssistantInteractor,
    ) : AssistantWorkProcessor {

        override suspend fun work(command: AssistantWorkCommand) = when (command) {
            is AssistantWorkCommand.LoadMessages -> loadMessagesWork()
            is AssistantWorkCommand.LoadQuotaExpiredStatus -> loadQuotaExpiredStatusWork()
            is AssistantWorkCommand.ClearChatHistory -> clearChatHistoryWork(command.chatId)
            is AssistantWorkCommand.SendMessage -> sendMessageWork(command.chatId, command.message)
            is AssistantWorkCommand.RetryAttempt -> retryAttemptWork(command.chatId)
            is AssistantWorkCommand.ClearUnsendMessage -> clearUnsendMessageWork(command.chatId)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadMessagesWork() = channelFlow<AssistantWorkResult> {
            var isChatCreate = false
            var isInit = false
            aiAssistantInteractor.fetchChats().flatMapLatest { chatsEither ->
                chatsEither.handleAndGet(
                    onLeftAction = { flowOf(EffectResult(AssistantEffect.ShowError(it))) },
                    onRightAction = { chats ->
                        val targetChatId = chats.firstOrNull()?.uid
                        if (targetChatId != null) {
                            aiAssistantInteractor.fetchChatHistory(targetChatId).map { chatHistoryEither ->
                                chatHistoryEither.handleAndGet(
                                    onLeftAction = { EffectResult(AssistantEffect.ShowError(it)) },
                                    onRightAction = { chatHistory ->
                                        if (!isInit) {
                                            if (chatHistory.messages.firstOrNull() is AiAssistantMessage.UserMessage) {
                                                val action = AssistantAction.UpdateResponseStatus(ResponseStatus.FAILURE)
                                                send(ActionResult(action))
                                            }
                                            isInit = true
                                        }
                                        ActionResult(AssistantAction.UpdateChatHistory(chatHistory.mapToUi()))
                                    }
                                )
                            }
                        } else {
                            if (!isChatCreate) {
                                aiAssistantInteractor.addChat().handle(
                                    onLeftAction = { send(EffectResult(AssistantEffect.ShowError(it))) },
                                    onRightAction = { isChatCreate = true },
                                )
                            }
                            flowOf(ActionResult(AssistantAction.UpdateChatHistory(null)))
                        }
                    },
                )
            }.collect { result ->
                send(result)
            }
        }.onStart {
            emit(ActionResult(AssistantAction.UpdateLoadingChat(true)))
        }

        private fun loadQuotaExpiredStatusWork() = flow {
            aiAssistantInteractor.quotaIsExpired().collectAndHandle(
                onLeftAction = { emit(EffectResult(AssistantEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(AssistantAction.UpdateQuotaExpiredStatus(it))) },
            )
        }

        private fun clearChatHistoryWork(chatId: UID) = flow {
            aiAssistantInteractor.clearHistory(chatId).handle(
                onLeftAction = { emit(EffectResult(AssistantEffect.ShowError(it))) },
            )
        }

        private fun sendMessageWork(chatId: UID?, message: String) = flow<AssistantWorkResult> {
            if (chatId != null) {
                aiAssistantInteractor.sendMessage(chatId, message).handle(
                    onLeftAction = {
                        val action = AssistantAction.UpdateResponseStatus(ResponseStatus.FAILURE)
                        emit(ActionResult(action))
                    },
                    onRightAction = {
                        val action = AssistantAction.UpdateResponseStatus(ResponseStatus.SUCCESS)
                        emit(ActionResult(action))
                    }
                )
            }
        }.onStart {
            emit(ActionResult(AssistantAction.UpdateResponseStatus(ResponseStatus.LOADING)))
        }

        private fun retryAttemptWork(chatId: String?) = flow<AssistantWorkResult> {
            if (chatId != null) {
                aiAssistantInteractor.retryAttempt(chatId).handle(
                    onLeftAction = {
                        val action = AssistantAction.UpdateResponseStatus(ResponseStatus.FAILURE)
                        emit(ActionResult(action))
                    },
                    onRightAction = {
                        val action = AssistantAction.UpdateResponseStatus(ResponseStatus.SUCCESS)
                        emit(ActionResult(action))
                    }
                )
            }
        }.onStart {
            emit(ActionResult(AssistantAction.UpdateResponseStatus(ResponseStatus.LOADING)))
        }

        private fun clearUnsendMessageWork(chatId: String?) = flow<AssistantWorkResult> {
            if (chatId != null) {
                aiAssistantInteractor.clearUnsendMessage(chatId).handle(
                    onLeftAction = { emit(EffectResult(AssistantEffect.ShowError(it))) },
                    onRightAction = {
                        val action = AssistantAction.UpdateResponseStatus(ResponseStatus.SUCCESS)
                        emit(ActionResult(action))
                    }
                )
            }
        }
    }
}

internal sealed class AssistantWorkCommand : WorkCommand {
    data object LoadMessages : AssistantWorkCommand()
    data object LoadQuotaExpiredStatus : AssistantWorkCommand()
    data class ClearChatHistory(val chatId: UID) : AssistantWorkCommand()
    data class SendMessage(val chatId: UID?, val message: String) : AssistantWorkCommand()
    data class RetryAttempt(val chatId: UID?) : AssistantWorkCommand()
    data class ClearUnsendMessage(val chatId: UID?) : AssistantWorkCommand()
}

internal typealias AssistantWorkResult = WorkResult<AssistantAction, AssistantEffect, AssistantOutput>