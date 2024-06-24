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
import entities.common.NumberOfRepeatWeek
import exceptions.FirebaseUserException
import extensions.dateTime
import extensions.exists
import extensions.snapshotGet
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
import models.schedules.BaseScheduleDetailsData
import models.schedules.BaseSchedulePojo
import models.subjects.SubjectPojo
import models.users.EmployeeDetailsData
import remote.StudyAssistantFirestore.UserData

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface BaseScheduleRemoteDataSource {

    suspend fun addOrUpdateSchedule(schedule: BaseScheduleDetailsData, targetUser: UID): UID
    suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<BaseScheduleDetailsData?>
    suspend fun fetchScheduleByDate(
        date: Instant,
        numberOfWeek: NumberOfRepeatWeek,
        targetUser: UID
    ): Flow<BaseScheduleDetailsData?>
    suspend fun fetchSchedulesByVersion(
        from: Instant,
        to: Instant,
        numberOfWeek: NumberOfRepeatWeek?,
        targetUser: UID
    ): Flow<List<BaseScheduleDetailsData>>
    suspend fun fetchClassById(uid: UID, scheduleId: UID, targetUser: UID): Flow<ClassDetailsData?>

    class Base(
        private val database: FirebaseFirestore,
    ) : BaseScheduleRemoteDataSource {

        override suspend fun addOrUpdateSchedule(
            schedule: BaseScheduleDetailsData,
            targetUser: UID
        ): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)
            val schedulePojo = schedule.mapToRemoteData()

            val reference = userDataRoot.collection(UserData.BASE_SCHEDULES)

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

        override suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<BaseScheduleDetailsData?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.BASE_SCHEDULES).document(uid)

            val schedulePojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<BaseSchedulePojo?>())
            }

            return schedulePojoFlow.map { schedulePojo ->
                schedulePojo?.mapToDetailsData(
                    classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) }
                )
            }
        }

        override suspend fun fetchScheduleByDate(
            date: Instant,
            numberOfWeek: NumberOfRepeatWeek,
            targetUser: UID
        ): Flow<BaseScheduleDetailsData?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val dateMillis = date.toEpochMilliseconds()
            val dateTime = date.dateTime()
            val dayOfWeek = dateTime.dayOfWeek.toString()
            val week = numberOfWeek.toString()

            val reference = userDataRoot.collection(UserData.BASE_SCHEDULES).where {
                val toVersionFilter = UserData.VERSION_TO greaterThanOrEqualTo dateMillis
                val fromVersionFilter = UserData.VERSION_FROM lessThanOrEqualTo dateMillis
                val dateFilter = (UserData.WEEK equalTo week) and (UserData.DAY_OF_WEEK equalTo dayOfWeek)
                return@where toVersionFilter and fromVersionFilter and dateFilter
            }.orderBy(UserData.VERSION_TO, Direction.DESCENDING)

            val schedulePojoFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<BaseSchedulePojo?>()) }.getOrNull(0)
            }

            return schedulePojoFlow.map { schedulePojo ->
                schedulePojo?.mapToDetailsData(
                    classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) },
                )
            }
        }

        override suspend fun fetchSchedulesByVersion(
            from: Instant,
            to: Instant,
            numberOfWeek: NumberOfRepeatWeek?,
            targetUser: UID
        ): Flow<List<BaseScheduleDetailsData>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()
            val week = numberOfWeek?.toString()

            val reference = userDataRoot.collection(UserData.BASE_SCHEDULES).where {
                val toDateFilter = UserData.VERSION_TO greaterThanOrEqualTo fromMillis
                val fromDateFilter = UserData.VERSION_FROM lessThanOrEqualTo toMillis
                val weekFilter = UserData.WEEK equalTo week
                val dateFilter = toDateFilter and fromDateFilter
                return@where if (week != null) dateFilter and weekFilter else dateFilter
            }.orderBy(UserData.VERSION_TO, Direction.DESCENDING)

            val schedulePojoListFlow = reference.snapshots.map { snapshot ->
                snapshot.documents.map { it.data(serializer<BaseSchedulePojo>()) }
            }

            return schedulePojoListFlow.map { schedulePojoList ->
                schedulePojoList.map { schedulePojo ->
                    schedulePojo.mapToDetailsData(
                        classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) }
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

            val scheduleReference = userDataRoot.collection(UserData.BASE_SCHEDULES).document(scheduleId)

            val classPojoFlow = scheduleReference.snapshots.map { snapshot ->
                snapshot.data(serializer<BaseSchedulePojo?>())?.classes?.get(uid)
            }

            return classPojoFlow.map { classPojo ->
                classPojo?.mapToDetails(userDataRoot, scheduleId)
            }
        }

        private suspend fun ClassData.mapToDetails(
            userDataRoot: DocumentReference,
            scheduleId: UID
        ): ClassDetailsData {
            val employeeReferenceRoot = userDataRoot.collection(UserData.EMPLOYEE)

            val organizationReference = userDataRoot.collection(UserData.ORGANIZATIONS).document(organizationId)
            val subjectReference = subjectId?.let { subjectId ->
                userDataRoot.collection(UserData.SUBJECTS).document(subjectId)
            }

            val organization = organizationReference.snapshotGet().data<OrganizationShortData>()
            val subject = subjectReference?.snapshotGet()?.data(serializer<SubjectPojo?>()).let { subjectPojo ->
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