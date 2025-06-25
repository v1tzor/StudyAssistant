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

package ru.aleshin.studyassistant.core.database.mappers.ai

import ru.aleshin.studyassistant.core.database.models.ai.AiChatEntity
import ru.aleshin.studyassistant.core.database.models.ai.AiChatHistoryEntityDetails
import ru.aleshin.studyassistant.sqldelight.ai.AiChatHistoryEntity
import ru.aleshin.studyassistant.sqldelight.ai.AiChatMessageEntity

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
internal fun AiChatHistoryEntityDetails.mapToBase() = AiChatHistoryEntity(
    uid = uid,
)

internal fun AiChatHistoryEntity.mapToDetails(
    messages: List<AiChatMessageEntity>
) = AiChatHistoryEntityDetails(
    uid = uid,
    messages = messages,
    lastMessage = messages.maxByOrNull { it.time },
)

internal fun AiChatHistoryEntity.mapToShort(
    lastMessage: AiChatMessageEntity?,
) = AiChatEntity(
    uid = uid,
    lastMessage = lastMessage
)