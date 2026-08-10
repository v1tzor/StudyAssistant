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

package ru.aleshin.studyassistant.core.api.functions

import ru.aleshin.studyassistant.core.api.models.FunctionExecutionPojo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class FunctionExecutionPojoTest {

    @Test
    fun successfulExecutionReturnsResponseBody() {
        val execution = execution(responseBody = "{\"success\":true}")

        assertEquals("{\"success\":true}", execution.requireResponseBody())
    }

    @Test
    fun emptyResponseBodyThrowsControlledException() {
        val execution = execution(responseBody = "")

        assertFailsWith<FunctionExecutionException> { execution.requireResponseBody() }
    }

    @Test
    fun failedResponseThrowsControlledException() {
        val execution = execution(
            status = "failed",
            responseStatusCode = 500,
            responseBody = "{\"errorCode\":\"server_unavailable\"}",
            errors = "Runtime failed",
        )

        val error = assertFailsWith<FunctionExecutionException> {
            execution.requireResponseBody()
        }
        assertEquals(500, error.statusCode)
        assertEquals(execution.responseBody, error.responseBody)
    }

    @Test
    fun executionWithErrorsIsRejected() {
        val execution = execution(
            responseBody = "{\"success\":true}",
            errors = "Runtime warning promoted to error",
        )

        assertFailsWith<FunctionExecutionException> { execution.requireResponseBody() }
    }

    private fun execution(
        status: String = "completed",
        responseStatusCode: Int = 200,
        responseBody: String,
        errors: String = "",
    ) = FunctionExecutionPojo(
        id = "execution-id",
        status = status,
        responseStatusCode = responseStatusCode,
        responseBody = responseBody,
        errors = errors,
    )
}
