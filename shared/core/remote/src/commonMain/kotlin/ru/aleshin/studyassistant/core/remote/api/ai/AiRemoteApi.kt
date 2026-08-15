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

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceException
import ru.aleshin.studyassistant.core.remote.ktor.NetworkConnectionChecker
import ru.aleshin.studyassistant.core.remote.ktor.StudyAssistantKtor
import ru.aleshin.studyassistant.core.remote.mappers.mapBackendAiError
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionResponsePojo

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
interface AiRemoteApi {

    suspend fun complete(
        request: AiCompletionRequestPojo,
        installationToken: String,
    ): AiCompletionResponsePojo

    class Backend(
        private val httpClient: HttpClient,
        private val connectionChecker: NetworkConnectionChecker,
        private val json: Json,
    ) : AiRemoteApi {

        override suspend fun complete(
            request: AiCompletionRequestPojo,
            installationToken: String,
        ): AiCompletionResponsePojo {
            if (!connectionChecker.isConnected()) throw InternetConnectionException()

            val response = try {
                httpClient.post(StudyAssistantKtor.Backend.AI_COMPLETIONS) {
                    header(StudyAssistantKtor.Backend.INSTALLATION_TOKEN_HEADER, installationToken)
                    setBody(request)
                }
            } catch (exception: IOException) {
                throw InternetConnectionException(exception)
            }
            val responseBody = response.bodyAsText()

            if (!response.status.isSuccess()) {
                throw mapBackendAiError(
                    status = response.status,
                    body = responseBody,
                    json = json,
                )
            }

            return runCatching {
                json.decodeFromString<AiCompletionResponsePojo>(responseBody)
            }.getOrElse {
                throw AiServiceException.ServerUnavailable()
            }
        }
    }
}
