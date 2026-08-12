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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import ru.aleshin.studyassistant.backend.database.DatabaseConfig
import ru.aleshin.studyassistant.backend.database.DatabaseFactory
import ru.aleshin.studyassistant.backend.database.tables.RateLimitEventsTable
import ru.aleshin.studyassistant.backend.installation.InstallationConfig
import ru.aleshin.studyassistant.backend.installation.domain.result.InstallationRegistrationStorageResult
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
@Testcontainers
class InstallationRegistrationPostgresIntegrationTest {

    private lateinit var databaseFactory: DatabaseFactory

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
        transaction(db = databaseFactory.database) {
            RateLimitEventsTable.deleteAll()
        }
    }

    @AfterTest
    fun tearDown() {
        databaseFactory.close()
    }

    @Test
    fun networkAndGlobalDailyBudgetsShouldBeAtomic() = runBlocking {
        val repository = repository(
            networkLimit = 2,
            globalLimit = 3,
        )
        val now = Instant.parse("2026-08-12T10:00:00Z")
        val firstNetwork = ByteArray(32) { 1 }
        val secondNetwork = ByteArray(32) { 2 }
        val thirdNetwork = ByteArray(32) { 3 }

        assertIs<InstallationRegistrationStorageResult.Reserved>(
            repository.reserve(firstNetwork, now),
        )
        assertIs<InstallationRegistrationStorageResult.Reserved>(
            repository.reserve(firstNetwork, now.plusSeconds(1)),
        )
        assertIs<InstallationRegistrationStorageResult.RateLimited>(
            repository.reserve(firstNetwork, now.plusSeconds(2)),
        )
        assertIs<InstallationRegistrationStorageResult.Reserved>(
            repository.reserve(secondNetwork, now.plusSeconds(3)),
        )
        assertIs<InstallationRegistrationStorageResult.RateLimited>(
            repository.reserve(thirdNetwork, now.plusSeconds(4)),
        )
        Unit
    }

    @Test
    fun concurrentReservationsShouldNotExceedNetworkBudget() = runBlocking {
        val repository = repository(
            networkLimit = 1,
            globalLimit = 100,
        )
        val networkHash = ByteArray(32) { 5 }
        val now = Instant.parse("2026-08-12T10:00:00Z")

        val results = List(20) {
            async {
                repository.reserve(networkHash, now)
            }
        }.awaitAll()

        assertEquals(
            1,
            results.count { result ->
                result is InstallationRegistrationStorageResult.Reserved
            },
        )
    }

    private fun repository(
        networkLimit: Int,
        globalLimit: Int,
    ): InstallationRegistrationRepositoryImpl {
        return InstallationRegistrationRepositoryImpl(
            database = databaseFactory.database,
            config = InstallationConfig(
                registrationLimitPerNetworkPerDay = networkLimit,
                globalRegistrationLimitPerDay = globalLimit,
            ),
        )
    }

    private companion object {

        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:18.4-alpine")
            .withDatabaseName("studyassistant_installation_test")
            .withUsername("studyassistant_test")
            .withPassword("studyassistant_test")
    }
}
