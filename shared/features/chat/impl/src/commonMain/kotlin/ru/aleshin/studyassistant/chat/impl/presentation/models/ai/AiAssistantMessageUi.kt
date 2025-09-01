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

package ru.aleshin.studyassistant.chat.impl.presentation.models.ai

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage

/**
 * @author Stanislav Aleshin on 22.06.2025.
 */
@Immutable
@Serializable
internal sealed interface AiAssistantMessageUi {
    val id: String
    val content: String?
    val messageType: AiAssistantMessage.Type

    val time: Instant
}

@Immutable
@Serializable
internal data class UserMessageUi(
    override val id: String = randomUUID(),
    override val content: String?,
    override val time: Instant,
    val name: String? = null
) : AiAssistantMessageUi {
    override val messageType = AiAssistantMessage.Type.USER
}

@Immutable
@Serializable
internal data class AssistantMessageUi(
    override val id: String,
    override val content: String?,
    override val time: Instant,
) : AiAssistantMessageUi {
    override val messageType = AiAssistantMessage.Type.ASSISTANT
}