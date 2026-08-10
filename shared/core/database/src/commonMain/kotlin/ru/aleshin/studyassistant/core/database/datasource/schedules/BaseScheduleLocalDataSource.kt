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

package ru.aleshin.studyassistant.core.database.datasource.schedules

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
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.extractAllItemToSet
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
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
import ru.aleshin.studyassistant.core.database.models.schedule.BaseScheduleDetailsEntity
import ru.aleshin.studyassistant.core.database.models.schedule.BaseScheduleEntity
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface BaseScheduleLocalDataSource {

    suspend fun addOrUpdateSchedule(item: BaseScheduleEntity)
    suspend fun addOrUpdateSchedules(items: List<BaseScheduleEntity>)

    suspend fun fetchScheduleDetailsById(uid: UID): Flow<BaseScheduleDetailsEntity?>
    suspend fun fetchScheduleDetailsByDate(date: Instant, numberOfWeek: NumberOfRepeatWeek): Flow<BaseScheduleDetailsEntity?>
    suspend fun fetchSchedulesByVersion(from: Instant, to: Instant, numberOfWeek: NumberOfRepeatWeek?): Flow<List<BaseScheduleDetailsEntity>>
    suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<ClassDetailsEntity?>
    suspend fun deleteSchedulesByTimeRange(from: Instant, to: Instant)

    class Base(
        private val scheduleQueries: BaseScheduleQueries,
        private val organizationsQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : BaseScheduleLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher


        override suspend fun addOrUpdateSchedule(item: BaseScheduleEntity) {
            val uid = item.uid.ifEmpty { randomUUID() }
            val updatedItem = item.copy(uid = uid).mapToEntity()
            scheduleQueries.addOrUpdateSchedule(updatedItem).await()
        }

        override suspend fun addOrUpdateSchedules(items: List<BaseScheduleEntity>) {
            items.forEach { item -> addOrUpdateSchedule(item) }
        }

        override suspend fun fetchScheduleDetailsById(uid: UID): Flow<BaseScheduleDetailsEntity?> {
            val query = scheduleQueries.fetchScheduleById(uid)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
                .flatMapToDetails()
        }

        override suspend fun fetchScheduleDetailsByDate(
            date: Instant,
            numberOfWeek: NumberOfRepeatWeek,
        ): Flow<BaseScheduleDetailsEntity?> {
            val dateMillis = date.toEpochMilliseconds()
            val dateTime = date.dateTime()
            val dayOfWeek = dateTime.dayOfWeek.toString()
            val week = numberOfWeek.toString()

            val query = scheduleQueries.fetchScheduleByDate(
                week = week,
                week_day_of_week = dayOfWeek,
                date_version_from = dateMillis,
                date_version_to = dateMillis,
            )
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }.flatMapToDetails()
        }

        override suspend fun fetchSchedulesByVersion(
            from: Instant,
            to: Instant,
            numberOfWeek: NumberOfRepeatWeek?,
        ): Flow<List<BaseScheduleDetailsEntity>> {
            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()
            val week = numberOfWeek?.toString()

            val query = if (week == null) {
                scheduleQueries.fetchSchedulesByTimeRange(fromMillis, toMillis)
            } else {
                scheduleQueries.fetchSchedulesByTimeRangeWithWeek(week, fromMillis, toMillis)
            }

            return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
        }

        override suspend fun fetchClassById(
            uid: UID,
            scheduleId: UID
        ): Flow<ClassDetailsEntity?> {
            val query = scheduleQueries.fetchScheduleById(scheduleId)
            val scheduleFlow = query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
                .flatMapToDetails()
            return scheduleFlow.map { schedule -> schedule?.classes?.find { it.uid == uid } }
        }

        override suspend fun deleteSchedulesByTimeRange(from: Instant, to: Instant) {
            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()

            scheduleQueries.deleteSchedulesByTimeRange(fromMillis, toMillis)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<BaseScheduleEntity>>.flatMapListToDetails() = flatMapLatest { schedules ->
            if (schedules.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = schedules.map { schedulePojo ->
                    schedulePojo.classes.map { Json.decodeFromString<ClassEntity>(it).organizationId }
                }.extractAllItemToSet()

                val organizationsMapFlow = organizationsQueries.fetchOrganizationsById(
                    uid = organizationsIds,
                    mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel, _, _, locationList, _, offices, _, updatedAt ->
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

                val subjectsMapFlow = subjectQueries.fetchSubjectsByOrganizations(organizationsIds)
                    .mapToListFlow(coroutineContext) { it.mapToBase() }
                    .map { subject -> subject.associateBy { it.uid } }

                val employeesMapFlow = employeeQueries.fetchEmployeesByOrganizations(organizationsIds)
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

        private fun Flow<BaseScheduleEntity?>.flatMapToDetails(): Flow<BaseScheduleDetailsEntity?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }

}
