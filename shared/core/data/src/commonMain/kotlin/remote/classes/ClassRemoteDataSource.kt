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

package remote.classes

import dev.gitlive.firebase.firestore.FirebaseFirestore
import exceptions.FirebaseUserException
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import mappers.subjects.mapToDetailsData
import mappers.tasks.mapToDetailsDate
import mappers.tasks.mapToRemoteDate
import models.classes.ClassDetailsData
import models.classes.ClassPojo
import models.organizations.OrganizationShortData
import models.subjects.SubjectPojo
import models.users.EmployeeDetailsData
import remote.StudyAssistantFirestore.UserData

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface ClassRemoteDataSource {

    suspend fun fetchClassById(uid: UID, targetUser: UID): Flow<ClassDetailsData?>
    suspend fun addOrUpdateClass(scheduleClass: ClassDetailsData, targetUser: UID): UID
    suspend fun deleteClass(uid: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : ClassRemoteDataSource {

        override suspend fun fetchClassById(uid: UID, targetUser: UID): Flow<ClassDetailsData?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.CLASSES).document(uid)

            val classPojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<ClassPojo?>())
            }

            return classPojoFlow.map { classPojo ->
                if (classPojo == null) return@map null
                val organizationId = classPojo.organizationId

                val employeeReferenceRoot = userDataRoot.collection(UserData.EMPLOYEE)

                val organizationReference = userDataRoot.collection(UserData.ORGANIZATIONS).document(organizationId)
                val subjectReference = classPojo.subjectId?.let { subjectId ->
                    userDataRoot.collection(UserData.SUBJECTS).document(subjectId)
                }

                val organization = organizationReference.get().data<OrganizationShortData>()
                val subject = subjectReference?.get()?.data<SubjectPojo>().let { subjectPojo ->
                    val employeeReference = subjectPojo?.teacher?.let { employeeReferenceRoot.document(it) }
                    val employee = employeeReference?.get()?.data(serializer<EmployeeDetailsData?>())
                    subjectPojo?.mapToDetailsData(employee)
                }
                val employee = classPojo.teacherId?.let { teacherId ->
                    val employeeReference = employeeReferenceRoot.document(teacherId)
                    employeeReference.get().data(serializer<EmployeeDetailsData?>())
                }

                classPojo.mapToDetailsDate(
                    organization = organization,
                    subject = subject,
                    employee = employee,
                )
            }
        }

        override suspend fun addOrUpdateClass(scheduleClass: ClassDetailsData, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val schedulePojo = scheduleClass.mapToRemoteDate()

            val reference = userDataRoot.collection(UserData.HOMEWORKS)

            return database.runTransaction {
                val isExist = schedulePojo.uid.isNotEmpty() && reference.document(schedulePojo.uid).get().exists
                if (isExist) {
                    reference.document(schedulePojo.uid).set(data = schedulePojo, merge = true)
                    return@runTransaction schedulePojo.uid
                } else {
                    val uid = reference.add(schedulePojo).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun deleteClass(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.CLASSES).document(uid)

            return reference.delete()
        }
    }
}
