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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.datasources.AvatarLocalDataSource
import ru.aleshin.studyassistant.core.data.datasources.AvatarType
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.database.datasource.employee.EmployeeLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
class EmployeeRepositoryImpl(
    private val localDataSource: EmployeeLocalDataSource,
    private val avatarLocalDataSource: AvatarLocalDataSource,
) : EmployeeRepository {

    override suspend fun addOrUpdateEmployee(employee: Employee): UID {
        val updatedEmployee = employee.copy(uid = employee.uid.ifBlank { randomUUID() })
        localDataSource.addOrUpdateEmployee(updatedEmployee.mapToLocalData())
        return updatedEmployee.uid
    }

    override suspend fun addOrUpdateEmployeeGroup(employees: List<Employee>) {
        val updatedEmployees = employees.map { employee ->
            employee.copy(uid = employee.uid.ifBlank { randomUUID() })
        }
        localDataSource.addOrUpdateEmployees(updatedEmployees.map { it.mapToLocalData() })
    }

    override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile): String {
        val avatar = avatarLocalDataSource.saveAvatar(AvatarType.EMPLOYEE, file)
        if (oldAvatarUrl != null && oldAvatarUrl != avatar) {
            avatarLocalDataSource.deleteAvatar(oldAvatarUrl)
        }
        return avatar
    }

    override suspend fun fetchEmployeeById(uid: UID): Flow<Employee?> {
        return localDataSource.fetchEmployeeById(uid).map { employee -> employee?.mapToDomain() }
    }

    override suspend fun fetchAllEmployeeByOrganization(organizationId: UID): Flow<List<Employee>> {
        return localDataSource.fetchAllEmployeeByOrganization(organizationId).map { employees ->
            employees.map { employee -> employee.mapToDomain() }
        }
    }

    override suspend fun deleteEmployee(targetId: UID) {
        localDataSource.deleteEmployeesByIds(listOf(targetId))
    }

    override suspend fun deleteAvatar(avatarUrl: String) {
        avatarLocalDataSource.deleteAvatar(avatarUrl)
    }
}
