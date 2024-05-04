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

package remote.tasks

import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import exceptions.FirebaseUserException
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import mappers.subjects.mapToDetailsData
import mappers.tasks.mapToDetailsData
import mappers.tasks.mapToRemoteData
import models.organizations.OrganizationShortData
import models.subjects.SubjectPojo
import models.tasks.HomeworkDetailsData
import models.tasks.HomeworkPojo
import models.users.EmployeeDetailsData
import remote.StudyAssistantFirestore.UserData

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface HomeworksRemoteDataSource {

    suspend fun fetchHomeworksByTime(from: Int, to: Int, targetUser: UID): Flow<List<HomeworkDetailsData>>
    suspend fun fetchHomeworkById(uid: UID, targetUser: UID): Flow<HomeworkDetailsData?>
    suspend fun addOrUpdateHomework(homework: HomeworkDetailsData, targetUser: UID): UID
    suspend fun deleteHomework(uid: UID, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : HomeworksRemoteDataSource {

        override suspend fun fetchHomeworksByTime(from: Int, to: Int, targetUser: UID): Flow<List<HomeworkDetailsData>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS).where {
                (UserData.HOMEWORK_DATE greaterThanOrEqualTo from) and (UserData.HOMEWORK_DATE lessThanOrEqualTo to)
            }

            val homeworkPojoListFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<HomeworkPojo>()) }
            }

            return homeworkPojoListFlow.map { homeworks ->
                homeworks.map { homeworkPojo ->
                    val organizationReference = userDataRoot.collection(UserData.ORGANIZATIONS).document(homeworkPojo.organizationId)
                    val subjectReference = homeworkPojo.subjectId?.let {
                        userDataRoot.collection(UserData.SUBJECTS).document(it)
                    }

                    val organization = organizationReference.get().data<OrganizationShortData>()
                    val subject = subjectReference?.get()?.data<SubjectPojo>().let { subjectPojo ->
                        val employeeReference = subjectPojo?.teacher?.let {
                            userDataRoot.collection(UserData.EMPLOYEE).document(it)
                        }
                        val employee = employeeReference?.get()?.data(serializer<EmployeeDetailsData?>())
                        subjectPojo?.mapToDetailsData(employee)
                    }

                    homeworkPojo.mapToDetailsData(
                        organization = organization,
                        subject = subject,
                    )
                }
            }
        }

        override suspend fun fetchHomeworkById(uid: UID, targetUser: UID): Flow<HomeworkDetailsData?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS).document(uid)

            val homeworkPojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<HomeworkPojo?>())
            }

            return homeworkPojoFlow.map { homeworkPojo ->
                if (homeworkPojo == null) return@map null
                val organizationReference = userDataRoot.collection(UserData.ORGANIZATIONS).document(homeworkPojo.organizationId)
                val subjectReference = homeworkPojo.subjectId?.let { userDataRoot.collection(UserData.SUBJECTS).document(it) }

                val organization = organizationReference.get().data<OrganizationShortData>()
                val subject = subjectReference?.get()?.data<SubjectPojo>().let { subjectPojo ->
                    val employeeReference = subjectPojo?.teacher?.let { userDataRoot.collection(UserData.EMPLOYEE).document(it) }
                    val employee = employeeReference?.get()?.data(serializer<EmployeeDetailsData?>())
                    subjectPojo?.mapToDetailsData(employee)
                }

                homeworkPojo.mapToDetailsData(
                    organization = organization,
                    subject = subject,
                )
            }
        }

        override suspend fun addOrUpdateHomework(homework: HomeworkDetailsData, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val homeworkPojo = homework.mapToRemoteData()

            val reference = userDataRoot.collection(UserData.HOMEWORKS)

            return database.runTransaction {
                val isExist = homeworkPojo.uid.isNotEmpty() && reference.document(homeworkPojo.uid).get().exists
                if (isExist) {
                    reference.document(homeworkPojo.uid).set(data = homeworkPojo, merge = true)
                    return@runTransaction homeworkPojo.uid
                } else {
                    val uid = reference.add(homeworkPojo).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun deleteHomework(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.HOMEWORKS).document(uid)

            return reference.delete()
        }
    }
}