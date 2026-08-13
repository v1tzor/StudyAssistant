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

package ru.aleshin.studyassistant.core.remote.datasources.ads

import ru.aleshin.studyassistant.core.remote.api.ads.AdRewardRemoteApi
import ru.aleshin.studyassistant.core.remote.models.ads.AdRewardChallengeRequestPojo
import ru.aleshin.studyassistant.core.remote.models.ads.AdRewardChallengeResponsePojo
import ru.aleshin.studyassistant.core.remote.models.ads.AdRewardCompletionResponsePojo

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
interface AdRewardRemoteDataSource {

    suspend fun createChallenge(request: AdRewardChallengeRequestPojo, installationToken: String): AdRewardChallengeResponsePojo
    suspend fun completeChallenge(challengeId: String, installationToken: String): AdRewardCompletionResponsePojo

    class Base(
        private val api: AdRewardRemoteApi
    ) : AdRewardRemoteDataSource {

        override suspend fun createChallenge(
            request: AdRewardChallengeRequestPojo,
            installationToken: String
        ): AdRewardChallengeResponsePojo {
            return api.createChallenge(request, installationToken)
        }

        override suspend fun completeChallenge(
            challengeId: String,
            installationToken: String,
        ): AdRewardCompletionResponsePojo {
            return api.completeChallenge(challengeId, installationToken)
        }
    }
}
