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

package ru.aleshin.studyassistant.core.database.datasource.subjects

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectEntity
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface SubjectsLocalDataSource {

    suspend fun addOrUpdateSubject(subject: SubjectEntity): UID
    suspend fun addOrUpdateSubjectsGroup(subjects: List<SubjectEntity>)
    suspend fun fetchSubjectById(uid: UID): Flow<SubjectDetailsEntity?>
    suspend fun fetchAllSubjectsByOrganization(organizationId: UID?): Flow<List<SubjectDetailsEntity>>
    suspend fun fetchAllSubjectsByNames(names: List<String>): List<SubjectDetailsEntity>
    suspend fun fetchSubjectsByEmployee(employeeId: UID): Flow<List<SubjectDetailsEntity>>
    suspend fun deleteSubject(targetId: UID)
    suspend fun deleteAllSubjects()

    class Base(
        private val subjectQueries: SubjectQueries,
        private val employeeQueries: EmployeeQueries,
        private val coroutineManager: CoroutineManager,
    ) : SubjectsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateSubject(subject: SubjectEntity): UID {
            val uid = subject.uid.ifEmpty { randomUUID() }
            subjectQueries.addOrUpdateSubject(subject.copy(uid = uid))

            return uid
        }

        override suspend fun addOrUpdateSubjectsGroup(subjects: List<SubjectEntity>) {
            subjects.forEach { subject -> addOrUpdateSubject(subject) }
        }

        override suspend fun fetchSubjectById(uid: UID): Flow<SubjectDetailsEntity?> {
            val query = subjectQueries.fetchSubjectById(uid)
            return query.asFlow().mapToOneOrNull(coroutineContext).flatMapToDetails()
        }

        override suspend fun fetchAllSubjectsByOrganization(organizationId: UID?): Flow<List<SubjectDetailsEntity>> {
            val query = if (organizationId != null) {
                subjectQueries.fetchSubjectsByOrganization(organizationId)
            } else {
                subjectQueries.fetchAllSubjects()
            }
            return query.asFlow().mapToList(coroutineContext).flatMapListToDetails()
        }

        override suspend fun fetchAllSubjectsByNames(names: List<String>): List<SubjectDetailsEntity> {
            val query = subjectQueries.fetchSubjectsByNames(names)
            val subjectEntityList = query.executeAsList()

            return subjectEntityList.map { subjectEntity ->
                val employee = subjectEntity.teacher_id?.let { teacherId ->
                    val employeeQuery = employeeQueries.fetchEmployeeById(teacherId)
                    employeeQuery.executeAsOneOrNull()
                }
                subjectEntity.mapToDetails(employee = employee)
            }
        }

        override suspend fun fetchSubjectsByEmployee(employeeId: UID): Flow<List<SubjectDetailsEntity>> {
            val query = subjectQueries.fetchSubjectsByEmployee(employeeId)
            return query.asFlow().mapToList(coroutineContext).flatMapListToDetails()
        }

        override suspend fun deleteSubject(targetId: UID) {
            subjectQueries.deleteSubject(targetId)
        }

        override suspend fun deleteAllSubjects() {
            subjectQueries.deleteAllSubjects()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<SubjectEntity>>.flatMapListToDetails() = flatMapLatest { subjects ->
            if (subjects.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = subjects.map { it.organization_id }

                val employeesMapFlow = employeeQueries.fetchEmployeesByOrganizations(organizationsIds)
                    .asFlow()
                    .mapToList(coroutineContext)
                    .map { employee -> employee.associateBy { it.uid } }

                combine(
                    flowOf(subjects),
                    employeesMapFlow,
                ) { subjectsList, employeesMap ->
                    subjectsList.map { subject ->
                        subject.mapToDetails(employee = employeesMap[subject.teacher_id])
                    }
                }
            }
        }

        private fun Flow<SubjectEntity?>.flatMapToDetails(): Flow<SubjectDetailsEntity?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }
}