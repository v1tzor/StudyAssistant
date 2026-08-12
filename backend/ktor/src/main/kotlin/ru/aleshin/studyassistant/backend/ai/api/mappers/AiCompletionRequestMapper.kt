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

package ru.aleshin.studyassistant.backend.ai.api.mappers

import ru.aleshin.studyassistant.backend.ai.api.dto.AiCompletionRequestDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiMessageRoleDto
import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletionCommand
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessage
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessageRole
import ru.aleshin.studyassistant.backend.ai.domain.model.AiToolCall
import java.util.UUID

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiCompletionRequestMapper {

    fun map(request: AiCompletionRequestDto): AiCompletionCommand {
        return AiCompletionCommand(
            messageId = UUID.fromString(request.messageId),
            locale = request.locale,
            timeZone = request.timeZone,
            messages = request.messages.map { message ->
                AiMessage(
                    role = when (message.role) {
                        AiMessageRoleDto.SYSTEM -> AiMessageRole.SYSTEM
                        AiMessageRoleDto.USER -> AiMessageRole.USER
                        AiMessageRoleDto.ASSISTANT -> AiMessageRole.ASSISTANT
                        AiMessageRoleDto.TOOL -> AiMessageRole.TOOL
                    },
                    content = message.content,
                    toolCalls = message.toolCalls.map { toolCall ->
                        AiToolCall(
                            id = toolCall.id,
                            name = toolCall.name,
                            arguments = toolCall.arguments,
                        )
                    },
                    toolCallId = message.toolCallId,
                )
            },
            toolNames = request.toolNames,
        )
    }
}
