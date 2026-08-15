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

package ru.aleshin.studyassistant.core.database.datasource.shared

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlinx.coroutines.CancellationException
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToEntity
import ru.aleshin.studyassistant.core.database.models.tasks.BaseHomeworkEntity
import ru.aleshin.studyassistant.sqldelight.shared.HomeworkShareReceiptQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
interface HomeworkShareLocalDataSource {

    suspend fun contains(shareCode: String): Boolean
    suspend fun importHomeworks(shareCode: String, importedAt: Long, homeworks: List<BaseHomeworkEntity>): Boolean

    class Base(
        private val homeworkShareQueries: HomeworkShareReceiptQueries,
        private val homeworkQueries: HomeworkQueries,
    ) : HomeworkShareLocalDataSource {

        override suspend fun contains(shareCode: String): Boolean {
            return homeworkShareQueries.fetchReceipt(shareCode).awaitAsOneOrNull() != null
        }

        override suspend fun importHomeworks(
            shareCode: String,
            importedAt: Long,
            homeworks: List<BaseHomeworkEntity>,
        ): Boolean {
            try {
                homeworkShareQueries.transaction {
                    homeworkShareQueries.addReceipt(shareCode, importedAt)
                    homeworks.forEach { homework ->
                        homeworkQueries.addOrUpdateHomework(homework.mapToEntity())
                    }
                }
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                if (contains(shareCode)) return false
                throw error
            }
            return true
        }
    }
}
