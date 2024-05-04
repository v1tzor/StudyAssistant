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

package database.classes

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import managers.CoroutineManager
import mappers.subjects.mapToDetailsData
import mappers.tasks.mapToDetailsDate
import mappers.tasks.mapToLocalDate
import mappers.users.mapToDetailsData
import models.classes.ClassDetailsData
import models.organizations.OrganizationShortData
import randomUUID
import ru.aleshin.studyassistant.sqldelight.`class`.ClassQueries
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface ClassLocalDataSource {

    suspend fun fetchClassById(uid: UID): Flow<ClassDetailsData?>
    suspend fun addOrUpdateClass(scheduleClass: ClassDetailsData): UID
    suspend fun deleteClass(uid: UID)

    class Base(
        private val classQueries: ClassQueries,
        private val organizationsQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val coroutineManager: CoroutineManager,
    ) : ClassLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun fetchClassById(uid: UID): Flow<ClassDetailsData?> {
            val query = classQueries.fetchClassById(uid)
            val classEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return classEntityFlow.map { classEntity ->
                if (classEntity == null) return@map null

                val subjectQuery = classEntity.subject_id?.let { subjectQueries.fetchSubjectById(it) }
                val organizationQuery = organizationsQueries.fetchOrganizationById(
                    uid = classEntity.organization_id,
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
                val employee = classEntity.teacher_id?.let { teacherId ->
                    val employeeQuery = employeeQueries.fetchEmployeeById(teacherId)
                    employeeQuery.executeAsOneOrNull()?.mapToDetailsData()
                }

                classEntity.mapToDetailsDate(
                    organization = organization,
                    subject = subject,
                    employee = employee,
                )
            }
        }

        override suspend fun addOrUpdateClass(scheduleClass: ClassDetailsData): UID {
            val uid = scheduleClass.uid.ifEmpty { randomUUID() }
            val scheduleClassEntity = scheduleClass.mapToLocalDate()
            classQueries.addOrUpdateClass(scheduleClassEntity.copy(uid = uid))

            return uid
        }

        override suspend fun deleteClass(uid: UID) {
            return classQueries.deleteClsas(uid)
        }
    }
}
