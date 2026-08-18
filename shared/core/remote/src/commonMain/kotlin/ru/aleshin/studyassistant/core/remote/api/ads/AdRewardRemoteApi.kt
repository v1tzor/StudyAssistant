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

package ru.aleshin.studyassistant.core.remote.api.ads

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.exceptions.InvalidInstallationException
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardException
import ru.aleshin.studyassistant.core.remote.ktor.NetworkConnectionChecker
import ru.aleshin.studyassistant.core.remote.ktor.StudyAssistantKtor
import ru.aleshin.studyassistant.core.remote.models.ads.AdRewardChallengeRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ads.AdRewardChallengeResponsePojo
import ru.aleshin.studyassistant.core.remote.models.ads.AdRewardCompletionResponsePojo
import ru.aleshin.studyassistant.core.remote.models.backend.BackendApiErrorPojo

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
interface AdRewardRemoteApi {

    suspend fun createChallenge(
        request: AdRewardChallengeRequestPojo,
        installationToken: String,
    ): AdRewardChallengeResponsePojo

    suspend fun completeChallenge(
        challengeId: String,
        installationToken: String,
    ): AdRewardCompletionResponsePojo

    class Backend(
        private val httpClient: HttpClient,
        private val connectionChecker: NetworkConnectionChecker,
        private val json: Json,
    ) : AdRewardRemoteApi {

        override suspend fun createChallenge(
            request: AdRewardChallengeRequestPojo,
            installationToken: String,
        ): AdRewardChallengeResponsePojo {
            val responseBody = post(
                path = StudyAssistantKtor.Backend.AD_REWARD_CHALLENGES,
                installationToken = installationToken,
                body = request,
            )
            return runCatching {
                json.decodeFromString<AdRewardChallengeResponsePojo>(responseBody)
            }.getOrElse {
                throw AdRewardException.ServerUnavailable()
            }
        }

        override suspend fun completeChallenge(
            challengeId: String,
            installationToken: String,
        ): AdRewardCompletionResponsePojo {
            val responseBody = post(
                path = "${StudyAssistantKtor.Backend.AD_REWARD_CHALLENGES}/$challengeId/complete",
                installationToken = installationToken,
                body = null,
            )
            return runCatching {
                json.decodeFromString<AdRewardCompletionResponsePojo>(responseBody)
            }.getOrElse {
                throw AdRewardException.ServerUnavailable()
            }
        }

        private suspend fun post(
            path: String,
            installationToken: String,
            body: AdRewardChallengeRequestPojo?,
        ): String {
            if (!connectionChecker.isConnected()) throw InternetConnectionException()
            val response = try {
                httpClient.post(path) {
                    header(StudyAssistantKtor.Backend.INSTALLATION_TOKEN_HEADER, installationToken)
                    if (body != null) setBody(body)
                }
            } catch (_: IOException) {
                throw InternetConnectionException()
            }
            val responseBody = response.bodyAsText()
            if (!response.status.isSuccess()) {
                val error = runCatching {
                    json.decodeFromString<BackendApiErrorPojo>(responseBody)
                }.getOrNull()
                if (error?.errorCode == "invalid_installation" ||
                    response.status == HttpStatusCode.Unauthorized
                ) {
                    throw InvalidInstallationException()
                }
                if (error?.errorCode == "reward_unavailable") {
                    throw AdRewardException.Unavailable()
                }
                throw AdRewardException.ServerUnavailable()
            }
            return responseBody
        }
    }
}
