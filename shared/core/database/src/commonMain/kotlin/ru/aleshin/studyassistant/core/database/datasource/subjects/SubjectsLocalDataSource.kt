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

package ru.aleshin.studyassistant.core.database.datasource.subjects

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToEntity
import ru.aleshin.studyassistant.core.database.models.subjects.BaseSubjectEntity
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface SubjectsLocalDataSource {

    suspend fun addOrUpdateSubject(item: BaseSubjectEntity)
    suspend fun addOrUpdateSubjects(items: List<BaseSubjectEntity>)
    suspend fun deleteSubjectsByIds(ids: List<String>)
    suspend fun fetchSubjectDetailsById(uid: UID): Flow<SubjectDetailsEntity?>
    suspend fun fetchAllSubjectsDetailsByOrg(organizationId: UID?): Flow<List<SubjectDetailsEntity>>
    suspend fun fetchSubjectsDetailsByEmployee(employeeId: UID): Flow<List<SubjectDetailsEntity>>
    suspend fun fetchAllSubjectsDetailsByNames(names: List<String>): List<SubjectDetailsEntity>

    class Base(
        private val subjectQueries: SubjectQueries,
        private val employeeQueries: EmployeeQueries,
        private val coroutineManager: CoroutineManager,
    ) : SubjectsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher

        override suspend fun addOrUpdateSubject(item: BaseSubjectEntity) {
            val uid = item.uid.ifEmpty { randomUUID() }
            val updatedItem = item.copy(uid = uid).mapToEntity()
            subjectQueries.addOrUpdateSubject(updatedItem).await()
        }

        override suspend fun addOrUpdateSubjects(items: List<BaseSubjectEntity>) {
            items.forEach { item -> addOrUpdateSubject(item) }
        }

        override suspend fun fetchSubjectDetailsById(uid: UID): Flow<SubjectDetailsEntity?> {
            val query = subjectQueries.fetchSubjectById(uid)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
                .flatMapToDetails()
        }

        override suspend fun fetchAllSubjectsDetailsByOrg(organizationId: UID?): Flow<List<SubjectDetailsEntity>> {
            val query = if (organizationId != null) {
                subjectQueries.fetchSubjectsByOrganization(organizationId)
            } else {
                subjectQueries.fetchAllSubjects()
            }
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
                .flatMapListToDetails()
        }

        override suspend fun fetchSubjectsDetailsByEmployee(employeeId: UID): Flow<List<SubjectDetailsEntity>> {
            val query = subjectQueries.fetchSubjectsByEmployee(employeeId)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
                .flatMapListToDetails()
        }

        override suspend fun fetchAllSubjectsDetailsByNames(names: List<String>): List<SubjectDetailsEntity> {
            val query = subjectQueries.fetchSubjectsByNames(names)
            val subjects = query.awaitAsList().map { it.mapToBase() }

            val employeeIds = subjects.mapNotNull { it.teacherId }
            val employeeMap = if (employeeIds.isNotEmpty()) {
                val query = employeeQueries.fetchEmployeesById(employeeIds)
                query.awaitAsList().associate { Pair(it.uid, it.mapToBase()) }
            } else {
                emptyMap()
            }

            return subjects.map { subjectEntity ->
                subjectEntity.mapToDetails(employee = employeeMap[subjectEntity.teacherId])
            }
        }

        override suspend fun deleteSubjectsByIds(ids: List<String>) {
            subjectQueries.deleteSubjects(ids).await()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<BaseSubjectEntity>>.flatMapListToDetails() =
            flatMapLatest { subjects ->
                if (subjects.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val organizationsIds = subjects.map { it.organizationId }

                    val employeesMapFlow = employeeQueries.fetchEmployeesByOrganizations(
                        organization_id = organizationsIds,
                    ).asFlow()
                        .mapToList(coroutineContext)
                        .map { employee -> employee.associate { Pair(it.uid, it.mapToBase()) } }

                    combine(
                        flowOf(subjects),
                        employeesMapFlow,
                    ) { subjectsList, employeesMap ->
                        subjectsList.map { subject ->
                            subject.mapToDetails(employee = employeesMap[subject.teacherId])
                        }
                    }
                }
            }

        private fun Flow<BaseSubjectEntity?>.flatMapToDetails(): Flow<SubjectDetailsEntity?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }

}
