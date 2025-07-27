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

import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.datasource.employee.EmployeeLocalDataSource.OfflineStorage
import ru.aleshin.studyassistant.core.database.datasource.employee.EmployeeLocalDataSource.SyncStorage
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToEntity
import ru.aleshin.studyassistant.core.database.models.employee.BaseEmployeeEntity
import ru.aleshin.studyassistant.core.database.utils.CombinedLocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalMultipleDocumentsCommands
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface EmployeeLocalDataSource : CombinedLocalDataSource<BaseEmployeeEntity, OfflineStorage, SyncStorage> {

    interface Commands : LocalMultipleDocumentsCommands<BaseEmployeeEntity> {

        suspend fun fetchAllEmployeeByOrganization(organizationId: UID?): Flow<List<BaseEmployeeEntity>>
        suspend fun fetchAllEmployees(): Flow<List<BaseEmployeeEntity>>

        abstract class Abstract(
            isCacheSource: Boolean,
            private val employeeQueries: EmployeeQueries,
            private val coroutineManager: CoroutineManager,
        ) : Commands {

            private val coroutineContext: CoroutineContext
                get() = coroutineManager.backgroundDispatcher

            private val isCacheData = if (isCacheSource) 1L else 0L

            override suspend fun addOrUpdateItem(item: BaseEmployeeEntity) {
                val uid = item.uid.ifEmpty { randomUUID() }
                val updatedItem = item.copy(uid = uid, isCacheData = isCacheData).mapToEntity()
                employeeQueries.addOrUpdateEmployee(updatedItem).await()
            }

            override suspend fun addOrUpdateItems(items: List<BaseEmployeeEntity>) {
                items.forEach { item -> addOrUpdateItem(item) }
            }

            override suspend fun fetchItemById(id: String): Flow<BaseEmployeeEntity?> {
                val query = employeeQueries.fetchEmployeeById(id, isCacheData)
                return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchItemsById(ids: List<String>): Flow<List<BaseEmployeeEntity>> {
                val query = employeeQueries.fetchEmployeesById(ids, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchAllEmployeeByOrganization(organizationId: UID?): Flow<List<BaseEmployeeEntity>> {
                val query = if (organizationId != null) {
                    employeeQueries.fetchEmployeesByOrganization(organizationId, isCacheData)
                } else {
                    employeeQueries.fetchAllEmployees(isCacheData)
                }
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchAllEmployees(): Flow<List<BaseEmployeeEntity>> {
                val query = employeeQueries.fetchAllEmployees(isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchAllMetadata(): List<MetadataModel> {
                val query = employeeQueries.fetchEmptyEmployees()
                return query.awaitAsList().map { entity ->
                    MetadataModel(entity.uid, entity.updated_at)
                }
            }

            override suspend fun deleteItemsById(ids: List<String>) {
                employeeQueries.deleteEmployeesById(ids, isCacheData).await()
            }

            override suspend fun deleteAllItems() {
                employeeQueries.deleteAllEmployee(isCacheData).await()
            }
        }
    }

    interface OfflineStorage : LocalDataSource.OnlyOffline, Commands {

        class Base(
            employeeQueries: EmployeeQueries,
            coroutineManager: CoroutineManager,
        ) : OfflineStorage, Commands.Abstract(
            isCacheSource = false,
            employeeQueries,
            coroutineManager
        )
    }

    interface SyncStorage : LocalDataSource.FullSynced.MultipleDocuments<BaseEmployeeEntity>, Commands {

        class Base(
            employeeQueries: EmployeeQueries,
            coroutineManager: CoroutineManager,
        ) : SyncStorage, Commands.Abstract(
            isCacheSource = true,
            employeeQueries,
            coroutineManager
        )
    }

    class Base(
        private val offlineStorage: OfflineStorage,
        private val syncStorage: SyncStorage,
    ) : EmployeeLocalDataSource {
        override fun offline() = offlineStorage
        override fun sync() = syncStorage
    }
}