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

package ru.aleshin.studyassistant.backend.ai.infrastructure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.aleshin.studyassistant.backend.ai.domain.repository.AiCleanupRepository
import ru.aleshin.studyassistant.backend.ai.domain.result.AiCleanupResult
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiCleanupRepositoryImpl(
    private val database: Database,
) : AiCleanupRepository {

    override suspend fun cleanup(now: Instant): AiCleanupResult = dbQuery {
        val requestCutoff = now
            .minus(RETENTION)
            .atOffset(ZoneOffset.UTC)
        val usageCutoff = now
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .minusDays(RETENTION.toDays())

        val removedRequests = AiRequestsTable.deleteWhere {
            AiRequestsTable.updatedAt less requestCutoff
        }
        val removedUsageRows = AiUsageTable.deleteWhere {
            AiUsageTable.usageDate less usageCutoff
        }

        AiCleanupResult(
            removedRequests = removedRequests,
            removedUsageRows = removedUsageRows,
        )
    }

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction(db = database) {
                block()
            }
        }
    }

    private companion object {

        val RETENTION: Duration = Duration.ofDays(2)
    }
}
