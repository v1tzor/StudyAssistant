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

package ru.aleshin.studyassistant.core.database.datasource.ai

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneNotNull
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.ai.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.ai.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.ai.mapToShort
import ru.aleshin.studyassistant.core.database.models.ai.AiChatEntity
import ru.aleshin.studyassistant.core.database.models.ai.AiChatHistoryEntityDetails
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.sqldelight.ai.AiChatHistoryEntity
import ru.aleshin.studyassistant.sqldelight.ai.AiChatHistoryQueries
import ru.aleshin.studyassistant.sqldelight.ai.AiChatMessageEntity
import ru.aleshin.studyassistant.sqldelight.ai.AiChatMessageQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
interface AiLocalDataSource : LocalDataSource.OnlyOffline {

    suspend fun fetchAllChats(): Flow<List<AiChatEntity>>
    suspend fun fetchChatHistoryById(uid: UID): Flow<AiChatHistoryEntityDetails?>
    suspend fun fetchChatHistoryLastMessage(chatId: UID): Flow<AiChatMessageEntity?>
    suspend fun addOrUpdateChat(chatHistory: AiChatHistoryEntityDetails)
    suspend fun addChatMessage(message: AiChatMessageEntity)
    suspend fun addChatMessages(messages: List<AiChatMessageEntity>)
    suspend fun deleteChat(chatId: UID)
    suspend fun deleteChatMessage(messageId: UID)
    suspend fun deleteAllChats()

    class Base(
        private val chatQueries: AiChatHistoryQueries,
        private val messagesQueries: AiChatMessageQueries,
        private val coroutineManager: CoroutineManager,
    ) : AiLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchAllChats(): Flow<List<AiChatEntity>> {
            val chatsFlow = chatQueries.fetchAllChats().asFlow().mapToList(coroutineContext)
            return chatsFlow.map { chats ->
                chats.map { chat ->
                    val query = messagesQueries.fetchLastMessagesByChatId(chat)
                    val lastMessage = query.asFlow().mapToOneNotNull(coroutineContext)
                    AiChatHistoryEntity(chat).mapToShort(lastMessage = lastMessage.first())
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchChatHistoryById(uid: UID): Flow<AiChatHistoryEntityDetails?> {
            val chatHistoryFlow = chatQueries.fetchChatById(uid).asFlow().mapToOneOrNull(coroutineContext)
            val messagesFlow = messagesQueries.fetchAllMessagesByChatId(uid).asFlow().mapToList(coroutineContext)
            return chatHistoryFlow.flatMapLatest { chat ->
                messagesFlow.map { messages ->
                    chat?.let { AiChatHistoryEntity(it) }?.mapToDetails(messages = messages)
                }
            }
        }

        override suspend fun fetchChatHistoryLastMessage(chatId: UID): Flow<AiChatMessageEntity?> {
            val query = messagesQueries.fetchLastMessagesByChatId(chatId)
            return query.asFlow().mapToOneOrNull(coroutineContext)
        }

        override suspend fun addOrUpdateChat(chatHistory: AiChatHistoryEntityDetails) {
            chatQueries.addOrUpdateChatHistory(chatHistory.mapToBase()).await()
            messagesQueries.deleteMessagesByChatId(chatHistory.uid).await()
            chatHistory.messages.forEach { message ->
                messagesQueries.addOrUpdateMessage(message).await()
            }
        }

        override suspend fun addChatMessage(message: AiChatMessageEntity) {
            messagesQueries.addOrUpdateMessage(message).await()
        }

        override suspend fun addChatMessages(messages: List<AiChatMessageEntity>) {
            return messages.forEach { addChatMessage(it) }
        }

        override suspend fun deleteChat(chatId: UID) {
            messagesQueries.deleteMessagesByChatId(chatId).await()
            chatQueries.deleteChatById(chatId).await()
        }

        override suspend fun deleteChatMessage(messageId: UID) {
            messagesQueries.deleteMessageById(messageId).await()
        }

        override suspend fun deleteAllChats() {
            chatQueries.deleteAllChats().await()
            messagesQueries.deleteAllMessages().await()
        }
    }
}