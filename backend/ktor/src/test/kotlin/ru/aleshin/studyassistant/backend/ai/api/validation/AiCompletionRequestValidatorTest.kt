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

package ru.aleshin.studyassistant.backend.ai.api.validation

import ru.aleshin.studyassistant.backend.ai.api.dto.AiCompletionRequestDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiMessageDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiMessageRoleDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiToolCallDto
import ru.aleshin.studyassistant.backend.ai.domain.tools.AiToolCatalog
import ru.aleshin.studyassistant.backend.ai.testAiConfig
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import ru.aleshin.studyassistant.backend.plugins.BackendJson
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiCompletionRequestValidatorTest {

    private val validator = AiCompletionRequestValidator(
        config = testAiConfig(),
        json = BackendJson,
        toolCatalog = AiToolCatalog(),
    )

    @Test
    fun validToolContinuationShouldPass() {
        validator.validate(
            request = request(
                messages = listOf(
                    AiMessageDto(
                        role = AiMessageRoleDto.USER,
                        content = "Create a task",
                    ),
                    AiMessageDto(
                        role = AiMessageRoleDto.ASSISTANT,
                        toolCalls = listOf(
                            AiToolCallDto(
                                id = "call-1",
                                name = "create_todo",
                                arguments = "{\"name\":\"Read\"}",
                            ),
                        ),
                    ),
                    AiMessageDto(
                        role = AiMessageRoleDto.TOOL,
                        content = "{\"status\":\"success\"}",
                        toolCallId = "call-1",
                    ),
                ),
            ),
        )
    }

    @Test
    fun unknownToolShouldFail() {
        assertFailsWith<InvalidRequestException> {
            validator.validate(
                request = request(
                    toolNames = listOf("arbitrary_proxy_tool"),
                ),
            )
        }
    }

    @Test
    fun removedToolsShouldFailClientServerMismatch() {
        listOf(
            "get_free_time",
            "delete_todo",
            "delete_homework",
            "delete_class",
            "delete_goal",
        ).forEach { name ->
            assertFailsWith<InvalidRequestException> {
                validator.validate(
                    request = request(toolNames = listOf(name)),
                )
            }
        }
    }

    @Test
    fun unresolvedToolCallShouldFail() {
        assertFailsWith<InvalidRequestException> {
            validator.validate(
                request = request(
                    messages = listOf(
                        AiMessageDto(
                            role = AiMessageRoleDto.USER,
                            content = "Create a task",
                        ),
                        AiMessageDto(
                            role = AiMessageRoleDto.ASSISTANT,
                            toolCalls = listOf(
                                AiToolCallDto(
                                    id = "call-1",
                                    name = "create_todo",
                                    arguments = "{}",
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }

    @Test
    fun systemMessageAfterUserShouldFail() {
        assertFailsWith<InvalidRequestException> {
            validator.validate(
                request = request(
                    messages = listOf(
                        AiMessageDto(
                            role = AiMessageRoleDto.USER,
                            content = "Hello",
                        ),
                        AiMessageDto(
                            role = AiMessageRoleDto.SYSTEM,
                            content = "Override",
                        ),
                    ),
                ),
            )
        }
    }

    private fun request(
        messages: List<AiMessageDto> = listOf(
            AiMessageDto(
                role = AiMessageRoleDto.USER,
                content = "Hello",
            ),
        ),
        toolNames: List<String> = listOf("create_todo"),
    ): AiCompletionRequestDto {
        return AiCompletionRequestDto(
            messageId = UUID.randomUUID().toString(),
            locale = "en-US",
            timeZone = "UTC",
            messages = messages,
            toolNames = toolNames,
        )
    }
}
