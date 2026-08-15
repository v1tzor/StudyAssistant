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

package ru.aleshin.studyassistant.core.database.datasource.shared

import ru.aleshin.studyassistant.core.data.Database
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToEntity
import ru.aleshin.studyassistant.core.database.mappers.organizations.mapToEntity
import ru.aleshin.studyassistant.core.database.mappers.schedules.mapToEntity
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToEntity
import ru.aleshin.studyassistant.core.database.models.employee.BaseEmployeeEntity
import ru.aleshin.studyassistant.core.database.models.organizations.BaseOrganizationEntity
import ru.aleshin.studyassistant.core.database.models.schedule.BaseScheduleEntity
import ru.aleshin.studyassistant.core.database.models.subjects.BaseSubjectEntity

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
interface ScheduleShareLocalDataSource {

    suspend fun importSchedule(
        organizations: List<BaseOrganizationEntity>,
        employees: List<BaseEmployeeEntity>,
        subjects: List<BaseSubjectEntity>,
        schedules: List<BaseScheduleEntity>,
    )

    class Base(
        private val database: Database,
    ) : ScheduleShareLocalDataSource {

        override suspend fun importSchedule(
            organizations: List<BaseOrganizationEntity>,
            employees: List<BaseEmployeeEntity>,
            subjects: List<BaseSubjectEntity>,
            schedules: List<BaseScheduleEntity>,
        ) {
            database.transaction {
                organizations.forEach { organization ->
                    database.organizationQueries.addOrUpdateOrganization(organization.mapToEntity())
                }
                employees.forEach { employee ->
                    database.employeeQueries.addOrUpdateEmployee(employee.mapToEntity())
                }
                subjects.forEach { subject ->
                    database.subjectQueries.addOrUpdateSubject(subject.mapToEntity())
                }
                schedules.forEach { schedule ->
                    database.baseScheduleQueries.addOrUpdateSchedule(schedule.mapToEntity())
                }
            }
        }
    }
}
