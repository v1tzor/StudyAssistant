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

package database.schedules

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import entities.common.NumberOfRepeatWeek
import extensions.dateTime
import extensions.randomUUID
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import managers.CoroutineManager
import mappers.schedules.mapToDetailsData
import mappers.schedules.mapToLocalData
import mappers.subjects.mapToDetailsData
import mappers.tasks.mapToDetailsData
import mappers.users.mapToDetailsData
import models.classes.ClassData
import models.classes.ClassDetailsData
import models.organizations.OrganizationShortData
import models.organizations.ScheduleTimeIntervalsData
import models.schedules.BaseScheduleDetailsData
import models.users.ContactInfoData
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface BaseScheduleLocalDataSource {

    suspend fun addOrUpdateSchedule(schedule: BaseScheduleDetailsData): UID
    suspend fun fetchScheduleById(uid: UID): Flow<BaseScheduleDetailsData?>
    suspend fun fetchScheduleByDate(date: Instant, numberOfWeek: NumberOfRepeatWeek): Flow<BaseScheduleDetailsData?>
    suspend fun fetchSchedulesByTimeRange(from: Instant, to: Instant, numberOfWeek: NumberOfRepeatWeek): Flow<List<BaseScheduleDetailsData>>
    suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<ClassDetailsData?>

    class Base(
        private val scheduleQueries: BaseScheduleQueries,
        private val organizationsQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : BaseScheduleLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateSchedule(schedule: BaseScheduleDetailsData): UID {
            val uid = schedule.uid.ifEmpty { randomUUID() }
            val scheduleEntity = schedule.mapToLocalData()
            scheduleQueries.addOrUpdateSchedule(scheduleEntity.copy(uid = uid))

            return uid
        }

        override suspend fun fetchScheduleById(uid: UID): Flow<BaseScheduleDetailsData?> {
            val query = scheduleQueries.fetchScheduleById(uid)
            val scheduleEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return scheduleEntityFlow.map { scheduleEntity ->
                scheduleEntity?.mapToDetailsData(
                    classesMapper = { it.mapToDetails(scheduleEntity.uid) }
                )
            }
        }

        override suspend fun fetchScheduleByDate(
            date: Instant,
            numberOfWeek: NumberOfRepeatWeek,
        ): Flow<BaseScheduleDetailsData?> {
            val dateMillis = date.toEpochMilliseconds()
            val dateTime = date.dateTime()
            val dayOfWeek = dateTime.dayOfWeek.toString()
            val week = numberOfWeek.toString()

            val query = scheduleQueries.fetchSchedulesByDate(week, dayOfWeek, dateMillis, dateMillis)
            val scheduleEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return scheduleEntityFlow.map { scheduleEntity ->
                scheduleEntity?.mapToDetailsData(
                    classesMapper = { it.mapToDetails(scheduleEntity.uid) }
                )
            }
        }

        override suspend fun fetchSchedulesByTimeRange(
            from: Instant,
            to: Instant,
            numberOfWeek: NumberOfRepeatWeek
        ): Flow<List<BaseScheduleDetailsData>> {
            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()
            val week = numberOfWeek.toString()

            val query = scheduleQueries.fetchSchedulesByTimeRange(week, fromMillis, toMillis)
            val scheduleEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return scheduleEntityListFlow.map { scheduleEntityList ->
                scheduleEntityList.map { scheduleEntity ->
                    scheduleEntity.mapToDetailsData(
                        classesMapper = { it.mapToDetails(scheduleEntity.uid) }
                    )
                }
            }
        }

        override suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<ClassDetailsData?> {
            val scheduleFlow = scheduleQueries.fetchScheduleById(scheduleId).asFlow().mapToOneOrNull(coroutineContext)

            return scheduleFlow.map { schedule ->
                val classes = schedule?.classes?.map { Json.decodeFromString<ClassData>(it) }
                val foundClass = classes?.find { it.uid == uid }
                return@map foundClass?.mapToDetails(scheduleId)
            }
        }

        private suspend fun ClassData.mapToDetails(scheduleId: UID): ClassDetailsData {
            val subjectQuery = subjectId?.let { subjectQueries.fetchSubjectById(it) }
            val organizationQuery = organizationsQueries.fetchOrganizationById(
                uid = organizationId,
                mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel, _, _, locationList, _, offices, _ ->
                    val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsData>(timeIntervalsModel)
                    val locations = locationList.map { Json.decodeFromString<ContactInfoData>(it) }
                    OrganizationShortData(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals)
                },
            )

            val organization = organizationQuery.executeAsOne()
            val subject = subjectQuery?.executeAsOneOrNull().let { subjectEntity ->
                val employeeQuery = subjectEntity?.teacher_id?.let { employeeQueries.fetchEmployeeById(it) }
                val employee = employeeQuery?.executeAsOneOrNull()?.mapToDetailsData()
                subjectEntity?.mapToDetailsData(employee)
            }
            val employee = teacherId?.let { teacherId ->
                val employeeQuery = employeeQueries.fetchEmployeeById(teacherId)
                employeeQuery.executeAsOneOrNull()?.mapToDetailsData()
            }

            return mapToDetailsData(
                scheduleId = scheduleId,
                organization = organization,
                subject = subject,
                employee = employee,
            )
        }
    }
}