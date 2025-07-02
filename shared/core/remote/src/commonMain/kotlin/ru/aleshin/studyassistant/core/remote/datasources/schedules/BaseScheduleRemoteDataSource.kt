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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.extractAllItemToSet
import ru.aleshin.studyassistant.core.common.extensions.observeCollectionMapByField
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotFlowGet
import ru.aleshin.studyassistant.core.common.extensions.snapshotGet
import ru.aleshin.studyassistant.core.common.extensions.snapshotListFlowGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.UserData
import ru.aleshin.studyassistant.core.remote.mappers.schedules.mapToDetails
import ru.aleshin.studyassistant.core.remote.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.remote.models.classes.ClassDetailsPojo
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
    suspend fun deleteSchedulesByTimeRange(from: Instant, to: Instant, targetUser: UID)

    class Base(
        private val database: FirebaseFirestore,
    ) : BaseScheduleRemoteDataSource {

        override suspend fun addOrUpdateSchedule(
            schedule: BaseSchedulePojo,
            targetUser: UID
        ): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.BASE_SCHEDULES)

            val scheduleId = schedule.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            return reference.document(scheduleId).set(schedule.copy(uid = scheduleId)).let {
                return@let scheduleId
            }
        }

        override suspend fun addOrUpdateSchedulesGroup(schedules: List<BaseSchedulePojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
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
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.BASE_SCHEDULES).document(uid)

            return reference.snapshotFlowGet<BaseSchedulePojo>().flatMapToDetails(userDataRoot)
        }

        override suspend fun fetchScheduleByDate(
            date: Instant,
            numberOfWeek: NumberOfRepeatWeek,
            targetUser: UID
        ): Flow<BaseScheduleDetailsPojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val dateMillis = date.toEpochMilliseconds()
            val dateTime = date.dateTime()
            val dayOfWeek = dateTime.dayOfWeek.toString()
            val week = numberOfWeek.toString()

            val scheduleFlow = userDataRoot.collection(UserData.BASE_SCHEDULES)
                .where {
                    val toVersionFilter = UserData.VERSION_TO greaterThanOrEqualTo dateMillis
                    val fromVersionFilter = UserData.VERSION_FROM lessThanOrEqualTo dateMillis
                    val dateFilter = (UserData.WEEK equalTo week) and (UserData.DAY_OF_WEEK equalTo dayOfWeek)
                    return@where toVersionFilter and fromVersionFilter and dateFilter
                }
                .orderBy(UserData.VERSION_TO, Direction.DESCENDING)
                .snapshotListFlowGet<BaseSchedulePojo>()
                .map { it.getOrNull(0) }
                .flatMapToDetails(userDataRoot)

            return scheduleFlow
        }

        override suspend fun fetchSchedulesByVersion(
            from: Instant,
            to: Instant,
            numberOfWeek: NumberOfRepeatWeek?,
            targetUser: UID
        ): Flow<List<BaseScheduleDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()
            val week = numberOfWeek?.toString()

            val schedulesFlow = userDataRoot.collection(UserData.BASE_SCHEDULES)
                .where {
                    val toDateFilter = UserData.VERSION_TO greaterThanOrEqualTo fromMillis
                    val fromDateFilter = UserData.VERSION_FROM lessThanOrEqualTo toMillis
                    val weekFilter = UserData.WEEK equalTo week
                    val dateFilter = toDateFilter and fromDateFilter
                    return@where if (week != null) dateFilter and weekFilter else dateFilter
                }
                .orderBy(UserData.VERSION_TO, Direction.DESCENDING)
                .snapshotListFlowGet<BaseSchedulePojo>()
                .flatMapListToDetails(userDataRoot)

            return schedulesFlow
        }

        override suspend fun fetchClassById(
            uid: UID,
            scheduleId: UID,
            targetUser: UID
        ): Flow<ClassDetailsPojo?> {
            return fetchScheduleById(scheduleId, targetUser).map { scheduleDetailsPojo ->
                scheduleDetailsPojo?.classes?.find { it.uid == uid }
            }
        }

        override suspend fun deleteSchedulesByTimeRange(
            from: Instant,
            to: Instant,
            targetUser: UID
        ) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()

            val schedulesReference = userDataRoot.collection(UserData.BASE_SCHEDULES).where {
                val toDateFilter = UserData.VERSION_TO greaterThanOrEqualTo fromMillis
                val fromDateFilter = UserData.VERSION_FROM lessThanOrEqualTo toMillis
                return@where fromDateFilter and toDateFilter
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

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<BaseSchedulePojo>>.flatMapListToDetails(
            userDataRoot: DocumentReference
        ): Flow<List<BaseScheduleDetailsPojo>> = flatMapLatest { schedules ->
            if (schedules.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = schedules.map { schedulePojo ->
                    schedulePojo.classes.values.map { it.organizationId }
                }.extractAllItemToSet()

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
                    flowOf(schedules),
                    organizationsMapFlow,
                    subjectsMapFlow,
                    employeesMapFlow,
                ) { schedulesList, organizationsMap, subjectsMap, employeesMap ->
                    schedulesList.map { schedule ->
                        schedule.mapToDetails { classPojo ->
                            classPojo.mapToDetails(
                                scheduleId = schedule.uid,
                                organization = checkNotNull(organizationsMap[classPojo.organizationId]),
                                employee = employeesMap[classPojo.teacherId],
                                subject = subjectsMap[classPojo.subjectId]?.mapToDetails(
                                    employee = employeesMap[subjectsMap[classPojo.subjectId]?.teacherId]
                                ),
                            )
                        }
                    }
                }
            }
        }

        private fun Flow<BaseSchedulePojo?>.flatMapToDetails(
            userDataRoot: DocumentReference
        ): Flow<BaseScheduleDetailsPojo?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails(userDataRoot)
                .map { it.getOrNull(0) }
        }
    }
}