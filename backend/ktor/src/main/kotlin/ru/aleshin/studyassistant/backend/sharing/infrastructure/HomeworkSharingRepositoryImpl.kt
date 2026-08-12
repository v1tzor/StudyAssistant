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
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.aleshin.studyassistant.backend.sharing.SharingConfig
import ru.aleshin.studyassistant.backend.sharing.domain.model.StoredHomeworkShare
import ru.aleshin.studyassistant.backend.sharing.domain.repository.HomeworkSharingRepository
import ru.aleshin.studyassistant.backend.sharing.domain.result.CodeLookupStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.CreateHomeworkShareStorageResult
import ru.aleshin.studyassistant.backend.database.tables.RateLimitEventsTable
import java.nio.ByteBuffer
import java.time.Instant
import java.time.ZoneOffset

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class HomeworkSharingRepositoryImpl(
    private val database: Database,
    private val config: SharingConfig,
) : HomeworkSharingRepository {

    override suspend fun tryCreate(
        share: StoredHomeworkShare
    ): CreateHomeworkShareStorageResult = dbQuery {
        lockGlobalStorage()
        lockInstallation(installationHash = share.creatorHash)

        val hourStart = share.createdAt.minusSeconds(SECONDS_PER_HOUR)

        val dayStart = share.createdAt.minusSeconds(SECONDS_PER_DAY)

        val hourlyCreates = eventAmount(
            installationHash = share.creatorHash,
            type = HOMEWORK_CREATE_EVENT,
            since = hourStart,
        )

        if (hourlyCreates + 1 > config.createLimitPerHour) {
            return@dbQuery CreateHomeworkShareStorageResult.Limited()
        }

        val dailyItems = eventAmount(
            installationHash = share.creatorHash,
            type = HOMEWORK_ITEMS_EVENT,
            since = dayStart,
        )

        if (dailyItems + share.itemCount > config.createdItemsLimitPerDay) {
            return@dbQuery CreateHomeworkShareStorageResult.Limited()
        }

        val activeItems = activeHomeworkItems(
            creatorHash = share.creatorHash,
            now = share.createdAt,
        )

        if (activeItems + share.itemCount > config.activeHomeworkItemsLimit) {
            return@dbQuery CreateHomeworkShareStorageResult.Limited()
        }

        if (!hasPayloadCapacity(
                creatorHash = share.creatorHash,
                payloadBytes = share.payload.size,
                now = share.createdAt,
            )
        ) {
            return@dbQuery CreateHomeworkShareStorageResult.Limited()
        }

        val inserted = HomeworkSharesTable.insertIgnore {
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
            return@dbQuery CreateHomeworkShareStorageResult.CodeConflict
        }

        insertEvent(
            installationHash = share.creatorHash,
            type = HOMEWORK_CREATE_EVENT,
            amount = 1,
            now = share.createdAt,
        )

        insertEvent(
            installationHash = share.creatorHash,
            type = HOMEWORK_ITEMS_EVENT,
            amount = share.itemCount,
            now = share.createdAt,
        )

        insertEvent(
            installationHash = share.creatorHash,
            type = SHARE_PAYLOAD_BYTES_EVENT,
            amount = share.payload.size,
            now = share.createdAt,
        )

        CreateHomeworkShareStorageResult.Created
    }

    override suspend fun findAvailable(
        codeHash: ByteArray,
        now: Instant,
    ): StoredHomeworkShare? = dbQuery {
        HomeworkSharesTable
            .selectAll()
            .where {
                (HomeworkSharesTable.codeHash eq codeHash) and
                        (HomeworkSharesTable.expiresAt greater now.atOffset(ZoneOffset.UTC))
            }
            .limit(1)
            .singleOrNull()
            ?.let { row ->
                StoredHomeworkShare(
                    id = row[HomeworkSharesTable.id],
                    codeHash = row[HomeworkSharesTable.codeHash],
                    creatorHash = row[HomeworkSharesTable.creatorHash],
                    itemCount = row[HomeworkSharesTable.itemCount],
                    payload = row[HomeworkSharesTable.payload],
                    payloadNonce = row[HomeworkSharesTable.payloadNonce],
                    createdAt = row[HomeworkSharesTable.createdAt].toInstant(),
                    expiresAt = row[HomeworkSharesTable.expiresAt].toInstant(),
                )
            }
    }

    override suspend fun recordCodeLookup(
        installationHash: ByteArray,
        now: Instant,
    ): CodeLookupStorageResult = dbQuery {
        lockInstallation(installationHash = installationHash)

        val windowStart = now.minus(config.codeLookupWindow)

        val attempts = eventAmount(
            installationHash = installationHash,
            type = HOMEWORK_CODE_LOOKUP_EVENT,
            since = windowStart,
        )

        if (attempts >= config.codeLookupLimit) {
            val oldestAttempt = RateLimitEventsTable
                .select(RateLimitEventsTable.createdAt)
                .where {
                    (RateLimitEventsTable.installationHash eq installationHash) and
                            (RateLimitEventsTable.type eq HOMEWORK_CODE_LOOKUP_EVENT) and
                            (RateLimitEventsTable.createdAt greaterEq windowStart.atOffset(
                                ZoneOffset.UTC
                            ))
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
            type = HOMEWORK_CODE_LOOKUP_EVENT,
            amount = 1,
            now = now,
        )

        CodeLookupStorageResult.Allowed
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

    private fun activeHomeworkItems(
        creatorHash: ByteArray,
        now: Instant,
    ): Int {
        val itemCountSum = HomeworkSharesTable.itemCount.sum()

        return HomeworkSharesTable
            .select(itemCountSum)
            .where {
                (HomeworkSharesTable.creatorHash eq creatorHash) and
                        (HomeworkSharesTable.expiresAt greater now.atOffset(ZoneOffset.UTC))
            }
            .singleOrNull()
            ?.get(itemCountSum)
            ?: 0
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

        const val HOMEWORK_CREATE_EVENT = "homework:create"

        const val HOMEWORK_ITEMS_EVENT = "homework:items"

        const val HOMEWORK_CODE_LOOKUP_EVENT = "homework:code_lookup"

        const val SHARE_PAYLOAD_BYTES_EVENT = "share:payload_bytes"

        const val GLOBAL_STORAGE_LOCK_KEY = -4_492_233_787_865_511_931L

        const val SECONDS_PER_HOUR = 60L * 60L
        const val SECONDS_PER_DAY = 24L * 60L * 60L
    }
}
