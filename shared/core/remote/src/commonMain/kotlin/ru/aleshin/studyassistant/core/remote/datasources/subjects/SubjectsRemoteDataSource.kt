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

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.deleteAll
import ru.aleshin.studyassistant.core.common.extensions.observeCollectionMapByField
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotFlowGet
import ru.aleshin.studyassistant.core.common.extensions.snapshotListFlowGet
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
    suspend fun fetchAllSubjectsByOrganization(organizationId: UID?, targetUser: UID): Flow<List<SubjectDetailsPojo>>
    suspend fun fetchAllSubjectsByNames(names: List<String>, targetUser: UID): List<SubjectDetailsPojo>
    suspend fun fetchSubjectsByEmployee(employeeId: UID, targetUser: UID): Flow<List<SubjectDetailsPojo>>
    suspend fun deleteSubject(targetId: UID, targetUser: UID)
    suspend fun deleteAllSubjects(targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : SubjectsRemoteDataSource {

        override suspend fun addOrUpdateSubject(subject: SubjectPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.SUBJECTS)

            val subjectId = subject.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            return reference.document(subjectId).set(subject.copy(uid = subjectId)).let {
                return@let subjectId
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

            val reference = userDataRoot.collection(UserData.SUBJECTS).document(uid)

            return reference.snapshotFlowGet<SubjectPojo>().flatMapToDetails(userDataRoot)
        }

        override suspend fun fetchAllSubjectsByOrganization(
            organizationId: UID?,
            targetUser: UID
        ): Flow<List<SubjectDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val subjectsReference = if (organizationId != null) {
                userDataRoot.collection(UserData.SUBJECTS).where {
                    UserData.ORGANIZATION_ID equalTo organizationId
                }
            } else {
                userDataRoot.collection(UserData.SUBJECTS)
            }

            val subjectsFlow = subjectsReference
                .snapshotListFlowGet<SubjectPojo>()
                .flatMapListToDetails(userDataRoot)

            return subjectsFlow
        }

        override suspend fun fetchAllSubjectsByNames(
            names: List<String>,
            targetUser: UID
        ): List<SubjectDetailsPojo> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val subjectsFlow = userDataRoot.collection(UserData.SUBJECTS)
                .where { UserData.SUBJECT_NAME inArray names }
                .snapshotListFlowGet<SubjectPojo>()
                .flatMapListToDetails(userDataRoot)

            return subjectsFlow.first()
        }

        override suspend fun fetchSubjectsByEmployee(
            employeeId: UID,
            targetUser: UID
        ): Flow<List<SubjectDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val subjectsFlow = userDataRoot.collection(UserData.SUBJECTS)
                .where { UserData.SUBJECT_TEACHER_ID equalTo employeeId }
                .orderBy(UserData.SUBJECT_TEACHER_ID)
                .snapshotListFlowGet<SubjectPojo>()
                .flatMapListToDetails(userDataRoot)

            return subjectsFlow
        }

        override suspend fun deleteSubject(targetId: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.SUBJECTS).document(targetId)

            return reference.delete()
        }

        override suspend fun deleteAllSubjects(targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.SUBJECTS)

            database.deleteAll(reference)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<SubjectPojo>>.flatMapListToDetails(
            userDataRoot: DocumentReference,
        ): Flow<List<SubjectDetailsPojo>> = flatMapLatest { subjects ->
            if (subjects.isEmpty()) {
                flowOf(emptyList())
            } else {
                val employeesMapFlow = userDataRoot.collection(UserData.EMPLOYEE)
                    .observeCollectionMapByField<EmployeePojo>(
                        ids = subjects.map { it.organizationId }.toSet(),
                        fieldName = UserData.ORGANIZATION_ID,
                        associateKey = { it.uid }
                    )

                combine(
                    flowOf(subjects),
                    employeesMapFlow,
                ) { subjectsList, employeesMap ->
                    subjectsList.map { subject ->
                        subject.mapToDetails(employee = employeesMap[subject.teacherId])
                    }
                }
            }
        }

        private fun Flow<SubjectPojo?>.flatMapToDetails(
            userDataRoot: DocumentReference,
        ): Flow<SubjectDetailsPojo?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails(userDataRoot)
                .map { it.getOrNull(0) }
        }
    }
}