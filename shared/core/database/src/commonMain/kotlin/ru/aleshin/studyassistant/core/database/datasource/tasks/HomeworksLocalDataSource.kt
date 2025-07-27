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

package ru.aleshin.studyassistant.core.database.datasource.tasks

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
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.datasource.tasks.HomeworksLocalDataSource.OfflineStorage
import ru.aleshin.studyassistant.core.database.datasource.tasks.HomeworksLocalDataSource.SyncStorage
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
import ru.aleshin.studyassistant.core.database.utils.CombinedLocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalMultipleDocumentsCommands
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface HomeworksLocalDataSource : CombinedLocalDataSource<BaseHomeworkEntity, OfflineStorage, SyncStorage> {

    interface Commands : LocalMultipleDocumentsCommands<BaseHomeworkEntity> {

        suspend fun fetchHomeworkDetailsById(uid: UID): Flow<HomeworkDetailsEntity?>
        suspend fun fetchHomeworksDetailsByTimeRange(from: Long, to: Long): Flow<List<HomeworkDetailsEntity>>
        suspend fun fetchOverdueHomeworksDetails(currentDate: Long): Flow<List<HomeworkDetailsEntity>>
        suspend fun fetchActiveLinkedHomeworksDetails(currentDate: Long): Flow<List<HomeworkDetailsEntity>>
        suspend fun fetchAllHomeworks(): Flow<List<BaseHomeworkEntity>>
        suspend fun fetchCompletedHomeworksCount(): Flow<Int>

        abstract class Abstract(
            isCacheSource: Boolean,
            private val homeworkQueries: HomeworkQueries,
            private val organizationsQueries: OrganizationQueries,
            private val employeeQueries: EmployeeQueries,
            private val subjectQueries: SubjectQueries,
            private val coroutineManager: CoroutineManager,
        ) : Commands {

            private val coroutineContext: CoroutineContext
                get() = coroutineManager.backgroundDispatcher

            private val isCacheData = if (isCacheSource) 1L else 0L

            override suspend fun addOrUpdateItem(item: BaseHomeworkEntity) {
                val uid = item.uid.ifEmpty { randomUUID() }
                val updatedItem = item.copy(uid = uid, isCacheData = isCacheData).mapToEntity()
                homeworkQueries.addOrUpdateHomework(updatedItem).await()
            }

            override suspend fun addOrUpdateItems(items: List<BaseHomeworkEntity>) {
                items.forEach { item -> addOrUpdateItem(item) }
            }

            override suspend fun fetchItemById(id: String): Flow<BaseHomeworkEntity?> {
                val query = homeworkQueries.fetchHomeworkById(id, isCacheData)
                return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchItemsById(ids: List<String>): Flow<List<BaseHomeworkEntity>> {
                val query = homeworkQueries.fetchHomeworksById(ids, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }
            override suspend fun fetchHomeworkDetailsById(uid: UID): Flow<HomeworkDetailsEntity?> {
                return fetchItemById(uid).flatMapToDetails()
            }

            override suspend fun fetchHomeworksDetailsByTimeRange(from: Long, to: Long): Flow<List<HomeworkDetailsEntity>> {
                val query = homeworkQueries.fetchHomeworksByTimeRange(from, to, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
            }

            override suspend fun fetchOverdueHomeworksDetails(currentDate: Long): Flow<List<HomeworkDetailsEntity>> {
                val query = homeworkQueries.fetchOverdueHomeworks(currentDate, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
            }

            override suspend fun fetchActiveLinkedHomeworksDetails(currentDate: Long): Flow<List<HomeworkDetailsEntity>> {
                val query = homeworkQueries.fetchActiveAndLinkedHomeworks(currentDate, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToEntity() }.flatMapListToDetails()
            }

            override suspend fun fetchAllHomeworks(): Flow<List<BaseHomeworkEntity>> {
                val query = homeworkQueries.fetchAllHomeworks(isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchCompletedHomeworksCount(): Flow<Int> {
                val query = homeworkQueries.fetchCompletedHomeworksCount(isCacheData)
                return query.mapToOneFlow(coroutineContext) { it.toInt() }
            }

            override suspend fun fetchAllMetadata(): List<MetadataModel> {
                val query = homeworkQueries.fetchEmptyHomeworks()
                return query.awaitAsList().map { entity ->
                    MetadataModel(entity.uid, entity.updated_at)
                }
            }

            override suspend fun deleteItemsById(ids: List<String>) {
                homeworkQueries.deleteHomeworks(ids, isCacheData).await()
            }

            override suspend fun deleteAllItems() {
                homeworkQueries.deleteAllHomeworks(isCacheData).await()
            }

            @OptIn(ExperimentalCoroutinesApi::class)
            private fun Flow<List<BaseHomeworkEntity>>.flatMapListToDetails() = flatMapLatest { homeworks ->
                if (homeworks.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val organizationsIds = homeworks.map { it.organizationId }.toSet()

                    val organizationsMapFlow = organizationsQueries.fetchOrganizationsById(
                        uid = organizationsIds,
                        is_cache_data = isCacheData,
                        mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel, _, _, locationList, _, offices, _, updatedAt, _ ->
                            val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsEntity>(
                                timeIntervalsModel
                            )
                            val locations = locationList.map {
                                Json.decodeFromString<ContactInfoEntity>(it)
                            }
                            OrganizationShortEntity(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals, updatedAt)
                        },
                    ).asFlow()
                        .mapToList(coroutineContext)
                        .map { organization -> organization.associateBy { it.uid } }

                    val subjectsMapFlow = subjectQueries.fetchSubjectsByOrganizations(organizationsIds, isCacheData)
                        .mapToListFlow(coroutineContext) { it.mapToBase() }
                        .map { subject -> subject.associateBy { it.uid } }

                    val employeesMapFlow = employeeQueries.fetchEmployeesByOrganizations(organizationsIds, isCacheData)
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
                                organization = checkNotNull(organizationsMap[homework.organizationId]),
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

    interface OfflineStorage : LocalDataSource.OnlyOffline, Commands {

        class Base(
            homeworkQueries: HomeworkQueries,
            organizationsQueries: OrganizationQueries,
            employeeQueries: EmployeeQueries,
            subjectQueries: SubjectQueries,
            coroutineManager: CoroutineManager,
        ) : OfflineStorage, Commands.Abstract(
            isCacheSource = false,
            homeworkQueries = homeworkQueries,
            organizationsQueries = organizationsQueries,
            employeeQueries = employeeQueries,
            subjectQueries = subjectQueries,
            coroutineManager = coroutineManager,
        )
    }

    interface SyncStorage : LocalDataSource.FullSynced.MultipleDocuments<BaseHomeworkEntity>, Commands {

        class Base(
            homeworkQueries: HomeworkQueries,
            organizationsQueries: OrganizationQueries,
            employeeQueries: EmployeeQueries,
            subjectQueries: SubjectQueries,
            coroutineManager: CoroutineManager,
        ) : SyncStorage, Commands.Abstract(
            isCacheSource = true,
            homeworkQueries = homeworkQueries,
            organizationsQueries = organizationsQueries,
            employeeQueries = employeeQueries,
            subjectQueries = subjectQueries,
            coroutineManager = coroutineManager,
        )
    }

    class Base(
        private val offlineStorage: OfflineStorage,
        private val syncStorage: SyncStorage
    ) : HomeworksLocalDataSource {
        override fun offline() = offlineStorage
        override fun sync() = syncStorage
    }
}