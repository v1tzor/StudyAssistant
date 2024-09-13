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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.exceptions.RemoteException
import ru.aleshin.studyassistant.core.common.inject.CrashlyticsService
import ru.aleshin.studyassistant.core.common.inject.MessagingService
import ru.aleshin.studyassistant.core.common.messages.UniversalPushToken
import ru.aleshin.studyassistant.core.common.wrappers.EitherWrapper.Abstract.Companion.ERROR_TAG
import ru.aleshin.studyassistant.core.remote.ktor.StudyAssistantKtor.UniversalMessaging.HOST
import ru.aleshin.studyassistant.core.remote.ktor.StudyAssistantKtor.UniversalMessaging.SEND_TOKENS
import ru.aleshin.studyassistant.core.remote.mappers.message.mapToRemote
import ru.aleshin.studyassistant.core.remote.models.message.PushProviderPojo
import ru.aleshin.studyassistant.core.remote.models.message.UniversalMessageData

/**
 * @author Stanislav Aleshin on 01.08.2024.
 */
interface MessageRemoteDataSource {

    suspend fun fetchToken(): Flow<UniversalPushToken>
    suspend fun sendMessage(message: UniversalMessageData)
    suspend fun deleteToken()

    class Base(
        private val httpClient: HttpClient,
        private val tokenProviderFactory: PushServiceAuthTokenFactory,
        private val messagingService: MessagingService,
        private val crashlyticsService: CrashlyticsService,
    ) : MessageRemoteDataSource {

        override suspend fun fetchToken(): Flow<UniversalPushToken> {
            return messagingService.fetchToken()
        }

        override suspend fun sendMessage(message: UniversalMessageData) {
            val universalMessage = message.mapToRemote { pushServiceType ->
                val tokenProvider = tokenProviderFactory.fetchTokenProvider(pushServiceType)
                return@mapToRemote PushProviderPojo(
                    projectId = tokenProvider.fetchProjectId(),
                    authToken = tokenProvider.fetchAuthToken(),
                )
            }
            if (universalMessage.isAvailable()) {
                val httpResponse = httpClient.post(HOST + SEND_TOKENS) {
                    contentType(ContentType.Application.Json)
                    setBody(universalMessage)
                }
                if (!httpResponse.status.isSuccess()) {
                    crashlyticsService.recordException(
                        tag = ERROR_TAG,
                        message = httpResponse.bodyAsText(),
                        exception = RemoteException(httpResponse.status.value, httpResponse.status.description),
                    )
                }
            }
        }

        override suspend fun deleteToken() {
            messagingService.deleteToken()
        }
    }
}