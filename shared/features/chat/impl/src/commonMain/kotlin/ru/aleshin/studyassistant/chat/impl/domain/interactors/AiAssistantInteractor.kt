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

package ru.aleshin.studyassistant.chat.impl.domain.interactors

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.aleshin.studyassistant.chat.impl.domain.common.ChatEitherWrapper
import ru.aleshin.studyassistant.chat.impl.domain.entities.AiToolConfirmationData
import ru.aleshin.studyassistant.chat.impl.domain.entities.AssistantChatData
import ru.aleshin.studyassistant.chat.impl.domain.entities.ChatFailures
import ru.aleshin.studyassistant.chat.impl.domain.tools.AiToolCallProcessor
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardChallenge
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardPurpose
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChat
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChatHistory
import ru.aleshin.studyassistant.core.domain.entities.ai.AiSettings
import ru.aleshin.studyassistant.core.domain.repositories.AdRewardRepository
import ru.aleshin.studyassistant.core.domain.repositories.AiAssistantRepository
import ru.aleshin.studyassistant.core.domain.repositories.AiSettingsRepository

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
internal interface AiAssistantInteractor {

    suspend fun addChat(): DomainResult<ChatFailures, UID>
    suspend fun fetchAiSettings(): FlowDomainResult<ChatFailures, AiSettings>
    suspend fun createQuotaReward(): DomainResult<ChatFailures, AdRewardChallenge>
    suspend fun completeQuotaReward(challengeId: String): UnitDomainResult<ChatFailures>
    suspend fun fetchChats(): FlowDomainResult<ChatFailures, List<AiChat>>
    suspend fun fetchChatHistory(chatId: UID): FlowDomainResult<ChatFailures, AssistantChatData>
    suspend fun clearHistory(chatId: UID): UnitDomainResult<ChatFailures>
    suspend fun sendMessage(chatId: UID, message: String?): UnitDomainResult<ChatFailures>
    suspend fun resolveToolCall(
        chatId: UID,
        toolCallId: UID,
        approved: Boolean,
    ): UnitDomainResult<ChatFailures>
    suspend fun retryAttempt(chatId: UID): UnitDomainResult<ChatFailures>
    suspend fun clearUnsendMessage(chatId: UID): UnitDomainResult<ChatFailures>

