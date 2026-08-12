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

package ru.aleshin.studyassistant.backend.sharing.api.validation

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import ru.aleshin.studyassistant.backend.plugins.BackendJson
import ru.aleshin.studyassistant.backend.sharing.SharingConfig
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class SharePayloadValidatorTest {

    private val validator = SharePayloadValidator(
        config = SharingConfig(
            maxPayloadBytes = 1_048_576,
            maxItemsPerShare = 20,
            homeworkLifetime = Duration.ofHours(24),
            scheduleLifetime = Duration.ofMinutes(30),
            scheduleClaimLifetime = Duration.ofMinutes(5),
            createLimitPerHour = 10,
            createdItemsLimitPerDay = 200,
            activeHomeworkItemsLimit = 200,
            activePayloadBytesLimitPerInstallation = 10_485_760,
            createdPayloadBytesLimitPerDay = 20_971_520,
            globalActivePayloadBytesLimit = 536_870_912,
            globalCreatedPayloadBytesLimitPerDay = 268_435_456,
            codeLookupLimit = 30,
            codeLookupWindow = Duration.ofMinutes(10),
        ),
        json = BackendJson,
    )

    @Test
    fun validHomeworkPayloadShouldReturnCanonicalBytesAndItemCount() {
        val payload = JsonObject(
            mapOf(
                "senderName" to JsonPrimitive("Student"),
                "date" to JsonPrimitive(1_786_390_400_000L),
                "homeworks" to JsonArray(listOf(JsonObject(emptyMap()))),
            ),
        )

        val result = validator.validateHomework(share = payload)

        assertEquals(1, result.itemCount)
        assertEquals(BackendJson.encodeToString(JsonObject.serializer(), payload), result.bytes.decodeToString())
    }

    @Test
    fun unknownTopLevelFieldShouldBeRejected() {
        val payload = JsonObject(
            mapOf(
                "senderName" to JsonPrimitive("Student"),
                "date" to JsonPrimitive(1_786_390_400_000L),
                "homeworks" to JsonArray(listOf(JsonObject(emptyMap()))),
                "padding" to JsonPrimitive("data"),
            ),
        )

        assertFailsWith<InvalidRequestException> {
            validator.validateHomework(share = payload)
        }
    }
}
