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

package ru.aleshin.studyassistant.core.remote.datasources.share

import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.api.AppwriteApi
import ru.aleshin.studyassistant.core.api.functions.FunctionExecutionException
import ru.aleshin.studyassistant.core.api.functions.FunctionsService
import ru.aleshin.studyassistant.core.remote.models.shared.CreateHomeworkShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.CreateShareResponsePojo
import ru.aleshin.studyassistant.core.remote.models.shared.FetchHomeworkShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.HomeworkSharePayloadPojo
import ru.aleshin.studyassistant.core.remote.models.shared.HomeworkShareResponsePojo
import ru.aleshin.studyassistant.core.remote.models.shared.ShareLinkResponsePojo

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
interface HomeworkShareRemoteDataSource {
    suspend fun createShare(
        share: HomeworkSharePayloadPojo,
        installationToken: String,
    ): ShareLinkResponsePojo

    suspend fun fetchShare(code: String, installationToken: String): HomeworkSharePayloadPojo

    class Base(
        private val functionsService: FunctionsService,
        private val json: Json,
    ) : HomeworkShareRemoteDataSource {

        override suspend fun createShare(
            share: HomeworkSharePayloadPojo,
            installationToken: String,
        ): ShareLinkResponsePojo = execute(
            request = json.encodeToString(
                CreateHomeworkShareRequestPojo(
                    operation = HOMEWORK_CREATE_OPERATION,
                    installationToken = installationToken,
                    share = share,
                )
            ),
            decode = { response -> json.decodeFromString<CreateShareResponsePojo>(response).link },
        )

        override suspend fun fetchShare(
            code: String,
            installationToken: String,
        ): HomeworkSharePayloadPojo = execute(
            request = json.encodeToString(
                FetchHomeworkShareRequestPojo(
                    operation = HOMEWORK_FETCH_OPERATION,
                    installationToken = installationToken,
                    code = code,
                )
            ),
            decode = { response -> json.decodeFromString<HomeworkShareResponsePojo>(response).share },
        )

        private suspend fun <T> execute(request: String, decode: (String) -> T): T {
            return try {
                decode(functionsService.execute(AppwriteApi.Functions.SHARING, request))
            } catch (error: FunctionExecutionException) {
                throw error.mapToShareException(json)
            }
        }

        private companion object {
            const val HOMEWORK_CREATE_OPERATION = "homework.create"
            const val HOMEWORK_FETCH_OPERATION = "homework.fetch"
        }
    }
}
