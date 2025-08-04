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

@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.users.convertToLocal
import ru.aleshin.studyassistant.core.data.mappers.users.convertToRemote
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.employee.EmployeeLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.managers.sync.EmployeeSourceSyncManager.Companion.EMPLOYEE_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.remote.datasources.employee.EmployeeRemoteDataSource

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
class EmployeeRepositoryImpl(
    private val remoteDataSource: EmployeeRemoteDataSource,
    private val localDataSource: EmployeeLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
    private val userSessionProvider: UserSessionProvider,
    private val resultSyncHandler: RemoteResultSyncHandler,
) : EmployeeRepository {

    override suspend fun addOrUpdateEmployee(employee: Employee): UID {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModel = employee.copy(uid = employee.uid.ifBlank { randomUUID() })

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItem(upsertModel.mapToLocalData())
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModel.mapToRemoteData(userId = currentUser),
                type = OfflineChangeType.UPSERT,
                sourceKey = EMPLOYEE_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItem(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItem(upsertModel.mapToLocalData())
        }

        return upsertModel.uid
    }

    override suspend fun addOrUpdateEmployeeGroup(employees: List<Employee>) {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModels = employees.map { it.copy(uid = it.uid.ifBlank { randomUUID() }) }

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModels.map { it.mapToRemoteData(currentUser) },
                type = OfflineChangeType.UPSERT,
                sourceKey = EMPLOYEE_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItems(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
        }
    }

    override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile): String {
        return remoteDataSource.uploadAvatar(oldAvatarUrl, file)
    }

    override suspend fun fetchEmployeeById(uid: UID): Flow<Employee?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchItemById(uid).map { it?.mapToDomain() }
            } else {
                localDataSource.offline().fetchItemById(uid).map { it?.mapToDomain() }
            }
        }
    }
    override suspend fun fetchAllEmployeeByOrganization(organizationId: UID): Flow<List<Employee>> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchAllEmployeeByOrganization(organizationId).map { employees ->
                    employees.map { employeeEntity -> employeeEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchAllEmployeeByOrganization(organizationId).map { employees ->
                    employees.map { employeeEntity -> employeeEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun deleteEmployee(targetId: UID) {
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        return if (isSubscriber) {
            localDataSource.sync().deleteItemsById(listOf(targetId))
            resultSyncHandler.executeOrAddToQueue(
                documentId = targetId,
                type = OfflineChangeType.DELETE,
                sourceKey = EMPLOYEE_SOURCE_KEY,
            ) {
                remoteDataSource.deleteItemById(targetId)
            }
        } else {
            localDataSource.offline().deleteItemsById(listOf(targetId))
        }
    }

    override suspend fun deleteAvatar(avatarUrl: String) {
        remoteDataSource.deleteAvatar(avatarUrl)
    }

    override suspend fun transferData(direction: DataTransferDirection, mergeData: Boolean) {
        val currentUser = userSessionProvider.getCurrentUserId()
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allEmployeesFlow = remoteDataSource.fetchAllItems(currentUser)
                val employees = allEmployeesFlow.first().map { it.convertToLocal() }

                if (!mergeData) {
                    localDataSource.offline().deleteAllItems()
                }
                localDataSource.offline().addOrUpdateItems(employees)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allEmployees = localDataSource.offline().fetchAllEmployees().first()
                val employeesRemote = allEmployees.map { it.convertToRemote(currentUser) }

                if (!mergeData) {
                    remoteDataSource.deleteAllItems(currentUser)
                }
                remoteDataSource.addOrUpdateItems(employeesRemote)

                localDataSource.sync().deleteAllItems()
                localDataSource.sync().addOrUpdateItems(allEmployees)
            }
        }
    }
}