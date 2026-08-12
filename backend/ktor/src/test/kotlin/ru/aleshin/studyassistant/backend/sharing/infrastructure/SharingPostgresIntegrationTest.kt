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

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import ru.aleshin.studyassistant.backend.database.DatabaseConfig
import ru.aleshin.studyassistant.backend.database.DatabaseFactory
import ru.aleshin.studyassistant.backend.sharing.SharingConfig
import ru.aleshin.studyassistant.backend.sharing.domain.model.StoredHomeworkShare
import ru.aleshin.studyassistant.backend.sharing.domain.model.StoredScheduleShare
import ru.aleshin.studyassistant.backend.sharing.domain.result.ClaimScheduleShareStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.CreateScheduleShareStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.CreateHomeworkShareStorageResult
import ru.aleshin.studyassistant.backend.sharing.infrastructure.ScheduleSharesTable
import ru.aleshin.studyassistant.backend.sharing.infrastructure.HomeworkSharesTable
import ru.aleshin.studyassistant.backend.database.tables.RateLimitEventsTable
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
@Testcontainers
class SharingPostgresIntegrationTest {

    private lateinit var databaseFactory: DatabaseFactory

    private lateinit var scheduleRepository: ScheduleSharingRepositoryImpl

    private lateinit var cleanupRepository: SharingCleanupRepositoryImpl

    @BeforeEach
    fun setUp() {
        migrateDatabase()

        databaseFactory = DatabaseFactory(
            config = DatabaseConfig(
                jdbcUrl = postgres.jdbcUrl,
                user = postgres.username,
                password = postgres.password,
                maximumPoolSize = 5,
                connectionTimeoutMs = 5_000,
            ),
        )

        clearApplicationTables()

        scheduleRepository = ScheduleSharingRepositoryImpl(
            database = databaseFactory.database,
            config = sharingConfig(),
        )

        cleanupRepository = SharingCleanupRepositoryImpl(
            database = databaseFactory.database,
        )
    }

    @AfterEach
    fun tearDown() {
        databaseFactory.close()
    }

    @Test
    fun onlyOneConcurrentClaimShouldSucceed() =
        runBlocking {
            val now = Instant.parse(
                "2026-08-11T18:00:00Z",
            )

            val share = StoredScheduleShare(
                id = UUID.randomUUID(),
                codeHash = randomHash(),
                creatorHash = randomHash(),
                itemCount = 1,
                payload = encryptedPayload(),
                payloadNonce = ByteArray(12) { 1 },
                createdAt = now,
                expiresAt = now.plus(Duration.ofMinutes(30)),
                claimHash = null,
                claimedUntil = null,
                consumedAt = null,
            )

            val createResult = scheduleRepository.tryCreate(
                share = share,
            )

            assertEquals(
                expected = CreateScheduleShareStorageResult.Created,
                actual = createResult,
            )

            val start = CompletableDeferred<Unit>()

            val first = async(context = Dispatchers.Default) {
                start.await()

                scheduleRepository.claim(
                    codeHash = share.codeHash,
                    claimHash = randomHash(),
                    now = now,
                    claimedUntil = now.plus(Duration.ofMinutes(5)),
                )
            }

            val second = async(
                context = Dispatchers.Default,
            ) {
                start.await()

                scheduleRepository.claim(
                    codeHash = share.codeHash,
                    claimHash = randomHash(),
                    now = now,
                    claimedUntil = now.plus(
                        Duration.ofMinutes(5),
                    ),
                )
            }

            start.complete(Unit)

            val results = awaitAll(first, second)

            assertEquals(
                expected = 1,
                actual = results.count { result ->
                    result is ClaimScheduleShareStorageResult.Claimed
                },
            )

            assertEquals(
                expected = 1,
                actual = results.count { result ->
                    result is ClaimScheduleShareStorageResult.Busy
                },
            )
        }

    @Test
    fun globalPayloadBudgetShouldSpanShareTypesAndInstallations() = runBlocking {
        val config = sharingConfig(
            maxPayloadBytes = MIN_ENCRYPTED_PAYLOAD_SIZE,
            activePayloadBytesLimitPerInstallation = MIN_ENCRYPTED_PAYLOAD_SIZE.toLong(),
            globalActivePayloadBytesLimit = 25L,
        )
        val scheduleRepository = ScheduleSharingRepositoryImpl(
            database = databaseFactory.database,
            config = config,
        )
        val homeworkRepository = HomeworkSharingRepositoryImpl(
            database = databaseFactory.database,
            config = config,
        )
        val now = Instant.parse("2026-08-11T18:00:00Z")

        val scheduleResult = scheduleRepository.tryCreate(
            StoredScheduleShare(
                id = UUID.randomUUID(),
                codeHash = randomHash(),
                creatorHash = randomHash(),
                itemCount = 1,
                payload = encryptedPayload(),
                payloadNonce = ByteArray(12) { 1 },
                createdAt = now,
                expiresAt = now.plus(Duration.ofMinutes(30)),
                claimHash = null,
                claimedUntil = null,
                consumedAt = null,
            ),
        )
        val homeworkResult = homeworkRepository.tryCreate(
            StoredHomeworkShare(
                id = UUID.randomUUID(),
                codeHash = randomHash(),
                creatorHash = randomHash(),
                itemCount = 1,
                payload = encryptedPayload(),
                payloadNonce = ByteArray(12) { 2 },
                createdAt = now.plusSeconds(1),
                expiresAt = now.plus(Duration.ofHours(24)),
            ),
        )

        assertEquals(CreateScheduleShareStorageResult.Created, scheduleResult)
        check(homeworkResult is CreateHomeworkShareStorageResult.Limited)
    }

