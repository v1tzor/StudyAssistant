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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.schedules.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.database.models.classes.ClassDetailsEntity
import ru.aleshin.studyassistant.core.database.models.classes.ClassEntity
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.database.models.organizations.ScheduleTimeIntervalsEntity
import ru.aleshin.studyassistant.core.database.models.schedule.BaseScheduleDetailsEntity
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleEntity
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface BaseScheduleLocalDataSource {

    suspend fun addOrUpdateSchedule(schedule: BaseScheduleEntity): UID
    suspend fun fetchScheduleById(uid: UID): Flow<BaseScheduleDetailsEntity?>
    suspend fun fetchScheduleByDate(date: Instant, numberOfWeek: NumberOfRepeatWeek): Flow<BaseScheduleDetailsEntity?>
    suspend fun fetchSchedulesByVersion(from: Instant, to: Instant, numberOfWeek: NumberOfRepeatWeek?): Flow<List<BaseScheduleDetailsEntity>>
    suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<ClassDetailsEntity?>

    class Base(
        private val scheduleQueries: BaseScheduleQueries,
        private val organizationsQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : BaseScheduleLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateSchedule(schedule: BaseScheduleEntity): UID {
            val uid = schedule.uid.ifEmpty { randomUUID() }
            scheduleQueries.addOrUpdateSchedule(schedule.copy(uid = uid))

            return uid
        }

        override suspend fun fetchScheduleById(uid: UID): Flow<BaseScheduleDetailsEntity?> {
            val query = scheduleQueries.fetchScheduleById(uid)
            val scheduleEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return scheduleEntityFlow.map { scheduleEntity ->
                scheduleEntity?.mapToDetails(
                    classesMapper = { it.mapToDetails(scheduleEntity.uid) }
                )
            }
        }

        override suspend fun fetchScheduleByDate(
            date: Instant,
            numberOfWeek: NumberOfRepeatWeek,
        ): Flow<BaseScheduleDetailsEntity?> {
            val dateMillis = date.toEpochMilliseconds()
            val dateTime = date.dateTime()
            val dayOfWeek = dateTime.dayOfWeek.toString()
            val week = numberOfWeek.toString()

            val query = scheduleQueries.fetchSchedulesByDate(week, dayOfWeek, dateMillis, dateMillis)
            val scheduleEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return scheduleEntityFlow.map { scheduleEntity ->
                scheduleEntity?.mapToDetails(
                    classesMapper = { it.mapToDetails(scheduleEntity.uid) }
                )
            }
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
            val scheduleEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return scheduleEntityListFlow.map { scheduleEntityList ->
                scheduleEntityList.map { scheduleEntity ->
                    scheduleEntity.mapToDetails(
                        classesMapper = { it.mapToDetails(scheduleEntity.uid) }
                    )
                }
            }
        }

        override suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<ClassDetailsEntity?> {
            val scheduleFlow = scheduleQueries.fetchScheduleById(scheduleId).asFlow().mapToOneOrNull(coroutineContext)

            return scheduleFlow.map { schedule ->
                val classes = schedule?.classes?.map { Json.decodeFromString<ClassEntity>(it) }
                val foundClass = classes?.find { it.uid == uid }
                return@map foundClass?.mapToDetails(scheduleId)
            }
        }

        private suspend fun ClassEntity.mapToDetails(scheduleId: UID): ClassDetailsEntity {
            val subjectQuery = subjectId?.let { subjectQueries.fetchSubjectById(it) }
            val organizationQuery = organizationsQueries.fetchOrganizationById(
                uid = organizationId,
                mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel,
                           _, _, locationList, _, offices, _ ->
                    val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsEntity>(timeIntervalsModel)
                    val locations = locationList.map { Json.decodeFromString<ContactInfoEntity>(it) }
                    OrganizationShortEntity(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals)
                },
            )

            val organization = organizationQuery.executeAsOne()
            val subject = subjectQuery?.executeAsOneOrNull().let { subjectEntity ->
                val employeeQuery = subjectEntity?.teacher_id?.let { employeeQueries.fetchEmployeeById(it) }
                val employee = employeeQuery?.executeAsOneOrNull()
                subjectEntity?.mapToDetails(employee)
            }
            val employee = teacherId?.let { teacherId ->
                val employeeQuery = employeeQueries.fetchEmployeeById(teacherId)
                employeeQuery.executeAsOneOrNull()
            }

            return mapToDetails(
                scheduleId = scheduleId,
                organization = organization,
                subject = subject,
                employee = employee,
            )
        }
    }
}