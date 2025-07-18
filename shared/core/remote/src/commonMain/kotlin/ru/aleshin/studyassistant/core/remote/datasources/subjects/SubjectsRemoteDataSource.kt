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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.appwrite.databases.DatabaseService
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Permission
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Query
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Employee
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Subjects
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
        private val database: DatabaseService,
    ) : SubjectsRemoteDataSource {

        override suspend fun addOrUpdateSubject(subject: SubjectPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val subjectId = subject.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            database.upsertDocument(
                databaseId = Subjects.DATABASE_ID,
                collectionId = Subjects.COLLECTION_ID,
                documentId = subjectId,
                data = subject.copy(uid = subjectId),
                permissions = Permission.onlyUserData(targetUser),
                nestedType = SubjectPojo.serializer(),
            )

            return subjectId
        }

        override suspend fun addOrUpdateSubjectsGroup(subjects: List<SubjectPojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            subjects.forEach { subject -> addOrUpdateSubject(subject, targetUser) }
        }

        override suspend fun fetchSubjectById(uid: UID, targetUser: UID): Flow<SubjectDetailsPojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            if (uid.isEmpty()) return flowOf(null)

            val subjectsFlow = database.getDocumentFlow(
                databaseId = Subjects.DATABASE_ID,
                collectionId = Subjects.COLLECTION_ID,
                documentId = uid,
                nestedType = SubjectPojo.serializer(),
            )

            return subjectsFlow.flatMapToDetails()
        }

        override suspend fun fetchAllSubjectsByOrganization(
            organizationId: UID?,
            targetUser: UID
        ): Flow<List<SubjectDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val subjectsFlow = database.listDocumentsFlow(
                databaseId = Subjects.DATABASE_ID,
                collectionId = Subjects.COLLECTION_ID,
                queries = if (organizationId != null) {
                    listOf(
                        Query.equal(Subjects.USER_ID, targetUser),
                        Query.equal(Subjects.ORGANIZATION_ID, organizationId),
                    )
                } else {
                    listOf(Query.equal(Subjects.USER_ID, targetUser))
                },
                nestedType = SubjectPojo.serializer(),
            )

            return subjectsFlow.flatMapListToDetails()
        }

        override suspend fun fetchAllSubjectsByNames(
            names: List<String>,
            targetUser: UID
        ): List<SubjectDetailsPojo> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val subjectsFlow = database.listDocumentsFlow(
                databaseId = Subjects.DATABASE_ID,
                collectionId = Subjects.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Subjects.USER_ID, targetUser),
                    Query.equal(Subjects.SUBJECT_NAME, names),
                ),
                nestedType = SubjectPojo.serializer(),
            )

            return subjectsFlow.flatMapListToDetails().first()
        }

        override suspend fun fetchSubjectsByEmployee(
            employeeId: UID,
            targetUser: UID
        ): Flow<List<SubjectDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val subjectsFlow = database.listDocumentsFlow(
                databaseId = Subjects.DATABASE_ID,
                collectionId = Subjects.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Subjects.USER_ID, targetUser),
                    Query.equal(Subjects.TEACHER_ID, employeeId),
                ),
                nestedType = SubjectPojo.serializer(),
            )

            return subjectsFlow.flatMapListToDetails()
        }

        override suspend fun deleteSubject(targetId: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            database.deleteDocument(
                databaseId = Subjects.DATABASE_ID,
                collectionId = Subjects.COLLECTION_ID,
                documentId = targetId,
            )
        }

        override suspend fun deleteAllSubjects(targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val subjectsFlow = fetchAllSubjectsByOrganization(null, targetUser)
            subjectsFlow.first().forEach { subject -> deleteSubject(subject.uid, targetUser) }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<SubjectPojo>>.flatMapListToDetails() = flatMapLatest { subjects ->
            if (subjects.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationIds = subjects.map { it.organizationId }.toSet().toList()
                val employeesMapFlow = database.listDocumentsFlow(
                    databaseId = Employee.DATABASE_ID,
                    collectionId = Employee.COLLECTION_ID,
                    queries = listOf(Query.equal(Employee.ORGANIZATION_ID, organizationIds)),
                    nestedType = EmployeePojo.serializer(),
                ).map { items ->
                    items.associateBy { it.uid }
                }

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

        private fun Flow<SubjectPojo?>.flatMapToDetails(): Flow<SubjectDetailsPojo?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }
}