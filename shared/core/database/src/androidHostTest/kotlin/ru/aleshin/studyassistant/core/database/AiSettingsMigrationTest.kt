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

package ru.aleshin.studyassistant.core.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver.Companion.IN_MEMORY
import ru.aleshin.studyassistant.core.data.Database
import ru.aleshin.studyassistant.sqldelight.ai.AiSettingsQueries
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
class AiSettingsMigrationTest {

    @Test
    fun migrationClampsLegacyQuotaAndAddsRewardLimits() {
        val driver = JdbcSqliteDriver(IN_MEMORY)
        driver.createVersionTwelveTable()
        driver.insertVersionTwelveSettings(quotaRemaining = 19, quotaResetAt = 123L)

        Database.Schema.migrate(driver, oldVersion = 12, newVersion = 13).value

        val settings = AiSettingsQueries(driver).fetchSettings().executeAsOne()
        assertEquals(12L, settings.quota_remaining)
        assertEquals(12L, settings.quota_limit)
        assertEquals(3L, settings.rewarded_resets_remaining)
        assertEquals(123L, settings.quota_reset_at)
        driver.close()
    }

    @Test
    fun migrationPreservesLegacyQuotaBelowNewLimit() {
        val driver = JdbcSqliteDriver(IN_MEMORY)
        driver.createVersionTwelveTable()
        driver.insertVersionTwelveSettings(quotaRemaining = 5, quotaResetAt = null)

        Database.Schema.migrate(driver, oldVersion = 12, newVersion = 13).value

        val settings = AiSettingsQueries(driver).fetchSettings().executeAsOne()
        assertEquals(5L, settings.quota_remaining)
        assertEquals(12L, settings.quota_limit)
        assertEquals(3L, settings.rewarded_resets_remaining)
        assertEquals(null, settings.quota_reset_at)
        driver.close()
    }

    private fun JdbcSqliteDriver.createVersionTwelveTable() {
        execute(
            identifier = null,
            sql = """
                CREATE TABLE aiSettingsEntity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    quota_remaining INTEGER NOT NULL,
                    quota_reset_at INTEGER DEFAULT NULL
                )
            """.trimIndent(),
            parameters = 0,
        ).value
    }

    private fun JdbcSqliteDriver.insertVersionTwelveSettings(
        quotaRemaining: Long,
        quotaResetAt: Long?,
    ) {
        execute(
            identifier = null,
            sql = """
                INSERT INTO aiSettingsEntity (id, quota_remaining, quota_reset_at)
                VALUES (1, ?, ?)
            """.trimIndent(),
            parameters = 2,
        ) {
            bindLong(0, quotaRemaining)
            bindLong(1, quotaResetAt)
        }.value
    }
}
