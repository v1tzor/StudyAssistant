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

package ru.aleshin.studyassistant.core.data.handlers

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToLocal
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToRemote
import ru.aleshin.studyassistant.core.database.datasource.ai.AiLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantResponse
import ru.aleshin.studyassistant.core.domain.entities.ai.dropUnconfirmedMessages
import ru.aleshin.studyassistant.core.domain.entities.ai.dropUntilConfirmedMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.optimisedMessagesForSend
import ru.aleshin.studyassistant.core.remote.api.ai.AiRemoteApi
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionToolChoicePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ChatModel
import ru.aleshin.studyassistant.core.remote.models.ai.UserMessagePojo

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AiConversationHandler {

    suspend fun retryLastMessage(chatId: UID): AiAssistantMessage.AssistantMessage?
    suspend fun sendUserMessage(
        chatId: UID,
        message: AiAssistantMessage.UserMessage?,
    ): AiAssistantResponse

    suspend fun sendToolResponse(
        chatId: UID,
        messages: List<AiAssistantMessage.ToolMessage>,
    ): AiAssistantResponse

    suspend fun deleteUnconfirmedMessages(chatId: UID)
    suspend fun testPersonalKey(apiKey: String)

    class Base(
        private val localDataSource: AiLocalDataSource,
        private val completionHandler: AiCompletionHandler,
        private val dateManager: DateManager,
    ) : AiConversationHandler {

        override suspend fun retryLastMessage(
            chatId: UID,
        ): AiAssistantMessage.AssistantMessage? {
            val messages = localDataSource.fetchChatHistoryById(chatId).first()
                ?.messages
                ?.map { it.mapToDomain() }
            if (messages.isNullOrEmpty()) throw NoSuchElementException()

            val assistantMessage = if (
                messages.last { it !is AiAssistantMessage.SystemMessage } is
                AiAssistantMessage.UserMessage
            ) {
                complete(messages)
            } else {
                messages.dropUntilConfirmedMessage { message ->
                    localDataSource.deleteChatMessage(message.id)
                }
            }
            return assistantMessage as? AiAssistantMessage.AssistantMessage
        }

        override suspend fun sendUserMessage(
            chatId: UID,
            message: AiAssistantMessage.UserMessage?,
        ): AiAssistantResponse {
            deleteUnconfirmedMessages(chatId)
            if (message != null) localDataSource.addChatMessage(message.mapToLocal(chatId))

            val messages = localDataSource.fetchChatHistoryById(chatId).first()
                ?.messages
                ?.map { it.mapToDomain() }
            if (messages.isNullOrEmpty()) throw NoSuchElementException()
            return completeResponse(messages)
        }

        override suspend fun sendToolResponse(
            chatId: UID,
            messages: List<AiAssistantMessage.ToolMessage>,
        ): AiAssistantResponse {
            localDataSource.addChatMessages(messages.map { it.mapToLocal(chatId) })
            val historyMessages = localDataSource.fetchChatHistoryById(chatId).first()
                ?.messages
                ?.map { it.mapToDomain() }
            if (historyMessages.isNullOrEmpty()) throw NoSuchElementException()
            return completeResponse(historyMessages)
        }

        override suspend fun deleteUnconfirmedMessages(chatId: UID) {
            val messages = localDataSource.fetchChatHistoryById(chatId).first()
                ?.messages
                ?.map { it.mapToDomain() }
            messages?.dropUnconfirmedMessages { message ->
                localDataSource.deleteChatMessage(message.id)
            }
        }

        override suspend fun testPersonalKey(apiKey: String) {
            completionHandler.testPersonalKey(
                request = ChatCompletionRequestPojo(
                    model = ChatModel.DEEPSEEK_CHAT.model,
                    messages = listOf(UserMessagePojo(content = "Reply with OK")),
                    maxTokens = 2,
                ),
                apiKey = apiKey,
            )
        }

        private suspend fun complete(
            messages: List<AiAssistantMessage>,
        ): AiAssistantMessage? {
            return completeResponse(messages).choices.firstOrNull()?.message
        }

        private suspend fun completeResponse(
            messages: List<AiAssistantMessage>,
        ): AiAssistantResponse {
            val optimisedMessages = messages.optimisedMessagesForSend()
            val request = ChatCompletionRequestPojo(
                model = ChatModel.DEEPSEEK_CHAT.model,
                messages = optimisedMessages.map { it.mapToRemote() },
                tools = AI_TOOLS,
                toolChoice = ChatCompletionToolChoicePojo.AUTO,
            )
            return completionHandler.complete(
                request = request,
                requestKey = optimisedMessages.last { it is AiAssistantMessage.UserMessage }.id,
            ).mapToDomain(
                time = dateManager.fetchCurrentInstant(),
            )
        }

        private companion object {
            val AI_TOOLS = listOf(
                AiRemoteApi.DeepSeek.createTodoTool,
                AiRemoteApi.DeepSeek.createHomework,
                AiRemoteApi.DeepSeek.getOrganizationsTool,
                AiRemoteApi.DeepSeek.getSubjectsTool,
                AiRemoteApi.DeepSeek.getEmployeeTool,
                AiRemoteApi.DeepSeek.getHomeworksTool,
                AiRemoteApi.DeepSeek.getOverdueHomeworksTool,
                AiRemoteApi.DeepSeek.getClassesByDateTool,
                AiRemoteApi.DeepSeek.getNearClassTool,
            )
        }
    }
}
