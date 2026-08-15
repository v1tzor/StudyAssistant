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

package ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.mappers

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletionRequest
import ru.aleshin.studyassistant.backend.ai.domain.model.AiFinishReason
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessage
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessageRole
import ru.aleshin.studyassistant.backend.ai.domain.model.AiToolCall
import ru.aleshin.studyassistant.backend.ai.domain.model.AiToolDefinition
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.DeepSeekConfig
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.DeepSeekJson
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekChatResponseDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekChoiceDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekFunctionDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekMessageDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekToolCallDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekUsageDto
import ru.aleshin.studyassistant.backend.ai.testDeepSeekConfig
import ru.aleshin.studyassistant.backend.ai.testAiConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class DeepSeekMapperTest {

    private val mapper = DeepSeekMapper(
        deepSeekConfig = testDeepSeekConfig(),
        aiConfig = testAiConfig(),
    )

    @Test
    fun requestShouldUseOnlyFlashAndDisableThinking() {
        val request = mapper.mapRequest(
            request = AiCompletionRequest(
                messages = listOf(
                    AiMessage(
                        role = AiMessageRole.USER,
                        content = "Hello",
                    ),
                ),
                tools = listOf(
                    AiToolDefinition(
                        name = "create_todo",
                        description = "Create a task",
                        parameters = buildJsonObject {
                            put("type", "object")
                        },
                    ),
                ),
            ),
        )

        assertEquals(DeepSeekConfig.MODEL, request.model)
        assertEquals("disabled", request.thinking.type)
        assertEquals("auto", request.toolChoice)
        assertEquals(false, request.stream)
    }

    @Test
    fun requestShouldEncodeRequiredFunctionToolType() {
        val request = mapper.mapRequest(
            request = AiCompletionRequest(
                messages = listOf(
                    AiMessage(
                        role = AiMessageRole.USER,
                        content = "Read my homework",
                    ),
                ),
                tools = listOf(
                    AiToolDefinition(
                        name = "get_homeworks",
                        description = "Read homework",
                        parameters = buildJsonObject {
                            put("type", "object")
                        },
                    ),
                ),
            ),
        )

        assertTrue(
            DeepSeekJson.encodeToString(request).contains(
                "\"tools\":[{\"type\":\"function\"",
            ),
        )
    }

    @Test
    fun assistantToolCallShouldKeepRequiredContentField() {
        val request = mapper.mapRequest(
            request = AiCompletionRequest(
                messages = listOf(
                    AiMessage(
                        role = AiMessageRole.ASSISTANT,
                        content = null,
                        toolCalls = listOf(
                            AiToolCall(
                                id = "call-1",
                                name = "get_homeworks",
                                arguments = "{}",
                            ),
                        ),
                    ),
                ),
                tools = listOf(
                    AiToolDefinition(
                        name = "get_homeworks",
                        description = "Read homework",
                        parameters = buildJsonObject {
                            put("type", "object")
                        },
                    ),
                ),
            ),
        )

        assertTrue(
            DeepSeekJson.encodeToString(request).contains(
                "\"role\":\"assistant\",\"content\":\"\"",
            ),
        )
    }

    @Test
    fun toolCallResponseShouldMapToDomain() {
        val request = AiCompletionRequest(
            messages = listOf(
                AiMessage(
                    role = AiMessageRole.USER,
                    content = "Create a task",
                ),
            ),
            tools = listOf(
                AiToolDefinition(
                    name = "create_todo",
                    description = "Create a task",
                    parameters = buildJsonObject {
                        put("type", "object")
                    },
                ),
            ),
        )
        val completion = mapper.mapResponse(
            response = DeepSeekChatResponseDto(
                choices = listOf(
                    DeepSeekChoiceDto(
                        message = DeepSeekMessageDto(
                            role = "assistant",
                            toolCalls = listOf(
                                DeepSeekToolCallDto(
                                    id = "call-1",
                                    function = DeepSeekFunctionDto(
                                        name = "create_todo",
                                        arguments = "{\"name\":\"Read\"}",
                                    ),
                                ),
                            ),
                        ),
                        finishReason = "tool_calls",
                    ),
                ),
                usage = DeepSeekUsageDto(
                    promptTokens = 10,
                    completionTokens = 4,
                    totalTokens = 14,
                ),
            ),
            request = request,
        )

        requireNotNull(completion)
        assertEquals(AiFinishReason.TOOL_CALLS, completion.finishReason)
        assertEquals("create_todo", completion.toolCalls.single().name)
        assertEquals(14, completion.usage?.totalTokens)
    }

    @Test
    fun providerToolCallIdShouldNotDuplicateConversationHistory() {
        val request = AiCompletionRequest(
            messages = listOf(
                AiMessage(
                    role = AiMessageRole.USER,
                    content = "Read my homework",
                ),
                AiMessage(
                    role = AiMessageRole.ASSISTANT,
                    content = null,
                    toolCalls = listOf(
                        AiToolCall(
                            id = "call-1",
                            name = "get_homeworks",
                            arguments = "{\"from\":\"2026-08-10\",\"to\":\"2026-08-11\"}",
                        ),
                    ),
                ),
                AiMessage(
                    role = AiMessageRole.TOOL,
                    content = "[]",
                    toolCallId = "call-1",
                ),
            ),
            tools = listOf(
                AiToolDefinition(
                    name = "get_homeworks",
                    description = "Read homework",
                    parameters = buildJsonObject { put("type", "object") },
                ),
            ),
        )

        val completion = mapper.mapResponse(
            response = DeepSeekChatResponseDto(
                choices = listOf(
                    DeepSeekChoiceDto(
                        message = DeepSeekMessageDto(
                            role = "assistant",
                            toolCalls = listOf(
                                DeepSeekToolCallDto(
                                    id = "call-1",
                                    function = DeepSeekFunctionDto(
                                        name = "get_homeworks",
                                        arguments = "{\"from\":\"2026-08-12\",\"to\":\"2026-08-13\"}",
                                    ),
                                ),
                            ),
                        ),
                        finishReason = "tool_calls",
                    ),
                ),
            ),
            request = request,
        )

        assertEquals(null, completion)
    }

    @Test
    fun emptyContentFilteredResponseShouldBeRejected() {
        val completion = mapper.mapResponse(
            response = DeepSeekChatResponseDto(
                choices = listOf(
                    DeepSeekChoiceDto(
                        message = DeepSeekMessageDto(role = "assistant"),
                        finishReason = "content_filter",
                    ),
                ),
            ),
            request = AiCompletionRequest(
                messages = listOf(AiMessage(role = AiMessageRole.USER, content = "Hello")),
                tools = emptyList(),
            ),
        )

        assertEquals(null, completion)
    }
}
