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
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface SubjectsRepository {
    suspend fun addOrUpdateSubject(subject: Subject, targetUser: UID): UID
    suspend fun addOrUpdateSubjectsGroup(subjects: List<Subject>, targetUser: UID)
    suspend fun fetchAllSubjectsByOrganization(organizationId: UID, targetUser: UID): Flow<List<Subject>>
    suspend fun fetchAllSubjectsByNames(names: List<UID>, targetUser: UID): List<Subject>
    suspend fun fetchSubjectsByEmployee(employeeId: UID, targetUser: UID): Flow<List<Subject>>
    suspend fun fetchSubjectById(uid: UID, targetUser: UID): Flow<Subject?>
    suspend fun deleteSubject(targetId: UID, targetUser: UID)
    suspend fun deleteAllSubjects(targetUser: UID)
    suspend fun transferData(direction: DataTransferDirection, targetUser: UID)
}