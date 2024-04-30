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

package database.organizations

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import managers.CoroutineManager
import mappers.mapToDetailsData
import mappers.mapToLocalData
import models.organizations.OrganizationDetails
import randomUUID
import remote.StudyAssistantFirestore.LIMITS
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface OrganizationsLocalDataSource {

    suspend fun fetchAllOrganization(): Flow<List<OrganizationDetails>>

    suspend fun fetchOrganizationById(uid: UID): Flow<OrganizationDetails>

    suspend fun addOrUpdateOrganization(organization: OrganizationDetails): UID

    class Base(
        private val organizationQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : OrganizationsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun fetchAllOrganization(): Flow<List<OrganizationDetails>> {
            val query = organizationQueries.fetchAllOrganizations()
            val organizationEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return organizationEntityListFlow.map { organizations ->
                organizations.map { organizationEntity ->
                    val organizationId = organizationEntity.uid
                    val employeeQuery = employeeQueries.fetchEmployeesByOrganization(organizationId, LIMITS.ORGANIZATION_EMPLOYEE)
                    val subjectQuery = subjectQueries.fetchSubjectsByOrganization(organizationId, LIMITS.ORGANIZATION_SUBJECTS)

                    val employeeList = employeeQuery.executeAsList().map { entity ->
                        entity.mapToDetailsData()
                    }
                    val subjectList = subjectQuery.executeAsList().map { entity ->
                        entity.mapToDetailsData(employeeList.find { it.uid == entity.teacher_id })
                    }

                    organizationEntity.mapToDetailsData(
                        employee = employeeList,
                        subjects = subjectList,
                    )
                }
            }
        }

        override suspend fun fetchOrganizationById(uid: UID): Flow<OrganizationDetails> {
            val query = organizationQueries.fetchById(uid)
            val organizationEntityFlow = query.asFlow().mapToOne(coroutineContext)

            return organizationEntityFlow.map { organizationEntity ->
                val organizationId = organizationEntity.uid
                val employeeQuery = employeeQueries.fetchEmployeesByOrganization(organizationId, LIMITS.ORGANIZATION_EMPLOYEE)
                val subjectQuery = subjectQueries.fetchSubjectsByOrganization(organizationId, LIMITS.ORGANIZATION_SUBJECTS)

                val employeeList = employeeQuery.executeAsList().map { entity ->
                    entity.mapToDetailsData()
                }
                val subjectList = subjectQuery.executeAsList().map { entity ->
                    entity.mapToDetailsData(employeeList.find { it.uid == entity.teacher_id })
                }

                organizationEntity.mapToDetailsData(
                    employee = employeeList,
                    subjects = subjectList,
                )
            }
        }

        override suspend fun addOrUpdateOrganization(organization: OrganizationDetails): UID {
            val uid = randomUUID()
            organizationQueries.addOrUpdateOrganization(organization.mapToLocalData())

            return uid
        }
    }
}