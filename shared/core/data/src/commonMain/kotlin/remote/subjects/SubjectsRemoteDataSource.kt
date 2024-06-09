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

package remote.subjects

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import exceptions.FirebaseUserException
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import mappers.subjects.mapToDetailsData
import mappers.subjects.mapToRemoteData
import models.subjects.SubjectDetailsData
import models.subjects.SubjectPojo
import models.users.EmployeeDetailsData
import remote.StudyAssistantFirestore.UserData

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface SubjectsRemoteDataSource {

    suspend fun addOrUpdateSubject(subject: SubjectDetailsData, targetUser: UID): UID
    suspend fun fetchSubjectById(uid: UID, targetUser: UID): Flow<SubjectDetailsData?>
    suspend fun fetchAllSubjectsByOrganization(organizationId: UID, targetUser: UID): Flow<List<SubjectDetailsData>>
    suspend fun deleteSubject(targetId: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : SubjectsRemoteDataSource {

        override suspend fun addOrUpdateSubject(subject: SubjectDetailsData, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val subjectPojo = subject.mapToRemoteData()

            val reference = userDataRoot.collection(UserData.SUBJECTS)

            return database.runTransaction {
                val isExist = subjectPojo.uid.isNotEmpty() && reference.document(subjectPojo.uid).get().exists
                if (isExist) {
                    reference.document(subjectPojo.uid).set(data = subjectPojo)
                    return@runTransaction subjectPojo.uid
                } else {
                    val uid = reference.add(subjectPojo).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun fetchSubjectById(uid: UID, targetUser: UID): Flow<SubjectDetailsData?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            if (uid.isEmpty()) return flowOf(null)
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val subjectsReference = userDataRoot.collection(UserData.SUBJECTS).document(uid)

            val subjectsPojoFlow = subjectsReference.snapshots.map { snapshot ->
                snapshot.data(serializer<SubjectPojo?>())
            }

            return subjectsPojoFlow.map { subjectPojo ->
                val employeeReferenceRoot = userDataRoot.collection(UserData.EMPLOYEE)

                val employeeReference = subjectPojo?.teacherId?.let { employeeReferenceRoot.document(it) }
                val employee = employeeReference?.get()?.data(serializer<EmployeeDetailsData?>())

                return@map subjectPojo?.mapToDetailsData(employee = employee)
            }
        }

        override suspend fun fetchAllSubjectsByOrganization(
            organizationId: UID,
            targetUser: UID
        ): Flow<List<SubjectDetailsData>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val subjectsReference = userDataRoot.collection(UserData.SUBJECTS).where {
                UserData.ORGANIZATION_ID equalTo organizationId
            }

            val subjectPojoListFlow = subjectsReference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<SubjectPojo>()) }
            }

            return subjectPojoListFlow.map { subjectPojoList ->
                subjectPojoList.map { subjectPojo ->
                    val employeeReferenceRoot = userDataRoot.collection(UserData.EMPLOYEE)

                    val employeeReference = subjectPojo.teacherId?.let { employeeReferenceRoot.document(it) }
                    val employee = employeeReference?.get()?.data(serializer<EmployeeDetailsData?>())

                    subjectPojo.mapToDetailsData(employee = employee)
                }
            }
        }

        override suspend fun deleteSubject(targetId: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.SUBJECTS).document(targetId)

            return reference.delete()
        }
    }
}
