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

package ru.aleshin.studyassistant.backend.sharing.infrastructure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.aleshin.studyassistant.backend.sharing.domain.repository.SharingCleanupRepository
import ru.aleshin.studyassistant.backend.sharing.domain.result.SharingCleanupResult
import ru.aleshin.studyassistant.backend.database.tables.RateLimitEventsTable
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class SharingCleanupRepositoryImpl(
    private val database: Database
) : SharingCleanupRepository {

    override suspend fun cleanup(now: Instant): SharingCleanupResult = dbQuery {
        val nowOffset = now.atOffset(ZoneOffset.UTC)

        val removedScheduleShares = ScheduleSharesTable.deleteWhere {
            ScheduleSharesTable.expiresAt lessEq nowOffset
        }

        val removedHomeworkShares = HomeworkSharesTable.deleteWhere {
            HomeworkSharesTable.expiresAt lessEq nowOffset
        }

        val rateLimitCutoff = now
            .minus(RATE_LIMIT_RETENTION)
            .atOffset(ZoneOffset.UTC)

        val removedRateLimitEvents = RateLimitEventsTable.deleteWhere {
            RateLimitEventsTable.createdAt less rateLimitCutoff
        }

        SharingCleanupResult(
            removedScheduleShares = removedScheduleShares,
            removedHomeworkShares = removedHomeworkShares,
            removedRateLimitEvents = removedRateLimitEvents,
        )
    }

    private suspend fun <T> dbQuery(
        block: () -> T,
    ): T {
        return withContext(Dispatchers.IO) {
            transaction(
                db = database,
            ) {
                block()
            }
        }
    }

    private companion object {

        val RATE_LIMIT_RETENTION: Duration = Duration.ofHours(24)
    }
}