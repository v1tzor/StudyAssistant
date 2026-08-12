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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.handlers.AiConversationHandler
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToLocal
import ru.aleshin.studyassistant.core.database.datasource.ai.AiLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantResponse
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChat
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChatHistory
import ru.aleshin.studyassistant.core.domain.repositories.AiAssistantRepository

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
internal class AiAssistantRepositoryImpl(
    private val localDataSource: AiLocalDataSource,
    private val conversationHandler: AiConversationHandler,
) : AiAssistantRepository {

    override suspend fun addOrUpdateChat(chatHistory: AiChatHistory) {
        localDataSource.addOrUpdateChat(chatHistory.mapToLocal())
    }

    override suspend fun fetchAllChats(): Flow<List<AiChat>> {
        return localDataSource.fetchAllChats().map { chats ->
            chats.map { chat -> chat.mapToDomain() }
        }
    }

    override suspend fun fetchChatHistoryById(uid: UID): Flow<AiChatHistory?> {
        return localDataSource.fetchChatHistoryById(uid).map { chatHistory ->
            chatHistory?.mapToDomain()
        }
    }

    override suspend fun fetchChatHistoryLastMessage(chatId: UID): Flow<AiAssistantMessage?> {
        return localDataSource.fetchChatHistoryLastMessage(chatId).map { message ->
            message?.mapToDomain()
        }
    }

    override suspend fun retrySendLastMessage(chatId: UID): AiAssistantMessage.AssistantMessage? {
        return conversationHandler.retryLastMessage(chatId)
    }

    override suspend fun sendUserMessage(
        chatId: UID,
        message: AiAssistantMessage.UserMessage?,
    ): AiAssistantResponse {
        return conversationHandler.sendUserMessage(chatId, message)
    }

    override suspend fun saveToolResponses(
        chatId: UID,
        messages: List<AiAssistantMessage.ToolMessage>
    ) {
        localDataSource.addChatMessages(messages.map { it.mapToLocal(chatId) })
    }

    override suspend fun completeToolRound(chatId: UID): AiAssistantResponse {
        return conversationHandler.completeToolRound(chatId)
    }

    override suspend fun saveAssistantMessage(
        chatId: UID,
        message: AiAssistantMessage.AssistantMessage
    ) {
        localDataSource.addChatMessage(message.mapToLocal(chatId))
    }

    override suspend fun updateSystemPromt(
        chatId: UID,
        message: AiAssistantMessage.SystemMessage
    ) {
        localDataSource.addChatMessage(message.mapToLocal(chatId))
    }

    override suspend fun deleteUnconfirmedMessages(chatId: UID) {
        conversationHandler.deleteUnconfirmedMessages(chatId)
    }

    override suspend fun deleteChat(chatId: UID?) {
        if (chatId == null) {
            localDataSource.deleteAllChats()
        } else {
            localDataSource.deleteChat(chatId)
        }
    }
}
