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

package ru.aleshin.studyassistant.core.data.handlers

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceException
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceType
import ru.aleshin.studyassistant.core.domain.managers.InstallationIdProvider
import ru.aleshin.studyassistant.core.remote.datasources.ai.AiAssistantRemoteDataSource
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionResponsePojo

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AiCompletionHandler {

    suspend fun complete(
        request: ChatCompletionRequestPojo,
        requestKey: String,
    ): ChatCompletionResponsePojo

    suspend fun testPersonalKey(request: ChatCompletionRequestPojo, apiKey: String)

    class Base(
        private val remoteDataSource: AiAssistantRemoteDataSource,
        private val settingsHandler: AiSettingsHandler,
        private val installationIdProvider: InstallationIdProvider,
    ) : AiCompletionHandler {

        override suspend fun complete(
            request: ChatCompletionRequestPojo,
            requestKey: String,
        ): ChatCompletionResponsePojo {
            return when (settingsHandler.fetchSettings().first().serviceType) {
                AiServiceType.PERSONAL -> {
                    val apiKey = checkNotNull(settingsHandler.fetchPersonalKey())
                    remoteDataSource.completePersonal(request, apiKey).response
                }
                AiServiceType.SHARED -> completeShared(request, requestKey)
            }
        }

        override suspend fun testPersonalKey(request: ChatCompletionRequestPojo, apiKey: String) {
            remoteDataSource.completePersonal(request, apiKey.trim())
        }

        private suspend fun completeShared(
            request: ChatCompletionRequestPojo,
            requestKey: String,
        ): ChatCompletionResponsePojo {
            val installationId = installationIdProvider.fetchInstallationId()
            try {
                val result = remoteDataSource.completeShared(request, installationId, requestKey)
                result.sharedQuotaRemaining?.let { remaining ->
                    settingsHandler.updateSharedQuota(
                        remaining = remaining,
                        resetAt = result.sharedQuotaResetAtEpochMillis?.let(
                            Instant::fromEpochMilliseconds
                        ),
                    )
                }
                return result.response
            } catch (error: AiServiceException.QuotaExceeded) {
                settingsHandler.updateSharedQuota(
                    remaining = 0,
                    resetAt = error.resetAtEpochMillis?.let(Instant::fromEpochMilliseconds),
                )
                throw error
            }
        }
    }
}
