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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.employee.EmployeeLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.remote.datasources.billing.SubscriptionChecker
import ru.aleshin.studyassistant.core.remote.datasources.employee.EmployeeRemoteDataSource

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
class EmployeeRepositoryImpl(
    private val remoteDataSource: EmployeeRemoteDataSource,
    private val localDataSource: EmployeeLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : EmployeeRepository {

    override suspend fun addOrUpdateEmployee(employee: Employee, targetUser: UID): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateEmployee(employee.mapToRemoteData(targetUser), targetUser)
        } else {
            localDataSource.addOrUpdateEmployee(employee.mapToLocalData())
        }
    }

    override suspend fun addOrUpdateEmployeeGroup(employees: List<Employee>, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateEmployeeGroup(employees.map { it.mapToRemoteData(targetUser) }, targetUser)
        } else {
            localDataSource.addOrUpdateEmployeeGroup(employees.map { it.mapToLocalData() })
        }
    }

    override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile, targetUser: UID): String {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.uploadAvatar(oldAvatarUrl, file, targetUser)
        } else {
            checkNotNull(file.uri)
        }
    }

    override suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<Employee?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchEmployeeById(uid, targetUser).map { employeePojo -> employeePojo?.mapToDomain() }
        } else {
            localDataSource.fetchEmployeeById(uid).map { employeeEntity -> employeeEntity?.mapToDomain() }
        }
    }
    override suspend fun fetchAllEmployeeByOrganization(organizationId: UID, targetUser: UID): Flow<List<Employee>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchAllEmployeeByOrganization(organizationId, targetUser).map { employees ->
                employees.map { employeePojo -> employeePojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchAllEmployeeByOrganization(organizationId).map { employees ->
                employees.map { employeeEntity -> employeeEntity.mapToDomain() }
            }
        }
    }

    override suspend fun deleteEmployee(targetId: UID, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.deleteEmployee(targetId, targetUser)
        } else {
            localDataSource.deleteEmployee(targetId)
        }
    }

    override suspend fun deleteAvatar(avatarUrl: String, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        if (isSubscriber) {
            remoteDataSource.deleteAvatar(avatarUrl, targetUser)
        }
    }

    override suspend fun deleteAllEmployee(targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.deleteAllEmployee(targetUser)
        } else {
            localDataSource.deleteAllEmployee()
        }
    }

    override suspend fun transferData(direction: DataTransferDirection, targetUser: UID) {
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allEmployee = remoteDataSource.fetchAllEmployeeByOrganization(
                    organizationId = null,
                    targetUser = targetUser,
                ).let { employeesFlow ->
                    return@let employeesFlow.first().map { it.mapToDomain().mapToLocalData() }
                }
                localDataSource.deleteAllEmployee()
                localDataSource.addOrUpdateEmployeeGroup(allEmployee)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allSchedules = localDataSource.fetchAllEmployeeByOrganization(
                    organizationId = null,
                ).let { employeesFlow ->
                    return@let employeesFlow.first().map { it.mapToDomain().mapToRemoteData(targetUser) }
                }
                remoteDataSource.deleteAllEmployee(targetUser)
                remoteDataSource.addOrUpdateEmployeeGroup(allSchedules, targetUser)
            }
        }
    }
}