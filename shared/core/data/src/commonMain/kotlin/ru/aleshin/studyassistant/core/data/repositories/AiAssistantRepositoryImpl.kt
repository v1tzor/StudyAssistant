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

package ru.aleshin.studyassistant.core.data.repositories

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToLocal
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToRemote
import ru.aleshin.studyassistant.core.database.datasource.ai.AiLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantResponse
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChat
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChatHistory
import ru.aleshin.studyassistant.core.domain.repositories.AiAssistantRepository
import ru.aleshin.studyassistant.core.remote.api.ai.AiRemoteApi
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionRequest
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionToolChoicePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ChatModel
import ru.aleshin.studyassistant.core.remote.models.ai.optimisedMessagesForSend

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
class AiAssistantRepositoryImpl(
    private val aiApi: AiRemoteApi,
    private val localDataSource: AiLocalDataSource,
    private val dateManager: DateManager,
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
            chatHistory?.mapToDomain()?.apply {
                Logger.e("test") { "messages -> $messages " }
            }
        }
    }

    override suspend fun fetchChatHistoryLastMessage(chatId: UID): Flow<AiAssistantMessage?> {
        return localDataSource.fetchChatHistoryLastMessage(chatId).map { message ->
            message?.mapToDomain()
        }
    }

    override suspend fun sendUserMessage(
        chatId: UID,
        message: AiAssistantMessage.UserMessage?,
    ): AiAssistantResponse {
        if (message != null) {
            localDataSource.addChatMessage(message.mapToLocal(chatId))
        }
        val chatHistory = localDataSource.fetchChatHistoryById(chatId).first()
        val messages = chatHistory?.messages?.map { it.mapToDomain().mapToRemote() }?.optimisedMessagesForSend()
        if (messages.isNullOrEmpty()) throw NoSuchElementException()
        val request = ChatCompletionRequest(
            model = ChatModel.DEEPSEEK_CHAT.model,
            messages = messages,
            tools = listOf(
                AiRemoteApi.Base.createTodoTool,
                AiRemoteApi.Base.createHomework,
                AiRemoteApi.Base.getOrganizationsTool,
                AiRemoteApi.Base.getSubjectsTool,
                AiRemoteApi.Base.getEmployeeTool,
                AiRemoteApi.Base.getHomeworksTool,
                AiRemoteApi.Base.getOverdueHomeworksTool,
                AiRemoteApi.Base.getClassesByDateTool,
                AiRemoteApi.Base.getNearClassTool,
            ),
            toolChoice = ChatCompletionToolChoicePojo.AUTO
        )
        return aiApi.chatCompletion(request).mapToDomain(
            time = dateManager.fetchCurrentInstant(),
        )
    }

    override suspend fun sendToolResponse(
        chatId: UID,
        messages: List<AiAssistantMessage.ToolMessage>
    ): AiAssistantResponse {
        localDataSource.addChatMessages(messages.map { it.mapToLocal(chatId) })
        val chatHistory = localDataSource.fetchChatHistoryById(chatId).first()
        val messages = chatHistory?.messages?.map { it.mapToDomain().mapToRemote() }?.optimisedMessagesForSend()
        if (messages.isNullOrEmpty()) throw NoSuchElementException()
        val request = ChatCompletionRequest(
            model = ChatModel.DEEPSEEK_CHAT.model,
            messages = messages,
            tools = listOf(
                AiRemoteApi.Base.createTodoTool,
                AiRemoteApi.Base.createHomework,
                AiRemoteApi.Base.getOrganizationsTool,
                AiRemoteApi.Base.getEmployeeTool,
                AiRemoteApi.Base.getSubjectsTool,
                AiRemoteApi.Base.getHomeworksTool,
                AiRemoteApi.Base.getOverdueHomeworksTool,
                AiRemoteApi.Base.getClassesByDateTool,
                AiRemoteApi.Base.getNearClassTool,
            ),
            toolChoice = ChatCompletionToolChoicePojo.AUTO,
        )
        return aiApi.chatCompletion(request).mapToDomain(
            time = dateManager.fetchCurrentInstant(),
        )
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

    override suspend fun deleteChat(chatId: UID) {
        localDataSource.deleteChat(chatId)
    }
}