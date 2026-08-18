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

package ru.aleshin.studyassistant.core.remote.mappers

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.exceptions.InvalidInstallationException
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.remote.models.shared.ShareErrorResponsePojo

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal fun mapBackendShareError(
    status: HttpStatusCode,
    body: String,
    json: Json,
): Throwable {
    val code = runCatching {
        json.decodeFromString<ShareErrorResponsePojo>(body).errorCode
    }.getOrNull()

    return when (code) {
        "invalid" -> ShareException.InvalidCode()
        "expired" -> ShareException.Expired()
        "claimed" -> ShareException.Claimed()
        "consumed" -> ShareException.Consumed()
        "item_limit" -> ShareException.ItemLimit()
        "too_large" -> ShareException.PayloadTooLarge()
        "rate_limit" -> ShareException.RateLimit()
        "share_limit" -> ShareException.ShareLimit()
        "invalid_installation" -> InvalidInstallationException()
        else -> when (status) {
            HttpStatusCode.Unauthorized -> InvalidInstallationException()
            HttpStatusCode.NotFound -> ShareException.InvalidCode()
            HttpStatusCode.Conflict -> ShareException.Claimed()
            HttpStatusCode.Gone -> ShareException.Consumed()
            HttpStatusCode.PayloadTooLarge -> ShareException.PayloadTooLarge()
            HttpStatusCode.TooManyRequests -> ShareException.RateLimit()
            else -> ShareException.ServerUnavailable()
        }
    }
}
