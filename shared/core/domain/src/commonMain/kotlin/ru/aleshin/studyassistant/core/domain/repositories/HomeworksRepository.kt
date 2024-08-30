/*
 * Copyright 2024 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.domain.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface HomeworksRepository {
    suspend fun addOrUpdateHomework(homework: Homework, targetUser: UID): UID
    suspend fun addHomeworksGroup(homeworks: List<Homework>, targetUser: UID)
    suspend fun fetchHomeworkById(uid: UID, targetUser: UID): Flow<Homework?>
    suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange, targetUser: UID): Flow<List<Homework>>
    suspend fun fetchOverdueHomeworks(currentDate: Instant, targetUser: UID): Flow<List<Homework>>
    suspend fun fetchActiveLinkedHomeworks(currentDate: Instant, targetUser: UID): Flow<List<Homework>>
    suspend fun fetchHomeworksByDate(date: Instant, targetUser: UID): Flow<List<Homework>>
    suspend fun deleteHomework(uid: UID, targetUser: UID)
    suspend fun deleteAllHomeworks(targetUser: UID)
    suspend fun transferData(direction: DataTransferDirection, targetUser: UID)
}