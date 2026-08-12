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
import kotlinx.datetime.TimeZone
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToBackend
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToLocal
import ru.aleshin.studyassistant.core.database.datasource.ai.AiLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantResponse
import ru.aleshin.studyassistant.core.domain.entities.ai.dropUnconfirmedMessages
import ru.aleshin.studyassistant.core.domain.entities.ai.dropUntilConfirmedMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.optimisedMessagesForSend
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionRequestPojo

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AiConversationHandler {

    suspend fun retryLastMessage(chatId: UID): AiAssistantMessage.AssistantMessage?
    suspend fun sendUserMessage(
        chatId: UID,
        message: AiAssistantMessage.UserMessage?,
    ): AiAssistantResponse

    suspend fun completeToolRound(chatId: UID): AiAssistantResponse

    suspend fun deleteUnconfirmedMessages(chatId: UID)

    class Base(
        private val localDataSource: AiLocalDataSource,
        private val completionHandler: AiCompletionHandler,
        private val dateManager: DateManager,
        private val deviceInfoProvider: DeviceInfoProvider,
    ) : AiConversationHandler {

        override suspend fun retryLastMessage(
            chatId: UID,
        ): AiAssistantMessage.AssistantMessage? {
            val messages = localDataSource.fetchChatHistoryById(chatId).first()
                ?.messages
                ?.map { it.mapToDomain() }
            if (messages.isNullOrEmpty()) throw NoSuchElementException()

            val lastMessage = messages.last { it !is AiAssistantMessage.SystemMessage }
            val assistantMessage = when (lastMessage) {
                is AiAssistantMessage.UserMessage,
                is AiAssistantMessage.ToolMessage,
                -> complete(messages)
                else -> messages.dropUntilConfirmedMessage { message ->
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

        override suspend fun completeToolRound(chatId: UID): AiAssistantResponse {
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

        private suspend fun complete(
            messages: List<AiAssistantMessage>,
        ): AiAssistantMessage? {
            return completeResponse(messages).choices.firstOrNull()?.message
        }

        private suspend fun completeResponse(
            messages: List<AiAssistantMessage>,
        ): AiAssistantResponse {
            val optimisedMessages = messages.optimisedMessagesForSend()
            val request = AiCompletionRequestPojo(
                messageId = optimisedMessages
                    .last { message -> message is AiAssistantMessage.UserMessage }
                    .id,
                locale = deviceInfoProvider.fetchDeviceLanguage(),
                timeZone = TimeZone.currentSystemDefault().id,
                toolProtocolVersion = TOOL_PROTOCOL_VERSION,
                messages = optimisedMessages.mapNotNull { message -> message.mapToBackend() },
                toolNames = AI_TOOL_NAMES,
            )
            return completionHandler.complete(request = request).mapToDomain(
                time = dateManager.fetchCurrentInstant(),
            )
        }

        private companion object {
            const val TOOL_PROTOCOL_VERSION = 1

            val AI_TOOL_NAMES = listOf(
                "get_profile",
                "create_todo",
                "update_todo",
                "complete_todo",
                "delete_todo",
                "create_homework",
                "update_homework",
                "complete_homework",
                "delete_homework",
                "get_organizations",
                "get_subjects",
                "get_employees",
                "get_employee",
                "get_todos",
                "get_homeworks",
                "get_overdue_homeworks",
                "get_classes_by_date",
                "get_classes_by_range",
                "get_near_class",
                "get_free_time",
            )
        }
    }
}
