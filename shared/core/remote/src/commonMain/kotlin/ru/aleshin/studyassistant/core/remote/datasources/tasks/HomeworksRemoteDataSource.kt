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

package ru.aleshin.studyassistant.core.remote.datasources.tasks

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
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Homeworks
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Organizations
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Subjects
import ru.aleshin.studyassistant.core.remote.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.remote.mappers.tasks.mapToDetails
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationShortPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.HomeworkDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.HomeworkPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface HomeworksRemoteDataSource {

    suspend fun addOrUpdateHomework(homework: HomeworkPojo, targetUser: UID): UID
    suspend fun addOrUpdateHomeworksGroup(homeworks: List<HomeworkPojo>, targetUser: UID)
    suspend fun fetchHomeworkById(uid: UID, targetUser: UID): Flow<HomeworkDetailsPojo?>
    suspend fun fetchHomeworksByTimeRange(from: Long, to: Long, targetUser: UID): Flow<List<HomeworkDetailsPojo>>
    suspend fun fetchOverdueHomeworks(currentDate: Long, targetUser: UID): Flow<List<HomeworkDetailsPojo>>
    suspend fun fetchActiveLinkedHomeworks(currentDate: Long, targetUser: UID): Flow<List<HomeworkDetailsPojo>>
    suspend fun fetchCompletedHomeworksCount(targetUser: UID): Flow<Int>
    suspend fun deleteHomework(uid: UID, targetUser: UID)
    suspend fun deleteAllHomework(targetUser: UID)

    class Base(
        private val database: DatabaseService,
    ) : HomeworksRemoteDataSource {

        override suspend fun addOrUpdateHomework(homework: HomeworkPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val homeworkId = homework.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            database.upsertDocument(
                databaseId = Homeworks.DATABASE_ID,
                collectionId = Homeworks.COLLECTION_ID,
                documentId = homeworkId,
                data = homework.copy(uid = homeworkId),
                permissions = Permission.onlyUserData(targetUser),
                nestedType = HomeworkPojo.serializer(),
            )

            return homeworkId
        }

        override suspend fun addOrUpdateHomeworksGroup(
            homeworks: List<HomeworkPojo>,
            targetUser: UID
        ) {
            homeworks.forEach { addOrUpdateHomework(it, targetUser) }
        }

        override suspend fun fetchHomeworkById(
            uid: UID,
            targetUser: UID
        ): Flow<HomeworkDetailsPojo?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val homeworkFlow = database.getDocumentFlow(
                databaseId = Homeworks.DATABASE_ID,
                collectionId = Homeworks.COLLECTION_ID,
                documentId = uid,
                nestedType = HomeworkPojo.serializer(),
            )

            return homeworkFlow.flatMapToDetails()
        }

        override suspend fun fetchHomeworksByTimeRange(
            from: Long,
            to: Long,
            targetUser: UID
        ): Flow<List<HomeworkDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val homeworksFlow = database.listDocumentsFlow(
                databaseId = Homeworks.DATABASE_ID,
                collectionId = Homeworks.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Homeworks.USER_ID, targetUser),
                    Query.between(Homeworks.DEADLINE, from, to),
                    Query.orderDesc(Homeworks.DEADLINE),
                ),
                nestedType = HomeworkPojo.serializer(),
            )

            return homeworksFlow.flatMapListToDetails()
        }

        override suspend fun fetchOverdueHomeworks(
            currentDate: Long,
            targetUser: UID,
        ): Flow<List<HomeworkDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val homeworksFlow = database.listDocumentsFlow(
                databaseId = Homeworks.DATABASE_ID,
                collectionId = Homeworks.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Homeworks.USER_ID, targetUser),
                    Query.lessThan(Homeworks.DEADLINE, currentDate),
                    Query.equal(Homeworks.DONE, false),
                    Query.isNull(Homeworks.COMPLETE_DATE),
                    Query.orderDesc(Homeworks.DEADLINE),
                ),
                nestedType = HomeworkPojo.serializer(),
            )

            return homeworksFlow.flatMapListToDetails()
        }

        override suspend fun fetchActiveLinkedHomeworks(
            currentDate: Long,
            targetUser: UID,
        ): Flow<List<HomeworkDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val homeworksFlow = database.listDocumentsFlow(
                databaseId = Homeworks.DATABASE_ID,
                collectionId = Homeworks.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Homeworks.USER_ID, targetUser),
                    Query.greaterThanEqual(Homeworks.DEADLINE, currentDate),
                    Query.isNotNull(Homeworks.CLASS_ID),
                    Query.equal(Homeworks.DONE, false),
                    Query.orderDesc(Homeworks.DEADLINE),
                ),
                nestedType = HomeworkPojo.serializer(),
            )

            return homeworksFlow.flatMapListToDetails()
        }

        override suspend fun deleteHomework(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            database.deleteDocument(
                databaseId = Homeworks.DATABASE_ID,
                collectionId = Homeworks.COLLECTION_ID,
                documentId = uid,
            )
        }

        override suspend fun fetchCompletedHomeworksCount(targetUser: UID): Flow<Int> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val homeworksFlow = database.listDocumentsFlow(
                databaseId = Homeworks.DATABASE_ID,
                collectionId = Homeworks.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Homeworks.USER_ID, targetUser),
                    Query.equal(Homeworks.DONE, true),
                    Query.isNotNull(Homeworks.COMPLETE_DATE),
                ),
                nestedType = HomeworkPojo.serializer(),
            )

            return homeworksFlow.map { it.size }
        }

        override suspend fun deleteAllHomework(targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val homeworks = fetchHomeworksByTimeRange(Long.MIN_VALUE, Long.MAX_VALUE, targetUser)
            homeworks.first().forEach { deleteHomework(it.uid, targetUser) }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<HomeworkPojo>>.flatMapListToDetails() = flatMapLatest { homeworks ->
            if (homeworks.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = homeworks.map { it.organizationId }.toSet().toList()

                val organizationsMapFlow = database.listDocumentsFlow(
                    databaseId = Organizations.DATABASE_ID,
                    collectionId = Organizations.COLLECTION_ID,
                    queries = listOf(Query.equal(Organizations.UID, organizationsIds)),
                    nestedType = OrganizationShortPojo.serializer(),
                ).map { items ->
                    items.associateBy { it.uid }
                }

                val subjectsMapFlow = database.listDocumentsFlow(
                    databaseId = Subjects.DATABASE_ID,
                    collectionId = Subjects.COLLECTION_ID,
                    queries = listOf(Query.equal(Subjects.ORGANIZATION_ID, organizationsIds)),
                    nestedType = SubjectPojo.serializer(),
                ).map { items ->
                    items.associateBy { it.uid }
                }

                val employeesMapFlow = database.listDocumentsFlow(
                    databaseId = Employee.DATABASE_ID,
                    collectionId = Employee.COLLECTION_ID,
                    queries = listOf(Query.equal(Employee.ORGANIZATION_ID, organizationsIds)),
                    nestedType = EmployeePojo.serializer(),
                ).map { items ->
                    items.associateBy { it.uid }
                }

                combine(
                    flowOf(homeworks),
                    organizationsMapFlow,
                    subjectsMapFlow,
                    employeesMapFlow,
                ) { homeworksList, organizationsMap, subjectsMap, employeesMap ->
                    homeworksList.map { homework ->
                        homework.mapToDetails(
                            organization = checkNotNull(organizationsMap[homework.organizationId]),
                            subject = subjectsMap[homework.subjectId]?.mapToDetails(
                                employee = employeesMap[subjectsMap[homework.subjectId]?.teacherId]
                            ),
                        )
                    }
                }
            }
        }

        private fun Flow<HomeworkPojo?>.flatMapToDetails(): Flow<HomeworkDetailsPojo?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }
}