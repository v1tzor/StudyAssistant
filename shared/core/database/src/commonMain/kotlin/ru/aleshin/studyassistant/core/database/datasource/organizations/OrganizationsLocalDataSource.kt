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

package ru.aleshin.studyassistant.core.database.datasource.organizations

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.organizations.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.organizations.mapToShort
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationDetailsEntity
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationEntity
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface OrganizationsLocalDataSource {

    suspend fun addOrUpdateOrganization(organization: OrganizationEntity): UID
    suspend fun addOrUpdateOrganizationsGroup(organizations: List<OrganizationEntity>)
    suspend fun fetchOrganizationById(uid: UID): Flow<OrganizationDetailsEntity?>
    suspend fun fetchOrganizationsById(uid: List<UID>): Flow<List<OrganizationDetailsEntity>>
    suspend fun fetchShortOrganizationById(uid: UID): Flow<OrganizationShortEntity?>
    suspend fun fetchAllOrganization(): Flow<List<OrganizationDetailsEntity>>
    suspend fun fetchAllShortOrganization(): Flow<List<OrganizationShortEntity>>

    class Base(
        private val organizationQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : OrganizationsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateOrganization(organization: OrganizationEntity): UID {
            val uid = organization.uid.ifEmpty { randomUUID() }
            organizationQueries.addOrUpdateOrganization(organization.copy(uid = uid))

            return uid
        }

        override suspend fun addOrUpdateOrganizationsGroup(organizations: List<OrganizationEntity>) {
            organizations.forEach { organization -> addOrUpdateOrganization(organization) }
        }

        override suspend fun fetchOrganizationById(uid: UID): Flow<OrganizationDetailsEntity?> {
            if (uid.isEmpty()) return flowOf(null)
            val query = organizationQueries.fetchOrganizationById(uid)
            val organizationEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return organizationEntityFlow.map { organizationEntity ->
                if (organizationEntity == null) return@map null
                val organizationId = organizationEntity.uid
                val employeeQuery = employeeQueries.fetchEmployeesByOrganization(organizationId)
                val subjectQuery = subjectQueries.fetchSubjectsByOrganization(organizationId)

                val employeeList = employeeQuery.executeAsList()
                val subjectList = subjectQuery.executeAsList().map { entity ->
                    entity.mapToDetails(employeeList.find { it.uid == entity.teacher_id })
                }

                organizationEntity.mapToDetails(
                    employee = employeeList,
                    subjects = subjectList,
                )
            }
        }

        override suspend fun fetchOrganizationsById(uid: List<UID>): Flow<List<OrganizationDetailsEntity>> {
            val query = organizationQueries.fetchOrganizationsById(uid)
            val organizationEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return organizationEntityListFlow.map { organizations ->
                organizations.map { organizationEntity ->
                    val organizationId = organizationEntity.uid
                    val employeeQuery = employeeQueries.fetchEmployeesByOrganization(organizationId)
                    val subjectQuery = subjectQueries.fetchSubjectsByOrganization(organizationId)

                    val employeeList = employeeQuery.executeAsList()
                    val subjectList = subjectQuery.executeAsList().map { entity ->
                        entity.mapToDetails(employeeList.find { it.uid == entity.teacher_id })
                    }

                    organizationEntity.mapToDetails(
                        employee = employeeList,
                        subjects = subjectList,
                    )
                }
            }
        }

        override suspend fun fetchShortOrganizationById(uid: UID): Flow<OrganizationShortEntity?> {
            val query = organizationQueries.fetchOrganizationById(uid)
            val organization = query.asFlow().mapToOneOrNull(coroutineContext).map { it?.mapToShort() }

            return organization
        }

        override suspend fun fetchAllOrganization(): Flow<List<OrganizationDetailsEntity>> {
            val query = organizationQueries.fetchAllOrganizations()
            val organizationEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return organizationEntityListFlow.map { organizations ->
                organizations.map { organizationEntity ->
                    val organizationId = organizationEntity.uid
                    val employeeQuery = employeeQueries.fetchEmployeesByOrganization(organizationId)
                    val subjectQuery = subjectQueries.fetchSubjectsByOrganization(organizationId)

                    val employeeList = employeeQuery.executeAsList()
                    val subjectList = subjectQuery.executeAsList().map { entity ->
                        entity.mapToDetails(employeeList.find { it.uid == entity.teacher_id })
                    }

                    organizationEntity.mapToDetails(
                        employee = employeeList,
                        subjects = subjectList,
                    )
                }
            }
        }

        override suspend fun fetchAllShortOrganization(): Flow<List<OrganizationShortEntity>> {
            val query = organizationQueries.fetchAllOrganizations()
            val organizations = query.asFlow().mapToList(coroutineContext).map { entities ->
                entities.map { it.mapToShort() }
            }

            return organizations
        }
    }
}