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
import ru.aleshin.studyassistant.core.api.functions.FunctionExecutionException
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceException
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.remote.api.ai.mapToAiException
import ru.aleshin.studyassistant.core.remote.datasources.share.mapToShareException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class FunctionErrorMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun shareRateLimitIsMapped() {
        val error = executionError("{\"errorCode\":\"rate_limit\",\"retryAt\":42}")

        assertIs<ShareException.RateLimit>(error.mapToShareException(json))
    }

    @Test
    fun shareDailyLimitIsMapped() {
        val error = executionError("{\"errorCode\":\"share_limit\",\"retryAt\":42}")

        assertIs<ShareException.ShareLimit>(error.mapToShareException(json))
    }

    @Test
    fun aiQuotaKeepsResetTime() {
        val error = executionError("{\"errorCode\":\"quota\",\"quotaResetAt\":42}")

        val mapped = assertIs<AiServiceException.QuotaExceeded>(error.mapToAiException(json))
        assertEquals(42, mapped.resetAtEpochMillis)
    }

    @Test
    fun malformedFunctionErrorIsControlled() {
        val error = executionError("")

        assertIs<ShareException.ServerUnavailable>(error.mapToShareException(json))
        assertIs<AiServiceException.ServerUnavailable>(error.mapToAiException(json))
    }

    private fun executionError(responseBody: String) = FunctionExecutionException(
        statusCode = 429,
        responseBody = responseBody,
        message = "Function execution failed",
    )
}
