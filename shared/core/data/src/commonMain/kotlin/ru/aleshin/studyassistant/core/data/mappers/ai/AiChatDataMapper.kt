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

package ru.aleshin.studyassistant.core.data.mappers.ai

import ru.aleshin.studyassistant.core.database.models.ai.AiChatEntity
import ru.aleshin.studyassistant.core.database.models.ai.AiChatHistoryEntityDetails
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChat
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChatHistory

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
fun AiChatEntity.mapToDomain() = AiChat(
    uid = uid,
    lastMessage = lastMessage?.mapToDomain(),
)

fun AiChatHistoryEntityDetails.mapToDomain() = AiChatHistory(
    uid = uid,
    messages = messages.map { it.mapToDomain() },
    lastMessage = lastMessage?.mapToDomain(),
)

fun AiChatHistory.mapToLocal() = AiChatHistoryEntityDetails(
    uid = uid,
    messages = messages.map { it.mapToLocal(uid) },
    lastMessage = lastMessage?.mapToLocal(uid),
)
