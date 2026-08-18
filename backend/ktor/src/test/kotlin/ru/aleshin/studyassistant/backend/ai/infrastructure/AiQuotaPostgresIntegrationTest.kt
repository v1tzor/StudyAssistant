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

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import ru.aleshin.studyassistant.backend.ads.domain.model.AdRewardPurpose
import ru.aleshin.studyassistant.backend.ads.infrastructure.AdRewardChallengesTable
import ru.aleshin.studyassistant.backend.ads.infrastructure.AdRewardRepositoryImpl
import ru.aleshin.studyassistant.backend.ai.AiConfig
import ru.aleshin.studyassistant.backend.ai.domain.result.AiQuotaReservationResult
import ru.aleshin.studyassistant.backend.database.DatabaseConfig
import ru.aleshin.studyassistant.backend.database.DatabaseFactory
import ru.aleshin.studyassistant.backend.database.tables.RateLimitEventsTable
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
@Testcontainers
class AiQuotaPostgresIntegrationTest {

    private lateinit var databaseFactory: DatabaseFactory

    private lateinit var repository: AiQuotaRepositoryImpl

    @BeforeTest
    fun setUp() {
        Flyway
            .configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        databaseFactory = DatabaseFactory(
            config = DatabaseConfig(
                jdbcUrl = postgres.jdbcUrl,
                user = postgres.username,
                password = postgres.password,
                maximumPoolSize = 5,
                connectionTimeoutMs = 5_000,
            ),
        )

        clearTables(
            database = databaseFactory.database,
        )

        repository = AiQuotaRepositoryImpl(
            database = databaseFactory.database,
            config = aiConfig(),
        )
    }

    @AfterTest
    fun tearDown() {
        databaseFactory.close()
    }

    @Test
    fun sameMessageShouldConsumeQuotaOnlyOnce() = runBlocking {
        val installationHash = randomHash()

        val messageId = UUID.randomUUID()

        val now = Instant.parse("2026-08-11T18:00:00Z")

        val first = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now,
        )

