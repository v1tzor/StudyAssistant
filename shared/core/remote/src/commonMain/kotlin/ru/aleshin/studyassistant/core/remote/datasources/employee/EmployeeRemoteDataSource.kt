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

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.deleteAll
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotFlowGet
import ru.aleshin.studyassistant.core.common.extensions.snapshotListFlowGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.remote.appwrite.storage.AppwriteStorage
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Permission
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Storage.BUCKET
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.UserData
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface EmployeeRemoteDataSource {

    suspend fun addOrUpdateEmployee(employee: EmployeePojo, targetUser: UID): UID
    suspend fun addOrUpdateEmployeeGroup(employees: List<EmployeePojo>, targetUser: UID)
    suspend fun uploadAvatar(uid: UID, file: InputFile, targetUser: UID): String
    suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<EmployeePojo?>
    suspend fun fetchAllEmployeeByOrganization(organizationId: UID?, targetUser: UID): Flow<List<EmployeePojo>>
    suspend fun deleteEmployee(targetId: UID, targetUser: UID)
    suspend fun deleteAllEmployee(targetUser: UID)
    suspend fun deleteAvatar(uid: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
        private val storage: AppwriteStorage,
    ) : EmployeeRemoteDataSource {

        override suspend fun addOrUpdateEmployee(employee: EmployeePojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE)

            val employeeId = employee.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            return reference.document(employeeId).set(employee.copy(uid = employeeId)).let {
                return@let employeeId
            }
        }

        override suspend fun addOrUpdateEmployeeGroup(employees: List<EmployeePojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE)

            database.batch().apply {
                employees.forEach { employee ->
                    val uid = employee.uid.takeIf { it.isNotBlank() } ?: randomUUID()
                    set(reference.document(uid), employee.copy(uid = uid))
                }
                return@apply commit()
            }
        }

        override suspend fun uploadAvatar(uid: UID, file: InputFile, targetUser: UID): String {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val file = storage.createFile(
                bucketId = BUCKET,
                fileId = randomUUID(),
                fileBytes = file.fileBytes,
                filename = file.filename,
                mimeType = file.mimeType,
                permissions = Permission.onlyUsersVisibleData(targetUser),
            )

            return file.getDownloadUrl()
        }

        override suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<EmployeePojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            if (uid.isEmpty()) return flowOf(null)
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE).document(uid)

            return reference.snapshotFlowGet<EmployeePojo>()
        }

        override suspend fun fetchAllEmployeeByOrganization(
            organizationId: UID?,
            targetUser: UID
        ): Flow<List<EmployeePojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = if (organizationId != null) {
                userDataRoot.collection(UserData.EMPLOYEE).where {
                    UserData.ORGANIZATION_ID equalTo organizationId
                }
            } else {
                userDataRoot.collection(UserData.EMPLOYEE)
            }

            val employeeFlow = reference.snapshotListFlowGet<EmployeePojo>()

            return employeeFlow
        }

        override suspend fun deleteEmployee(targetId: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE).document(targetId)

            return reference.delete()
        }

        override suspend fun deleteAllEmployee(targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE)

            database.deleteAll(reference)
        }

        override suspend fun deleteAvatar(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            storage.deleteFile(BUCKET, uid)
        }
    }
}