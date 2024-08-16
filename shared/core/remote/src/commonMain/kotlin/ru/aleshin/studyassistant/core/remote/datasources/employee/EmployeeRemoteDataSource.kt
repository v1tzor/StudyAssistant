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
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.exists
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.Storage
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.UserData
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface EmployeeRemoteDataSource {

    suspend fun addOrUpdateEmployee(employee: EmployeePojo, targetUser: UID): UID
    suspend fun addOrUpdateEmployeeGroup(employees: List<EmployeePojo>, targetUser: UID)
    suspend fun uploadAvatar(uid: UID, file: File, targetUser: UID): String
    suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<EmployeePojo?>
    suspend fun fetchAllEmployeeByOrganization(organizationId: UID, targetUser: UID): Flow<List<EmployeePojo>>
    suspend fun deleteEmployee(targetId: UID, targetUser: UID)
    suspend fun deleteAvatar(uid: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
        private val storage: FirebaseStorage,
    ) : EmployeeRemoteDataSource {

        override suspend fun addOrUpdateEmployee(employee: EmployeePojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE)

            return database.runTransaction {
                val isExist = employee.uid.isNotEmpty() && reference.document(employee.uid).exists()
                if (isExist) {
                    reference.document(employee.uid).set(employee)
                    return@runTransaction employee.uid
                } else {
                    val uid = reference.add(employee).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun addOrUpdateEmployeeGroup(employees: List<EmployeePojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
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

        override suspend fun uploadAvatar(uid: UID, file: File, targetUser: UID): String {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val storageRoot = storage.reference.child(targetUser).child(Storage.EMPLOYEE).child(uid)

            val avatarReference = storageRoot.child(Storage.EMPLOYEE_AVATAR).child(Storage.EMPLOYEE_AVATAR_FILE)
            avatarReference.putFile(file)

            return avatarReference.getDownloadUrl()
        }

        override suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<EmployeePojo?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            if (uid.isEmpty()) return flowOf(null)
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE).document(uid)

            val employeeFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<EmployeePojo?>())
            }

            return employeeFlow
        }

        override suspend fun fetchAllEmployeeByOrganization(
            organizationId: UID,
            targetUser: UID
        ): Flow<List<EmployeePojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE).where {
                UserData.ORGANIZATION_ID equalTo organizationId
            }

            val employeeFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<EmployeePojo>()) }
            }

            return employeeFlow
        }

        override suspend fun deleteEmployee(targetId: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE).document(targetId)

            return reference.delete()
        }

        override suspend fun deleteAvatar(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val storageRoot = storage.reference.child(targetUser).child(Storage.EMPLOYEE).child(uid)

            val avatarReference = storageRoot.child(Storage.EMPLOYEE_AVATAR).child(Storage.EMPLOYEE_AVATAR_FILE)

            return avatarReference.delete()
        }
    }
}