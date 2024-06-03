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

package database.employee

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import managers.CoroutineManager
import mappers.users.mapToDetailsData
import mappers.users.mapToLocalData
import models.users.EmployeeDetailsData
import randomUUID
import remote.StudyAssistantFirestore.LIMITS.EDITOR_EMPLOYEE
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface EmployeeLocalDataSource {

    suspend fun addOrUpdateEmployee(employee: EmployeeDetailsData): UID
    suspend fun fetchEmployeeById(uid: UID): Flow<EmployeeDetailsData?>
    suspend fun fetchAllEmployeeByOrganization(organizationId: UID): Flow<List<EmployeeDetailsData>>
    suspend fun deleteEmployee(targetId: UID)

    class Base(
        private val employeeQueries: EmployeeQueries,
        private val coroutineManager: CoroutineManager,
    ) : EmployeeLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateEmployee(employee: EmployeeDetailsData): UID {
            val uid = employee.uid.ifEmpty { randomUUID() }
            val employeeEntity = employee.mapToLocalData()
            employeeQueries.addOrUpdateEmployee(employeeEntity.copy(uid = uid))

            return uid
        }

        override suspend fun fetchEmployeeById(uid: UID): Flow<EmployeeDetailsData?> {
            val query = employeeQueries.fetchEmployeeById(uid)
            val employeeEntityFlow = query.asFlow().mapToOneOrNull(coroutineContext)

            return employeeEntityFlow.map { employeeEntity ->
                employeeEntity?.mapToDetailsData()
            }
        }

        override suspend fun fetchAllEmployeeByOrganization(organizationId: UID): Flow<List<EmployeeDetailsData>> {
            val query = employeeQueries.fetchEmployeesByOrganization(organizationId, EDITOR_EMPLOYEE)
            val employeeEntityListFlow = query.asFlow().mapToList(coroutineContext)

            return employeeEntityListFlow.map { employeeEntityList ->
                employeeEntityList.map { it.mapToDetailsData() }
            }
        }

        override suspend fun deleteEmployee(targetId: UID) {
            employeeQueries.deleteEmployee(targetId)
        }
    }
}
