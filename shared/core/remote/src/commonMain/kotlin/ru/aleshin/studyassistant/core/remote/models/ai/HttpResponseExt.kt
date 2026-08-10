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

package ru.aleshin.studyassistant.core.remote.models.ai

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
suspend inline fun <reified T> HttpResponse.bodyOrAiError(): T {
    if (status.isSuccess()) return body()

    val error = runCatching { body<DeepSeekErrorPojo>() }.getOrNull()
    throw DeepSeekException(
        statusCode = status.value,
        headers = headers,
        error = error,
        message = status.description,
    )
}
