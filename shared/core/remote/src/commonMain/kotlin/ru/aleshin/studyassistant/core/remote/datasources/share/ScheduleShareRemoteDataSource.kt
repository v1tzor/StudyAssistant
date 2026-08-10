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
import ru.aleshin.studyassistant.core.remote.models.shared.ClaimScheduleShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ConfirmScheduleShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.CreateScheduleShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.CreateShareResponsePojo
import ru.aleshin.studyassistant.core.remote.models.shared.ReleaseScheduleShareRequestPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ScheduleShareClaimPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ScheduleSharePayloadPojo
import ru.aleshin.studyassistant.core.remote.models.shared.ScheduleShareResponsePojo
import ru.aleshin.studyassistant.core.remote.models.shared.ShareLinkResponsePojo

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
interface ScheduleShareRemoteDataSource {
    suspend fun createShare(
        share: ScheduleSharePayloadPojo,
        installationToken: String,
    ): ShareLinkResponsePojo

    suspend fun claimShare(code: String, installationToken: String): ScheduleShareClaimPojo
    suspend fun confirmShare(claimToken: String)
    suspend fun releaseShare(claimToken: String)

    class Base(
        private val functionsService: FunctionsService,
        private val json: Json,
    ) : ScheduleShareRemoteDataSource {

        override suspend fun createShare(
            share: ScheduleSharePayloadPojo,
            installationToken: String,
        ): ShareLinkResponsePojo = execute(
            request = json.encodeToString(
                CreateScheduleShareRequestPojo(
                    operation = SCHEDULE_CREATE_OPERATION,
                    installationToken = installationToken,
                    share = share,
                )
            ),
            decode = { response -> json.decodeFromString<CreateShareResponsePojo>(response).link },
        )

        override suspend fun claimShare(
            code: String,
            installationToken: String,
        ): ScheduleShareClaimPojo = execute(
            request = json.encodeToString(
                ClaimScheduleShareRequestPojo(
                    operation = SCHEDULE_CLAIM_OPERATION,
                    installationToken = installationToken,
                    code = code,
                )
            ),
            decode = { response -> json.decodeFromString<ScheduleShareResponsePojo>(response).claim },
        )

        override suspend fun confirmShare(claimToken: String) {
            execute(
                request = json.encodeToString(
                    ConfirmScheduleShareRequestPojo(
                        operation = SCHEDULE_CONFIRM_OPERATION,
                        claimToken = claimToken,
                    )
                ),
                decode = { Unit },
            )
        }

        override suspend fun releaseShare(claimToken: String) {
            execute(
                request = json.encodeToString(
                    ReleaseScheduleShareRequestPojo(
                        operation = SCHEDULE_RELEASE_OPERATION,
                        claimToken = claimToken,
                    )
                ),
                decode = { Unit },
            )
        }

        private suspend fun <T> execute(request: String, decode: (String) -> T): T {
            return try {
                decode(functionsService.execute(AppwriteApi.Functions.SHARING, request))
            } catch (error: FunctionExecutionException) {
                throw error.mapToShareException(json)
            }
        }

        private companion object {
            const val SCHEDULE_CREATE_OPERATION = "schedule.create"
            const val SCHEDULE_CLAIM_OPERATION = "schedule.claim"
            const val SCHEDULE_CONFIRM_OPERATION = "schedule.confirm"
            const val SCHEDULE_RELEASE_OPERATION = "schedule.release"
        }
    }
}
