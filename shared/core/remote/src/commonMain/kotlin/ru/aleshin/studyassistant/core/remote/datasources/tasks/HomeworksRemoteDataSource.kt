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

import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
        private val database: FirebaseFirestore,
    ) : HomeworksRemoteDataSource {

        override suspend fun addOrUpdateHomework(homework: HomeworkPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS)

            val homeworkId = homework.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            return reference.document(homeworkId).set(homework.copy(uid = homeworkId)).let {
                return@let homeworkId
            }
        }

        override suspend fun addOrUpdateHomeworksGroup(
            homeworks: List<HomeworkPojo>,
            targetUser: UID
        ) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS)

            database.batch().apply {
                homeworks.forEach { homework ->
                    val uid = homework.uid.takeIf { it.isNotBlank() } ?: randomUUID()
                    set(reference.document(uid), homework)
                }
                return@apply commit()
            }
        }

        override suspend fun fetchHomeworkById(
            uid: UID,
            targetUser: UID
        ): Flow<HomeworkDetailsPojo?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS).document(uid)

            return reference.snapshotFlowGet<HomeworkPojo?>().flatMapToDetails(userDataRoot)
        }

        override suspend fun fetchHomeworksByTimeRange(
            from: Long,
            to: Long,
            targetUser: UID
        ): Flow<List<HomeworkDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val homeworksFlow = userDataRoot.collection(UserData.HOMEWORKS)
                .where {
                    val fromDeadlineFilter = UserData.HOMEWORK_DEADLINE greaterThanOrEqualTo from
                    val toDeadlineFilter = UserData.HOMEWORK_DEADLINE lessThanOrEqualTo to
                    return@where fromDeadlineFilter and toDeadlineFilter
                }
                .orderBy(UserData.HOMEWORK_DEADLINE, Direction.DESCENDING)
                .snapshotListFlowGet<HomeworkPojo>()
                .flatMapListToDetails(userDataRoot)

            return homeworksFlow
        }

        override suspend fun fetchOverdueHomeworks(
            currentDate: Long,
            targetUser: UID,
        ): Flow<List<HomeworkDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val homeworksFlow = userDataRoot.collection(UserData.HOMEWORKS)
                .where {
                    val deadlineFilter = UserData.HOMEWORK_DEADLINE lessThan currentDate
                    val doneFilter = UserData.HOMEWORK_DONE equalTo false
                    val completeDateFilter = UserData.HOMEWORK_COMPLETE_DATE equalTo null
                    return@where deadlineFilter and doneFilter and completeDateFilter
                }
                .orderBy(UserData.HOMEWORK_DEADLINE, Direction.DESCENDING)
                .snapshotListFlowGet<HomeworkPojo>()
                .flatMapListToDetails(userDataRoot)

            return homeworksFlow
        }

        override suspend fun fetchActiveLinkedHomeworks(
            currentDate: Long,
            targetUser: UID,
        ): Flow<List<HomeworkDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val homeworksFlow = userDataRoot.collection(UserData.HOMEWORKS)
                .where {
                    val deadlineFilter = UserData.HOMEWORK_DEADLINE greaterThanOrEqualTo currentDate
                    val classIdFilter = UserData.HOMEWORK_CLASS_ID notEqualTo null
                    val doneFilter = UserData.HOMEWORK_DONE equalTo false
                    return@where deadlineFilter and classIdFilter and doneFilter
                }
                .orderBy(UserData.HOMEWORK_DEADLINE, Direction.DESCENDING)
                .snapshotListFlowGet<HomeworkPojo>()
                .flatMapListToDetails(userDataRoot)

            return homeworksFlow
        }

        override suspend fun deleteHomework(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS).document(uid)

            return reference.delete()
        }

        override suspend fun fetchCompletedHomeworksCount(targetUser: UID): Flow<Int> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val homeworksFlow = userDataRoot.collection(UserData.HOMEWORKS)
                .where {
                    val doneFilter = UserData.HOMEWORK_DONE equalTo true
                    val completeDateFilter = UserData.HOMEWORK_COMPLETE_DATE notEqualTo null
                    return@where doneFilter and completeDateFilter
                }
                .orderBy(UserData.HOMEWORK_DEADLINE, Direction.DESCENDING)
                .snapshots
                .map { snapshot -> snapshot.documents.size }

            return homeworksFlow
        }

        override suspend fun deleteAllHomework(targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS)

            database.deleteAll(reference)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<HomeworkPojo>>.flatMapListToDetails(
            userDataRoot: DocumentReference
        ): Flow<List<HomeworkDetailsPojo>> = flatMapLatest { homeworks ->
            if (homeworks.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = homeworks.map { it.organizationId }.toSet()

                val organizationsMapFlow = userDataRoot.collection(UserData.ORGANIZATIONS)
                    .observeCollectionMapByField<OrganizationShortPojo>(
                        ids = organizationsIds,
                        associateKey = { it.uid }
                    )

                val subjectsMapFlow = userDataRoot.collection(UserData.SUBJECTS)
                    .observeCollectionMapByField<SubjectPojo>(
                        ids = organizationsIds,
                        fieldName = UserData.ORGANIZATION_ID,
                        associateKey = { it.uid }
                    )

                val employeesMapFlow = userDataRoot.collection(UserData.EMPLOYEE)
                    .observeCollectionMapByField<EmployeePojo>(
                        ids = organizationsIds,
                        fieldName = UserData.ORGANIZATION_ID,
                        associateKey = { it.uid }
                    )

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

        private fun Flow<HomeworkPojo?>.flatMapToDetails(
            userDataRoot: DocumentReference
        ): Flow<HomeworkDetailsPojo?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails(userDataRoot)
                .map { it.getOrNull(0) }
        }
    }
}