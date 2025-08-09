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

package ru.aleshin.studyassistant.core.remote.api.billing

import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.remote.BuildKonfig.RU_STORE_API_KEY_ID
import ru.aleshin.studyassistant.core.remote.BuildKonfig.RU_STORE_API_KEY_PRIVATE

/**
 * @author Stanislav Aleshin on 06.08.2025.
 */
interface RuStoreJweTokenProvider {

    suspend fun getJweToken(): String

    class Base(private val httpClient: HttpClient) : RuStoreJweTokenProvider {

        companion object {
            const val ENDPOINT = "https://public-api.rustore.ru/public/auth/"
        }

        override suspend fun getJweToken(): String {
            val signature = SignatureGenerator.generateSignature(
                keyId = RU_STORE_API_KEY_ID,
                privateKeyContent = RU_STORE_API_KEY_PRIVATE,
            )

            val response = httpClient.request {
                method = HttpMethod.Post
                url(ENDPOINT)
                contentType(ContentType.Application.Json)
                setBody(signature)
            }.bodyAsText()

            val responseBody = response.fromJson<JsonElement>().jsonObject["body"]

            val jweKey = responseBody?.jsonObject["jwe"]?.jsonPrimitive?.contentOrNull

            return jweKey ?: throw RuStoreJweTokenException(response)
        }
    }
}

class RuStoreJweTokenException(override val message: String?) : Exception(message)