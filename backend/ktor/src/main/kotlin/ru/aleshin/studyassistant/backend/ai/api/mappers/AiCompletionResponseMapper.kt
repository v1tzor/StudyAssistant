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

import ru.aleshin.studyassistant.backend.ai.api.dto.AiCompletionMessageDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiCompletionResponseDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiFinishReasonDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiTokenUsageDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiToolCallDto
import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletion
import ru.aleshin.studyassistant.backend.ai.domain.model.AiFinishReason
import ru.aleshin.studyassistant.backend.ai.domain.model.AiQuota
import java.time.Instant

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiCompletionResponseMapper {

    fun map(
        completion: AiCompletion,
        quota: AiQuota,
        quotaResetAt: Instant,
    ): AiCompletionResponseDto {
        return AiCompletionResponseDto(
            message = AiCompletionMessageDto(
                content = completion.content,
                toolCalls = completion.toolCalls.map { toolCall ->
                    AiToolCallDto(
                        id = toolCall.id,
                        name = toolCall.name,
                        arguments = toolCall.arguments,
                    )
                },
            ),
            finishReason = when (completion.finishReason) {
                AiFinishReason.STOP -> AiFinishReasonDto.STOP
                AiFinishReason.LENGTH -> AiFinishReasonDto.LENGTH
                AiFinishReason.CONTENT_FILTER -> AiFinishReasonDto.CONTENT_FILTER
                AiFinishReason.TOOL_CALLS -> AiFinishReasonDto.TOOL_CALLS
                AiFinishReason.UNKNOWN -> AiFinishReasonDto.UNKNOWN
            },
            usage = completion.usage?.let { usage ->
                AiTokenUsageDto(
                    promptTokens = usage.promptTokens,
                    completionTokens = usage.completionTokens,
                    totalTokens = usage.totalTokens,
                )
            },
            quotaRemaining = quota.remaining,
            quotaLimit = quota.limit,
            rewardedResetsRemaining = quota.rewardedResetsRemaining,
            quotaResetAt = quotaResetAt.toEpochMilli(),
        )
    }
}
