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

package ru.aleshin.studyassistant.core.domain.repositories

import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantResponse
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChat
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChatHistory

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
interface AiAssistantRepository {
    suspend fun addOrUpdateChat(chatHistory: AiChatHistory)
    suspend fun fetchAllChats(): Flow<List<AiChat>>
    suspend fun fetchChatHistoryById(uid: UID): Flow<AiChatHistory?>
    suspend fun fetchChatHistoryLastMessage(chatId: UID): Flow<AiAssistantMessage?>
    suspend fun retrySendLastMessage(chatId: UID): AiAssistantMessage.AssistantMessage?
    suspend fun sendUserMessage(chatId: UID, message: AiAssistantMessage.UserMessage?): AiAssistantResponse
    suspend fun sendToolResponse(chatId: UID, messages: List<AiAssistantMessage.ToolMessage>): AiAssistantResponse
    suspend fun saveAssistantMessage(chatId: UID, message: AiAssistantMessage.AssistantMessage)
    suspend fun updateSystemPromt(chatId: UID, message: AiAssistantMessage.SystemMessage)
    suspend fun deleteUnconfirmedMessages(chatId: UID)
    suspend fun deleteChat(chatId: UID)
}