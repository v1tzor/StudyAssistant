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

package ru.aleshin.studyassistant.core.remote.datasources.ai

import ru.aleshin.studyassistant.core.remote.api.ai.AiRemoteApi
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionResponsePojo

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
interface AiAssistantRemoteDataSource {

    suspend fun complete(
        request: AiCompletionRequestPojo,
        installationToken: String,
    ): AiCompletionResponsePojo

    class Base(
        private val api: AiRemoteApi,
    ) : AiAssistantRemoteDataSource {

        override suspend fun complete(
            request: AiCompletionRequestPojo,
            installationToken: String,
        ): AiCompletionResponsePojo {
            return api.complete(
                request = request,
                installationToken = installationToken,
            )
        }
    }
}
