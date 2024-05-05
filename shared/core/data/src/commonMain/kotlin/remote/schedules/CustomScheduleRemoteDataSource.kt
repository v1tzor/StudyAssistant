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

package remote.schedules

import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.orderBy
import dev.gitlive.firebase.firestore.where
import exceptions.FirebaseUserException
import functional.UID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import managers.DateManager
import mappers.schedules.mapToDetailsData
import mappers.schedules.mapToRemoteData
import mappers.subjects.mapToDetailsData
import mappers.tasks.mapToDetailsDate
import models.classes.ClassDetailsData
import models.classes.ClassPojo
import models.organizations.OrganizationShortData
import models.schedules.BaseScheduleDetailsData
import models.schedules.BaseSchedulePojo
import models.schedules.CustomScheduleDetailsData
import models.schedules.CustomSchedulePojo
import models.subjects.SubjectPojo
import models.users.EmployeeDetailsData
import remote.StudyAssistantFirestore.UserData

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface CustomScheduleRemoteDataSource {

    suspend fun fetchScheduleByDate(date: Int, targetUser: UID): Flow<CustomScheduleDetailsData?>
    suspend fun fetchSchedulesByTimeRange(from: Int, to: Int, targetUser: UID): Flow<List<CustomScheduleDetailsData>>
    suspend fun addOrUpdateSchedule(schedule: CustomScheduleDetailsData, targetUser: UID): UID

    class Base(
        private val database: FirebaseFirestore,
        private val dateManager: DateManager,
    ) : CustomScheduleRemoteDataSource {

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchScheduleByDate(date: Int, targetUser: UID): Flow<CustomScheduleDetailsData?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).where {
                UserData.CUSTOM_SCHEDULE_DATE equalTo date
            }.orderBy(UserData.VERSION_TO, Direction.DESCENDING)

            val schedulePojoFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<CustomSchedulePojo?>()) }.getOrNull(0)
            }

            return schedulePojoFlow.flatMapLatest { schedulePojo ->
                if (schedulePojo == null) return@flatMapLatest flowOf(null)
                val classesReference = userDataRoot.collection(UserData.CLASSES).where {
                    UserData.CLASS_SCHEDULE_ID equalTo schedulePojo.uid
                }

                return@flatMapLatest classesReference.snapshots.map { classesQuerySnapshot ->
                    val classPojoList = classesQuerySnapshot.documents.map { snapshot ->
                        snapshot.data(serializer<ClassPojo>()).mapToDetails(userDataRoot)
                    }
                    schedulePojo.mapToDetailsData(classes = classPojoList)
                }
            }
        }

        override suspend fun fetchSchedulesByTimeRange(
            from: Int,
            to: Int,
            targetUser: UID
        ): Flow<List<CustomScheduleDetailsData>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).where {
                (UserData.CUSTOM_SCHEDULE_DATE greaterThanOrEqualTo from) and (UserData.CUSTOM_SCHEDULE_DATE lessThanOrEqualTo to)
            }.orderBy(UserData.VERSION_TO, Direction.DESCENDING)

            val schedulePojoListFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<CustomSchedulePojo>()) }
            }

            return schedulePojoListFlow.map { schedulePojoList ->
                schedulePojoList.map { schedulePojo ->
                    val classesReference = userDataRoot.collection(UserData.CLASSES).where {
                        UserData.CLASS_SCHEDULE_ID equalTo schedulePojo.uid
                    }
                    val classes = classesReference.get().documents.map { snapshot ->
                        snapshot.data(serializer<ClassPojo>()).mapToDetails(userDataRoot)
                    }
                    schedulePojo.mapToDetailsData(classes = classes)
                }
            }
        }

        override suspend fun addOrUpdateSchedule(
            schedule: CustomScheduleDetailsData,
            targetUser: UID
        ): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val schedulePojo = schedule.mapToRemoteData()

            val reference = userDataRoot.collection(UserData.BASE_SCHEDULES)

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

        private suspend fun ClassPojo.mapToDetails(userDataRoot: DocumentReference): ClassDetailsData {
            val employeeReferenceRoot = userDataRoot.collection(UserData.EMPLOYEE)

            val organizationReference = userDataRoot.collection(UserData.ORGANIZATIONS).document(organizationId)
            val subjectReference = subjectId?.let { subjectId ->
                userDataRoot.collection(UserData.SUBJECTS).document(subjectId)
            }

            val organization = organizationReference.get().data<OrganizationShortData>()
            val subject = subjectReference?.get()?.data<SubjectPojo>().let { subjectPojo ->
                val employeeReference = subjectPojo?.teacher?.let { employeeReferenceRoot.document(it) }
                val employee = employeeReference?.get()?.data(serializer<EmployeeDetailsData?>())
                subjectPojo?.mapToDetailsData(employee)
            }
            val employee = teacherId?.let { teacherId ->
                val employeeReference = employeeReferenceRoot.document(teacherId)
                employeeReference.get().data(serializer<EmployeeDetailsData?>())
            }

            return mapToDetailsDate(
                organization = organization,
                subject = subject,
                employee = employee,
            )
        }
    }
}
