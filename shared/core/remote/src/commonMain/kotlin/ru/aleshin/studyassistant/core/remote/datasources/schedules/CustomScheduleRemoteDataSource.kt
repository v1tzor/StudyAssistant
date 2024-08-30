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

package ru.aleshin.studyassistant.core.remote.datasources.schedules

import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.UserData
import ru.aleshin.studyassistant.core.remote.mappers.schedules.mapToDetails
import ru.aleshin.studyassistant.core.remote.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.remote.models.classes.ClassDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.classes.ClassPojo
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationShortPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.CustomScheduleDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.CustomSchedulePojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface CustomScheduleRemoteDataSource {

    suspend fun addOrUpdateSchedule(schedule: CustomSchedulePojo, targetUser: UID): UID
    suspend fun addOrUpdateSchedulesGroup(schedules: List<CustomSchedulePojo>, targetUser: UID)
    suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<CustomScheduleDetailsPojo?>
    suspend fun fetchScheduleByDate(date: Instant, targetUser: UID): Flow<CustomScheduleDetailsPojo?>
    suspend fun fetchSchedulesByTimeRange(from: Instant, to: Instant, targetUser: UID): Flow<List<CustomScheduleDetailsPojo>>
    suspend fun fetchClassById(uid: UID, scheduleId: UID, targetUser: UID): Flow<ClassDetailsPojo?>
    suspend fun deleteScheduleById(scheduleId: UID, targetUser: UID)
    suspend fun deleteSchedulesByTimeRange(from: Instant, to: Instant, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : CustomScheduleRemoteDataSource {

        override suspend fun addOrUpdateSchedule(
            schedule: CustomSchedulePojo,
            targetUser: UID
        ): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES)

            val uid = schedule.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            return reference.document(uid).set(data = schedule.copy(uid = uid)).let {
                return@let uid
            }
        }

        override suspend fun addOrUpdateSchedulesGroup(
            schedules: List<CustomSchedulePojo>,
            targetUser: UID
        ) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES)

            database.batch().apply {
                schedules.forEach { schedule ->
                    val uid = schedule.uid.takeIf { it.isNotBlank() } ?: randomUUID()
                    set(reference.document(uid), schedule.copy(uid = uid))
                }
                return@apply commit()
            }
        }

        override suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<CustomScheduleDetailsPojo?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).document(uid)

            val schedulePojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<CustomSchedulePojo?>())
            }

            return schedulePojoFlow.map { schedulePojo ->
                schedulePojo?.mapToDetails(
                    classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) },
                )
            }
        }

        override suspend fun fetchScheduleByDate(date: Instant, targetUser: UID): Flow<CustomScheduleDetailsPojo?> {
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
                schedulePojo?.mapToDetails(
                    classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) },
                )
            }
        }

        override suspend fun fetchSchedulesByTimeRange(
            from: Instant,
            to: Instant,
            targetUser: UID
        ): Flow<List<CustomScheduleDetailsPojo>> {
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
                    schedulePojo.mapToDetails(
                        classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) },
                    )
                }
            }
        }

        override suspend fun fetchClassById(
            uid: UID,
            scheduleId: UID,
            targetUser: UID
        ): Flow<ClassDetailsPojo?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val scheduleReference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).document(scheduleId)

            val classPojoFlow = scheduleReference.snapshots.map { snapshot ->
                snapshot.data(serializer<CustomSchedulePojo?>())?.classes?.get(uid)
            }

            return classPojoFlow.map { classPojo ->
                classPojo?.mapToDetails(userDataRoot, scheduleId)
            }
        }

        override suspend fun deleteScheduleById(scheduleId: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).document(scheduleId)

            return reference.delete()
        }

        override suspend fun deleteSchedulesByTimeRange(
            from: Instant,
            to: Instant,
            targetUser: UID
        ) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()

            val schedulesReference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).where {
                val firstDateFilter = UserData.CUSTOM_SCHEDULE_DATE greaterThanOrEqualTo fromMillis
                val secondDateFilter = UserData.CUSTOM_SCHEDULE_DATE lessThanOrEqualTo toMillis
                return@where firstDateFilter and secondDateFilter
            }

            val deletableScheduleReferences = schedulesReference.snapshotGet().map { snapshot ->
                snapshot.reference
            }

            database.batch().apply {
                deletableScheduleReferences.forEach { scheduleReference ->
                    delete(scheduleReference)
                }
                return@apply commit()
            }
        }

        private suspend fun ClassPojo.mapToDetails(userDataRoot: DocumentReference, scheduleId: UID): ClassDetailsPojo {
            val employeeReferenceRoot = userDataRoot.collection(UserData.EMPLOYEE)

            val organizationReference = userDataRoot.collection(UserData.ORGANIZATIONS).document(organizationId)
            val subjectReference = subjectId?.let { subjectId ->
                userDataRoot.collection(UserData.SUBJECTS).document(subjectId)
            }

            val organization = organizationReference.snapshotGet().data<OrganizationShortPojo>()
            val subject = subjectReference?.snapshotGet()?.data(serializer<SubjectPojo>()).let { subjectPojo ->
                val employeeReference = subjectPojo?.teacherId?.let { employeeReferenceRoot.document(it) }
                val employee = employeeReference?.snapshotGet()?.data(serializer<EmployeePojo?>())
                subjectPojo?.mapToDetails(employee)
            }
            val employee = teacherId?.let { teacherId ->
                val employeeReference = employeeReferenceRoot.document(teacherId)
                employeeReference.snapshotGet().data(serializer<EmployeePojo?>())
            }

            return mapToDetails(
                scheduleId = scheduleId,
                organization = organization,
                subject = subject,
                employee = employee,
            )
        }
    }
}