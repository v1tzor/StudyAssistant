/*
 * Copyright 2024 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.database.mappers.storage

import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.http.HeadersImpl
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import io.ktor.util.toMap
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.sqldelight.storage.CachedResponseDataEntity

/**
 * @author Stanislav Aleshin on 11.07.2025.
 */
fun CachedResponseData.toCacheEntity(): CachedResponseDataEntity {
    return CachedResponseDataEntity(
        url = url.toString(),
        status_code = statusCode.value.toLong(),
        request_time = requestTime.timestamp,
        response_time = responseTime.timestamp,
        http_version = version.toString(),
        expires_time = expires.timestamp, // + MILLIS_IN_MINUTE,
        headers = headers.toMap().toJson<Map<String, List<String>>>(),
        vary_keys = varyKeys.toJson(MapSerializer(String.serializer(), String.serializer())),
        body = body,
    )
}

fun CachedResponseDataEntity.toCachedResponseData(): CachedResponseData {
    return CachedResponseData(
        url = Url(url),
        statusCode = HttpStatusCode.fromValue(status_code.toInt()),
        requestTime = GMTDate(request_time),
        responseTime = GMTDate(response_time),
        version = HttpProtocolVersion.parse(http_version),
        expires = GMTDate(expires_time),
        headers = HeadersImpl(headers.fromJson<Map<String, List<String>>>()),
        varyKeys = vary_keys.fromJson(MapSerializer(String.serializer(), String.serializer())),
        body = body,
    )
}