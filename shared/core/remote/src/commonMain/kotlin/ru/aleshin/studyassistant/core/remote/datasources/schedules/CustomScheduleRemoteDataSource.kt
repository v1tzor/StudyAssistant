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
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.extractAllItemToSet
import ru.aleshin.studyassistant.core.common.extensions.observeCollectionMapByField
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotFlowGet
import ru.aleshin.studyassistant.core.common.extensions.snapshotGet
import ru.aleshin.studyassistant.core.common.extensions.snapshotListFlowGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.UserData
import ru.aleshin.studyassistant.core.remote.mappers.schedules.mapToDetails
import ru.aleshin.studyassistant.core.remote.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.remote.models.classes.ClassDetailsPojo
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

            val scheduleId = schedule.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            return reference.document(scheduleId).set(data = schedule.copy(uid = scheduleId)).let {
                return@let scheduleId
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

            return reference.snapshotFlowGet<CustomSchedulePojo>().flatMapToDetails(userDataRoot)
        }

        override suspend fun fetchScheduleByDate(date: Instant, targetUser: UID): Flow<CustomScheduleDetailsPojo?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val dateMillis = date.toEpochMilliseconds()

            val reference = userDataRoot.collection(UserData.CUSTOM_SCHEDULES).where {
                UserData.CUSTOM_SCHEDULE_DATE equalTo dateMillis
            }.orderBy(UserData.CUSTOM_SCHEDULE_DATE, Direction.DESCENDING)

            val scheduleFlow = reference
                .snapshotListFlowGet<CustomSchedulePojo>()
                .flatMapListToDetails(userDataRoot)
                .map { it.getOrNull(0) }

            return scheduleFlow
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

            val schedulesFlow = userDataRoot.collection(UserData.CUSTOM_SCHEDULES)
                .where {
                    val firstDateFilter = UserData.CUSTOM_SCHEDULE_DATE greaterThanOrEqualTo fromMillis
                    val secondDateFilter = UserData.CUSTOM_SCHEDULE_DATE lessThanOrEqualTo toMillis
                    return@where firstDateFilter and secondDateFilter
                }
                .orderBy(UserData.CUSTOM_SCHEDULE_DATE, Direction.DESCENDING)
                .snapshotListFlowGet<CustomSchedulePojo>()
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

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<CustomSchedulePojo>>.flatMapListToDetails(
            userDataRoot: DocumentReference
        ): Flow<List<CustomScheduleDetailsPojo>> = flatMapLatest { schedules ->
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

        private fun Flow<CustomSchedulePojo?>.flatMapToDetails(
            userDataRoot: DocumentReference
        ): Flow<CustomScheduleDetailsPojo?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails(userDataRoot)
                .map { it.getOrNull(0) }
        }
    }
}