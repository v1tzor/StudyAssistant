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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.exists
import ru.aleshin.studyassistant.core.common.extensions.snapshotGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirestore.UserData
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
    suspend fun fetchHomeworkById(uid: UID, targetUser: UID): Flow<HomeworkDetailsPojo?>
    suspend fun fetchHomeworksByTimeRange(from: Long, to: Long, targetUser: UID): Flow<List<HomeworkDetailsPojo>>
    suspend fun fetchOverdueHomeworks(currentDate: Long, targetUser: UID): Flow<List<HomeworkDetailsPojo>>
    suspend fun fetchActiveLinkedHomeworks(currentDate: Long, targetUser: UID): Flow<List<HomeworkDetailsPojo>>
    suspend fun deleteHomework(uid: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : HomeworksRemoteDataSource {

        override suspend fun addOrUpdateHomework(homework: HomeworkPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS)

            return database.runTransaction {
                val isExist = homework.uid.isNotEmpty() && reference.document(homework.uid).exists()
                if (isExist) {
                    reference.document(homework.uid).set(data = homework)
                    return@runTransaction homework.uid
                } else {
                    val uid = reference.add(homework).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun fetchHomeworkById(uid: UID, targetUser: UID): Flow<HomeworkDetailsPojo?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS).document(uid)

            val homeworkPojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<HomeworkPojo?>())
            }

            return homeworkPojoFlow.map { homeworkPojo -> homeworkPojo?.mapToDetails(userDataRoot) }
        }

        override suspend fun fetchHomeworksByTimeRange(
            from: Long,
            to: Long,
            targetUser: UID
        ): Flow<List<HomeworkDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS).where {
                val fromDeadlineFilter = UserData.HOMEWORK_DEADLINE greaterThanOrEqualTo from
                val toDeadlineFilter = UserData.HOMEWORK_DEADLINE lessThanOrEqualTo to
                return@where fromDeadlineFilter and toDeadlineFilter
            }.orderBy(UserData.HOMEWORK_DEADLINE, Direction.DESCENDING)

            val homeworkPojoListFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<HomeworkPojo>()) }
            }

            return homeworkPojoListFlow.map { homeworks ->
                homeworks.map { homeworkPojo -> homeworkPojo.mapToDetails(userDataRoot) }
            }
        }

        override suspend fun fetchOverdueHomeworks(
            currentDate: Long,
            targetUser: UID,
        ): Flow<List<HomeworkDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS).where {
                val deadlineFilter = UserData.HOMEWORK_DEADLINE lessThan currentDate
                val doneFilter = UserData.HOMEWORK_DONE equalTo false
                val completeDateFilter = UserData.HOMEWORK_COMPLETE_DATE equalTo null
                return@where deadlineFilter and doneFilter and completeDateFilter
            }.orderBy(UserData.HOMEWORK_DEADLINE, Direction.DESCENDING)

            val homeworkPojoListFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<HomeworkPojo>()) }
            }

            return homeworkPojoListFlow.map { homeworks ->
                homeworks.map { homeworkPojo -> homeworkPojo.mapToDetails(userDataRoot) }
            }
        }

        override suspend fun fetchActiveLinkedHomeworks(
            currentDate: Long,
            targetUser: UID,
        ): Flow<List<HomeworkDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS).where {
                val deadlineFilter = UserData.HOMEWORK_DEADLINE greaterThanOrEqualTo currentDate
                val classIdFilter = UserData.HOMEWORK_CLASS_ID notEqualTo null
                val doneFilter = UserData.HOMEWORK_DONE equalTo false
                return@where deadlineFilter and classIdFilter and doneFilter
            }.orderBy(UserData.HOMEWORK_DEADLINE, Direction.DESCENDING)

            val homeworkPojoListFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<HomeworkPojo>()) }
            }

            return homeworkPojoListFlow.map { homeworks ->
                homeworks.map { homeworkPojo -> homeworkPojo.mapToDetails(userDataRoot) }
            }
        }

        override suspend fun deleteHomework(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS).document(uid)

            return reference.delete()
        }

        private suspend fun HomeworkPojo.mapToDetails(userDataRoot: DocumentReference): HomeworkDetailsPojo {
            val organizationReference = userDataRoot.collection(UserData.ORGANIZATIONS).document(organizationId)
            val subjectReference = subjectId?.let { userDataRoot.collection(UserData.SUBJECTS).document(it) }

            val organization = organizationReference.snapshotGet().data<OrganizationShortPojo>()
            val subject = subjectReference?.snapshotGet()?.data(serializer<SubjectPojo?>()).let { subjectPojo ->
                val employeeReference = subjectPojo?.teacherId?.let {
                    userDataRoot.collection(UserData.EMPLOYEE).document(it)
                }
                val employee = employeeReference?.snapshotGet()?.data(serializer<EmployeePojo?>())
                subjectPojo?.mapToDetails(employee)
            }

            return mapToDetails(
                organization = organization,
                subject = subject,
            )
        }
    }
}