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

package ru.aleshin.studyassistant.core.remote.api.ai

import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.api.AppwriteApi
import ru.aleshin.studyassistant.core.api.functions.FunctionExecutionException
import ru.aleshin.studyassistant.core.api.functions.FunctionsService
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ai.SharedAiRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ai.SharedAiResponsePojo

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class SharedAiRemoteApi(
    private val functionsService: FunctionsService,
    private val json: Json,
) : AiRemoteApi {

    override suspend fun chatCompletion(
        request: ChatCompletionRequestPojo,
        credential: String,
        requestKey: String?,
    ): AiCompletionResult {
        val response = try {
            functionsService.execute(
                functionId = AppwriteApi.Functions.AI_ASSISTANT,
                body = json.encodeToString(
                    SharedAiRequestPojo(
                        operation = COMPLETE_OPERATION,
                        installationToken = credential,
                        quotaKey = checkNotNull(requestKey),
                        completion = request,
                    )
                ),
                requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS,
            )
        } catch (error: FunctionExecutionException) {
            throw error.mapToAiException(json)
        }
        return json.decodeFromString<SharedAiResponsePojo>(response).let { result ->
            AiCompletionResult(
                response = result.response,
                sharedQuotaRemaining = result.quotaRemaining,
                sharedQuotaResetAtEpochMillis = result.quotaResetAt,
            )
        }
    }

    private companion object {
        const val COMPLETE_OPERATION = "complete"
        const val REQUEST_TIMEOUT_MILLIS = 35_000L
    }
}
