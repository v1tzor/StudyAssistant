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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToDetails
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.database.models.organizations.ScheduleTimeIntervalsEntity
import ru.aleshin.studyassistant.core.database.models.tasks.HomeworkDetailsEntity
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkEntity
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface HomeworksLocalDataSource {

    suspend fun addOrUpdateHomework(homework: HomeworkEntity): UID
    suspend fun addOrUpdateHomeworksGroup(homeworks: List<HomeworkEntity>)
    suspend fun fetchHomeworkById(uid: UID): Flow<HomeworkDetailsEntity?>
    suspend fun fetchHomeworksByTimeRange(from: Long, to: Long): Flow<List<HomeworkDetailsEntity>>
    suspend fun fetchOverdueHomeworks(currentDate: Long): Flow<List<HomeworkDetailsEntity>>
    suspend fun fetchActiveLinkedHomeworks(currentDate: Long): Flow<List<HomeworkDetailsEntity>>
    suspend fun deleteHomework(uid: UID)
    suspend fun deleteAllHomework()

    class Base(
        private val homeworkQueries: HomeworkQueries,
        private val organizationsQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : HomeworksLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateHomework(homework: HomeworkEntity): UID {
            val uid = homework.uid.ifEmpty { randomUUID() }
            homeworkQueries.addOrUpdateHomework(homework.copy(uid = uid))

            return uid
        }

        override suspend fun addOrUpdateHomeworksGroup(homeworks: List<HomeworkEntity>) {
            homeworks.forEach { addOrUpdateHomework(it) }
        }

        override suspend fun fetchHomeworkById(uid: UID): Flow<HomeworkDetailsEntity?> {
            val query = homeworkQueries.fetchHomeworkById(uid)
            val homeworkEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return homeworkEntityFlow.map { homeworkEntity ->
                if (homeworkEntity == null) return@map null

                val subjectQuery = homeworkEntity.subject_id?.let { subjectQueries.fetchSubjectById(it) }
                val organizationQuery = organizationsQueries.fetchOrganizationById(
                    uid = homeworkEntity.organization_id,
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

                homeworkEntity.mapToDetails(
                    organization = organization,
                    subject = subject,
                )
            }
        }

        override suspend fun fetchHomeworksByTimeRange(from: Long, to: Long): Flow<List<HomeworkDetailsEntity>> {
            val query = homeworkQueries.fetchHomeworksByTimeRange(from, to)
            val homeworkEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return homeworkEntityListFlow.map { homeworks ->
                homeworks.map { homeworkEntity ->
                    val organizationQuery = organizationsQueries.fetchOrganizationById(
                        uid = homeworkEntity.organization_id,
                        mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel,
                                   _, _, locationList, _, offices, _ ->
                            val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsEntity>(timeIntervalsModel)
                            val locations = locationList.map { Json.decodeFromString<ContactInfoEntity>(it) }
                            OrganizationShortEntity(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals)
                        },
                    )
                    val subjectQuery = homeworkEntity.subject_id?.let { subjectQueries.fetchSubjectById(it) }

                    val organization = organizationQuery.executeAsOne()
                    val subject = subjectQuery?.executeAsOne().let { subjectEntity ->
                        val employeeQuery = subjectEntity?.teacher_id?.let { employeeQueries.fetchEmployeeById(it) }
                        val employee = employeeQuery?.executeAsOne()
                        subjectEntity?.mapToDetails(employee)
                    }

                    homeworkEntity.mapToDetails(
                        organization = organization,
                        subject = subject,
                    )
                }
            }
        }

        override suspend fun fetchOverdueHomeworks(currentDate: Long): Flow<List<HomeworkDetailsEntity>> {
            val query = homeworkQueries.fetchOverdueHomeworks(currentDate)
            val homeworkEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return homeworkEntityListFlow.map { homeworks ->
                homeworks.map { homeworkEntity ->
                    val organizationQuery = organizationsQueries.fetchOrganizationById(
                        uid = homeworkEntity.organization_id,
                        mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel,
                                   _, _, locationList, _, offices, _ ->
                            val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsEntity>(timeIntervalsModel)
                            val locations = locationList.map { Json.decodeFromString<ContactInfoEntity>(it) }
                            OrganizationShortEntity(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals)
                        },
                    )
                    val subjectQuery = homeworkEntity.subject_id?.let { subjectQueries.fetchSubjectById(it) }

                    val organization = organizationQuery.executeAsOne()
                    val subject = subjectQuery?.executeAsOne().let { subjectEntity ->
                        val employeeQuery = subjectEntity?.teacher_id?.let { employeeQueries.fetchEmployeeById(it) }
                        val employee = employeeQuery?.executeAsOne()
                        subjectEntity?.mapToDetails(employee)
                    }

                    homeworkEntity.mapToDetails(
                        organization = organization,
                        subject = subject,
                    )
                }
            }
        }

        override suspend fun fetchActiveLinkedHomeworks(currentDate: Long): Flow<List<HomeworkDetailsEntity>> {
            val query = homeworkQueries.fetchActiveAndLinkedHomeworks(currentDate)
            val homeworkEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return homeworkEntityListFlow.map { homeworks ->
                homeworks.map { homeworkEntity ->
                    val organizationQuery = organizationsQueries.fetchOrganizationById(
                        uid = homeworkEntity.organization_id,
                        mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel,
                                   _, _, locationList, _, offices, _ ->
                            val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsEntity>(timeIntervalsModel)
                            val locations = locationList.map { Json.decodeFromString<ContactInfoEntity>(it) }
                            OrganizationShortEntity(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals)
                        },
                    )
                    val subjectQuery = homeworkEntity.subject_id?.let { subjectQueries.fetchSubjectById(it) }

                    val organization = organizationQuery.executeAsOne()
                    val subject = subjectQuery?.executeAsOne().let { subjectEntity ->
                        val employeeQuery = subjectEntity?.teacher_id?.let { employeeQueries.fetchEmployeeById(it) }
                        val employee = employeeQuery?.executeAsOne()
                        subjectEntity?.mapToDetails(employee)
                    }

                    homeworkEntity.mapToDetails(
                        organization = organization,
                        subject = subject,
                    )
                }
            }
        }

        override suspend fun deleteHomework(uid: UID) {
            homeworkQueries.deleteHomework(uid)
        }

        override suspend fun deleteAllHomework() {
            homeworkQueries.deleteAllHomeworks()
        }
    }
}