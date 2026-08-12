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

package ru.aleshin.studyassistant.backend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class DatabaseFactory(config: DatabaseConfig) : DatabaseProbe, AutoCloseable {

    private val dataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.user
            password = config.password

            driverClassName = "org.postgresql.Driver"

            maximumPoolSize = config.maximumPoolSize
            minimumIdle = 1
            poolName = "studyassistant-postgres"

            connectionTimeout = config.connectionTimeoutMs
            validationTimeout = 3_000
            maxLifetime = config.maxLifetimeMs

            initializationFailTimeout = 0

            connectionInitSql = "SET statement_timeout = ${config.statementTimeoutMs}; " +
                "SET idle_in_transaction_session_timeout = ${config.idleTransactionTimeoutMs}"

            addDataSourceProperty(
                "tcpKeepAlive",
                "true",
            )
            addDataSourceProperty("ApplicationName", "studyassistant-backend")
            addDataSourceProperty("reWriteBatchedInserts", "true")
        },
    )

    val database: Database = Database.connect(datasource = dataSource)

    override suspend fun isReady(): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                dataSource.connection.use { connection ->
                    connection
                        .prepareStatement("SELECT 1")
                        .use { statement ->
                            statement
                                .executeQuery()
                                .use { result -> result.next() && result.getInt(1) == 1 }
                        }
                }
            }.getOrDefault(false)
        }
    }

    override fun close() {
        if (!dataSource.isClosed) {
            dataSource.close()
        }
    }
}
