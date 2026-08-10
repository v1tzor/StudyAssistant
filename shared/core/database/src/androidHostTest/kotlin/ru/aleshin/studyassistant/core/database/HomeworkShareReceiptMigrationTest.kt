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
import ru.aleshin.studyassistant.sqldelight.shared.HomeworkShareReceiptQueries
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class HomeworkShareReceiptMigrationTest {

    @Test
    fun migrationPreservesReceipts() {
        val driver = JdbcSqliteDriver(IN_MEMORY)
        driver.execute(
            identifier = null,
            sql = """
                CREATE TABLE currentSharedHomeworksEntity (
                    id INTEGER PRIMARY KEY NOT NULL
                )
            """.trimIndent(),
            parameters = 0,
        ).value
        driver.createVersionSevenReceiptTable()
        driver.insertVersionSevenReceipt(RECEIPT_CODE, IMPORTED_AT)

        Database.Schema.migrate(driver, oldVersion = 7, newVersion = 8).value

        val receipt = HomeworkShareReceiptQueries(driver)
            .fetchReceipt(RECEIPT_CODE)
            .executeAsOne()
        assertEquals(RECEIPT_CODE, receipt)
        driver.close()
    }

    @Test
    fun migrationContinuesWhenLegacyTableWasAlreadyDropped() {
        val driver = JdbcSqliteDriver(IN_MEMORY)
        driver.createVersionSevenReceiptTable()
        driver.insertVersionSevenReceipt(RECEIPT_CODE, IMPORTED_AT)

        Database.Schema.migrate(driver, oldVersion = 7, newVersion = 8).value

        val receipt = HomeworkShareReceiptQueries(driver)
            .fetchReceipt(RECEIPT_CODE)
            .executeAsOne()
        assertEquals(RECEIPT_CODE, receipt)
        driver.close()
    }

    private fun JdbcSqliteDriver.createVersionSevenReceiptTable() {
        execute(
            identifier = null,
            sql = """
                CREATE TABLE homeworkShareReceiptEntity (
                    code_hash TEXT PRIMARY KEY NOT NULL,
                    imported_at INTEGER NOT NULL
                )
            """.trimIndent(),
            parameters = 0,
        ).value
    }

    private fun JdbcSqliteDriver.insertVersionSevenReceipt(code: String, importedAt: Long) {
        execute(
            identifier = null,
            sql = """
                INSERT INTO homeworkShareReceiptEntity (code_hash, imported_at)
                VALUES (?, ?)
            """.trimIndent(),
            parameters = 2,
        ) {
            bindString(0, code)
            bindLong(1, importedAt)
        }.value
    }

    private companion object {
        const val RECEIPT_CODE = "AAAA-BBBB-CCCC"
        const val IMPORTED_AT = 123L
    }
}
