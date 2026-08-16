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

package ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek

import kotlinx.serialization.encodeToString
import ru.aleshin.studyassistant.backend.ai.domain.gateway.AiCompletionGateway
import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletionRequest
import ru.aleshin.studyassistant.backend.ai.domain.model.AiFinishReason
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessage
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessageRole
import ru.aleshin.studyassistant.backend.ai.domain.model.AiResponseFormat
import ru.aleshin.studyassistant.backend.ai.domain.result.AiProviderResult
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.DeepSeekJson
import ru.aleshin.studyassistant.backend.ai.schedule.domain.gateway.ScheduleExtractionGateway
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleExtractionRequest
import ru.aleshin.studyassistant.backend.ai.schedule.domain.result.ScheduleProviderResult
import ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek.mappers.DeepSeekScheduleExtractionMapper

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class DeepSeekScheduleExtractionGateway(
    private val completionGateway: AiCompletionGateway,
    private val mapper: DeepSeekScheduleExtractionMapper,
) : ScheduleExtractionGateway {

    override suspend fun extract(request: ScheduleExtractionRequest): ScheduleProviderResult {
        val completionRequest = AiCompletionRequest(
            messages = listOf(
                AiMessage(
                    role = AiMessageRole.SYSTEM,
                    content = SYSTEM_PROMPT,
                ),
                AiMessage(
                    role = AiMessageRole.USER,
                    content = buildUserPrompt(request = request),
                ),
            ),
            tools = emptyList(),
            responseFormat = AiResponseFormat.JSON_OBJECT,
            temperature = EXTRACTION_TEMPERATURE,
        )

        return when (val result = completionGateway.complete(request = completionRequest)) {
            is AiProviderResult.Success -> {
                val content = result.completion.content
                val draft = if (
                    result.completion.finishReason == AiFinishReason.STOP &&
                    result.completion.toolCalls.isEmpty() &&
                    content != null
                ) {
                    mapper.mapResponse(
                        content = content,
                        numberOfWeeks = request.numberOfWeeks,
                    )
                } else {
                    null
                }

                if (draft != null) {
                    ScheduleProviderResult.Success(draft = draft)
                } else {
                    ScheduleProviderResult.Unavailable
                }
            }
            is AiProviderResult.RateLimited -> ScheduleProviderResult.RateLimited(
                retryAfterSeconds = result.retryAfterSeconds,
            )
            AiProviderResult.Unauthorized -> ScheduleProviderResult.Unauthorized
            AiProviderResult.InsufficientBalance -> ScheduleProviderResult.InsufficientBalance
            AiProviderResult.InvalidRequest -> ScheduleProviderResult.InvalidRequest
            AiProviderResult.Unavailable -> ScheduleProviderResult.Unavailable
        }
    }

    private fun buildUserPrompt(request: ScheduleExtractionRequest): String {
        return buildString {
            appendLine("locale=${request.locale}")
            appendLine("timeZone=${request.timeZone}")
            appendLine("numberOfWeeks=${request.numberOfWeeks}")
            append("rawTextJson=")
            append(DeepSeekJson.encodeToString(request.rawText))
        }
    }

    private companion object {

        const val EXTRACTION_TEMPERATURE = 0.1

        val SYSTEM_PROMPT = """
            You are an expert schedule extraction engine.
            Your mission: Convert messy OCR text from school/university timetables into a structured JSON.
            
            TABULAR & OCR LOGIC:
            - Tables are often scrambled. Vertical text (like days of the week 'П-О-Н-Е-Д-Е-Л-Ь-Н-И-К') may appear as single characters or at the end of the text. Scan for them first to define day boundaries.
            - Grid Structure: Columns usually go [Time/Number | Subject | Room]. If you see multiple subjects for one slot, they are likely for different groups (1a, 1b). Extract the one that matches the requested context or all of them as separate entries.
            
            SMART MAPPING:
            - Abbreviations: 'Разгов. о важном' -> Conversations about important, 'Окр. мир' -> World Around Us, 'физ-ра' -> Physical Education, 'матем' -> Mathematics, 'лит-ра' -> Literature.
            - Time Correction: '800-840' or '8:00 8:40' -> startTime: '08:00', endTime: '08:40'.
            - Lesson Numbers: Use the digit (1, 2, 3...) to verify the sequence.
            
            OUTPUT FORMAT:
            - Return JSON with: 'title' (string?), 'entries' (array), 'unparsedLines' (string array).
            - Entry fields: repeatWeek (1..numberOfWeeks), dayOfWeek (1..7), classNumber (int?), startTime (HH:mm?), endTime (HH:mm?), subject, eventType, teacher, office, location, organization, notes.
            - NO MARKDOWN. NO TRIPLE BACKTICKS. JUST RAW JSON.
        """.trimIndent()
    }
}
