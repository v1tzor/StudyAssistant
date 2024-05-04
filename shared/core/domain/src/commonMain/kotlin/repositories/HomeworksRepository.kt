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

package repositories

import entities.tasks.Homework
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.Flow

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface HomeworksRepository {
    suspend fun fetchHomeworksByTime(timeRange: TimeRange, targetUser: UID): Flow<List<Homework>>
    suspend fun fetchHomeworkById(uid: UID, targetUser: UID): Flow<Homework?>
    suspend fun addOrUpdateHomework(homework: Homework, targetUser: UID): UID
    suspend fun deleteHomework(uid: UID, targetUser: UID)
}