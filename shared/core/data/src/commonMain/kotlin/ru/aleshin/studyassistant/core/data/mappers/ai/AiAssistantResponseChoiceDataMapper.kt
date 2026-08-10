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

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantFinishReason
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantResponseChoice
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionChoicePojo

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
fun ChatCompletionChoicePojo.mapToDomain(time: Instant) = AiAssistantResponseChoice(
    index = index,
    message = message.mapToDomain(time = time),
    finishReason = finishReason?.let { AiAssistantFinishReason.fromString(it) },
)
