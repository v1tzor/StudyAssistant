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
import ru.aleshin.studyassistant.core.api.functions.FunctionExecutionException
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceException
import ru.aleshin.studyassistant.core.remote.models.ai.SharedAiErrorPojo

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal fun FunctionExecutionException.mapToAiException(json: Json): Throwable {
    val error = runCatching {
        json.decodeFromString<SharedAiErrorPojo>(responseBody)
    }.getOrNull()
    return when (error?.errorCode) {
        "quota" -> AiServiceException.QuotaExceeded(error.quotaResetAt)
        "rate_limit" -> AiServiceException.RateLimited()
        else -> AiServiceException.ServerUnavailable()
    }
}
