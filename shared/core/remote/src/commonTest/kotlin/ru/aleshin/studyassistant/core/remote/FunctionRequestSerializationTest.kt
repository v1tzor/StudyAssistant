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

package ru.aleshin.studyassistant.core.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ai.SharedAiRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ClaimScheduleShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ConfirmScheduleShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.CreateHomeworkShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.CreateScheduleShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.FetchHomeworkShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.HomeworkSharePayloadPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ReleaseScheduleShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ScheduleSharePayloadPojo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class FunctionRequestSerializationTest {

    private val json = Json { encodeDefaults = false }

    @Test
    fun `function operations are always encoded`() {
        val requests = listOf(
            json.encodeToString(CreateHomeworkShareRequestPojo(
                operation = "homework.create",
                installationToken = "installation",
                share = HomeworkSharePayloadPojo(
                    senderName = "Sender",
                    date = 0L,
                    homeworks = emptyList(),
                ),
            )) to "homework.create",
            json.encodeToString(FetchHomeworkShareRequestPojo(
                operation = "homework.fetch",
                installationToken = "installation",
                code = "0123456789AB",
            )) to "homework.fetch",
            json.encodeToString(CreateScheduleShareRequestPojo(
                operation = "schedule.create",
                installationToken = "installation",
                share = ScheduleSharePayloadPojo(
                    senderName = "Sender",
                    schedules = emptyList(),
                    organizations = emptyList(),
                ),
            )) to "schedule.create",
            json.encodeToString(ClaimScheduleShareRequestPojo(
                operation = "schedule.claim",
                installationToken = "installation",
                code = "0123456789AB",
            )) to "schedule.claim",
            json.encodeToString(ConfirmScheduleShareRequestPojo(
                operation = "schedule.confirm",
                claimToken = "claim",
            )) to "schedule.confirm",
            json.encodeToString(ReleaseScheduleShareRequestPojo(
                operation = "schedule.release",
                claimToken = "claim",
            )) to "schedule.release",
            json.encodeToString(SharedAiRequestPojo(
                operation = "complete",
                installationToken = "installation",
                quotaKey = "quota",
                completion = ChatCompletionRequestPojo(
                    messages = emptyList(),
                    model = "model",
                ),
            )) to "complete",
        )

        requests.forEach { (request, operation) ->
            val encodedOperation = json.parseToJsonElement(request)
                .jsonObject.getValue("operation").jsonPrimitive.content
            assertEquals(operation, encodedOperation)
        }
    }

    @Test
    fun `shared ai request matches function contract`() {
        val request = json.encodeToString(SharedAiRequestPojo(
            operation = "complete",
            installationToken = "00000000-0000-0000-0000-000000000000",
            quotaKey = "11111111-1111-1111-1111-111111111111",
            completion = ChatCompletionRequestPojo(
                messages = emptyList(),
                model = "deepseek-chat",
            ),
        )).let(json::parseToJsonElement).jsonObject

        assertEquals("complete", request.getValue("operation").jsonPrimitive.content)
        assertEquals(36, request.getValue("installationToken").jsonPrimitive.content.length)
        assertEquals(36, request.getValue("quotaKey").jsonPrimitive.content.length)
        assertTrue(request.getValue("completion").jsonObject.containsKey("messages"))
        assertEquals(
            "deepseek-chat",
            request.getValue("completion").jsonObject.getValue("model").jsonPrimitive.content,
        )
    }
}
