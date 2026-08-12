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

package ru.aleshin.studyassistant.backend.ai.domain.services

import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletionCommand
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessage
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessageRole
import ru.aleshin.studyassistant.backend.ai.domain.model.AiToolCall
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiCompletionFingerprintFactoryTest {

    private val factory = AiCompletionFingerprintFactory()

    @Test
    fun toolContinuationShouldKeepConversationBindingAndChangeExecutionBinding() {
        val initial = command()
        val continuation = initial.copy(
            messages = initial.messages + listOf(
                AiMessage(
                    role = AiMessageRole.ASSISTANT,
                    toolCalls = listOf(
                        AiToolCall(
                            id = "call-1",
                            name = "get_todos",
                            arguments = "{}",
                        ),
                    ),
                ),
                AiMessage(
                    role = AiMessageRole.TOOL,
                    content = "{\"todos\":[]}",
                    toolCallId = "call-1",
                ),
            ),
        )

        assertEquals(factory.conversation(initial), factory.conversation(continuation))
        assertNotEquals(factory.execution(initial), factory.execution(continuation))
    }

    @Test
    fun securityRelevantInputShouldChangeConversationBinding() {
        val initial = command()

        assertNotEquals(
            factory.conversation(initial),
            factory.conversation(initial.copy(locale = "ru-RU")),
        )
        assertNotEquals(
            factory.conversation(initial),
            factory.conversation(initial.copy(toolNames = listOf("get_homeworks"))),
        )
        assertNotEquals(
            factory.conversation(initial),
            factory.conversation(
                initial.copy(
                    messages = initial.messages.toMutableList().also { messages ->
                        messages[0] = messages[0].copy(content = "Changed history")
                    },
                ),
            ),
        )
    }

    private fun command(): AiCompletionCommand {
        return AiCompletionCommand(
            messageId = UUID.randomUUID(),
            locale = "en-US",
            timeZone = "UTC",
            messages = listOf(
                AiMessage(
                    role = AiMessageRole.USER,
                    content = "Previous question",
                ),
                AiMessage(
                    role = AiMessageRole.ASSISTANT,
                    content = "Previous answer",
                ),
                AiMessage(
                    role = AiMessageRole.USER,
                    content = "Current question",
                ),
            ),
            toolNames = listOf("get_todos"),
        )
    }
}
