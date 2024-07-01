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

package database.tasks

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import extensions.randomUUID
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import managers.CoroutineManager
import mappers.subjects.mapToDetailsData
import mappers.tasks.mapToDetailsData
import mappers.tasks.mapToLocalData
import mappers.users.mapToDetailsData
import models.organizations.OrganizationShortData
import models.organizations.ScheduleTimeIntervalsData
import models.tasks.HomeworkDetailsData
import models.users.ContactInfoData
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface HomeworksLocalDataSource {

    suspend fun addOrUpdateHomework(homework: HomeworkDetailsData): UID
    suspend fun fetchHomeworkById(uid: UID): Flow<HomeworkDetailsData?>
    suspend fun fetchHomeworksByTimeRange(from: Long, to: Long): Flow<List<HomeworkDetailsData>>
    suspend fun fetchOverdueHomeworks(currentDate: Long): Flow<List<HomeworkDetailsData>>
    suspend fun fetchActiveLinkedHomeworks(currentDate: Long): Flow<List<HomeworkDetailsData>>
    suspend fun deleteHomework(uid: UID)

    class Base(
        private val homeworkQueries: HomeworkQueries,
        private val organizationsQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : HomeworksLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateHomework(homework: HomeworkDetailsData): UID {
            val uid = homework.uid.ifEmpty { randomUUID() }
            val homeworkEntity = homework.mapToLocalData()
            homeworkQueries.addOrUpdateHomeworks(homeworkEntity.copy(uid = uid))

            return uid
        }

        override suspend fun fetchHomeworkById(uid: UID): Flow<HomeworkDetailsData?> {
            val query = homeworkQueries.fetchHomeworkById(uid)
            val homeworkEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return homeworkEntityFlow.map { homeworkEntity ->
                if (homeworkEntity == null) return@map null

                val subjectQuery = homeworkEntity.subject_id?.let { subjectQueries.fetchSubjectById(it) }
                val organizationQuery = organizationsQueries.fetchOrganizationById(
                    uid = homeworkEntity.organization_id,
                    mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel,
                               _, _, locationList, _, offices, _ ->
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

                homeworkEntity.mapToDetailsData(
                    organization = organization,
                    subject = subject,
                )
            }
        }

        override suspend fun fetchHomeworksByTimeRange(from: Long, to: Long): Flow<List<HomeworkDetailsData>> {
            val query = homeworkQueries.fetchHomeworksByTimeRange(from, to)
            val homeworkEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return homeworkEntityListFlow.map { homeworks ->
                homeworks.map { homeworkEntity ->
                    val organizationQuery = organizationsQueries.fetchOrganizationById(
                        uid = homeworkEntity.organization_id,
                        mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel,
                                   _, _, locationList, _, offices, _ ->
                            val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsData>(timeIntervalsModel)
                            val locations = locationList.map { Json.decodeFromString<ContactInfoData>(it) }
                            OrganizationShortData(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals)
                        },
                    )
                    val subjectQuery = homeworkEntity.subject_id?.let { subjectQueries.fetchSubjectById(it) }

                    val organization = organizationQuery.executeAsOne()
                    val subject = subjectQuery?.executeAsOne().let { subjectEntity ->
                        val employeeQuery = subjectEntity?.teacher_id?.let { employeeQueries.fetchEmployeeById(it) }
                        val employee = employeeQuery?.executeAsOne()?.mapToDetailsData()
                        subjectEntity?.mapToDetailsData(employee)
                    }

                    homeworkEntity.mapToDetailsData(
                        organization = organization,
                        subject = subject,
                    )
                }
            }
        }

        override suspend fun fetchOverdueHomeworks(currentDate: Long): Flow<List<HomeworkDetailsData>> {
            val query = homeworkQueries.fetchOverdueHomeworks(currentDate)
            val homeworkEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return homeworkEntityListFlow.map { homeworks ->
                homeworks.map { homeworkEntity ->
                    val organizationQuery = organizationsQueries.fetchOrganizationById(
                        uid = homeworkEntity.organization_id,
                        mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel,
                                   _, _, locationList, _, offices, _ ->
                            val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsData>(timeIntervalsModel)
                            val locations = locationList.map { Json.decodeFromString<ContactInfoData>(it) }
                            OrganizationShortData(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals)
                        },
                    )
                    val subjectQuery = homeworkEntity.subject_id?.let { subjectQueries.fetchSubjectById(it) }

                    val organization = organizationQuery.executeAsOne()
                    val subject = subjectQuery?.executeAsOne().let { subjectEntity ->
                        val employeeQuery = subjectEntity?.teacher_id?.let { employeeQueries.fetchEmployeeById(it) }
                        val employee = employeeQuery?.executeAsOne()?.mapToDetailsData()
                        subjectEntity?.mapToDetailsData(employee)
                    }

                    homeworkEntity.mapToDetailsData(
                        organization = organization,
                        subject = subject,
                    )
                }
            }
        }

        override suspend fun fetchActiveLinkedHomeworks(currentDate: Long): Flow<List<HomeworkDetailsData>> {
            val query = homeworkQueries.fetchActiveAndLinkedHomeworks(currentDate)
            val homeworkEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return homeworkEntityListFlow.map { homeworks ->
                homeworks.map { homeworkEntity ->
                    val organizationQuery = organizationsQueries.fetchOrganizationById(
                        uid = homeworkEntity.organization_id,
                        mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel,
                                   _, _, locationList, _, offices, _ ->
                            val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsData>(timeIntervalsModel)
                            val locations = locationList.map { Json.decodeFromString<ContactInfoData>(it) }
                            OrganizationShortData(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals)
                        },
                    )
                    val subjectQuery = homeworkEntity.subject_id?.let { subjectQueries.fetchSubjectById(it) }

                    val organization = organizationQuery.executeAsOne()
                    val subject = subjectQuery?.executeAsOne().let { subjectEntity ->
                        val employeeQuery = subjectEntity?.teacher_id?.let { employeeQueries.fetchEmployeeById(it) }
                        val employee = employeeQuery?.executeAsOne()?.mapToDetailsData()
                        subjectEntity?.mapToDetailsData(employee)
                    }

                    homeworkEntity.mapToDetailsData(
                        organization = organization,
                        subject = subject,
                    )
                }
            }
        }

        override suspend fun deleteHomework(uid: UID) {
            homeworkQueries.deleteHomework(uid)
        }
    }
}