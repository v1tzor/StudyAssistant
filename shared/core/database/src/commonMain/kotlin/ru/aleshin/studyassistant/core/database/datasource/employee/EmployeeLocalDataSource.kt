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

package ru.aleshin.studyassistant.core.database.datasource.employee

import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToEntity
import ru.aleshin.studyassistant.core.database.models.employee.BaseEmployeeEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface EmployeeLocalDataSource {

    suspend fun addOrUpdateEmployee(item: BaseEmployeeEntity)
    suspend fun addOrUpdateEmployees(items: List<BaseEmployeeEntity>)
    suspend fun fetchEmployeeById(id: String): Flow<BaseEmployeeEntity?>
    suspend fun fetchAllEmployeeByOrganization(organizationId: UID?): Flow<List<BaseEmployeeEntity>>
    suspend fun deleteEmployeesByIds(ids: List<String>)

    class Base(
        private val employeeQueries: EmployeeQueries,
        private val coroutineManager: CoroutineManager,
    ) : EmployeeLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher

        override suspend fun addOrUpdateEmployee(item: BaseEmployeeEntity) {
            val uid = item.uid.ifEmpty { randomUUID() }
            val updatedItem = item.copy(uid = uid).mapToEntity()
            employeeQueries.addOrUpdateEmployee(updatedItem)
        }

        override suspend fun addOrUpdateEmployees(items: List<BaseEmployeeEntity>) {
            items.forEach { item -> addOrUpdateEmployee(item) }
        }

        override suspend fun fetchEmployeeById(id: String): Flow<BaseEmployeeEntity?> {
            val query = employeeQueries.fetchEmployeeById(id)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchAllEmployeeByOrganization(organizationId: UID?): Flow<List<BaseEmployeeEntity>> {
            val query = if (organizationId != null) {
                employeeQueries.fetchEmployeesByOrganization(organizationId)
            } else {
                employeeQueries.fetchAllEmployees()
            }
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun deleteEmployeesByIds(ids: List<String>) {
            employeeQueries.deleteEmployeesById(ids)
        }
    }
}
