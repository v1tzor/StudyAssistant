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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToLocalData
import ru.aleshin.studyassistant.core.database.datasource.tasks.HomeworksLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class HomeworksRepositoryImpl(
    private val localDataSource: HomeworksLocalDataSource,
) : HomeworksRepository {

    override suspend fun addOrUpdateHomework(homework: Homework): UID {
        val updatedHomework = homework.copy(uid = homework.uid.ifBlank { randomUUID() })
        localDataSource.addOrUpdateHomework(updatedHomework.mapToLocalData())
        return updatedHomework.uid
    }

    override suspend fun addHomeworksGroup(homeworks: List<Homework>) {
        val updatedHomeworks = homeworks.map { homework ->
            homework.copy(uid = homework.uid.ifBlank { randomUUID() })
        }
        localDataSource.addOrUpdateHomeworks(updatedHomeworks.map { it.mapToLocalData() })
    }

    override suspend fun fetchHomeworkById(uid: UID): Flow<Homework?> {
        return localDataSource.fetchHomeworkDetailsById(uid).map { homework -> homework?.mapToDomain() }
    }

    override suspend fun fetchHomeworksByDate(date: Instant): Flow<List<Homework>> {
        return localDataSource.fetchHomeworksDetailsByTimeRange(
            from = date.startThisDay().toEpochMilliseconds(),
            to = date.endThisDay().toEpochMilliseconds(),
        ).map { homeworks -> homeworks.map { it.mapToDomain() } }
    }

    override suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange): Flow<List<Homework>> {
        return localDataSource.fetchHomeworksDetailsByTimeRange(
            from = timeRange.from.toEpochMilliseconds(),
            to = timeRange.to.toEpochMilliseconds(),
        ).map { homeworks -> homeworks.map { it.mapToDomain() } }
    }

    override suspend fun fetchOverdueHomeworks(currentDate: Instant): Flow<List<Homework>> {
        return localDataSource.fetchOverdueHomeworksDetails(
            currentDate.endThisDay().toEpochMilliseconds()
        ).map { homeworks -> homeworks.map { it.mapToDomain() } }
    }

    override suspend fun fetchActiveLinkedHomeworks(currentDate: Instant): Flow<List<Homework>> {
        return localDataSource.fetchActiveLinkedHomeworksDetails(
            currentDate.toEpochMilliseconds()
        ).map { homeworks -> homeworks.map { it.mapToDomain() } }
    }

    override suspend fun fetchCompletedHomeworksCount(): Flow<Int> {
        return localDataSource.fetchCompletedHomeworksCount()
    }

    override suspend fun fetchCompletedHomeworksByTimeRange(
        timeRange: TimeRange,
    ): Flow<List<Homework>> {
        return localDataSource.fetchCompletedHomeworksDetailsByTimeRange(
            from = timeRange.from.toEpochMilliseconds(),
            to = timeRange.to.toEpochMilliseconds(),
        ).map { homeworks -> homeworks.map { it.mapToDomain() } }
    }

    override suspend fun deleteHomework(uid: UID) {
        localDataSource.deleteHomeworksByIds(listOf(uid))
    }
}
