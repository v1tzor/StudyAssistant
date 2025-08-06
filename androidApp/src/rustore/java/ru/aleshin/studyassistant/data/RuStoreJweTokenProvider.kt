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

package ru.aleshin.studyassistant.data

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
import ru.aleshin.studyassistant.android.BuildConfig.RU_STORE_API_KEY_ID
import ru.aleshin.studyassistant.android.BuildConfig.RU_STORE_API_KEY_PRIVATE
import ru.aleshin.studyassistant.core.common.extensions.fromJson

/**
 * @author Stanislav Aleshin on 06.08.2025.
 */
interface RuStoreJweTokenProvider {

    suspend fun getJweToken(): String

    class Base(private val httpClient: HttpClient) : RuStoreJweTokenProvider {

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

            val jweKey = response.fromJson<JsonElement>().jsonObject["body"]?.jsonObject["jwe"]?.jsonPrimitive?.contentOrNull

            return jweKey ?: throw RuStoreJweTokenException(response)
        }
    }

    companion object {
        const val ENDPOINT = "https://public-api.rustore.ru/public/auth/"
    }
}

class RuStoreJweTokenException(override val message: String?) : Exception(message)