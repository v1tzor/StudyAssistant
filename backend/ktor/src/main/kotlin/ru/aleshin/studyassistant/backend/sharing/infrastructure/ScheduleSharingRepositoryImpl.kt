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
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import ru.aleshin.studyassistant.backend.sharing.SharingConfig
import ru.aleshin.studyassistant.backend.sharing.domain.model.StoredScheduleShare
import ru.aleshin.studyassistant.backend.sharing.domain.repository.ScheduleSharingRepository
import ru.aleshin.studyassistant.backend.sharing.domain.result.ClaimActionStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.ClaimScheduleShareStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.CodeLookupStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.CreateScheduleShareStorageResult
import ru.aleshin.studyassistant.backend.database.tables.RateLimitEventsTable
import java.nio.ByteBuffer
import java.time.Instant
import java.time.ZoneOffset

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class ScheduleSharingRepositoryImpl(
    private val database: Database,
    private val config: SharingConfig,
) : ScheduleSharingRepository {

    override suspend fun tryCreate(
        share: StoredScheduleShare
    ): CreateScheduleShareStorageResult = dbQuery {
        lockGlobalStorage()
        lockInstallation(installationHash = share.creatorHash)

        val hourlyCreates = eventAmount(
            installationHash = share.creatorHash,
            type = SCHEDULE_CREATE_EVENT,
            since = share.createdAt.minusSeconds(SECONDS_PER_HOUR),
        )

        if (hourlyCreates + 1 > config.createLimitPerHour) {
            return@dbQuery CreateScheduleShareStorageResult.Limited()
        }

        val dailyItems = eventAmount(
            installationHash = share.creatorHash,
            type = SCHEDULE_ITEMS_EVENT,
            since = share.createdAt.minusSeconds(SECONDS_PER_DAY),
        )

        if (dailyItems + share.itemCount > config.createdItemsLimitPerDay) {
            return@dbQuery CreateScheduleShareStorageResult.Limited()
        }

        if (!hasPayloadCapacity(
                creatorHash = share.creatorHash,
                payloadBytes = share.payload.size,
                now = share.createdAt,
            )
        ) {
            return@dbQuery CreateScheduleShareStorageResult.Limited()
        }

        val inserted = ScheduleSharesTable.insertIgnore {
            it[id] = share.id
            it[codeHash] = share.codeHash
            it[creatorHash] = share.creatorHash
            it[itemCount] = share.itemCount
            it[payload] = share.payload
            it[payloadNonce] = share.payloadNonce
            it[createdAt] = share.createdAt.atOffset(ZoneOffset.UTC)
            it[expiresAt] = share.expiresAt.atOffset(ZoneOffset.UTC)
        }

        if (inserted.insertedCount == 0) {
            return@dbQuery CreateScheduleShareStorageResult.CodeConflict
        }

        insertEvent(
            installationHash = share.creatorHash,
            type = SCHEDULE_CREATE_EVENT,
            amount = 1,
            now = share.createdAt,
        )

        insertEvent(
            installationHash = share.creatorHash,
            type = SCHEDULE_ITEMS_EVENT,
            amount = share.itemCount,
            now = share.createdAt,
        )

        insertEvent(
            installationHash = share.creatorHash,
            type = SHARE_PAYLOAD_BYTES_EVENT,
            amount = share.payload.size,
            now = share.createdAt,
        )

        CreateScheduleShareStorageResult.Created
    }

    override suspend fun recordCodeLookup(
        installationHash: ByteArray,
        now: Instant,
    ): CodeLookupStorageResult = dbQuery {
        lockInstallation(installationHash = installationHash)

        val windowStart = now.minus(config.codeLookupWindow)

        val attempts = eventAmount(
            installationHash = installationHash,
            type = SCHEDULE_CODE_LOOKUP_EVENT,
            since = windowStart,
        )

        if (attempts >= config.codeLookupLimit) {
            val oldestAttempt = RateLimitEventsTable
                .select(RateLimitEventsTable.createdAt)
                .where {
                    (RateLimitEventsTable.installationHash eq installationHash) and
                            (RateLimitEventsTable.type eq SCHEDULE_CODE_LOOKUP_EVENT) and
                            (RateLimitEventsTable.createdAt greaterEq windowStart.atOffset(ZoneOffset.UTC))
                }
                .orderBy(RateLimitEventsTable.createdAt, SortOrder.ASC)
                .limit(1)
                .singleOrNull()
                ?.get(RateLimitEventsTable.createdAt)
                ?.toInstant()

            return@dbQuery CodeLookupStorageResult.Limited(
                retryAt = oldestAttempt?.plus(config.codeLookupWindow),
            )
        }

        insertEvent(
            installationHash = installationHash,
            type = SCHEDULE_CODE_LOOKUP_EVENT,
            amount = 1,
            now = now,
        )

        CodeLookupStorageResult.Allowed
    }

    override suspend fun claim(
        codeHash: ByteArray,
        claimHash: ByteArray,
        now: Instant,
        claimedUntil: Instant,
    ): ClaimScheduleShareStorageResult = dbQuery {
        val row = ScheduleSharesTable
            .selectAll()
            .where {
                ScheduleSharesTable.codeHash eq codeHash
            }
            .forUpdate()
            .limit(1)
            .singleOrNull() ?: return@dbQuery ClaimScheduleShareStorageResult.NotFound

        val share = row.toStoredScheduleShare()

        if (!share.expiresAt.isAfter(now)) {
            return@dbQuery ClaimScheduleShareStorageResult.NotFound
        }

        if (share.consumedAt != null) {
            return@dbQuery ClaimScheduleShareStorageResult.Consumed
        }

        if (share.claimedUntil != null && share.claimedUntil.isAfter(now)) {
            return@dbQuery ClaimScheduleShareStorageResult.Busy(
                claimedUntil = share.claimedUntil,
            )
        }

        ScheduleSharesTable.update(
            where = {
                ScheduleSharesTable.id eq share.id
            },
        ) {
            it[ScheduleSharesTable.claimHash] = claimHash
            it[ScheduleSharesTable.claimedUntil] = claimedUntil.atOffset(ZoneOffset.UTC)
        }

        ClaimScheduleShareStorageResult.Claimed(
            share = share.copy(
                claimHash = claimHash,
                claimedUntil = claimedUntil,
            ),
        )
    }

    override suspend fun confirm(
        claimHash: ByteArray,
        now: Instant,
    ): ClaimActionStorageResult = dbQuery {
        val row = ScheduleSharesTable
            .selectAll()
            .where {
                ScheduleSharesTable.claimHash eq claimHash
            }
            .forUpdate()
            .limit(1)
            .singleOrNull() ?: return@dbQuery ClaimActionStorageResult.InvalidClaim

        val share = row.toStoredScheduleShare()

        val claimedUntil = share.claimedUntil ?: return@dbQuery ClaimActionStorageResult.InvalidClaim

        if (!claimedUntil.isAfter(now)) {
            return@dbQuery ClaimActionStorageResult.InvalidClaim
        }

        /*
         * Confirm is idempotent while the claim is valid.
         */
        if (share.consumedAt == null) {
            ScheduleSharesTable.update(
                where = {
                    ScheduleSharesTable.id eq share.id
                },
            ) {
                it[consumedAt] = now.atOffset(ZoneOffset.UTC)
            }
        }

        ClaimActionStorageResult.Success
    }

    override suspend fun release(
        claimHash: ByteArray,
        now: Instant,
    ): ClaimActionStorageResult = dbQuery {
        val row = ScheduleSharesTable
            .selectAll()
            .where {
                ScheduleSharesTable.claimHash eq claimHash
            }
            .forUpdate()
            .limit(1)
            .singleOrNull() ?: return@dbQuery ClaimActionStorageResult.InvalidClaim

        val share = row.toStoredScheduleShare()

        val claimedUntil = share.claimedUntil
            ?: return@dbQuery ClaimActionStorageResult.InvalidClaim

        if (!claimedUntil.isAfter(now)) {
            return@dbQuery ClaimActionStorageResult.InvalidClaim
        }

        if (share.consumedAt != null) {
            return@dbQuery ClaimActionStorageResult.Consumed
        }

        ScheduleSharesTable.update(
            where = {
                ScheduleSharesTable.id eq share.id
            },
        ) {
            it[ScheduleSharesTable.claimHash] = null
            it[ScheduleSharesTable.claimedUntil] = null
        }

        ClaimActionStorageResult.Success
    }

    private fun ResultRow.toStoredScheduleShare(): StoredScheduleShare {
        return StoredScheduleShare(
            id = this[ScheduleSharesTable.id],
            codeHash = this[ScheduleSharesTable.codeHash],
            creatorHash = this[ScheduleSharesTable.creatorHash],
            itemCount = this[ScheduleSharesTable.itemCount],
            payload = this[ScheduleSharesTable.payload],
            payloadNonce = this[ScheduleSharesTable.payloadNonce],
            createdAt = this[ScheduleSharesTable.createdAt].toInstant(),
            expiresAt = this[ScheduleSharesTable.expiresAt].toInstant(),
            claimHash = this[ScheduleSharesTable.claimHash],
            claimedUntil = this[ScheduleSharesTable.claimedUntil]?.toInstant(),
            consumedAt = this[ScheduleSharesTable.consumedAt]?.toInstant(),
        )
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
            ?.get(amountSum) ?: 0
    }

    private fun insertEvent(
        installationHash: ByteArray,
        type: String,
        amount: Int,
        now: Instant,
    ) {
        RateLimitEventsTable.insert {
            it[RateLimitEventsTable.installationHash] = installationHash
            it[RateLimitEventsTable.type] = type
            it[RateLimitEventsTable.amount] = amount
            it[RateLimitEventsTable.createdAt] = now.atOffset(ZoneOffset.UTC)
        }
    }

    private fun hasPayloadCapacity(
        creatorHash: ByteArray,
        payloadBytes: Int,
        now: Instant,
    ): Boolean {
        val dayStart = now.minusSeconds(SECONDS_PER_DAY)
        val installationDailyBytes = eventAmount(
            installationHash = creatorHash,
            type = SHARE_PAYLOAD_BYTES_EVENT,
            since = dayStart,
        )
        if (installationDailyBytes + payloadBytes > config.createdPayloadBytesLimitPerDay) {
            return false
        }

        val globalDailyBytes = globalEventAmount(
            type = SHARE_PAYLOAD_BYTES_EVENT,
            since = dayStart,
        )
        if (globalDailyBytes + payloadBytes > config.globalCreatedPayloadBytesLimitPerDay) {
            return false
        }

        if (activePayloadBytes(creatorHash = creatorHash, now = now) + payloadBytes >
            config.activePayloadBytesLimitPerInstallation
        ) {
            return false
        }

        return globalActivePayloadBytes(now = now) + payloadBytes <=
            config.globalActivePayloadBytesLimit
    }

    private fun activePayloadBytes(
        creatorHash: ByteArray,
        now: Instant,
    ): Long {
        val scheduleSum = ScheduleSharesTable.payloadSize.sum()
        val homeworkSum = HomeworkSharesTable.payloadSize.sum()
        val timestamp = now.atOffset(ZoneOffset.UTC)
        val scheduleBytes = ScheduleSharesTable
            .select(scheduleSum)
            .where {
                (ScheduleSharesTable.creatorHash eq creatorHash) and
                    (ScheduleSharesTable.expiresAt greater timestamp)
            }
            .singleOrNull()
            ?.get(scheduleSum)
            ?: 0L
        val homeworkBytes = HomeworkSharesTable
            .select(homeworkSum)
            .where {
                (HomeworkSharesTable.creatorHash eq creatorHash) and
                    (HomeworkSharesTable.expiresAt greater timestamp)
            }
            .singleOrNull()
            ?.get(homeworkSum)
            ?: 0L
        return scheduleBytes + homeworkBytes
    }

    private fun globalActivePayloadBytes(now: Instant): Long {
        val scheduleSum = ScheduleSharesTable.payloadSize.sum()
        val homeworkSum = HomeworkSharesTable.payloadSize.sum()
        val timestamp = now.atOffset(ZoneOffset.UTC)
        val scheduleBytes = ScheduleSharesTable
            .select(scheduleSum)
            .where { ScheduleSharesTable.expiresAt greater timestamp }
            .singleOrNull()
            ?.get(scheduleSum)
            ?: 0L
        val homeworkBytes = HomeworkSharesTable
            .select(homeworkSum)
            .where { HomeworkSharesTable.expiresAt greater timestamp }
            .singleOrNull()
            ?.get(homeworkSum)
            ?: 0L
        return scheduleBytes + homeworkBytes
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

    private fun lockInstallation(
        installationHash: ByteArray,
    ) {
        val key = ByteBuffer.wrap(installationHash, 0, Long.SIZE_BYTES).long
        TransactionManager.current().exec("SELECT pg_advisory_xact_lock($key)")
    }

    private fun lockGlobalStorage() {
        TransactionManager.current().exec("SELECT pg_advisory_xact_lock($GLOBAL_STORAGE_LOCK_KEY)")
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

        const val SCHEDULE_CREATE_EVENT = "schedule:create"

        const val SCHEDULE_ITEMS_EVENT = "schedule:items"

        const val SCHEDULE_CODE_LOOKUP_EVENT = "schedule:code_lookup"

        const val SHARE_PAYLOAD_BYTES_EVENT = "share:payload_bytes"

        const val GLOBAL_STORAGE_LOCK_KEY = -4_492_233_787_865_511_931L

        const val SECONDS_PER_HOUR = 60L * 60L

        const val SECONDS_PER_DAY = 24L * 60L * 60L
    }
}
