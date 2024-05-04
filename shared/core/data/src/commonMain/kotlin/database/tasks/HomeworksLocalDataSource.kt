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
import app.cash.sqldelight.coroutines.mapToOne
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import managers.CoroutineManager
import mappers.subjects.mapToDetailsData
import mappers.tasks.mapToDetailsData
import mappers.tasks.mapToLocalData
import mappers.users.mapToDetailsData
import models.organizations.OrganizationShortData
import models.tasks.HomeworkDetailsData
import randomUUID
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface HomeworksLocalDataSource {

    suspend fun fetchHomeworksByTime(from: Long, to: Long): Flow<List<HomeworkDetailsData>>
    suspend fun fetchHomeworkById(uid: UID): Flow<HomeworkDetailsData?>
    suspend fun addOrUpdateHomework(homework: HomeworkDetailsData): UID
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

        override suspend fun fetchHomeworksByTime(from: Long, to: Long): Flow<List<HomeworkDetailsData>> {
            val query = homeworkQueries.fetchHomeworksByTimeRange(from, to)
            val homeworkEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return homeworkEntityListFlow.map { homeworks ->
                homeworks.map { homeworkEntity ->
                    val organizationQuery = organizationsQueries.fetchById(
                        uid = homeworkEntity.organization_id,
                        mapper = { uid, _, shortName, _, type, avatar, _, _, _, _, _, _ ->
                            OrganizationShortData(uid, shortName, type, avatar)
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

        override suspend fun fetchHomeworkById(uid: UID): Flow<HomeworkDetailsData?> {
            val query = homeworkQueries.fetchHomeworkById(uid)
            val homeworkEntityFlow = query.asFlow().mapToOne(coroutineContext)

            return homeworkEntityFlow.map { homeworkEntity ->
                val organizationQuery = organizationsQueries.fetchById(
                    uid = homeworkEntity.organization_id,
                    mapper = { uid, _, shortName, _, type, avatar, _, _, _, _, _, _ ->
                        OrganizationShortData(uid, shortName, type, avatar)
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

        override suspend fun addOrUpdateHomework(homework: HomeworkDetailsData): UID {
            val uid = randomUUID()
            homeworkQueries.addOrUpdateHomeworks(homework.mapToLocalData())

            return uid
        }

        override suspend fun deleteHomework(uid: UID) {
            homeworkQueries.deleteHomework(uid)
        }
    }
}