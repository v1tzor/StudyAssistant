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

package ru.aleshin.studyassistant.core.remote.datasources.subjects

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.exists
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.UserData
import ru.aleshin.studyassistant.core.remote.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
interface SubjectsRemoteDataSource {

    suspend fun addOrUpdateSubject(subject: SubjectPojo, targetUser: UID): UID
    suspend fun addOrUpdateSubjectsGroup(subjects: List<SubjectPojo>, targetUser: UID)
    suspend fun fetchSubjectById(uid: UID, targetUser: UID): Flow<SubjectDetailsPojo?>
    suspend fun fetchAllSubjectsByOrganization(organizationId: UID, targetUser: UID): Flow<List<SubjectDetailsPojo>>
    suspend fun fetchAllSubjectsByNames(names: List<String>, targetUser: UID): List<SubjectDetailsPojo>
    suspend fun fetchSubjectsByEmployee(employeeId: UID, targetUser: UID): Flow<List<SubjectDetailsPojo>>
    suspend fun deleteSubject(targetId: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : SubjectsRemoteDataSource {

        override suspend fun addOrUpdateSubject(subject: SubjectPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.SUBJECTS)

            return database.runTransaction {
                val isExist = subject.uid.isNotEmpty() && reference.document(subject.uid).exists()
                if (isExist) {
                    reference.document(subject.uid).set(data = subject)
                    return@runTransaction subject.uid
                } else {
                    val uid = reference.add(subject).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun addOrUpdateSubjectsGroup(subjects: List<SubjectPojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.SUBJECTS)

            database.batch().apply {
                subjects.forEach { subject ->
                    val uid = subject.uid.takeIf { it.isNotBlank() } ?: randomUUID()
                    set(reference.document(uid), subject.copy(uid = uid))
                }
                return@apply commit()
            }
        }

        override suspend fun fetchSubjectById(uid: UID, targetUser: UID): Flow<SubjectDetailsPojo?> {
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
                val employee = employeeReference?.snapshotGet()?.data(serializer<EmployeePojo?>())

                return@map subjectPojo?.mapToDetails(employee = employee)
            }
        }

        override suspend fun fetchAllSubjectsByOrganization(
            organizationId: UID,
            targetUser: UID
        ): Flow<List<SubjectDetailsPojo>> {
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
                    val employee = employeeReference?.snapshotGet()?.data(serializer<EmployeePojo?>())

                    subjectPojo.mapToDetails(employee = employee)
                }
            }
        }

        override suspend fun fetchAllSubjectsByNames(
            names: List<String>,
            targetUser: UID
        ): List<SubjectDetailsPojo> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val subjectsReference = userDataRoot.collection(UserData.SUBJECTS).where {
                UserData.SUBJECT_NAME inArray names
            }

            val subjectPojoList = subjectsReference.snapshotGet().map { snapshot ->
                snapshot.data(serializer<SubjectPojo>())
            }

            return subjectPojoList.map { subjectPojo ->
                val employeeReferenceRoot = userDataRoot.collection(UserData.EMPLOYEE)

                val employeeReference = subjectPojo.teacherId?.let { employeeReferenceRoot.document(it) }
                val employee = employeeReference?.snapshotGet()?.data(serializer<EmployeePojo?>())

                subjectPojo.mapToDetails(employee = employee)
            }
        }

        override suspend fun fetchSubjectsByEmployee(
            employeeId: UID,
            targetUser: UID
        ): Flow<List<SubjectDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val subjectsReference = userDataRoot.collection(UserData.SUBJECTS).where {
                UserData.SUBJECT_TEACHER_ID equalTo employeeId
            }.orderBy(UserData.SUBJECT_TEACHER_ID)

            val subjectPojoListFlow = subjectsReference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<SubjectPojo>()) }
            }

            return subjectPojoListFlow.map { subjectPojoList ->
                subjectPojoList.map { subjectPojo ->
                    val employeeReferenceRoot = userDataRoot.collection(UserData.EMPLOYEE)

                    val employeeReference = subjectPojo.teacherId?.let { employeeReferenceRoot.document(it) }
                    val employee = employeeReference?.snapshotGet()?.data(serializer<EmployeePojo?>())

                    subjectPojo.mapToDetails(employee = employee)
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