    class Base(
        private val aiAssistantRepository: AiAssistantRepository,
        private val aiSettingsRepository: AiSettingsRepository,
        private val adRewardRepository: AdRewardRepository,
        private val toolCallProcessor: AiToolCallProcessor,
        private val dateManager: DateManager,
        private val eitherWrapper: ChatEitherWrapper,
    ) : AiAssistantInteractor {

        private val toolDecisionMutex = Mutex()

        override suspend fun addChat() = eitherWrapper.wrap {
            val chatId = randomUUID()
            aiAssistantRepository.addOrUpdateChat(AiChatHistory(uid = chatId, messages = emptyList()))
            chatId
        }

        override suspend fun fetchAiSettings() = eitherWrapper.wrapFlow {
            aiSettingsRepository.fetchSettings()
        }

        override suspend fun createQuotaReward() = eitherWrapper.wrap {
            adRewardRepository.createChallenge(AdRewardPurpose.AI_QUOTA_RESET)
        }

        override suspend fun completeQuotaReward(challengeId: String) = eitherWrapper.wrapUnit {
            adRewardRepository.completeChallenge(challengeId)
        }

        override suspend fun fetchChats() = eitherWrapper.wrapFlow {
            aiAssistantRepository.fetchAllChats()
        }

        override suspend fun fetchChatHistory(chatId: UID) = eitherWrapper.wrapFlow {
            aiAssistantRepository.fetchChatHistoryById(chatId).map { chat ->
                val history = checkNotNull(chat) { "Chat($chatId) is not found" }
                val visibleMessages = history.messages.filter { message ->
                    (message is AiAssistantMessage.UserMessage || message is AiAssistantMessage.AssistantMessage) && !message.content.isNullOrEmpty()
                }.sortedByDescending(AiAssistantMessage::time)
                AssistantChatData(
                    history = history.copy(
                        messages = visibleMessages,
                        lastMessage = history.lastMessage?.takeIf { message ->
                            (message is AiAssistantMessage.UserMessage || message is AiAssistantMessage.AssistantMessage) && !message.content.isNullOrEmpty()
                        },
                    ),
                    pendingMutations = toolCallProcessor.pendingMutations(history.messages).map { call ->
                        AiToolConfirmationData(
                            call = call,
                            preview = toolCallProcessor.confirmationPreview(call),
                        )
                    },
                )
            }.distinctUntilChangedBy { data ->
                data.history.messages to data.pendingMutations
            }
        }

        override suspend fun clearHistory(chatId: UID) = eitherWrapper.wrapUnit {
            val chat = aiAssistantRepository.fetchChatHistoryById(chatId).first()
            if (chat != null) {
                aiAssistantRepository.addOrUpdateChat(chat.copy(messages = emptyList(), lastMessage = null))
            }
        }

        override suspend fun sendMessage(chatId: UID, message: String?) = eitherWrapper.wrapUnit {
            recoverUnconfirmedOnFailure(chatId) {
                val userMessage = message?.let { content ->
                    AiAssistantMessage.UserMessage(
                        content = content,
                        time = dateManager.fetchCurrentInstant(),
                    )
                }
                val response = aiAssistantRepository.sendUserMessage(chatId, userMessage)
                handleMessage(
                    chatId = chatId,
                    assistantMessage = response.choices.firstOrNull()?.message,
                    toolRound = 0,
                )
            }
        }

        override suspend fun resolveToolCall(
            chatId: UID,
            toolCallId: UID,
            approved: Boolean,
        ) = eitherWrapper.wrapUnit {
            recoverUnconfirmedOnFailure(chatId) {
                toolDecisionMutex.withLock {
                    val history = checkNotNull(aiAssistantRepository.fetchChatHistoryById(chatId).first())
                    val activeCalls = toolCallProcessor.activeCalls(history.messages)
                    val targetCall = activeCalls.find { it.id == toolCallId } ?: throw IllegalArgumentException("Tool call is not pending")
                    require(toolCallProcessor.isMutation(targetCall)) {
                        "Read-only tool cannot be confirmed"
                    }

                    aiAssistantRepository.saveToolResponses(
                        chatId = chatId,
                        messages = listOf(toolCallProcessor.execute(targetCall, approved)),
                    )

                    val updatedHistory = checkNotNull(
                        aiAssistantRepository.fetchChatHistoryById(chatId).first(),
                    )
                    val unresolvedCalls = toolCallProcessor.activeCalls(updatedHistory.messages)
                    if (unresolvedCalls.any(toolCallProcessor::isMutation)) return@withLock

                    val readResults = unresolvedCalls.map { call -> toolCallProcessor.execute(call) }
                    if (readResults.isNotEmpty()) {
                        aiAssistantRepository.saveToolResponses(chatId, readResults)
                    }
                    val response = aiAssistantRepository.completeToolRound(chatId)
                    handleMessage(
                        chatId = chatId,
                        assistantMessage = response.choices.firstOrNull()?.message,
                        toolRound = countToolRounds(updatedHistory.messages),
                    )
                }
            }
        }

        override suspend fun retryAttempt(chatId: UID) = eitherWrapper.wrapUnit {
            recoverUnconfirmedOnFailure(chatId) {
                val assistantMessage = aiAssistantRepository.retrySendLastMessage(chatId) ?: return@recoverUnconfirmedOnFailure
                handleMessage(chatId, assistantMessage, toolRound = 0)
            }
        }

        override suspend fun clearUnsendMessage(chatId: UID) = eitherWrapper.wrapUnit {
            aiAssistantRepository.deleteUnconfirmedMessages(chatId)
        }

        private suspend fun handleMessage(
            chatId: UID,
            assistantMessage: AiAssistantMessage?,
            toolRound: Int,
        ) {
            val message = checkNotNull(assistantMessage as? AiAssistantMessage.AssistantMessage)
            val toolCalls = message.toolCalls.orEmpty()
            aiAssistantRepository.saveAssistantMessage(chatId, message)

            if (toolCalls.isEmpty()) return
            if (toolCalls.any(toolCallProcessor::isMutation)) return

            val results = toolCalls.map { call -> toolCallProcessor.execute(call) }
            aiAssistantRepository.saveToolResponses(chatId, results)
            if (toolRound + 1 >= MAX_TOOL_ROUNDS) return

            val response = aiAssistantRepository.completeToolRound(chatId)
            handleMessage(
                chatId = chatId,
                assistantMessage = response.choices.firstOrNull()?.message,
                toolRound = toolRound + 1,
            )
        }

        private suspend fun recoverUnconfirmedOnFailure(
            chatId: UID,
            block: suspend () -> Unit,
        ) {
            try {
                block()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                aiAssistantRepository.deleteUnconfirmedMessages(chatId)
                throw error
            }
        }

        private fun countToolRounds(messages: List<AiAssistantMessage>): Int {
            val ordered = messages.sortedBy(AiAssistantMessage::time)
            val lastUserIndex = ordered.indexOfLast { it is AiAssistantMessage.UserMessage }
            return ordered.drop(lastUserIndex + 1)
                .filterIsInstance<AiAssistantMessage.AssistantMessage>()
                .count { !it.toolCalls.isNullOrEmpty() }
        }

        private companion object {
            const val MAX_TOOL_ROUNDS = 8
        }
    }
}
