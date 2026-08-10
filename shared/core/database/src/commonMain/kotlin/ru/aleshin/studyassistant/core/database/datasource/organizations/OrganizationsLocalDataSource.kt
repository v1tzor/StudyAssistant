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

package ru.aleshin.studyassistant.core.database.datasource.organizations

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.organizations.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.organizations.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.organizations.mapToEntity
import ru.aleshin.studyassistant.core.database.mappers.organizations.mapToShort
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.database.models.organizations.BaseOrganizationEntity
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationDetailsEntity
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface OrganizationsLocalDataSource {

    suspend fun addOrUpdateOrganization(item: BaseOrganizationEntity)
    suspend fun addOrUpdateOrganizations(items: List<BaseOrganizationEntity>)

    suspend fun fetchOrganizationDetailsById(uid: UID): Flow<OrganizationDetailsEntity?>
    suspend fun fetchOrganizationsDetailsById(uid: List<UID>): Flow<List<OrganizationDetailsEntity>>
    suspend fun fetchShortOrganizationById(uid: UID): Flow<OrganizationShortEntity?>
    suspend fun fetchAllOrganizationDetails(showHide: Boolean = false): Flow<List<OrganizationDetailsEntity>>
    suspend fun fetchAllShortOrganization(): Flow<List<OrganizationShortEntity>>

    class Base(
        private val organizationQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : OrganizationsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher


        override suspend fun addOrUpdateOrganization(item: BaseOrganizationEntity) {
            val uid = item.uid.ifEmpty { randomUUID() }
            val updatedItem = item.copy(uid = uid).mapToEntity()
            organizationQueries.addOrUpdateOrganization(updatedItem).await()
        }

        override suspend fun addOrUpdateOrganizations(items: List<BaseOrganizationEntity>) {
            items.forEach { item -> addOrUpdateOrganization(item) }
        }

        override suspend fun fetchOrganizationDetailsById(uid: UID): Flow<OrganizationDetailsEntity?> {
            if (uid.isEmpty()) return flowOf(null)
            val query = organizationQueries.fetchOrganizationById(uid)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
                .flatMapToDetails()
        }

        override suspend fun fetchOrganizationsDetailsById(uid: List<UID>): Flow<List<OrganizationDetailsEntity>> {
            val query = organizationQueries.fetchOrganizationsById(uid)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
                .flatMapListToDetails()
        }

        override suspend fun fetchShortOrganizationById(uid: UID): Flow<OrganizationShortEntity?> {
            val query = organizationQueries.fetchOrganizationById(uid)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase().mapToShort() }
        }

        override suspend fun fetchAllOrganizationDetails(showHide: Boolean): Flow<List<OrganizationDetailsEntity>> {
            val query = if (showHide) {
                organizationQueries.fetchAllOrganizations()
            } else {
                organizationQueries.fetchAllNotHideOrganizations()
            }
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
                .flatMapListToDetails()
        }

        override suspend fun fetchAllShortOrganization(): Flow<List<OrganizationShortEntity>> {
            val query = organizationQueries.fetchAllNotHideOrganizations()
            return query.mapToListFlow(coroutineContext) { it.mapToBase().mapToShort() }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<BaseOrganizationEntity>>.flatMapListToDetails() =
            flatMapLatest { organizations ->
                if (organizations.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val organizationsIds = organizations.map { it.uid }.toSet()

                    val subjectsMapFlow = subjectQueries.fetchSubjectsByOrganizations(
                        organization_id = organizationsIds,
                    ).mapToListFlow(coroutineContext) { it.mapToBase() }.map { subject ->
                        subject.groupBy { it.organizationId }
                    }

                    val employeesMapFlow = employeeQueries.fetchEmployeesByOrganizations(
                        organization_id = organizationsIds,
                    ).mapToListFlow(coroutineContext) { it.mapToBase() }.map { employee ->
                        employee.groupBy { it.organizationId }
                    }

                    combine(
                        flowOf(organizations),
                        subjectsMapFlow,
                        employeesMapFlow,
                    ) { organizationsList, subjectsMap, employeesMap ->
                        organizationsList.map { organization ->
                            organization.mapToDetails(
                                employee = employeesMap.getOrElse(organization.uid) { emptyList() },
                                subjects = subjectsMap.getOrElse(organization.uid) { emptyList() }
                                    .map { subject ->
                                        val employee =
                                            employeesMap[organization.uid]?.find { it.uid == subject.teacherId }
                                        subject.mapToDetails(employee = employee)
                                    },
                            )
                        }
                    }
                }
            }

        private fun Flow<BaseOrganizationEntity?>.flatMapToDetails(): Flow<OrganizationDetailsEntity?> {
            return map { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }

}
