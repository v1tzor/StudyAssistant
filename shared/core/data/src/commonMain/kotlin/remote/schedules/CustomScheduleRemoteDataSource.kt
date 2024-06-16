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
import extensions.exists
import extensions.snapshotGet
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.serializer
import mappers.schedules.mapToDetailsData
import mappers.schedules.mapToRemoteData
import mappers.subjects.mapToDetailsData
import mappers.tasks.mapToDetailsData
import models.classes.ClassData
import models.classes.ClassDetailsData
import models.organizations.OrganizationShortData
import models.schedules.CustomScheduleDetailsData
import models.schedules.CustomSchedulePojo
import models.subjects.SubjectPojo
import models.users.EmployeeDetailsData
import remote.StudyAssistantFirestore.UserData

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface CustomScheduleRemoteDataSource {

    suspend fun addOrUpdateSchedule(schedule: CustomScheduleDetailsData, targetUser: UID): UID
    suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<CustomScheduleDetailsData?>
    suspend fun fetchScheduleByDate(date: Instant, targetUser: UID): Flow<CustomScheduleDetailsData?>
    suspend fun fetchSchedulesByTimeRange(from: Instant, to: Instant, targetUser: UID): Flow<List<CustomScheduleDetailsData>>
    suspend fun fetchClassById(uid: UID, scheduleId: UID, targetUser: UID): Flow<ClassDetailsData?>

    class Base(
        private val database: FirebaseFirestore,
    ) : CustomScheduleRemoteDataSource {

        override suspend fun addOrUpdateSchedule(
            schedule: CustomScheduleDetailsData,
            targetUser: UID
        ): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val schedulePojo = schedule.mapToRemoteData()

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES)

            return database.runTransaction {
                val isExist = schedulePojo.uid.isNotEmpty() && reference.document(schedulePojo.uid).exists()
                if (isExist) {
                    reference.document(schedulePojo.uid).set(data = schedulePojo)
                    return@runTransaction schedulePojo.uid
                } else {
                    val uid = reference.add(schedulePojo).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<CustomScheduleDetailsData?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).document(uid)

            val schedulePojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<CustomSchedulePojo?>())
            }

            return schedulePojoFlow.map { schedulePojo ->
                schedulePojo?.mapToDetailsData(
                    classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) },
                )
            }
        }

        override suspend fun fetchScheduleByDate(date: Instant, targetUser: UID): Flow<CustomScheduleDetailsData?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val dateMillis = date.toEpochMilliseconds()

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).where {
                UserData.CUSTOM_SCHEDULE_DATE equalTo dateMillis
            }.orderBy(UserData.CUSTOM_SCHEDULE_DATE, Direction.DESCENDING)

            val schedulePojoFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<CustomSchedulePojo?>()) }.getOrNull(0)
            }

            return schedulePojoFlow.map { schedulePojo ->
                schedulePojo?.mapToDetailsData(
                    classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) },
                )
            }
        }

        override suspend fun fetchSchedulesByTimeRange(
            from: Instant,
            to: Instant,
            targetUser: UID
        ): Flow<List<CustomScheduleDetailsData>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).where {
                val firstDateFilter = UserData.CUSTOM_SCHEDULE_DATE greaterThanOrEqualTo fromMillis
                val secondDateFilter = UserData.CUSTOM_SCHEDULE_DATE lessThanOrEqualTo toMillis
                return@where firstDateFilter and secondDateFilter
            }.orderBy(UserData.CUSTOM_SCHEDULE_DATE, Direction.DESCENDING)

            val schedulePojoListFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<CustomSchedulePojo>()) }
            }

            return schedulePojoListFlow.map { schedulePojoList ->
                schedulePojoList.map { schedulePojo ->
                    schedulePojo.mapToDetailsData(
                        classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) },
                    )
                }
            }
        }

        override suspend fun fetchClassById(
            uid: UID,
            scheduleId: UID,
            targetUser: UID
        ): Flow<ClassDetailsData?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val scheduleReference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).document(scheduleId)
            val classReference = scheduleReference.collection(UserData.SCHEDULE_CLASSES).document(uid)

            val classPojoFlow = classReference.snapshots.map { snapshot ->
                snapshot.data(serializer<ClassData?>())
            }

            return classPojoFlow.map { classPojo ->
                classPojo?.mapToDetails(userDataRoot, scheduleId)
            }
        }

        private suspend fun ClassData.mapToDetails(userDataRoot: DocumentReference, scheduleId: UID): ClassDetailsData {
            val employeeReferenceRoot = userDataRoot.collection(UserData.EMPLOYEE)

            val organizationReference = userDataRoot.collection(UserData.ORGANIZATIONS).document(organizationId)
            val subjectReference = subjectId?.let { subjectId ->
                userDataRoot.collection(UserData.SUBJECTS).document(subjectId)
            }

            val organization = organizationReference.snapshotGet().data<OrganizationShortData>()
            val subject = subjectReference?.snapshotGet()?.data<SubjectPojo>().let { subjectPojo ->
                val employeeReference = subjectPojo?.teacherId?.let { employeeReferenceRoot.document(it) }
                val employee = employeeReference?.snapshotGet()?.data(serializer<EmployeeDetailsData?>())
                subjectPojo?.mapToDetailsData(employee)
            }
            val employee = teacherId?.let { teacherId ->
                val employeeReference = employeeReferenceRoot.document(teacherId)
                employeeReference.snapshotGet().data(serializer<EmployeeDetailsData?>())
            }

            return mapToDetailsData(
                scheduleId = scheduleId,
                organization = organization,
                subject = subject,
                employee = employee,
            )
        }
    }
}