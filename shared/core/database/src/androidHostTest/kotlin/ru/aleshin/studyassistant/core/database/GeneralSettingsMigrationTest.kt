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
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver.Companion.IN_MEMORY
import kotlinx.coroutines.runBlocking
import ru.aleshin.studyassistant.core.data.Database
import ru.aleshin.studyassistant.sqldelight.settings.GeneralQueries
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
class GeneralSettingsMigrationTest {

    @Test
    fun migrationMarksCompletedUsersAsSetupAndTurnsFactoryNotificationsOn() {
        val driver = JdbcSqliteDriver(IN_MEMORY)
        driver.createVersionThirteenTables()
        driver.insertVersionThirteenGeneral(isFirstStart = 0)
        driver.insertVersionThirteenNotifications(factoryOff = true)

        runBlocking { Database.Schema.migrate(driver, oldVersion = 13, newVersion = 14).await() }

        val general = runBlocking { GeneralQueries(driver).fetchSettings().awaitAsOne() }
        assertEquals(0L, general.is_first_start)
        assertEquals(1L, general.is_setup)

        assertEquals(600000L, driver.queryLong("beginning_of_classes"))
        assertEquals(1L, driver.queryLong("end_of_classes"))
        assertEquals(72000000L, driver.queryLong("unfinished_homeworks"))
        assertEquals(7L, driver.queryLong("high_workload"))
        driver.close()
    }

    @Test
    fun migrationKeepsUnfinishedUsersOnSetupAndPreservesCustomNotifications() {
        val driver = JdbcSqliteDriver(IN_MEMORY)
        driver.createVersionThirteenTables()
        driver.insertVersionThirteenGeneral(isFirstStart = 1)
        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO notificationSettingsEntity (
                    id,
                    beginning_of_classes,
                    exceptions_for_beginning_of_classes,
                    end_of_classes,
                    exceptions_for_end_of_classes,
                    unfinished_homeworks,
                    high_workload
                )
                VALUES (1, 300000, '', 0, '', 3600000, 4)
            """.trimIndent(),
            parameters = 0,
        ).value

        runBlocking { Database.Schema.migrate(driver, oldVersion = 13, newVersion = 14).await() }

        val general = runBlocking { GeneralQueries(driver).fetchSettings().awaitAsOne() }
        assertEquals(1L, general.is_first_start)
        assertEquals(0L, general.is_setup)

        assertEquals(300000L, driver.queryLong("beginning_of_classes"))
        assertEquals(0L, driver.queryLong("end_of_classes"))
        assertEquals(3600000L, driver.queryLong("unfinished_homeworks"))
        assertEquals(4L, driver.queryLong("high_workload"))
        driver.close()
    }

    private fun JdbcSqliteDriver.createVersionThirteenTables() {
        execute(
            identifier = null,
            sql = """
                CREATE TABLE generalSettingsEntity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    is_first_start INTEGER NOT NULL,
                    is_unfinished_setup TEXT DEFAULT NULL,
                    theme TEXT NOT NULL,
                    language TEXT NOT NULL
                )
            """.trimIndent(),
            parameters = 0,
        ).value
        execute(
            identifier = null,
            sql = """
                CREATE TABLE notificationSettingsEntity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    beginning_of_classes INTEGER,
                    exceptions_for_beginning_of_classes TEXT NOT NULL,
                    end_of_classes INTEGER NOT NULL,
                    exceptions_for_end_of_classes TEXT NOT NULL,
                    unfinished_homeworks INTEGER,
                    high_workload INTEGER
                )
            """.trimIndent(),
            parameters = 0,
        ).value
    }

    private fun JdbcSqliteDriver.insertVersionThirteenGeneral(isFirstStart: Long) {
        execute(
            identifier = null,
            sql = """
                INSERT INTO generalSettingsEntity (id, is_first_start, is_unfinished_setup, theme, language)
                VALUES (1, ?, NULL, 'DEFAULT', 'DEFAULT')
            """.trimIndent(),
            parameters = 1,
        ) {
            bindLong(0, isFirstStart)
        }.value
    }

    private fun JdbcSqliteDriver.insertVersionThirteenNotifications(factoryOff: Boolean) {
        execute(
            identifier = null,
            sql = """
                INSERT INTO notificationSettingsEntity (
                    id,
                    beginning_of_classes,
                    exceptions_for_beginning_of_classes,
                    end_of_classes,
                    exceptions_for_end_of_classes,
                    unfinished_homeworks,
                    high_workload
                )
                VALUES (1, NULL, '', 0, '', NULL, NULL)
            """.trimIndent(),
            parameters = 0,
        ).value
        if (!factoryOff) error("Use a dedicated insert for custom notifications")
    }

    private fun JdbcSqliteDriver.queryLong(column: String): Long? {
        return executeQuery(
            identifier = null,
            sql = "SELECT $column FROM notificationSettingsEntity WHERE id = 1",
            mapper = { cursor ->
                val value = if (cursor.next().value) cursor.getLong(0) else null
                QueryResult.Value(value)
            },
            parameters = 0,
        ).value
    }
}
