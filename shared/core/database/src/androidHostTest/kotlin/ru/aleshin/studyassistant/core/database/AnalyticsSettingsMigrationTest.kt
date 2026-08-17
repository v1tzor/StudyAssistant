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

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver.Companion.IN_MEMORY
import kotlinx.coroutines.runBlocking
import ru.aleshin.studyassistant.core.data.Database
import ru.aleshin.studyassistant.sqldelight.settings.AnalyticsQueries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
class AnalyticsSettingsMigrationTest {

    @Test
    fun migrationCreatesAnalyticsSettingsWithMonthDefault() {
        val driver = JdbcSqliteDriver(IN_MEMORY)

        runBlocking { Database.Schema.migrate(driver, oldVersion = 14, newVersion = 15).await() }

        val settings = runBlocking { AnalyticsQueries(driver).fetchSettings().awaitAsOne() }
        assertEquals(1L, settings.id)
        assertEquals("MONTH", settings.period)
        assertNull(settings.custom_from)
        assertNull(settings.custom_to)
        driver.close()
    }
}
