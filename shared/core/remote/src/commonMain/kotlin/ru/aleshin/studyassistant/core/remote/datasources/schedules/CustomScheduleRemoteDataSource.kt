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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.api.AppwriteApi.CustomSchedules
import ru.aleshin.studyassistant.core.api.AppwriteApi.Employee
import ru.aleshin.studyassistant.core.api.AppwriteApi.Organizations
import ru.aleshin.studyassistant.core.api.AppwriteApi.Subjects
import ru.aleshin.studyassistant.core.api.databases.DatabaseApi
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.extractAllItemToSet
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
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
        private val database: DatabaseApi,
    ) : CustomScheduleRemoteDataSource {

        override suspend fun addOrUpdateSchedule(
            schedule: CustomSchedulePojo,
            targetUser: UID
        ): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val scheduleId = schedule.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            database.upsertDocument(
                databaseId = CustomSchedules.DATABASE_ID,
                collectionId = CustomSchedules.COLLECTION_ID,
                documentId = scheduleId,
                data = schedule.copy(uid = scheduleId),
                permissions = Permission.onlyUserData(targetUser),
                nestedType = CustomSchedulePojo.serializer(),
            )

            return scheduleId
        }

        override suspend fun addOrUpdateSchedulesGroup(
            schedules: List<CustomSchedulePojo>,
            targetUser: UID
        ) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            schedules.forEach { schedule -> addOrUpdateSchedule(schedule, targetUser) }
        }

        override suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<CustomScheduleDetailsPojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val scheduleFlow = database.getDocumentFlow(
                databaseId = CustomSchedules.DATABASE_ID,
                collectionId = CustomSchedules.COLLECTION_ID,
                documentId = uid,
                nestedType = CustomSchedulePojo.serializer(),
            )

            return scheduleFlow.flatMapToDetails()
        }

        override suspend fun fetchScheduleByDate(date: Instant, targetUser: UID): Flow<CustomScheduleDetailsPojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val dateMillis = date.toEpochMilliseconds()

            val scheduleFlow = database.listDocumentsFlow(
                databaseId = CustomSchedules.DATABASE_ID,
                collectionId = CustomSchedules.COLLECTION_ID,
                queries = listOf(
                    Query.equal(CustomSchedules.USER_ID, targetUser),
                    Query.equal(CustomSchedules.DATE, dateMillis),
                    Query.orderDesc(CustomSchedules.DATE),
                ),
                nestedType = CustomSchedulePojo.serializer(),
            )

            return scheduleFlow.flatMapListToDetails().map { it.getOrNull(0) }
        }

        override suspend fun fetchSchedulesByTimeRange(
            from: Instant,
            to: Instant,
            targetUser: UID
        ): Flow<List<CustomScheduleDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()

            val schedulesFlow = database.listDocumentsFlow(
                databaseId = CustomSchedules.DATABASE_ID,
                collectionId = CustomSchedules.COLLECTION_ID,
                queries = listOf(
                    Query.equal(CustomSchedules.USER_ID, targetUser),
                    Query.between(CustomSchedules.DATE, fromMillis, toMillis),
                    Query.orderDesc(CustomSchedules.DATE),
                ),
                nestedType = CustomSchedulePojo.serializer(),
            )

            return schedulesFlow.flatMapListToDetails()
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
            if (targetUser.isEmpty()) throw AppwriteUserException()

            database.deleteDocument(
                databaseId = CustomSchedules.DATABASE_ID,
                collectionId = CustomSchedules.COLLECTION_ID,
                documentId = scheduleId,
            )
        }

        override suspend fun deleteSchedulesByTimeRange(
            from: Instant,
            to: Instant,
            targetUser: UID
        ) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val schedules = fetchSchedulesByTimeRange(from, to, targetUser).first()
            schedules.forEach { deleteScheduleById(it.uid, targetUser) }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<CustomSchedulePojo>>.flatMapListToDetails() = flatMapLatest { schedules ->
            if (schedules.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = schedules.map { schedulePojo ->
                    schedulePojo.classes.map { Json.decodeFromString<ClassPojo>(it).organizationId }
                }.extractAllItemToSet().toList()

                val organizationsMapFlow = database.listDocumentsFlow(
                    databaseId = Organizations.DATABASE_ID,
                    collectionId = Organizations.COLLECTION_ID,
                    queries = listOf(Query.equal(Organizations.UID, organizationsIds)),
                    nestedType = OrganizationShortPojo.serializer(),
                ).map { items ->
                    items.associateBy { it.uid }
                }

                val subjectsMapFlow = database.listDocumentsFlow(
                    databaseId = Subjects.DATABASE_ID,
                    collectionId = Subjects.COLLECTION_ID,
                    queries = listOf(Query.equal(Subjects.ORGANIZATION_ID, organizationsIds)),
                    nestedType = SubjectPojo.serializer(),
                ).map { items ->
                    items.associateBy { it.uid }
                }

                val employeesMapFlow = database.listDocumentsFlow(
                    databaseId = Employee.DATABASE_ID,
                    collectionId = Employee.COLLECTION_ID,
                    queries = listOf(Query.equal(Employee.ORGANIZATION_ID, organizationsIds)),
                    nestedType = EmployeePojo.serializer(),
                ).map { items ->
                    items.associateBy { it.uid }
                }

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

        private fun Flow<CustomSchedulePojo?>.flatMapToDetails(): Flow<CustomScheduleDetailsPojo?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }
}