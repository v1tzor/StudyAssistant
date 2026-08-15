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

package ru.aleshin.studyassistant.core.database.datasource.tasks

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToEntity
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.database.models.organizations.ScheduleTimeIntervalsEntity
import ru.aleshin.studyassistant.core.database.models.tasks.BaseHomeworkEntity
import ru.aleshin.studyassistant.core.database.models.tasks.HomeworkDetailsEntity
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface HomeworksLocalDataSource {

    suspend fun addOrUpdateHomework(item: BaseHomeworkEntity)
    suspend fun addOrUpdateHomeworks(items: List<BaseHomeworkEntity>)
    suspend fun fetchHomeworkDetailsById(uid: UID): Flow<HomeworkDetailsEntity?>
    suspend fun fetchHomeworksDetailsByTimeRange(from: Long, to: Long): Flow<List<HomeworkDetailsEntity>>
    suspend fun fetchOverdueHomeworksDetails(currentDate: Long): Flow<List<HomeworkDetailsEntity>>
    suspend fun fetchActiveLinkedHomeworksDetails(currentDate: Long): Flow<List<HomeworkDetailsEntity>>
    suspend fun fetchCompletedHomeworksCount(): Flow<Int>
    suspend fun fetchCompletedHomeworksDetailsByTimeRange(from: Long, to: Long): Flow<List<HomeworkDetailsEntity>>
    suspend fun deleteHomeworksByIds(ids: List<String>)

    class Base(
        private val homeworkQueries: HomeworkQueries,
        private val organizationsQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : HomeworksLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher

        override suspend fun addOrUpdateHomework(item: BaseHomeworkEntity) {
            val uid = item.uid.ifEmpty { randomUUID() }
            val updatedItem = item.copy(uid = uid).mapToEntity()
            homeworkQueries.addOrUpdateHomework(updatedItem)
        }

        override suspend fun addOrUpdateHomeworks(items: List<BaseHomeworkEntity>) {
            items.forEach { item -> addOrUpdateHomework(item) }
        }

        override suspend fun fetchHomeworkDetailsById(uid: UID): Flow<HomeworkDetailsEntity?> {
            val query = homeworkQueries.fetchHomeworkById(uid)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }.flatMapToDetails()
        }

        override suspend fun fetchHomeworksDetailsByTimeRange(
            from: Long,
            to: Long
        ): Flow<List<HomeworkDetailsEntity>> {
            val query = homeworkQueries.fetchHomeworksByTimeRange(from, to)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
        }

        override suspend fun fetchOverdueHomeworksDetails(currentDate: Long): Flow<List<HomeworkDetailsEntity>> {
            val query = homeworkQueries.fetchOverdueHomeworks(currentDate)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
        }

        override suspend fun fetchActiveLinkedHomeworksDetails(currentDate: Long): Flow<List<HomeworkDetailsEntity>> {
            val query = homeworkQueries.fetchActiveAndLinkedHomeworks(currentDate)
            return query.mapToListFlow(coroutineContext) { it.mapToEntity() }.flatMapListToDetails()
        }

        override suspend fun fetchCompletedHomeworksCount(): Flow<Int> {
            val query = homeworkQueries.fetchCompletedHomeworksCount()
            return query.mapToOneFlow(coroutineContext) { it.toInt() }
        }

        override suspend fun fetchCompletedHomeworksDetailsByTimeRange(
            from: Long,
            to: Long,
        ): Flow<List<HomeworkDetailsEntity>> {
            val query = homeworkQueries.fetchCompletedHomeworksByTimeRange(from, to)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
        }

        override suspend fun deleteHomeworksByIds(ids: List<String>) {
            homeworkQueries.deleteHomeworks(ids)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<BaseHomeworkEntity>>.flatMapListToDetails() = flatMapLatest { homeworks ->
            if (homeworks.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = homeworks.map { it.organizationId }.toSet()

                val organizationsMapFlow = organizationsQueries.fetchOrganizationsById(
                    uid = organizationsIds,
                    mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel, _, _, locationList, _, offices, _, updatedAt ->
                        val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsEntity>(timeIntervalsModel)
                        val locations = locationList.map {
                            Json.decodeFromString<ContactInfoEntity>(it)
                        }
                        OrganizationShortEntity(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals, updatedAt)
                    },
                ).asFlow()
                    .mapToList(coroutineContext)
                    .map { organization -> organization.associateBy { it.uid } }

                val subjectsMapFlow = subjectQueries.fetchSubjectsByOrganizations(organizationsIds)
                    .mapToListFlow(coroutineContext) { it.mapToBase() }
                    .map { subject -> subject.associateBy { it.uid } }

                val employeesMapFlow = employeeQueries.fetchEmployeesByOrganizations(organizationsIds)
                    .mapToListFlow(coroutineContext) { it.mapToBase() }
                    .map { employee -> employee.associateBy { it.uid } }

                combine(
                    flowOf(homeworks),
                    organizationsMapFlow,
                    subjectsMapFlow,
                    employeesMapFlow,
                ) { homeworksList, organizationsMap, subjectsMap, employeesMap ->
                    homeworksList.map { homework ->
                        homework.mapToDetails(
                            organization = organizationsMap[homework.organizationId],
                            subject = subjectsMap[homework.subjectId]?.mapToDetails(
                                employee = employeesMap[subjectsMap[homework.subjectId]?.teacherId]
                            ),
                        )
                    }
                }
            }
        }

        private fun Flow<BaseHomeworkEntity?>.flatMapToDetails(): Flow<HomeworkDetailsEntity?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }
}
