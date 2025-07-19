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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.api.AppwriteApi.BaseSchedules
import ru.aleshin.studyassistant.core.api.AppwriteApi.Employee
import ru.aleshin.studyassistant.core.api.AppwriteApi.Organizations
import ru.aleshin.studyassistant.core.api.AppwriteApi.Subjects
import ru.aleshin.studyassistant.core.api.databases.DatabaseApi
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.extractAllItemToSet
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
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
    suspend fun deleteSchedulesByTimeRange(from: Instant, to: Instant, targetUser: UID)

    class Base(
        private val database: DatabaseApi,
    ) : BaseScheduleRemoteDataSource {

        override suspend fun addOrUpdateSchedule(
            schedule: BaseSchedulePojo,
            targetUser: UID
        ): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val scheduleId = schedule.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            database.upsertDocument(
                databaseId = BaseSchedules.DATABASE_ID,
                collectionId = BaseSchedules.COLLECTION_ID,
                documentId = scheduleId,
                data = schedule.copy(uid = scheduleId),
                permissions = Permission.onlyUserData(targetUser),
                nestedType = BaseSchedulePojo.serializer(),
            )

            return scheduleId
        }

        override suspend fun addOrUpdateSchedulesGroup(schedules: List<BaseSchedulePojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            schedules.forEach { schedule -> addOrUpdateSchedule(schedule, targetUser) }
        }

        override suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<BaseScheduleDetailsPojo?> {
            if (uid.isEmpty()) return flowOf(null)
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val scheduleFlow = database.getDocumentFlow(
                databaseId = BaseSchedules.DATABASE_ID,
                collectionId = BaseSchedules.COLLECTION_ID,
                documentId = uid,
                nestedType = BaseSchedulePojo.serializer(),
            )

            return scheduleFlow.flatMapToDetails()
        }

        override suspend fun fetchScheduleByDate(
            date: Instant,
            numberOfWeek: NumberOfRepeatWeek,
            targetUser: UID
        ): Flow<BaseScheduleDetailsPojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val dateMillis = date.toEpochMilliseconds()
            val dateTime = date.dateTime()
            val dayOfWeek = dateTime.dayOfWeek.toString()
            val week = numberOfWeek.toString()

            val scheduleFlow = database.listDocumentsFlow(
                databaseId = BaseSchedules.DATABASE_ID,
                collectionId = BaseSchedules.COLLECTION_ID,
                queries = listOf(
                    Query.equal(BaseSchedules.USER_ID, targetUser),
                    Query.greaterThanEqual(BaseSchedules.VERSION_TO, dateMillis),
                    Query.lessThanEqual(BaseSchedules.VERSION_FROM, dateMillis),
                    Query.equal(BaseSchedules.WEEK, week),
                    Query.equal(BaseSchedules.DAY_OF_WEEK, dayOfWeek),
                    Query.orderDesc(BaseSchedules.VERSION_TO)
                ),
                nestedType = BaseSchedulePojo.serializer(),
            )

            return scheduleFlow.map { it.getOrNull(0) }.flatMapToDetails()
        }

        override suspend fun fetchSchedulesByVersion(
            from: Instant,
            to: Instant,
            numberOfWeek: NumberOfRepeatWeek?,
            targetUser: UID
        ): Flow<List<BaseScheduleDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()
            val week = numberOfWeek?.toString()

            val schedulesFlow = database.listDocumentsFlow(
                databaseId = BaseSchedules.DATABASE_ID,
                collectionId = BaseSchedules.COLLECTION_ID,
                queries = listOf(
                    Query.equal(BaseSchedules.USER_ID, targetUser),
                    Query.greaterThanEqual(BaseSchedules.VERSION_TO, fromMillis),
                    Query.lessThanEqual(BaseSchedules.VERSION_FROM, toMillis),
                    if (week == null) {
                        Query.isNotNull(BaseSchedules.WEEK)
                    } else {
                        Query.equal(BaseSchedules.WEEK, week)
                    },
                    Query.orderDesc(BaseSchedules.VERSION_TO),
                ),
                nestedType = BaseSchedulePojo.serializer(),
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

        override suspend fun deleteSchedulesByTimeRange(
            from: Instant,
            to: Instant,
            targetUser: UID
        ) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val fromMillis = from.toEpochMilliseconds()
            val toMillis = to.toEpochMilliseconds()

            val schedules = database.listDocuments(
                databaseId = BaseSchedules.DATABASE_ID,
                collectionId = BaseSchedules.COLLECTION_ID,
                queries = listOf(
                    Query.equal(BaseSchedules.USER_ID, targetUser),
                    Query.greaterThanEqual(BaseSchedules.VERSION_TO, fromMillis),
                    Query.lessThanEqual(BaseSchedules.VERSION_FROM, toMillis),
                    Query.orderDesc(BaseSchedules.VERSION_TO),
                ),
                nestedType = BaseSchedulePojo.serializer(),
            )

            schedules.documents.forEach {
                database.deleteDocument(
                    databaseId = BaseSchedules.DATABASE_ID,
                    collectionId = BaseSchedules.COLLECTION_ID,
                    documentId = it.data.uid,
                )
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<BaseSchedulePojo>>.flatMapListToDetails() = flatMapLatest { schedules ->
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

        private fun Flow<BaseSchedulePojo?>.flatMapToDetails(): Flow<BaseScheduleDetailsPojo?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }
}