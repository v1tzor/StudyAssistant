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

package ru.aleshin.studyassistant.core.remote.datasources.employee

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.api.AppwriteApi.Employee
import ru.aleshin.studyassistant.core.api.AppwriteApi.Storage.BUCKET
import ru.aleshin.studyassistant.core.api.databases.DatabaseApi
import ru.aleshin.studyassistant.core.api.models.extractBucketIdFromFileUrl
import ru.aleshin.studyassistant.core.api.models.extractIdFromFileUrl
import ru.aleshin.studyassistant.core.api.storage.StorageApi
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.getString
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface EmployeeRemoteDataSource {

    suspend fun addOrUpdateEmployee(employee: EmployeePojo, targetUser: UID): UID
    suspend fun addOrUpdateEmployeeGroup(employees: List<EmployeePojo>, targetUser: UID)
    suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile, targetUser: UID): String
    suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<EmployeePojo?>
    suspend fun fetchAllEmployeeByOrganization(organizationId: UID?, targetUser: UID): Flow<List<EmployeePojo>>
    suspend fun deleteEmployee(targetId: UID, targetUser: UID)
    suspend fun deleteAllEmployee(targetUser: UID)
    suspend fun deleteAvatar(avatarUrl: UID, targetUser: UID)

    class Base(
        private val database: DatabaseApi,
        private val storage: StorageApi,
    ) : EmployeeRemoteDataSource {

        override suspend fun addOrUpdateEmployee(employee: EmployeePojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val employeeId = employee.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            database.upsertDocument(
                databaseId = Employee.DATABASE_ID,
                collectionId = Employee.COLLECTION_ID,
                documentId = employeeId,
                data = employee.copy(uid = employeeId),
                permissions = Permission.onlyUserData(targetUser),
                nestedType = EmployeePojo.serializer(),
            )

            return employeeId
        }

        override suspend fun addOrUpdateEmployeeGroup(employees: List<EmployeePojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            employees.forEach { employee -> addOrUpdateEmployee(employee, targetUser) }
        }

        override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile, targetUser: UID): String {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            if (!oldAvatarUrl.isNullOrBlank()) {
                deleteAvatar(oldAvatarUrl, targetUser)
            }

            val file = storage.createFile(
                bucketId = BUCKET,
                fileId = randomUUID(),
                file = file,
                permissions = Permission.avatarData(targetUser),
            )

            return file.getDownloadUrl()
        }

        override suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<EmployeePojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            if (uid.isEmpty()) return flowOf(null)

            val employeesFlow = database.getDocumentFlow(
                databaseId = Employee.DATABASE_ID,
                collectionId = Employee.COLLECTION_ID,
                documentId = uid,
                nestedType = EmployeePojo.serializer(),
            )

            return employeesFlow
        }

        override suspend fun fetchAllEmployeeByOrganization(
            organizationId: UID?,
            targetUser: UID
        ): Flow<List<EmployeePojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val employeesFlow = database.listDocumentsFlow(
                databaseId = Employee.DATABASE_ID,
                collectionId = Employee.COLLECTION_ID,
                queries = if (organizationId != null) {
                    listOf(
                        Query.equal(Employee.USER_ID, targetUser),
                        Query.equal(Employee.ORGANIZATION_ID, organizationId),
                    )
                } else {
                    listOf(Query.equal(Employee.USER_ID, targetUser))
                },
                nestedType = EmployeePojo.serializer(),
            )

            return employeesFlow
        }

        override suspend fun deleteEmployee(targetId: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val avatarUrl = database.getDocumentOrNull(
                databaseId = Employee.DATABASE_ID,
                collectionId = Employee.COLLECTION_ID,
                documentId = targetId,
                queries = listOf(Query.select(listOf(Employee.AVATAR_URL))),
                nestedType = JsonElement.serializer(),
            )?.data?.getString(Employee.AVATAR_URL)

            if (!avatarUrl.isNullOrBlank()) {
                deleteAvatar(avatarUrl, targetUser)
            }

            database.deleteDocument(
                databaseId = Employee.DATABASE_ID,
                collectionId = Employee.COLLECTION_ID,
                documentId = targetId,
            )
        }

        override suspend fun deleteAllEmployee(targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val employees = fetchAllEmployeeByOrganization(null, targetUser)
            employees.first().forEach {

                // Premium avatars are not deleted

                database.deleteDocument(
                    databaseId = Employee.DATABASE_ID,
                    collectionId = Employee.COLLECTION_ID,
                    documentId = it.uid,
                )
            }
        }

        override suspend fun deleteAvatar(avatarUrl: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            require(avatarUrl.isNotBlank()) { "Employee avatar url is empty" }

            storage.deleteFile(
                bucketId = avatarUrl.extractBucketIdFromFileUrl(),
                fileId = avatarUrl.extractIdFromFileUrl(),
            )
        }
    }
}