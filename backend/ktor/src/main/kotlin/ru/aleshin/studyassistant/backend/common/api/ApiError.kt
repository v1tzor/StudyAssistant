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

package ru.aleshin.studyassistant.backend.common.api

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
@Serializable
data class ApiErrorResponse(
    val errorCode: String,
    val retryAt: Long? = null,
    val quotaResetAt: Long? = null,
)

open class ApiException(
    val status: HttpStatusCode,
    val errorCode: String,
    val retryAt: Long? = null,
    val quotaResetAt: Long? = null,
) : RuntimeException(errorCode)

class InvalidRequestException : ApiException(
    status = HttpStatusCode.BadRequest,
    errorCode = "invalid",
)

class PayloadTooLargeApiException : ApiException(
    status = HttpStatusCode.PayloadTooLarge,
    errorCode = "too_large",
)

class UnsupportedMediaTypeApiException : ApiException(
    status = HttpStatusCode.UnsupportedMediaType,
    errorCode = "unsupported_media_type",
)

class InvalidShareException : ApiException(
    status = HttpStatusCode.NotFound,
    errorCode = "invalid",
)

class RateLimitException(
    retryAt: Long? = null,
) : ApiException(
    status = HttpStatusCode.TooManyRequests,
    errorCode = "rate_limit",
    retryAt = retryAt,
)

class QuotaExceededException(
    quotaResetAt: Long,
) : ApiException(
    status = HttpStatusCode.TooManyRequests,
    errorCode = "quota",
    quotaResetAt = quotaResetAt,
)

class RewardUnavailableException : ApiException(
    status = HttpStatusCode.Conflict,
    errorCode = "reward_unavailable",
)

class ServerUnavailableException : ApiException(
    status = HttpStatusCode.ServiceUnavailable,
    errorCode = "server_unavailable",
)

class ClaimedShareException : ApiException(
    status = HttpStatusCode.Conflict,
    errorCode = "claimed",
)

class ConsumedShareException : ApiException(
    status = HttpStatusCode.Gone,
    errorCode = "consumed",
)