    @Test
    fun cleanupShouldRemoveOnlyExpiredData() =
        runBlocking {
            val now = Instant.parse(
                "2026-08-11T18:00:00Z",
            )

            transaction(
                db = databaseFactory.database,
            ) {
                insertScheduleShare(
                    expiresAt = now.minusSeconds(1),
                )

                insertScheduleShare(
                    expiresAt = now.plusSeconds(60),
                )

                insertHomeworkShare(
                    expiresAt = now.minusSeconds(1),
                )

                insertHomeworkShare(
                    expiresAt = now.plusSeconds(60),
                )

                insertRateLimitEvent(
                    createdAt = now.minus(
                        Duration.ofHours(25),
                    ),
                )

                insertRateLimitEvent(
                    createdAt = now.minus(
                        Duration.ofHours(23),
                    ),
                )
            }

            val result = cleanupRepository.cleanup(
                now = now,
            )

            assertEquals(
                expected = 1,
                actual = result.removedScheduleShares,
            )

            assertEquals(
                expected = 1,
                actual = result.removedHomeworkShares,
            )

            assertEquals(
                expected = 1,
                actual = result.removedRateLimitEvents,
            )

            transaction(
                db = databaseFactory.database,
            ) {
                assertEquals(
                    expected = 1L,
                    actual = ScheduleSharesTable.selectAll().count(),
                )

                assertEquals(
                    expected = 1L,
                    actual = HomeworkSharesTable.selectAll().count(),
                )

                assertEquals(
                    expected = 1L,
                    actual = RateLimitEventsTable.selectAll().count(),
                )
            }
        }

    private fun migrateDatabase() {
        Flyway
            .configure()
            .dataSource(
                postgres.jdbcUrl,
                postgres.username,
                postgres.password,
            )
            .locations(
                "classpath:db/migration",
            )
            .load()
            .migrate()
    }

    private fun clearApplicationTables() {
        transaction(
            db = databaseFactory.database,
        ) {
            exec(
                """
                TRUNCATE TABLE
                    rate_limit_events,
                    homework_shares,
                    schedule_shares
                RESTART IDENTITY
                """.trimIndent(),
            )
        }
    }

    private fun insertScheduleShare(
        expiresAt: Instant,
    ) {
        ScheduleSharesTable.insert {
            it[id] = UUID.randomUUID()
            it[codeHash] = randomHash()
            it[creatorHash] = randomHash()
            it[itemCount] = 1
            it[payload] = encryptedPayload()
            it[payloadNonce] = ByteArray(12) { 1 }
            it[createdAt] = expiresAt.minusSeconds(60).atOffset(ZoneOffset.UTC)
            it[ScheduleSharesTable.expiresAt] = expiresAt.atOffset(ZoneOffset.UTC)
        }
    }

    private fun insertHomeworkShare(
        expiresAt: Instant,
    ) {
        HomeworkSharesTable.insert {
            it[id] = UUID.randomUUID()
            it[codeHash] = randomHash()
            it[creatorHash] = randomHash()
            it[itemCount] = 1
            it[payload] = encryptedPayload()
            it[payloadNonce] = ByteArray(12) { 1 }
            it[createdAt] = expiresAt.minusSeconds(60).atOffset(ZoneOffset.UTC)
            it[HomeworkSharesTable.expiresAt] = expiresAt.atOffset(ZoneOffset.UTC)
        }
    }

    private fun insertRateLimitEvent(
        createdAt: Instant,
    ) {
        RateLimitEventsTable.insert {
            it[installationHash] = randomHash()
            it[type] = "homework:create"
            it[amount] = 1
            it[RateLimitEventsTable.createdAt] = createdAt.atOffset(ZoneOffset.UTC)
        }
    }

    private fun randomHash(): ByteArray {
        return ByteArray(HASH_SIZE_BYTES).also(
            secureRandom::nextBytes,
        )
    }

    private fun encryptedPayload(): ByteArray {
        return ByteArray(MIN_ENCRYPTED_PAYLOAD_SIZE) { 1 }
    }

    private fun sharingConfig(
        maxPayloadBytes: Int = 1_048_576,
        activePayloadBytesLimitPerInstallation: Long = 10_485_760,
        globalActivePayloadBytesLimit: Long = 536_870_912,
    ): SharingConfig {
        return SharingConfig(
            maxPayloadBytes = maxPayloadBytes,
            maxItemsPerShare = 20,
            homeworkLifetime = Duration.ofHours(24),
            scheduleLifetime = Duration.ofMinutes(30),
            scheduleClaimLifetime = Duration.ofMinutes(5),
            createLimitPerHour = 10,
            createdItemsLimitPerDay = 200,
            activeHomeworkItemsLimit = 200,
            activePayloadBytesLimitPerInstallation = activePayloadBytesLimitPerInstallation,
            createdPayloadBytesLimitPerDay = 20_971_520,
            globalActivePayloadBytesLimit = globalActivePayloadBytesLimit,
            globalCreatedPayloadBytesLimitPerDay = 268_435_456,
            codeLookupLimit = 30,
            codeLookupWindow = Duration.ofMinutes(10),
        )
    }

    private companion object {

        const val HASH_SIZE_BYTES = 32

        const val MIN_ENCRYPTED_PAYLOAD_SIZE = 17

        val secureRandom = SecureRandom()

        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:18.4-alpine")
            .withDatabaseName("studyassistant_test")
            .withUsername("studyassistant_test")
            .withPassword("studyassistant_test")
    }
}
