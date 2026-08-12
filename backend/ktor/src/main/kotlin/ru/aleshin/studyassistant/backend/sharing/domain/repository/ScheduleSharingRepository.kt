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

package ru.aleshin.studyassistant.backend.sharing.domain.repository

import ru.aleshin.studyassistant.backend.sharing.domain.model.StoredScheduleShare
import ru.aleshin.studyassistant.backend.sharing.domain.result.ClaimActionStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.ClaimScheduleShareStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.CodeLookupStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.CreateScheduleShareStorageResult
import java.time.Instant

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
interface ScheduleSharingRepository {

    suspend fun tryCreate(
        share: StoredScheduleShare
    ): CreateScheduleShareStorageResult

    suspend fun recordCodeLookup(
        installationHash: ByteArray,
        now: Instant
    ): CodeLookupStorageResult

    suspend fun claim(
        codeHash: ByteArray,
        claimHash: ByteArray,
        now: Instant,
        claimedUntil: Instant,
    ): ClaimScheduleShareStorageResult

    suspend fun confirm(
        claimHash: ByteArray,
        now: Instant,
    ): ClaimActionStorageResult

    suspend fun release(
        claimHash: ByteArray,
        now: Instant,
    ): ClaimActionStorageResult
}