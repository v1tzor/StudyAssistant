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
import ru.aleshin.studyassistant.sqldelight.user.ProfileQueries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
class ProfileDescriptionMigrationTest {

    @Test
    fun migrationDropsProfileDescriptionAndKeepsOtherFields() {
        val driver = JdbcSqliteDriver(IN_MEMORY)
        driver.createVersionFifteenProfileTable()
        driver.insertVersionFifteenProfile()

        runBlocking { Database.Schema.migrate(driver, oldVersion = 15, newVersion = 16).await() }

        val profile = runBlocking { ProfileQueries(driver).fetchProfile().awaitAsOne() }
        assertEquals("local-profile", profile.uid)
        assertEquals("Student", profile.username)
        assertEquals("/avatars/profile/image.jpg", profile.avatar)
        assertEquals("City", profile.city)
        assertEquals("2001-02-03", profile.birthday)
        assertEquals("MALE", profile.sex)
        assertEquals(123456789L, profile.updated_at)
        assertFalse(driver.hasDescriptionColumn())
        driver.close()
    }

    private fun JdbcSqliteDriver.createVersionFifteenProfileTable() {
        execute(
            identifier = null,
            sql = """
                CREATE TABLE profileEntity (
                    id INTEGER PRIMARY KEY NOT NULL,
                    uid TEXT NOT NULL,
                    username TEXT NOT NULL,
                    avatar TEXT,
                    description TEXT,
                    city TEXT,
                    birthday TEXT,
                    sex TEXT,
                    updated_at INTEGER NOT NULL
                )
            """.trimIndent(),
            parameters = 0,
        ).value
    }

    private fun JdbcSqliteDriver.insertVersionFifteenProfile() {
        execute(
            identifier = null,
            sql = """
                INSERT INTO profileEntity (
                    id, uid, username, avatar, description, city, birthday, sex, updated_at
                )
                VALUES (
                    1,
                    'local-profile',
                    'Student',
                    '/avatars/profile/image.jpg',
                    'Description',
                    'City',
                    '2001-02-03',
                    'MALE',
                    123456789
                )
            """.trimIndent(),
            parameters = 0,
        ).value
    }

    private fun JdbcSqliteDriver.hasDescriptionColumn(): Boolean {
        return executeQuery(
            identifier = null,
            sql = "PRAGMA table_info(profileEntity)",
            mapper = { cursor ->
                var hasDescription = false
                while (cursor.next().value) {
                    if (cursor.getString(1) == "description") {
                        hasDescription = true
                    }
                }
                QueryResult.Value(hasDescription)
            },
            parameters = 0,
        ).value
    }
}
