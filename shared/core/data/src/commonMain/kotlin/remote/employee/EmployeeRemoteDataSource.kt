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

package remote.employee

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import exceptions.FirebaseUserException
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import models.users.EmployeeDetailsData
import remote.StudyAssistantFirestore.UserData

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface EmployeeRemoteDataSource {

    suspend fun addOrUpdateEmployee(employee: EmployeeDetailsData, targetUser: UID): UID
    suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<EmployeeDetailsData?>
    suspend fun fetchAllEmployeeByOrganization(organizationId: UID, targetUser: UID): Flow<List<EmployeeDetailsData>>
    suspend fun deleteEmployee(targetId: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : EmployeeRemoteDataSource {

        override suspend fun addOrUpdateEmployee(employee: EmployeeDetailsData, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE)

            return database.runTransaction {
                val isExist = employee.uid.isNotEmpty() && reference.document(employee.uid).get().exists
                if (isExist) {
                    reference.document(employee.uid).set(data = employee)
                    return@runTransaction employee.uid
                } else {
                    val uid = reference.add(employee).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun fetchEmployeeById(uid: UID, targetUser: UID): Flow<EmployeeDetailsData?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            if (uid.isEmpty()) return flowOf(null)
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE).document(uid)

            val employeeFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<EmployeeDetailsData?>())
            }

            return employeeFlow
        }

        override suspend fun fetchAllEmployeeByOrganization(
            organizationId: UID,
            targetUser: UID
        ): Flow<List<EmployeeDetailsData>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE).where {
                UserData.ORGANIZATION_ID equalTo organizationId
            }

            val employeeFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<EmployeeDetailsData>()) }
            }

            return employeeFlow
        }

        override suspend fun deleteEmployee(targetId: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.EMPLOYEE).document(targetId)

            return reference.delete()
        }
    }
}
