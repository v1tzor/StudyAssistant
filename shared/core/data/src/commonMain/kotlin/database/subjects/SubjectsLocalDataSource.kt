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

package database.subjects

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import managers.CoroutineManager
import mappers.subjects.mapToDetailsData
import mappers.subjects.mapToLocalData
import mappers.users.mapToDetailsData
import models.subjects.SubjectDetailsData
import randomUUID
import remote.StudyAssistantFirestore.LIMITS.EDITOR_SUBJECTS
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface SubjectsLocalDataSource {

    suspend fun addOrUpdateSubject(subject: SubjectDetailsData): UID
    suspend fun fetchSubjectById(uid: UID): Flow<SubjectDetailsData?>
    suspend fun fetchAllSubjectsByOrganization(organizationId: UID): Flow<List<SubjectDetailsData>>
    suspend fun deleteSubject(targetId: UID)

    class Base(
        private val subjectQueries: SubjectQueries,
        private val employeeQueries: EmployeeQueries,
        private val coroutineManager: CoroutineManager,
    ) : SubjectsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateSubject(subject: SubjectDetailsData): UID {
            val uid = subject.uid.ifEmpty { randomUUID() }
            val subjectEntity = subject.mapToLocalData()
            subjectQueries.addOrUpdateSubject(subjectEntity.copy(uid = uid))

            return uid
        }

        override suspend fun fetchSubjectById(uid: UID): Flow<SubjectDetailsData?> {
            val query = subjectQueries.fetchSubjectById(uid)
            val subjectEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return subjectEntityFlow.map { subjectEntity ->
                val employee = subjectEntity?.teacher_id?.let { teacherId ->
                    val employeeQuery = employeeQueries.fetchEmployeeById(teacherId)
                    employeeQuery.executeAsOneOrNull()?.mapToDetailsData()
                }
                return@map subjectEntity?.mapToDetailsData(employee = employee)
            }
        }

        override suspend fun fetchAllSubjectsByOrganization(organizationId: UID): Flow<List<SubjectDetailsData>> {
            val query = subjectQueries.fetchSubjectsByOrganization(organizationId, EDITOR_SUBJECTS)
            val subjectEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return subjectEntityListFlow.map { subjectEntityList ->
                subjectEntityList.map { subjectEntity ->
                    val employee = subjectEntity.teacher_id?.let { teacherId ->
                        val employeeQuery = employeeQueries.fetchEmployeeById(teacherId)
                        employeeQuery.executeAsOneOrNull()?.mapToDetailsData()
                    }
                    subjectEntity.mapToDetailsData(employee = employee)
                }
            }
        }

        override suspend fun deleteSubject(targetId: UID) {
            subjectQueries.deleteSubject(targetId)
        }
    }
}
