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

package ru.aleshin.studyassistant.core.remote.datasources.message

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.common.extensions.getString
import ru.aleshin.studyassistant.core.common.extensions.tryFromJson
import ru.aleshin.studyassistant.core.remote.BuildKonfig

/**
 * @author Stanislav Aleshin on 05.08.2024.
 */
class HmsAuthTokenProviderImpl(
    private val httpClient: HttpClient,
) : HmsAuthTokenProvider {

    private companion object {
        const val GRANT_TYPE = "client_credentials"
    }

    override suspend fun fetchAccessToken(): String? {
        return try {
            val response = httpClient.request {
                method = HttpMethod.Post

                val formParameters = parameters {
                    append("grant_type", GRANT_TYPE)
                    append("client_id", BuildKonfig.HMS_APP_ID)
                    append("client_secret", BuildKonfig.HMS_CLIENT_SECRET)
                }

                setBody(FormDataContent(formParameters))
            }
            val data = response.bodyAsText().tryFromJson(JsonElement.serializer())

            if (response.status.isSuccess()) {
                data?.getString("access_token")?.takeIf { it.isNotBlank() }
            } else {
                throw HuaweiTokenException(message = data?.getString("error_description") ?: "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    class HuaweiTokenException(message: String) : Exception(message)
}