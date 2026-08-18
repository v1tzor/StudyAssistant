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
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import ru.aleshin.studyassistant.core.remote.api.share.BackendShareApi
import ru.aleshin.studyassistant.core.remote.models.shared.ScheduleShareClaimPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ScheduleSharePayloadPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ShareLinkResponsePojo

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
interface ScheduleShareRemoteDataSource {

    suspend fun createShare(
        share: ScheduleSharePayloadPojo,
        installationToken: String,
    ): ShareLinkResponsePojo

    suspend fun claimShare(code: String, installationToken: String): ScheduleShareClaimPojo
    suspend fun confirmShare(claimToken: String, installationToken: String)
    suspend fun releaseShare(claimToken: String, installationToken: String)

    class Base(
        private val api: BackendShareApi,
        private val json: Json,
    ) : ScheduleShareRemoteDataSource {

        override suspend fun createShare(
            share: ScheduleSharePayloadPojo,
            installationToken: String,
        ): ShareLinkResponsePojo {
            return api.createSchedule(
                share = json.encodeToJsonElement(share).jsonObject,
                installationToken = installationToken,
            )
        }

        override suspend fun claimShare(
            code: String,
            installationToken: String,
        ): ScheduleShareClaimPojo {
            val claim = api.claimSchedule(
                code = code,
                installationToken = installationToken,
            )
            return ScheduleShareClaimPojo(
                claimToken = claim.claimToken,
                share = json.decodeFromJsonElement(claim.share),
            )
        }

        override suspend fun confirmShare(claimToken: String, installationToken: String) {
            api.confirmSchedule(
                claimToken = claimToken,
                installationToken = installationToken,
            )
        }

        override suspend fun releaseShare(claimToken: String, installationToken: String) {
            api.releaseSchedule(
                claimToken = claimToken,
                installationToken = installationToken,
            )
        }
    }
}
