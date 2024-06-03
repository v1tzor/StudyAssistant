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

import database.employee.EmployeeLocalDataSource
import entities.employee.Employee
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mappers.users.mapToData
import mappers.users.mapToDomain
import payments.SubscriptionChecker
import remote.employee.EmployeeRemoteDataSource

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
            remoteDataSource.addOrUpdateEmployee(employee.mapToData(), targetUser)
        } else {
            localDataSource.addOrUpdateEmployee(employee.mapToData())
        }
    }

    override suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<Employee?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val employeeFlow = if (isSubscriber) {
            remoteDataSource.fetchEmployeeById(uid, targetUser)
        } else {
            localDataSource.fetchEmployeeById(uid)
        }

        return employeeFlow.map { employeeData ->
            employeeData?.mapToDomain()
        }
    }
    override suspend fun fetchAllEmployeeByOrganization(organizationId: UID, targetUser: UID): Flow<List<Employee>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val employeeListFlow = if (isSubscriber) {
            remoteDataSource.fetchAllEmployeeByOrganization(organizationId, targetUser)
        } else {
            localDataSource.fetchAllEmployeeByOrganization(organizationId)
        }

        return employeeListFlow.map { employeeListData ->
            employeeListData.map { it.mapToDomain() }
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
}