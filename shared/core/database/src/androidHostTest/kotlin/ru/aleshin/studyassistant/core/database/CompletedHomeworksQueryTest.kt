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
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkEntity
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
class CompletedHomeworksQueryTest {

    @Test
    fun queryUsesCompletionDateAndExcludesIncompleteRows() {
        val driver = JdbcSqliteDriver(IN_MEMORY)
        Database.Schema.create(driver).value
        val queries = HomeworkQueries(driver)
        queries.addOrUpdateHomework(homework("inside", isDone = true, completeDate = 200L)).value
        queries.addOrUpdateHomework(homework("outside", isDone = true, completeDate = 400L)).value
        queries.addOrUpdateHomework(homework("incomplete", isDone = false, completeDate = null)).value

        val result = queries.fetchCompletedHomeworksByTimeRange(100L, 300L).executeAsList()

        assertEquals(listOf("inside"), result.map { it.uid })
        driver.close()
    }

    private fun homework(uid: String, isDone: Boolean, completeDate: Long?) = HomeworkEntity(
        uid = uid,
        class_id = null,
        deadline = 1_000L,
        subject_id = null,
        organization_id = "organization",
        theoretical_tasks = "",
        practical_tasks = "",
        presentations = "",
        test = null,
        priority = "STANDARD",
        is_done = if (isDone) 1L else 0L,
        complete_date = completeDate,
        updated_at = 0L,
    )
}
