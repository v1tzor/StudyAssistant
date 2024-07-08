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

package ru.aleshin.studyassistant.core.database.datasource.employee

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface EmployeeLocalDataSource {

    suspend fun addOrUpdateEmployee(employee: EmployeeEntity): UID
    suspend fun fetchEmployeeById(uid: UID): Flow<EmployeeEntity?>
    suspend fun fetchAllEmployeeByOrganization(organizationId: UID): Flow<List<EmployeeEntity>>
    suspend fun deleteEmployee(targetId: UID)

    class Base(
        private val employeeQueries: EmployeeQueries,
        private val coroutineManager: CoroutineManager,
    ) : EmployeeLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateEmployee(employee: EmployeeEntity): UID {
            val uid = employee.uid.ifEmpty { randomUUID() }
            employeeQueries.addOrUpdateEmployee(employee.copy(uid = uid))

            return uid
        }

        override suspend fun fetchEmployeeById(uid: UID): Flow<EmployeeEntity?> {
            val query = employeeQueries.fetchEmployeeById(uid)
            return query.asFlow().mapToOneOrNull(coroutineContext)
        }

        override suspend fun fetchAllEmployeeByOrganization(organizationId: UID): Flow<List<EmployeeEntity>> {
            val query = employeeQueries.fetchEmployeesByOrganization(organizationId)
            return query.asFlow().mapToList(coroutineContext)
        }

        override suspend fun deleteEmployee(targetId: UID) {
            employeeQueries.deleteEmployee(targetId)
        }
    }
}