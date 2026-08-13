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
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import ru.aleshin.studyassistant.backend.ai.AiConfig
import ru.aleshin.studyassistant.backend.ai.domain.model.AiQuota
import ru.aleshin.studyassistant.backend.ai.domain.repository.AiQuotaRepository
import ru.aleshin.studyassistant.backend.ai.domain.result.AiQuotaReservationResult
import ru.aleshin.studyassistant.backend.ads.infrastructure.AiRewardGrantsTable
import ru.aleshin.studyassistant.backend.database.tables.RateLimitEventsTable
import java.nio.ByteBuffer
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiQuotaRepositoryImpl(
    private val database: Database,
    private val config: AiConfig,
) : AiQuotaRepository {

    override suspend fun reserve(
        installationHash: ByteArray,
        messageId: UUID,
        requestHash: ByteArray,
        executionHash: ByteArray,
        now: Instant,
    ): AiQuotaReservationResult = dbQuery {
        lockGlobalBudget()
        lockInstallation(installationHash = installationHash)

        val usageDate = now
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        val resetAt = usageDate
            .plusDays(1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
        val currentUsed = currentUsage(
            installationHash = installationHash,
            usageDate = usageDate,
        )
        val grantedAmount = rewardedAmount(
            installationHash = installationHash,
            usageDate = usageDate,
        )
        val currentLimit = config.dailyMessageLimit + grantedAmount
        val rewardedResetsRemaining = (
            config.maxRewardedResetsPerDay - grantedAmount / config.rewardedMessageAmount
        ).coerceAtLeast(0)

        val existingRequest = AiRequestsTable
            .select(
                AiRequestsTable.inFlight,
                AiRequestsTable.executionCount,
                AiRequestsTable.requestHash,
                AiRequestsTable.lastExecutionHash,
                AiRequestsTable.succeeded,
                AiRequestsTable.updatedAt,
            )
            .where {
                (AiRequestsTable.installationHash eq installationHash) and
                    (AiRequestsTable.messageId eq messageId)
            }
            .limit(1)
            .singleOrNull()

        if (existingRequest != null) {
            val storedRequestHash = existingRequest[AiRequestsTable.requestHash]
            if (storedRequestHash.isNotEmpty() && !storedRequestHash.contentEquals(requestHash)) {
                return@dbQuery AiQuotaReservationResult.IdempotencyConflict
            }
            if (existingRequest[AiRequestsTable.lastExecutionHash].contentEquals(executionHash)) {
                return@dbQuery AiQuotaReservationResult.IdempotencyReplay
            }
            if (existingRequest[AiRequestsTable.executionCount] >= config.maxExecutionsPerMessage) {
                return@dbQuery AiQuotaReservationResult.MessageExecutionLimitExceeded
            }
        } else if (currentUsed >= currentLimit) {
            return@dbQuery AiQuotaReservationResult.QuotaExceeded(
                quota = AiQuota(
                    used = currentUsed,
                    limit = currentLimit,
                    rewardedResetsRemaining = rewardedResetsRemaining,
                ),
                resetAt = resetAt,
            )
        }

        if (activeExecutions(installationHash = installationHash, now = now) >=
            config.maxConcurrentExecutionsPerInstallation
        ) {
            return@dbQuery AiQuotaReservationResult.RateLimited(retryAt = null)
        }

        val dayStart = usageDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
        if (globalEventAmount(type = AI_EXECUTION_EVENT, since = dayStart) >=
            config.globalDailyExecutionLimit
        ) {
            return@dbQuery AiQuotaReservationResult.RateLimited(retryAt = resetAt)
        }

        checkExecutionLimit(
            installationHash = installationHash,
            now = now,
        )?.let { result ->
            return@dbQuery result
        }

        recordExecution(
            installationHash = installationHash,
            now = now,
        )

        if (existingRequest != null) {
            val stale = !existingRequest[AiRequestsTable.succeeded] &&
                !existingRequest[AiRequestsTable.updatedAt]
                    .toInstant()
                    .plus(config.reservationTimeout)
                    .isAfter(now)
            val activeAttempts = existingRequest[AiRequestsTable.inFlight]
                .takeUnless { stale }
                ?: 0

            AiRequestsTable.update(
                where = {
                    (AiRequestsTable.installationHash eq installationHash) and
                        (AiRequestsTable.messageId eq messageId)
                },
            ) {
                it[inFlight] = activeAttempts + 1
                it[executionCount] = existingRequest[AiRequestsTable.executionCount] + 1
                it[AiRequestsTable.requestHash] = requestHash
                it[lastExecutionHash] = executionHash
                it[updatedAt] = now.atOffset(ZoneOffset.UTC)
            }

            return@dbQuery AiQuotaReservationResult.Reserved(
                quota = AiQuota(
                    used = currentUsed,
                    limit = currentLimit,
                    rewardedResetsRemaining = rewardedResetsRemaining,
                ),
                resetAt = resetAt,
                isNewMessage = false,
            )
        }

        val nextUsed = currentUsed + 1

        if (currentUsed == 0) {
            AiUsageTable.insert {
                it[AiUsageTable.installationHash] = installationHash
                it[AiUsageTable.usageDate] = usageDate
                it[AiUsageTable.used] = nextUsed
            }
        } else {
            AiUsageTable.update(
                where = {
                    (AiUsageTable.installationHash eq installationHash) and
                        (AiUsageTable.usageDate eq usageDate)
                },
            ) {
                it[used] = nextUsed
            }
        }

        AiRequestsTable.insert {
            it[AiRequestsTable.installationHash] = installationHash
            it[AiRequestsTable.messageId] = messageId
            it[AiRequestsTable.requestHash] = requestHash
            it[AiRequestsTable.lastExecutionHash] = executionHash
            it[AiRequestsTable.usageDate] = usageDate
            it[AiRequestsTable.executionCount] = 1
            it[AiRequestsTable.inFlight] = 1
            it[AiRequestsTable.succeeded] = false
            it[AiRequestsTable.createdAt] = now.atOffset(ZoneOffset.UTC)
            it[AiRequestsTable.updatedAt] = now.atOffset(ZoneOffset.UTC)
        }

        AiQuotaReservationResult.Reserved(
            quota = AiQuota(
                used = nextUsed,
                limit = currentLimit,
                rewardedResetsRemaining = rewardedResetsRemaining,
            ),
            resetAt = resetAt,
            isNewMessage = true,
        )
    }

    override suspend fun finalize(
        installationHash: ByteArray,
        messageId: UUID,
        succeeded: Boolean,
        now: Instant,
    ) = dbQuery {
        lockInstallation(installationHash = installationHash)

        val request = AiRequestsTable
            .select(
                AiRequestsTable.usageDate,
                AiRequestsTable.inFlight,
                AiRequestsTable.succeeded,
            )
            .where {
                (AiRequestsTable.installationHash eq installationHash) and
                    (AiRequestsTable.messageId eq messageId)
            }
            .limit(1)
            .singleOrNull()
            ?: return@dbQuery

        val remainingAttempts = (request[AiRequestsTable.inFlight] - 1).coerceAtLeast(0)
        val hasSucceeded = request[AiRequestsTable.succeeded] || succeeded

        if (remainingAttempts == 0 && !hasSucceeded) {
            AiRequestsTable.deleteWhere {
                (AiRequestsTable.installationHash eq installationHash) and
                    (AiRequestsTable.messageId eq messageId)
            }
            decrementUsage(
                installationHash = installationHash,
                usageDate = request[AiRequestsTable.usageDate],
            )
        } else {
            AiRequestsTable.update(
                where = {
                    (AiRequestsTable.installationHash eq installationHash) and
                        (AiRequestsTable.messageId eq messageId)
                },
            ) {
                it[inFlight] = remainingAttempts
                it[AiRequestsTable.succeeded] = hasSucceeded
                it[updatedAt] = now.atOffset(ZoneOffset.UTC)
            }
        }
    }

    private fun checkExecutionLimit(
        installationHash: ByteArray,
        now: Instant,
    ): AiQuotaReservationResult.RateLimited? {
        val windowStart = now.minus(config.executionWindow)
        val count = eventAmount(
            installationHash = installationHash,
            type = AI_EXECUTION_EVENT,
            since = windowStart,
        )

        if (count < config.executionLimit) {
            return null
        }

        val oldest = RateLimitEventsTable
            .select(RateLimitEventsTable.createdAt)
            .where {
                (RateLimitEventsTable.installationHash eq installationHash) and
                    (RateLimitEventsTable.type eq AI_EXECUTION_EVENT) and
                    (RateLimitEventsTable.createdAt greaterEq windowStart.atOffset(ZoneOffset.UTC))
            }
            .orderBy(RateLimitEventsTable.createdAt, SortOrder.ASC)
            .limit(1)
            .singleOrNull()
            ?.get(RateLimitEventsTable.createdAt)
            ?.toInstant()

        return AiQuotaReservationResult.RateLimited(
            retryAt = oldest?.plus(config.executionWindow),
        )
    }

    private fun currentUsage(
        installationHash: ByteArray,
        usageDate: LocalDate,
    ): Int {
        return AiUsageTable
            .select(AiUsageTable.used)
            .where {
                (AiUsageTable.installationHash eq installationHash) and
                    (AiUsageTable.usageDate eq usageDate)
            }
            .limit(1)
            .singleOrNull()
            ?.get(AiUsageTable.used)
            ?: 0
    }

    private fun rewardedAmount(
        installationHash: ByteArray,
        usageDate: LocalDate,
    ): Int {
        val amountSum = AiRewardGrantsTable.amount.sum()

        return AiRewardGrantsTable
            .select(amountSum)
            .where {
                (AiRewardGrantsTable.installationHash eq installationHash) and
                    (AiRewardGrantsTable.usageDate eq usageDate)
            }
            .singleOrNull()
            ?.get(amountSum)
            ?: 0
    }

    private fun activeExecutions(
        installationHash: ByteArray,
        now: Instant,
    ): Int {
        val inFlightSum = AiRequestsTable.inFlight.sum()
        val activeAfter = now.minus(config.reservationTimeout).atOffset(ZoneOffset.UTC)

        return AiRequestsTable
            .select(inFlightSum)
            .where {
                (AiRequestsTable.installationHash eq installationHash) and
                    (AiRequestsTable.updatedAt greaterEq activeAfter)
            }
            .singleOrNull()
            ?.get(inFlightSum)
            ?: 0
    }

    private fun decrementUsage(
        installationHash: ByteArray,
        usageDate: LocalDate,
    ) {
        val used = currentUsage(
            installationHash = installationHash,
            usageDate = usageDate,
        )

        if (used <= 1) {
            AiUsageTable.deleteWhere {
                (AiUsageTable.installationHash eq installationHash) and
                    (AiUsageTable.usageDate eq usageDate)
            }
        } else {
            AiUsageTable.update(
                where = {
                    (AiUsageTable.installationHash eq installationHash) and
                        (AiUsageTable.usageDate eq usageDate)
                },
            ) {
                it[AiUsageTable.used] = used - 1
            }
        }
    }

    private fun eventAmount(
        installationHash: ByteArray,
        type: String,
        since: Instant,
    ): Int {
        val amountSum = RateLimitEventsTable.amount.sum()

        return RateLimitEventsTable
            .select(amountSum)
            .where {
                (RateLimitEventsTable.installationHash eq installationHash) and
                    (RateLimitEventsTable.type eq type) and
                    (RateLimitEventsTable.createdAt greaterEq since.atOffset(ZoneOffset.UTC))
            }
            .singleOrNull()
            ?.get(amountSum)
            ?: 0
    }

    private fun globalEventAmount(
        type: String,
        since: Instant,
    ): Int {
        val amountSum = RateLimitEventsTable.amount.sum()

        return RateLimitEventsTable
            .select(amountSum)
            .where {
                (RateLimitEventsTable.type eq type) and
                    (RateLimitEventsTable.createdAt greaterEq since.atOffset(ZoneOffset.UTC))
            }
            .singleOrNull()
            ?.get(amountSum)
            ?: 0
    }

    private fun recordExecution(
        installationHash: ByteArray,
        now: Instant,
    ) {
        RateLimitEventsTable.insert {
            it[RateLimitEventsTable.installationHash] = installationHash
            it[RateLimitEventsTable.type] = AI_EXECUTION_EVENT
            it[RateLimitEventsTable.amount] = 1
            it[RateLimitEventsTable.createdAt] = now.atOffset(ZoneOffset.UTC)
        }
    }

    private fun lockInstallation(installationHash: ByteArray) {
        val key = ByteBuffer.wrap(installationHash, 0, Long.SIZE_BYTES).long
        TransactionManager.current().exec("SELECT pg_advisory_xact_lock($key)")
    }

    private fun lockGlobalBudget() {
        TransactionManager.current().exec("SELECT pg_advisory_xact_lock($GLOBAL_BUDGET_LOCK_KEY)")
    }

    private suspend fun <T> dbQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction(db = database) {
                block()
            }
        }
    }

    private companion object {

        const val AI_EXECUTION_EVENT = "ai:execution"
        const val GLOBAL_BUDGET_LOCK_KEY = -6_212_460_841_208_561_109L
    }
}
