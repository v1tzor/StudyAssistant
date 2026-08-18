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

package ru.aleshin.studyassistant.core.remote.api.share

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.remote.ktor.NetworkConnectionChecker
import ru.aleshin.studyassistant.core.remote.ktor.StudyAssistantKtor
import ru.aleshin.studyassistant.core.remote.mappers.mapBackendShareError
import ru.aleshin.studyassistant.core.remote.models.shared.CreateShareResponsePojo
import ru.aleshin.studyassistant.core.remote.models.shared.ShareLinkResponsePojo
import ru.aleshin.studyassistant.core.remote.models.shared.backend.ClaimTokenRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.backend.CreatePayloadShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.backend.HomeworkShareTransportResponsePojo
import ru.aleshin.studyassistant.core.remote.models.shared.backend.ScheduleClaimResponsePojo
import ru.aleshin.studyassistant.core.remote.models.shared.backend.ScheduleClaimTransportPojo
import ru.aleshin.studyassistant.core.remote.models.shared.backend.ShareCodeRequestPojo

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class BackendShareApi(
    private val httpClient: HttpClient,
    private val connectionChecker: NetworkConnectionChecker,
    private val json: Json,
) {

    suspend fun createSchedule(
        share: JsonObject,
        installationToken: String,
    ): ShareLinkResponsePojo {
        return execute<CreatePayloadShareRequestPojo, CreateShareResponsePojo>(
            path = StudyAssistantKtor.Backend.SCHEDULE_SHARE_CREATE,
            request = CreatePayloadShareRequestPojo(share = share),
            installationToken = installationToken,
        ).link
    }

    suspend fun claimSchedule(
        code: String,
        installationToken: String,
    ): ScheduleClaimTransportPojo {
        return execute<ShareCodeRequestPojo, ScheduleClaimResponsePojo>(
            path = StudyAssistantKtor.Backend.SCHEDULE_SHARE_CLAIM,
            request = ShareCodeRequestPojo(code = code),
            installationToken = installationToken,
        ).claim
    }

    suspend fun confirmSchedule(claimToken: String, installationToken: String) {
        for (attempt in 0 until CONFIRM_ATTEMPTS) {
            try {
                executeWithoutResponse(
                    path = StudyAssistantKtor.Backend.SCHEDULE_SHARE_CONFIRM,
                    request = ClaimTokenRequestPojo(claimToken = claimToken),
                    installationToken = installationToken,
                )
                return
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val retryable = error is InternetConnectionException ||
                    error is ShareException.ServerUnavailable
                if (!retryable || attempt == CONFIRM_ATTEMPTS - 1) throw error
                delay(CONFIRM_RETRY_DELAY_MILLIS * (attempt + 1))
            }
        }
    }

    suspend fun releaseSchedule(claimToken: String, installationToken: String) {
        executeWithoutResponse(
            path = StudyAssistantKtor.Backend.SCHEDULE_SHARE_RELEASE,
            request = ClaimTokenRequestPojo(claimToken = claimToken),
            installationToken = installationToken,
        )
    }

    suspend fun createHomework(
        share: JsonObject,
        installationToken: String,
    ): ShareLinkResponsePojo {
        return execute<CreatePayloadShareRequestPojo, CreateShareResponsePojo>(
            path = StudyAssistantKtor.Backend.HOMEWORK_SHARE_CREATE,
            request = CreatePayloadShareRequestPojo(share = share),
            installationToken = installationToken,
        ).link
    }

    suspend fun fetchHomework(
        code: String,
        installationToken: String,
    ): JsonObject {
        return execute<ShareCodeRequestPojo, HomeworkShareTransportResponsePojo>(
            path = StudyAssistantKtor.Backend.HOMEWORK_SHARE_FETCH,
            request = ShareCodeRequestPojo(code = code),
            installationToken = installationToken,
        ).share
    }

    private suspend inline fun <reified Request : Any, reified Response : Any> execute(
        path: String,
        request: Request,
        installationToken: String? = null,
    ): Response {
        val responseBody = executeRequest(
            path = path,
            request = request,
            installationToken = installationToken,
        )

        return runCatching {
            json.decodeFromString<Response>(responseBody)
        }.getOrElse {
            throw ShareException.ServerUnavailable()
        }
    }

    private suspend inline fun <reified Request : Any> executeWithoutResponse(
        path: String,
        request: Request,
        installationToken: String? = null,
    ) {
        executeRequest(
            path = path,
            request = request,
            installationToken = installationToken,
        )
    }

    private suspend inline fun <reified Request : Any> executeRequest(
        path: String,
        request: Request,
        installationToken: String?,
    ): String {
        if (!connectionChecker.isConnected()) throw InternetConnectionException()

        val response = try {
            httpClient.post(path) {
                installationToken?.let { token ->
                    header(StudyAssistantKtor.Backend.INSTALLATION_TOKEN_HEADER, token)
                }
                setBody(request)
            }
        } catch (_: IOException) {
            throw InternetConnectionException()
        }
        val responseBody = response.bodyAsText()

        if (!response.status.isSuccess()) {
            throw mapBackendShareError(
                status = response.status,
                body = responseBody,
                json = json,
            )
        }
        return responseBody
    }

    private companion object {
        const val CONFIRM_ATTEMPTS = 4
        const val CONFIRM_RETRY_DELAY_MILLIS = 250L
    }
}
