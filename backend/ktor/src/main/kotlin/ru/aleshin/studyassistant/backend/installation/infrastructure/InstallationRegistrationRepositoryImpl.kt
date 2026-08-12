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

package ru.aleshin.studyassistant.backend.installation.infrastructure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.aleshin.studyassistant.backend.database.tables.RateLimitEventsTable
import ru.aleshin.studyassistant.backend.installation.InstallationConfig
import ru.aleshin.studyassistant.backend.installation.domain.repository.InstallationRegistrationRepository
import ru.aleshin.studyassistant.backend.installation.domain.result.InstallationRegistrationStorageResult
import java.nio.ByteBuffer
import java.time.Instant
import java.time.ZoneOffset

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class InstallationRegistrationRepositoryImpl(
    private val database: Database,
    private val config: InstallationConfig,
) : InstallationRegistrationRepository {

    override suspend fun reserve(
        networkHash: ByteArray,
        now: Instant,
    ): InstallationRegistrationStorageResult = dbQuery {
        lockGlobalRegistrations()
        lockNetwork(networkHash = networkHash)

        val usageDate = now
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        val dayStart = usageDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
        val retryAt = usageDate
            .plusDays(1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
        val globalCount = eventAmount(
            networkHash = null,
            since = dayStart,
        )
        val networkCount = eventAmount(
            networkHash = networkHash,
            since = dayStart,
        )

        if (globalCount >= config.globalRegistrationLimitPerDay ||
            networkCount >= config.registrationLimitPerNetworkPerDay
        ) {
            return@dbQuery InstallationRegistrationStorageResult.RateLimited(
                retryAt = retryAt,
            )
        }

        RateLimitEventsTable.insert {
            it[installationHash] = networkHash
            it[type] = REGISTRATION_EVENT
            it[amount] = 1
            it[createdAt] = now.atOffset(ZoneOffset.UTC)
        }

        InstallationRegistrationStorageResult.Reserved
    }

    private fun eventAmount(
        networkHash: ByteArray?,
        since: Instant,
    ): Int {
        val amountSum = RateLimitEventsTable.amount.sum()
        val baseCondition = (RateLimitEventsTable.type eq REGISTRATION_EVENT) and
            (RateLimitEventsTable.createdAt greaterEq since.atOffset(ZoneOffset.UTC))
        val condition = networkHash?.let { hash ->
            baseCondition and (RateLimitEventsTable.installationHash eq hash)
        } ?: baseCondition

        return RateLimitEventsTable
            .select(amountSum)
            .where { condition }
            .singleOrNull()
            ?.get(amountSum)
            ?: 0
    }

    private fun lockNetwork(networkHash: ByteArray) {
        val key = ByteBuffer.wrap(networkHash, 0, Long.SIZE_BYTES).long
        TransactionManager.current().exec("SELECT pg_advisory_xact_lock($key)")
    }

    private fun lockGlobalRegistrations() {
        TransactionManager.current().exec(
            "SELECT pg_advisory_xact_lock($GLOBAL_REGISTRATION_LOCK_KEY)",
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

        const val REGISTRATION_EVENT = "installation:registration"
        const val GLOBAL_REGISTRATION_LOCK_KEY = 4_151_364_631_860_346_803L
    }
}
