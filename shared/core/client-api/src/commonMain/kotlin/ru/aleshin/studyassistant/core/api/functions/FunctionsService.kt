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

import io.ktor.http.HttpMethod
import ru.aleshin.studyassistant.core.api.BaseAppwriteService
import ru.aleshin.studyassistant.core.api.client.AppwriteClient
import ru.aleshin.studyassistant.core.api.models.ClientParam
import ru.aleshin.studyassistant.core.api.models.FunctionExecutionPojo

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class FunctionsService(
    client: AppwriteClient,
) : BaseAppwriteService(client) {

    suspend fun createExecution(
        functionId: String,
        body: String,
        requestTimeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    ): FunctionExecutionPojo {
        return client.call(
            method = HttpMethod.Post,
            path = "/functions/$functionId/executions",
            deserializer = FunctionExecutionPojo.serializer(),
            headers = mapOf("content-type" to "application/json"),
            params = listOf(
                ClientParam.StringParam("body", body),
                ClientParam.BooleanParam("async", false),
                ClientParam.StringParam("path", "/"),
                ClientParam.StringParam("method", HttpMethod.Post.value),
                ClientParam.MapParam("headers", mapOf("content-type" to "application/json")),
            ),
            requestTimeoutMillis = requestTimeoutMillis,
        )
    }

    suspend fun execute(
        functionId: String,
        body: String,
        requestTimeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    ): String = createExecution(functionId, body, requestTimeoutMillis).requireResponseBody()

    private companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 30_000L
    }
}

internal fun FunctionExecutionPojo.requireResponseBody(): String {
    if (
        status != "completed" ||
        responseStatusCode !in 200..299 ||
        errors.isNotBlank() ||
        responseBody.isBlank()
    ) {
        throw FunctionExecutionException(
            statusCode = responseStatusCode,
            responseBody = responseBody,
            message = errors.ifBlank { "Appwrite Function execution failed" },
        )
    }
    return responseBody
}