        val second = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now.plusSeconds(1),
        )

        val firstReserved = first as AiQuotaReservationResult.Reserved

        val secondReserved = second as AiQuotaReservationResult.Reserved

        assertEquals(
            expected = 1,
            actual = firstReserved.quota.used,
        )

        assertEquals(
            expected = true,
            actual = firstReserved.isNewMessage,
        )

        assertEquals(
            expected = 1,
            actual = secondReserved.quota.used,
        )

        assertEquals(
            expected = false,
            actual = secondReserved.isNewMessage,
        )
    }

    @Test
    fun reusedMessageIdWithDifferentContentShouldBeRejected() = runBlocking {
        val installationHash = randomHash()
        val messageId = UUID.randomUUID()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = ByteArray(HASH_SIZE_BYTES) { 1 },
            executionHash = ByteArray(HASH_SIZE_BYTES) { 1 },
            now = now,
        )
        val result = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = ByteArray(HASH_SIZE_BYTES) { 2 },
            executionHash = ByteArray(HASH_SIZE_BYTES) { 2 },
            now = now.plusSeconds(1),
        )

        check(result is AiQuotaReservationResult.IdempotencyConflict)
    }

    @Test
    fun identicalProviderExecutionShouldBeRejected() = runBlocking {
        val installationHash = randomHash()
        val messageId = UUID.randomUUID()
        val requestHash = randomHash()
        val executionHash = randomHash()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = requestHash,
            executionHash = executionHash,
            now = now,
        )
        val result = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = requestHash,
            executionHash = executionHash,
            now = now.plusSeconds(1),
        )

        check(result is AiQuotaReservationResult.IdempotencyReplay)
    }

    @Test
    fun cachedResponseShouldNotBeReplayedForNewOrFailedExecution() = runBlocking {
        val installationHash = randomHash()
        val messageId = UUID.randomUUID()
        val requestHash = randomHash()
        val firstExecutionHash = randomHash()
        val secondExecutionHash = randomHash()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        val firstReserve = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = requestHash,
            executionHash = firstExecutionHash,
            now = now,
        ) as AiQuotaReservationResult.Reserved
        repository.saveResponse(
            installationHash = installationHash,
            messageId = messageId,
            executionHash = firstExecutionHash,
            responsePayload = ByteArray(17) { 1 },
            responseNonce = ByteArray(12) { 2 },
        )
        repository.finalize(
            installationHash = installationHash,
            messageId = messageId,
            succeeded = true,
            reservationGeneration = firstReserve.reservationGeneration,
            now = now.plusSeconds(1),
        )

        val nextExecution = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = requestHash,
            executionHash = secondExecutionHash,
            now = now.plusSeconds(2),
        )
        check(nextExecution is AiQuotaReservationResult.Reserved)

        val activeReplay = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = requestHash,
            executionHash = secondExecutionHash,
            now = now.plusSeconds(3),
        ) as AiQuotaReservationResult.IdempotencyReplay
        assertNull(activeReplay.responsePayload)
        assertNull(activeReplay.responseNonce)

        repository.finalize(
            installationHash = installationHash,
            messageId = messageId,
            succeeded = false,
            reservationGeneration = nextExecution.reservationGeneration,
            now = now.plusSeconds(4),
        )

        val retry = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = requestHash,
            executionHash = secondExecutionHash,
            now = now.plusSeconds(5),
        )
        check(retry is AiQuotaReservationResult.Reserved)
    }

    @Test
    fun staleConcurrentExecutionShouldNotOverwriteLatestResponseCache() = runBlocking {
        val installationHash = randomHash()
        val messageId = UUID.randomUUID()
        val requestHash = randomHash()
        val firstExecutionHash = randomHash()
        val secondExecutionHash = randomHash()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        check(
            repository.reserve(
                installationHash = installationHash,
                messageId = messageId,
                requestHash = requestHash,
                executionHash = firstExecutionHash,
                now = now,
            ) is AiQuotaReservationResult.Reserved,
        )
        check(
            repository.reserve(
                installationHash = installationHash,
                messageId = messageId,
                requestHash = requestHash,
                executionHash = secondExecutionHash,
                now = now.plusSeconds(1),
            ) is AiQuotaReservationResult.Reserved,
        )

        repository.saveResponse(
            installationHash = installationHash,
            messageId = messageId,
            executionHash = firstExecutionHash,
            responsePayload = ByteArray(17) { 3 },
            responseNonce = ByteArray(12) { 4 },
        )

        val replay = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = requestHash,
            executionHash = secondExecutionHash,
            now = now.plusSeconds(2),
        ) as AiQuotaReservationResult.IdempotencyReplay
        assertNull(replay.responsePayload)
        assertNull(replay.responseNonce)
    }

    @Test
    fun globalDailyBudgetShouldSpanInstallations() = runBlocking {
        val limitedRepository = AiQuotaRepositoryImpl(
            database = databaseFactory.database,
            config = aiConfig(globalDailyExecutionLimit = 2),
        )
        val now = Instant.parse("2026-08-11T18:00:00Z")

        repeat(2) { index ->
            check(
                limitedRepository.reserve(
                    installationHash = randomHash(),
                    messageId = UUID.randomUUID(),
                    now = now.plusSeconds(index.toLong()),
                ) is AiQuotaReservationResult.Reserved,
            )
        }

        val result = limitedRepository.reserve(
            installationHash = randomHash(),
            messageId = UUID.randomUUID(),
            now = now.plusSeconds(2),
        )

        check(result is AiQuotaReservationResult.RateLimited)
        assertEquals(
            now.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1)
                .atStartOfDay(ZoneOffset.UTC).toInstant(),
            result.retryAt,
        )
    }

    @Test
    fun perInstallationConcurrencyShouldPreserveGlobalCapacity() = runBlocking {
        val limitedRepository = AiQuotaRepositoryImpl(
            database = databaseFactory.database,
            config = aiConfig(maxConcurrentExecutionsPerInstallation = 2),
        )
        val installationHash = randomHash()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        repeat(2) { index ->
            check(
                limitedRepository.reserve(
                    installationHash = installationHash,
                    messageId = UUID.randomUUID(),
                    now = now.plusSeconds(index.toLong()),
                ) is AiQuotaReservationResult.Reserved,
            )
        }

        check(
            limitedRepository.reserve(
                installationHash = installationHash,
                messageId = UUID.randomUUID(),
                now = now.plusSeconds(2),
            ) is AiQuotaReservationResult.RateLimited,
        )
    }

    @Test
    fun concurrentLastQuotaShouldAllowOnlyOneNewMessage() = runBlocking {
        val installationHash = randomHash()

        val now = Instant.parse("2026-08-11T18:00:00Z")

        repeat(11) { index ->
            repository.reserve(
                installationHash = installationHash,
                messageId = UUID.randomUUID(),
                now = now.minusSeconds((11 - index).toLong()),
            )
        }

        val start = CompletableDeferred<Unit>()

        val first = async(Dispatchers.Default) {
            start.await()

            repository.reserve(
                installationHash = installationHash,
                messageId = UUID.randomUUID(),
                now = now,
            )
        }

        val second = async(Dispatchers.Default) {
            start.await()

            repository.reserve(
                installationHash = installationHash,
                messageId = UUID.randomUUID(),
                now = now,
            )
        }

        start.complete(Unit)

        val results = awaitAll(first, second)

        assertEquals(
            expected = 1,
            actual = results.count {
                it is AiQuotaReservationResult.Reserved
            },
        )

        assertEquals(
            expected = 1,
            actual = results.count {
                it is AiQuotaReservationResult.QuotaExceeded
            },
        )

        val reserved = results
            .filterIsInstance<AiQuotaReservationResult.Reserved>()
            .single()

        assertEquals(
            expected = 12,
            actual = reserved.quota.used,
        )
    }

    @Test
    fun rewardedQuotaShouldGrantTwelveMessagesAtMostThreeTimesPerDay() = runBlocking {
        val config = aiConfig()
        val rewardRepository = AdRewardRepositoryImpl(
            database = databaseFactory.database,
            config = config,
        )
        val installationHash = randomHash()
        var currentTime = Instant.parse("2026-08-11T01:00:00Z")

        repeat(config.dailyMessageLimit) { index ->
            check(
                repository.reserve(
                    installationHash = installationHash,
                    messageId = UUID.randomUUID(),
                    now = currentTime.plusSeconds(index.toLong()),
                ) is AiQuotaReservationResult.Reserved,
            )
        }
        currentTime = currentTime.plus(Duration.ofHours(2))

        repeat(config.maxRewardedResetsPerDay) { rewardIndex ->
            val challenge = checkNotNull(
                rewardRepository.createChallenge(
                    installationHash = installationHash,
                    purpose = AdRewardPurpose.AI_QUOTA_RESET,
                    subjectHash = null,
                    now = currentTime,
                ),
            )
            val completion = checkNotNull(
                rewardRepository.completeChallenge(
                    installationHash = installationHash,
                    challengeId = challenge.id,
                    now = currentTime.plusSeconds(1),
                ),
            )

            assertEquals(12, completion.quota?.remaining)
            assertEquals(2 - rewardIndex, completion.quota?.rewardedResetsRemaining)

            repeat(config.rewardedMessageAmount) { index ->
                check(
                    repository.reserve(
                        installationHash = installationHash,
                        messageId = UUID.randomUUID(),
                        now = currentTime.plusSeconds(index.toLong() + 2),
                    ) is AiQuotaReservationResult.Reserved,
                )
            }
            currentTime = currentTime.plus(Duration.ofHours(2))
        }

        assertEquals(
            null,
            rewardRepository.createChallenge(
                installationHash = installationHash,
                purpose = AdRewardPurpose.AI_QUOTA_RESET,
                subjectHash = null,
                now = currentTime,
            ),
        )
    }

    @Test
    fun thirtyFirstExecutionShouldBeRateLimited() = runBlocking {
        val installationHash = randomHash()
        val messageIds = List(4) { UUID.randomUUID() }
        val now = Instant.parse("2026-08-11T18:00:00Z")

        repeat(30) { index ->
            val result = repository.reserve(
                installationHash = installationHash,
                messageId = messageIds[index / 8],
                now = now.plusSeconds(index.toLong()),
            )

            check(result is AiQuotaReservationResult.Reserved)
        }

        val result = repository.reserve(
            installationHash = installationHash,
            messageId = messageIds.last(),
            now = now.plusSeconds(30),
        )

        check(result is AiQuotaReservationResult.RateLimited)
    }

    @Test
    fun ninthExecutionForSameMessageShouldBeRejected() = runBlocking {
        val installationHash = randomHash()
        val messageId = UUID.randomUUID()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        repeat(8) { index ->
            check(
                repository.reserve(
                    installationHash = installationHash,
                    messageId = messageId,
                    now = now.plusSeconds(index.toLong()),
                ) is AiQuotaReservationResult.Reserved,
            )
        }

        val result = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now.plusSeconds(8),
        )

        check(result is AiQuotaReservationResult.MessageExecutionLimitExceeded)
    }

    @Test
    fun rejectedDailyQuotaShouldNotRecordProviderExecution() = runBlocking {
        val installationHash = randomHash()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        repeat(12) { index ->
            check(
                repository.reserve(
                    installationHash = installationHash,
                    messageId = UUID.randomUUID(),
                    now = now.plusSeconds(index.toLong()),
                ) is AiQuotaReservationResult.Reserved,
            )
        }

        check(
            repository.reserve(
                installationHash = installationHash,
                messageId = UUID.randomUUID(),
                now = now.plusSeconds(12),
            ) is AiQuotaReservationResult.QuotaExceeded,
        )

        transaction(db = databaseFactory.database) {
            assertEquals(12L, RateLimitEventsTable.selectAll().count())
        }
    }

    @Test
    fun failedNewMessageShouldReleaseDailyQuota() = runBlocking {
        val installationHash = randomHash()
        val messageId = UUID.randomUUID()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        val first = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now,
        ) as AiQuotaReservationResult.Reserved

        repository.finalize(
            installationHash = installationHash,
            messageId = messageId,
            succeeded = false,
            reservationGeneration = first.reservationGeneration,
            now = now.plusSeconds(1),
        )

        val retry = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now.plusSeconds(2),
        ) as AiQuotaReservationResult.Reserved

        assertEquals(1, first.quota.used)
        assertEquals(1, retry.quota.used)
        assertEquals(true, retry.isNewMessage)
    }

    @Test
    fun overlappingAttemptsShouldKeepOneChargeWhenAnyAttemptSucceeds() = runBlocking {
        val installationHash = randomHash()
        val messageId = UUID.randomUUID()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        val firstAttempt = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now,
        ) as AiQuotaReservationResult.Reserved
        val secondAttempt = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now.plusSeconds(1),
        ) as AiQuotaReservationResult.Reserved

        repository.finalize(
            installationHash = installationHash,
            messageId = messageId,
            succeeded = false,
            reservationGeneration = firstAttempt.reservationGeneration,
            now = now.plusSeconds(2),
        )
        repository.finalize(
            installationHash = installationHash,
            messageId = messageId,
            succeeded = true,
            reservationGeneration = secondAttempt.reservationGeneration,
            now = now.plusSeconds(3),
        )

        val continuation = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now.plusSeconds(4),
        ) as AiQuotaReservationResult.Reserved

        assertEquals(1, continuation.quota.used)
        assertEquals(false, continuation.isNewMessage)
    }

    @Test
    fun lateFailedFinalizeAfterTimeoutMustNotDeleteNewReservation() = runBlocking {
        val installationHash = randomHash()
        val messageId = UUID.randomUUID()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        val first = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now,
        ) as AiQuotaReservationResult.Reserved

        val retry = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now.plusSeconds(301),
        ) as AiQuotaReservationResult.Reserved

        repository.finalize(
            installationHash = installationHash,
            messageId = messageId,
            succeeded = false,
            reservationGeneration = first.reservationGeneration,
            now = now.plusSeconds(302),
        )

        assertEquals(1, first.quota.used)
        assertEquals(false, retry.isNewMessage)
        transaction(db = databaseFactory.database) {
            assertEquals(1L, AiRequestsTable.selectAll().count())
            assertEquals(1, AiUsageTable.selectAll().single()[AiUsageTable.used])
        }

        repository.finalize(
            installationHash = installationHash,
            messageId = messageId,
            succeeded = false,
            reservationGeneration = retry.reservationGeneration,
            now = now.plusSeconds(303),
        )

        val refunded = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = now.plusSeconds(304),
        ) as AiQuotaReservationResult.Reserved

        assertEquals(1, refunded.quota.used)
        assertEquals(true, refunded.isNewMessage)
    }

    @Test
    fun failedAttemptsShouldStillReachExecutionLimit() = runBlocking {
        val installationHash = randomHash()
        val now = Instant.parse("2026-08-11T18:00:00Z")

        repeat(30) { index ->
            val messageId = UUID.randomUUID()
            val attemptTime = now.plusSeconds(index.toLong())

            val reserved = repository.reserve(
                installationHash = installationHash,
                messageId = messageId,
                now = attemptTime,
            ) as AiQuotaReservationResult.Reserved
            repository.finalize(
                installationHash = installationHash,
                messageId = messageId,
                succeeded = false,
                reservationGeneration = reserved.reservationGeneration,
                now = attemptTime,
            )
        }

        val result = repository.reserve(
            installationHash = installationHash,
            messageId = UUID.randomUUID(),
            now = now.plusSeconds(30),
        )

        check(result is AiQuotaReservationResult.RateLimited)
    }

    @Test
    fun cleanupShouldRemoveExpiredReservationAndUsageRows() = runBlocking {
        val installationHash = randomHash()
        val messageId = UUID.randomUUID()
        val createdAt = Instant.parse("2026-08-08T18:00:00Z")
        val cleanupAt = Instant.parse("2026-08-12T18:00:00Z")

        val reserved = repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            now = createdAt,
        ) as AiQuotaReservationResult.Reserved
        repository.finalize(
            installationHash = installationHash,
            messageId = messageId,
            succeeded = true,
            reservationGeneration = reserved.reservationGeneration,
            now = createdAt,
        )
        transaction(db = databaseFactory.database) {
            AdRewardChallengesTable.insert {
                it[id] = Uuid.parse(UUID.randomUUID().toString())
                it[AdRewardChallengesTable.installationHash] = installationHash
                it[purpose] = AdRewardPurpose.SCHEDULE_IMPORT.value
                it[subjectHash] = randomHash()
                it[AdRewardChallengesTable.createdAt] = createdAt.atOffset(ZoneOffset.UTC)
                it[expiresAt] = createdAt.plusSeconds(60).atOffset(ZoneOffset.UTC)
                it[completedAt] = null
                it[consumedAt] = null
            }
            AdRewardChallengesTable.insert {
                it[id] = Uuid.parse(UUID.randomUUID().toString())
                it[AdRewardChallengesTable.installationHash] = installationHash
                it[purpose] = AdRewardPurpose.SCHEDULE_IMPORT.value
                it[subjectHash] = randomHash()
                it[AdRewardChallengesTable.createdAt] = cleanupAt.minusSeconds(60).atOffset(ZoneOffset.UTC)
                it[expiresAt] = cleanupAt.plusSeconds(60).atOffset(ZoneOffset.UTC)
                it[completedAt] = null
                it[consumedAt] = null
            }
        }

        val result = AiCleanupRepositoryImpl(
            database = databaseFactory.database,
        ).cleanup(
            now = cleanupAt,
        )

        assertEquals(1, result.removedRequests)
        assertEquals(1, result.removedUsageRows)
        assertEquals(1, result.removedRewardChallenges)

        transaction(db = databaseFactory.database) {
            assertEquals(0L, AiRequestsTable.selectAll().count())
            assertEquals(0L, AiUsageTable.selectAll().count())
            assertEquals(1L, AdRewardChallengesTable.selectAll().count())
        }
    }

    private fun clearTables(
        database: Database,
    ) {
        transaction(
            db = database,
        ) {
            exec(
                """
                TRUNCATE TABLE
                    ai_reward_grants,
                    ad_reward_challenges,
                    ai_requests,
                    ai_usage,
                    rate_limit_events
                RESTART IDENTITY
                """.trimIndent(),
            )
        }
    }

    private fun randomHash(): ByteArray {
        return ByteArray(HASH_SIZE_BYTES).also(secureRandom::nextBytes)
    }

    private suspend fun AiQuotaRepositoryImpl.reserve(
        installationHash: ByteArray,
        messageId: UUID,
        now: Instant,
    ): AiQuotaReservationResult {
        return reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = ByteArray(HASH_SIZE_BYTES) { messageId.hashCode().toByte() },
            executionHash = ByteArray(HASH_SIZE_BYTES) { index ->
                (now.epochSecond + index).toByte()
            },
            now = now,
        )
    }

    private fun aiConfig(
        globalDailyExecutionLimit: Int = 2_000,
        maxConcurrentExecutionsPerInstallation: Int = 100,
    ): AiConfig {
        return AiConfig(
            dailyMessageLimit = 12,
            rewardedMessageAmount = 12,
            maxRewardedResetsPerDay = 3,
            rewardChallengeLifetime = Duration.ofMinutes(15),
            globalDailyExecutionLimit = globalDailyExecutionLimit,
            executionLimit = 30,
            maxExecutionsPerMessage = 8,
            maxConcurrentExecutionsPerInstallation = maxConcurrentExecutionsPerInstallation,
            executionWindow = Duration.ofHours(1),
            reservationTimeout = Duration.ofMinutes(5),
            maxRequestBodyBytes = 262_144,
            maxMessages = 80,
            maxMessageCharacters = 32_768,
            maxTotalContentCharacters = 131_072,
            maxTools = 32,
            maxToolDescriptionCharacters = 2_048,
            maxToolSchemaCharacters = 16_384,
            maxToolCallsPerMessage = 16,
            maxToolArgumentsCharacters = 16_384,
            maxScheduleRequestBodyBytes = 1_048_576,
            maxScheduleImageBytes = 786_432,
            maxScheduleNoteCharacters = 120,
            maxScheduleEntries = 300,
            maxScheduleFieldCharacters = 512,
            maxScheduleUnparsedLines = 100,
        )
    }

    private companion object {

        const val HASH_SIZE_BYTES = 32

        val secureRandom = SecureRandom()

        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:18.4-alpine")
            .withDatabaseName("studyassistant_ai_test",)
            .withUsername("studyassistant_test")
            .withPassword("studyassistant_test")
    }
}
