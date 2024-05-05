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
import functional.UID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import managers.CoroutineManager
import managers.DateManager
import mappers.schedules.mapToDetailsData
import mappers.schedules.mapToLocalData
import mappers.subjects.mapToDetailsData
import mappers.tasks.mapToDetailsDate
import mappers.users.mapToDetailsData
import models.classes.ClassDetailsData
import models.organizations.OrganizationShortData
import models.schedules.BaseScheduleDetailsData
import models.schedules.CustomScheduleDetailsData
import randomUUID
import ru.aleshin.studyassistant.sqldelight.`class`.ClassEntity
import ru.aleshin.studyassistant.sqldelight.`class`.ClassQueries
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleQueries
import ru.aleshin.studyassistant.sqldelight.schedules.CustomScheduleQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface CustomScheduleLocalDataSource {

    suspend fun fetchScheduleByDate(date: Long): Flow<CustomScheduleDetailsData?>
    suspend fun fetchSchedulesByTimeRange(from: Long, to: Long): Flow<List<CustomScheduleDetailsData>>
    suspend fun addOrUpdateSchedule(schedule: CustomScheduleDetailsData): UID

    class Base(
        private val scheduleQueries: CustomScheduleQueries,
        private val classQueries: ClassQueries,
        private val organizationsQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : CustomScheduleLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchScheduleByDate(date: Long): Flow<CustomScheduleDetailsData?> {
            val query = scheduleQueries.fetchSchedulesByDate(date)
            val scheduleEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return scheduleEntityFlow.flatMapLatest { scheduleEntity ->
                if (scheduleEntity == null) return@flatMapLatest flowOf(null)
                val classesQuery = classQueries.fetchClassByScheduleId(scheduleEntity.uid)
                val classListFlow = classesQuery.asFlow().mapToList(coroutineContext)

                return@flatMapLatest classListFlow.map { classList ->
                    val classes = classList.map { it.mapToDetails() }
                    scheduleEntity.mapToDetailsData(classes = classes)
                }
            }
        }

        override suspend fun fetchSchedulesByTimeRange(from: Long, to: Long): Flow<List<CustomScheduleDetailsData>> {
            val query = scheduleQueries.fetchSchedulesByTimeRange(from, to)
            val scheduleEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return scheduleEntityListFlow.map { scheduleEntityList ->
                scheduleEntityList.map { scheduleEntity ->
                    val classesQuery = classQueries.fetchClassByScheduleId(scheduleEntity.uid)
                    val classes = classesQuery.executeAsList().map { it.mapToDetails() }
                    scheduleEntity.mapToDetailsData(classes = classes)
                }
            }
        }

        override suspend fun addOrUpdateSchedule(schedule: CustomScheduleDetailsData): UID {
            val uid = schedule.uid.ifEmpty { randomUUID() }
            val scheduleClassEntity = schedule.mapToLocalData()
            scheduleQueries.addOrUpdateSchedule(scheduleClassEntity.copy(uid = uid))

            return uid
        }

        private suspend fun ClassEntity.mapToDetails(): ClassDetailsData {
            val subjectQuery = subject_id?.let { subjectQueries.fetchSubjectById(it) }
            val organizationQuery = organizationsQueries.fetchOrganizationById(
                uid = organization_id,
                mapper = { uid, _, shortName, _, type, avatar, _, _, _, _, _, _ ->
                    OrganizationShortData(uid, shortName, type, avatar)
                },
            )

            val organization = organizationQuery.executeAsOne()
            val subject = subjectQuery?.executeAsOneOrNull().let { subjectEntity ->
                val employeeQuery = subjectEntity?.teacher_id?.let { employeeQueries.fetchEmployeeById(it) }
                val employee = employeeQuery?.executeAsOneOrNull()?.mapToDetailsData()
                subjectEntity?.mapToDetailsData(employee)
            }
            val employee = teacher_id?.let { teacherId ->
                val employeeQuery = employeeQueries.fetchEmployeeById(teacherId)
                employeeQuery.executeAsOneOrNull()?.mapToDetailsData()
            }

            return mapToDetailsDate(
                organization = organization,
                subject = subject,
                employee = employee,
            )
        }
    }
}
