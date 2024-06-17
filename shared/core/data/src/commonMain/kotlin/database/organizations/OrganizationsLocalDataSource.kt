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
import app.cash.sqldelight.coroutines.mapToOneOrNull
import extensions.randomUUID
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import managers.CoroutineManager
import mappers.organizations.mapToDetailsData
import mappers.organizations.mapToLocalData
import mappers.subjects.mapToDetailsData
import mappers.users.mapToDetailsData
import models.organizations.OrganizationDetailsData
import models.organizations.OrganizationShortData
import models.organizations.ScheduleTimeIntervalsData
import models.users.ContactInfoData
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface OrganizationsLocalDataSource {

    suspend fun addOrUpdateOrganization(organization: OrganizationDetailsData): UID
    suspend fun fetchOrganizationById(uid: UID): Flow<OrganizationDetailsData?>
    suspend fun fetchAllOrganization(): Flow<List<OrganizationDetailsData>>
    suspend fun fetchAllShortOrganization(): Flow<List<OrganizationShortData>>

    class Base(
        private val organizationQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : OrganizationsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateOrganization(organization: OrganizationDetailsData): UID {
            val uid = organization.uid.ifEmpty { randomUUID() }
            val organizationEntity = organization.mapToLocalData()
            organizationQueries.addOrUpdateOrganization(organizationEntity.copy(uid = uid))

            return uid
        }

        override suspend fun fetchOrganizationById(uid: UID): Flow<OrganizationDetailsData?> {
            if (uid.isEmpty()) return flowOf(null)
            val query = organizationQueries.fetchOrganizationById(uid)
            val organizationEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return organizationEntityFlow.map { organizationEntity ->
                if (organizationEntity == null) return@map null
                val organizationId = organizationEntity.uid
                val employeeQuery = employeeQueries.fetchEmployeesByOrganization(organizationId)
                val subjectQuery = subjectQueries.fetchSubjectsByOrganization(organizationId)

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

        override suspend fun fetchAllOrganization(): Flow<List<OrganizationDetailsData>> {
            val query = organizationQueries.fetchAllOrganizations()
            val organizationEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return organizationEntityListFlow.map { organizations ->
                organizations.map { organizationEntity ->
                    val organizationId = organizationEntity.uid
                    val employeeQuery = employeeQueries.fetchEmployeesByOrganization(organizationId)
                    val subjectQuery = subjectQueries.fetchSubjectsByOrganization(organizationId)

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

        override suspend fun fetchAllShortOrganization(): Flow<List<OrganizationShortData>> {
            val query = organizationQueries.fetchAllOrganizations(
                mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel, _, _, locationList, _, offices, _ ->
                    val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsData>(timeIntervalsModel)
                    val locations = locationList.map { Json.decodeFromString<ContactInfoData>(it) }
                    OrganizationShortData(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals)
                },
            )
            val organizations = query.asFlow().mapToList(coroutineContext)

            return organizations
        }
    }
}