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

package repositories

import entities.employee.Employee
import functional.UID
import kotlinx.coroutines.flow.Flow

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface EmployeeRepository {
    suspend fun addOrUpdateEmployee(employee: Employee, targetUser: UID): UID
    suspend fun fetchAllEmployeeByOrganization(organizationId: UID, targetUser: UID): Flow<List<Employee>>
    suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<Employee?>
    suspend fun deleteEmployee(targetId: UID, targetUser: UID)
}
