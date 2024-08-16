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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.exists
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.UserData
import ru.aleshin.studyassistant.core.remote.mappers.schedules.mapToDetails
import ru.aleshin.studyassistant.core.remote.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.remote.models.classes.ClassDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.classes.ClassPojo
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationShortPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.BaseScheduleDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.BaseSchedulePojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface BaseScheduleRemoteDataSource {

    suspend fun addOrUpdateSchedule(schedule: BaseSchedulePojo, targetUser: UID): UID
    suspend fun addOrUpdateSchedulesGroup(schedules: List<BaseSchedulePojo>, targetUser: UID)
    suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<BaseScheduleDetailsPojo?>
    suspend fun fetchScheduleByDate(
        date: Instant,
        numberOfWeek: NumberOfRepeatWeek,
        targetUser: UID
    ): Flow<BaseScheduleDetailsPojo?>
    suspend fun fetchSchedulesByVersion(
        from: Instant,
        to: Instant,
        numberOfWeek: NumberOfRepeatWeek?,
        targetUser: UID
    ): Flow<List<BaseScheduleDetailsPojo>>
    suspend fun fetchClassById(uid: UID, scheduleId: UID, targetUser: UID): Flow<ClassDetailsPojo?>

    class Base(
        private val database: FirebaseFirestore,
    ) : BaseScheduleRemoteDataSource {

        override suspend fun addOrUpdateSchedule(
            schedule: BaseSchedulePojo,
            targetUser: UID
        ): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.BASE_SCHEDULES)

            return database.runTransaction {
                val isExist = schedule.uid.isNotEmpty() && reference.document(schedule.uid).exists()
                if (isExist) {
                    reference.document(schedule.uid).set(data = schedule)
                    return@runTransaction schedule.uid
                } else {
                    val uid = reference.add(schedule).id
                    reference.document(uid).update(UserData.UID to uid)
                    return@runTransaction uid
                }
            }
        }

        override suspend fun addOrUpdateSchedulesGroup(schedules: List<BaseSchedulePojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.BASE_SCHEDULES)

            database.batch().apply {
                schedules.forEach { schedule ->
                    val uid = schedule.uid.takeIf { it.isNotBlank() } ?: randomUUID()
                    set(reference.document(uid), schedule.copy(uid = uid))
                }
                return@apply commit()
            }
        }

        override suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<BaseScheduleDetailsPojo?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.BASE_SCHEDULES).document(uid)

            val schedulePojoFlow = reference.snapshots.map { snapshot ->
                snapshot.data(serializer<BaseSchedulePojo?>())
            }

            return schedulePojoFlow.map { schedulePojo ->
                schedulePojo?.mapToDetails(
                    classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) }
                )
            }
        }

        override suspend fun fetchScheduleByDate(
            date: Instant,
            numberOfWeek: NumberOfRepeatWeek,
            targetUser: UID
        ): Flow<BaseScheduleDetailsPojo?> {
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
                schedulePojo?.mapToDetails(
                    classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) },
                )
            }
        }

        override suspend fun fetchSchedulesByVersion(
            from: Instant,
            to: Instant,
            numberOfWeek: NumberOfRepeatWeek?,
            targetUser: UID
        ): Flow<List<BaseScheduleDetailsPojo>> {
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
                    schedulePojo.mapToDetails(
                        classMapper = { it.mapToDetails(userDataRoot, schedulePojo.uid) }
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

            val scheduleReference = userDataRoot.collection(UserData.BASE_SCHEDULES).document(scheduleId)

            val classPojoFlow = scheduleReference.snapshots.map { snapshot ->
                snapshot.data(serializer<BaseSchedulePojo?>())?.classes?.get(uid)
            }

            return classPojoFlow.map { classPojo ->
                classPojo?.mapToDetails(userDataRoot, scheduleId)
            }
        }

        private suspend fun ClassPojo.mapToDetails(
            userDataRoot: DocumentReference,
            scheduleId: UID
        ): ClassDetailsPojo {
            val employeeReferenceRoot = userDataRoot.collection(UserData.EMPLOYEE)

            val organizationReference = userDataRoot.collection(UserData.ORGANIZATIONS).document(organizationId)
            val subjectReference = subjectId?.let { subjectId ->
                userDataRoot.collection(UserData.SUBJECTS).document(subjectId)
            }

            val organization = organizationReference.snapshotGet().data<OrganizationShortPojo>()
            val subject = subjectReference?.snapshotGet()?.data(serializer<SubjectPojo?>()).let { subjectPojo ->
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