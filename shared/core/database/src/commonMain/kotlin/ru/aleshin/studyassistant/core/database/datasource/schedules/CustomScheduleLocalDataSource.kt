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

package ru.aleshin.studyassistant.core.database.datasource.schedules

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
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.extractAllItemToSet
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.datasource.schedules.CustomScheduleLocalDataSource.OfflineStorage
import ru.aleshin.studyassistant.core.database.datasource.schedules.CustomScheduleLocalDataSource.SyncStorage
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.schedules.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.schedules.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.schedules.mapToEntity
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.database.models.classes.ClassDetailsEntity
import ru.aleshin.studyassistant.core.database.models.classes.ClassEntity
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.database.models.organizations.ScheduleTimeIntervalsEntity
import ru.aleshin.studyassistant.core.database.models.schedule.CustomScheduleDetailsEntity
import ru.aleshin.studyassistant.core.database.models.schedule.CustomScheduleEntity
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.core.database.utils.CombinedLocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalMultipleDocumentsCommands
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.schedules.CustomScheduleQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface CustomScheduleLocalDataSource : CombinedLocalDataSource<CustomScheduleEntity, OfflineStorage, SyncStorage> {

    interface Commands : LocalMultipleDocumentsCommands<CustomScheduleEntity> {

        suspend fun fetchScheduleDetailsById(uid: UID): Flow<CustomScheduleDetailsEntity?>
        suspend fun fetchScheduleDetailsByDate(date: Instant): Flow<CustomScheduleDetailsEntity?>
        suspend fun fetchSchedulesDetailsByTimeRange(from: Instant, to: Instant): Flow<List<CustomScheduleDetailsEntity>>
        suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<ClassDetailsEntity?>
        suspend fun fetchSchedulesByTimeRangeEmpty(from: Instant, to: Instant): List<MetadataModel>
        suspend fun fetchAllSchedules(): Flow<List<CustomScheduleEntity>>
        suspend fun deleteSchedulesByTimeRange(from: Instant, to: Instant)

        abstract class Abstract(
            isCacheSource: Boolean,
            private val scheduleQueries: CustomScheduleQueries,
            private val organizationsQueries: OrganizationQueries,
            private val employeeQueries: EmployeeQueries,
            private val subjectQueries: SubjectQueries,
            private val coroutineManager: CoroutineManager,
        ) : Commands {

            private val coroutineContext: CoroutineContext
                get() = coroutineManager.backgroundDispatcher

            private val isCacheData = if (isCacheSource) 1L else 0L

            override suspend fun addOrUpdateItem(item: CustomScheduleEntity) {
                val uid = item.uid.ifEmpty { randomUUID() }
                val updatedItem = item.copy(uid = uid, isCacheData = isCacheData).mapToEntity()
                scheduleQueries.addOrUpdateSchedule(updatedItem).await()
            }

            override suspend fun addOrUpdateItems(items: List<CustomScheduleEntity>) {
                items.forEach { item -> addOrUpdateItem(item) }
            }

            override suspend fun fetchItemById(id: String): Flow<CustomScheduleEntity?> {
                val query = scheduleQueries.fetchScheduleById(id, isCacheData)
                return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchItemsById(ids: List<String>): Flow<List<CustomScheduleEntity>> {
                val query = scheduleQueries.fetchSchedulesById(ids, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchScheduleDetailsById(uid: UID): Flow<CustomScheduleDetailsEntity?> {
                return fetchItemById(uid).flatMapToDetails()
            }

            override suspend fun fetchScheduleDetailsByDate(date: Instant): Flow<CustomScheduleDetailsEntity?> {
                val dateMillis = date.toEpochMilliseconds()

                val query = scheduleQueries.fetchSchedulesByDate(dateMillis, isCacheData)
                return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }.flatMapToDetails()
            }

            override suspend fun fetchSchedulesDetailsByTimeRange(from: Instant, to: Instant): Flow<List<CustomScheduleDetailsEntity>> {
                val fromMillis = from.toEpochMilliseconds()
                val toMillis = to.toEpochMilliseconds()

                val query = scheduleQueries.fetchSchedulesByTimeRange(fromMillis, toMillis, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
            }

            override suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<ClassDetailsEntity?> {
                val scheduleFlow = fetchItemById(scheduleId).flatMapToDetails()
                return scheduleFlow.map { schedule -> schedule?.classes?.find { it.uid == uid } }
            }

            override suspend fun fetchSchedulesByTimeRangeEmpty(from: Instant, to: Instant): List<MetadataModel> {
                val fromMillis = from.toEpochMilliseconds()
                val toMillis = to.toEpochMilliseconds()
                val query = scheduleQueries.fetchEmptySchedulesByTimeRange(fromMillis, toMillis)
                return query.awaitAsList().map { entity ->
                    MetadataModel(entity.uid, entity.updated_at)
                }
            }

            override suspend fun fetchAllSchedules(): Flow<List<CustomScheduleEntity>> {
                val query = scheduleQueries.fetchAllSchedules(isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchAllMetadata(): List<MetadataModel> {
                val query = scheduleQueries.fetchEmptySchedules()
                return query.awaitAsList().map { entity ->
                    MetadataModel(entity.uid, entity.updated_at)
                }
            }

            override suspend fun deleteItemsById(ids: List<String>) {
                scheduleQueries.deleteSchedulesById(ids, isCacheData).await()
            }

            override suspend fun deleteAllItems() {
                scheduleQueries.deleteAllSchedules(isCacheData).await()
            }

            override suspend fun deleteSchedulesByTimeRange(from: Instant, to: Instant) {
                val fromMillis = from.toEpochMilliseconds()
                val toMillis = to.toEpochMilliseconds()

                scheduleQueries.deleteSchedulesByTimeRange(fromMillis, toMillis, isCacheData).await()
            }

            @OptIn(ExperimentalCoroutinesApi::class)
            private fun Flow<List<CustomScheduleEntity>>.flatMapListToDetails() = flatMapLatest { schedules ->
                if (schedules.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val organizationsIds = schedules.map { schedulePojo ->
                        schedulePojo.classes.map { Json.decodeFromString<ClassEntity>(it).organizationId }
                    }.extractAllItemToSet()

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
                        flowOf(schedules),
                        organizationsMapFlow,
                        subjectsMapFlow,
                        employeesMapFlow,
                    ) { schedulesList, organizationsMap, subjectsMap, employeesMap ->
                        schedulesList.map { schedule ->
                            schedule.mapToDetails { classPojo ->
                                classPojo.mapToDetails(
                                    scheduleId = schedule.uid,
                                    organization = organizationsMap[classPojo.organizationId],
                                    employee = employeesMap[classPojo.teacherId],
                                    subject = subjectsMap[classPojo.subjectId]?.mapToDetails(
                                        employee = employeesMap[subjectsMap[classPojo.subjectId]?.teacherId]
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            private fun Flow<CustomScheduleEntity?>.flatMapToDetails(): Flow<CustomScheduleDetailsEntity?> {
                return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                    .flatMapListToDetails()
                    .map { it.getOrNull(0) }
            }
        }
    }

    interface OfflineStorage : LocalDataSource.OnlyOffline, Commands {

        class Base(
            scheduleQueries: CustomScheduleQueries,
            organizationsQueries: OrganizationQueries,
            employeeQueries: EmployeeQueries,
            subjectQueries: SubjectQueries,
            coroutineManager: CoroutineManager,
        ) : OfflineStorage, Commands.Abstract(
            isCacheSource = false,
            scheduleQueries = scheduleQueries,
            organizationsQueries = organizationsQueries,
            employeeQueries = employeeQueries,
            subjectQueries = subjectQueries,
            coroutineManager = coroutineManager,
        )
    }

    interface SyncStorage : LocalDataSource.FullSynced.MultipleDocuments<CustomScheduleEntity>, Commands {

        class Base(
            scheduleQueries: CustomScheduleQueries,
            organizationsQueries: OrganizationQueries,
            employeeQueries: EmployeeQueries,
            subjectQueries: SubjectQueries,
            coroutineManager: CoroutineManager,
        ) : SyncStorage, Commands.Abstract(
            isCacheSource = true,
            scheduleQueries = scheduleQueries,
            organizationsQueries = organizationsQueries,
            employeeQueries = employeeQueries,
            subjectQueries = subjectQueries,
            coroutineManager = coroutineManager,
        )
    }

    class Base(
        private val offlineStorage: OfflineStorage,
        private val syncStorage: SyncStorage
    ) : CustomScheduleLocalDataSource {
        override fun offline() = offlineStorage
        override fun sync() = syncStorage
    }
}