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

import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.uriString
import ru.aleshin.studyassistant.core.common.payments.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.employee.EmployeeLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
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
            remoteDataSource.addOrUpdateEmployee(employee.mapToRemoteData(), targetUser)
        } else {
            localDataSource.addOrUpdateEmployee(employee.mapToLocalData())
        }
    }

    override suspend fun uploadAvatar(uid: UID, file: File, targetUser: UID): String {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.uploadAvatar(uid, file, targetUser)
        } else {
            file.uriString()
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

    override suspend fun deleteAvatar(uid: UID, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        if (isSubscriber) {
            remoteDataSource.deleteAvatar(uid, targetUser)
        }
    }
}