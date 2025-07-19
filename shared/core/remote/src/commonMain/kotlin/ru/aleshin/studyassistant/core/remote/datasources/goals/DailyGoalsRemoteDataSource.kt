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

package ru.aleshin.studyassistant.core.remote.datasources.goals

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import ru.aleshin.studyassistant.core.api.AppwriteApi.Employee
import ru.aleshin.studyassistant.core.api.AppwriteApi.Goals
import ru.aleshin.studyassistant.core.api.AppwriteApi.Homeworks
import ru.aleshin.studyassistant.core.api.AppwriteApi.Organizations
import ru.aleshin.studyassistant.core.api.AppwriteApi.Subjects
import ru.aleshin.studyassistant.core.api.AppwriteApi.Todo
import ru.aleshin.studyassistant.core.api.databases.DatabaseApi
import ru.aleshin.studyassistant.core.api.utils.Permission
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.remote.mappers.goals.mapToDetails
import ru.aleshin.studyassistant.core.remote.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.remote.mappers.tasks.convertToDetails
import ru.aleshin.studyassistant.core.remote.mappers.tasks.mapToDetails
import ru.aleshin.studyassistant.core.remote.models.goals.GoalDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.goals.GoalPojo
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationShortPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.HomeworkPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 19.04.2025.
 */
interface DailyGoalsRemoteDataSource {

    suspend fun addOrUpdateGoal(goal: GoalPojo, targetUser: UID): UID
    suspend fun addDailyDailyGoals(dailyGoals: List<GoalPojo>, targetUser: UID)
    suspend fun fetchGoalById(uid: UID, targetUser: UID): Flow<GoalDetailsPojo?>
    suspend fun fetchGoalByContentId(contentId: UID, targetUser: UID): Flow<GoalDetailsPojo?>
    suspend fun fetchDailyGoalsByTimeRange(from: Long, to: Long, targetUser: UID): Flow<List<GoalDetailsPojo>>
    suspend fun fetchShortDailyGoalsByTimeRange(from: Long, to: Long, targetUser: UID): Flow<List<GoalPojo>>
    suspend fun fetchShortActiveDailyGoals(targetUser: UID): Flow<List<GoalPojo>>
    suspend fun fetchOverdueDailyGoals(currentDate: Long, targetUser: UID): Flow<List<GoalDetailsPojo>>
    suspend fun fetchDailyGoalsByDate(date: Long, targetUser: UID): Flow<List<GoalDetailsPojo>>
    suspend fun deleteGoal(uid: UID, targetUser: UID)
    suspend fun deleteAllDailyGoals(targetUser: UID)

    class Base(
        private val database: DatabaseApi,
    ) : DailyGoalsRemoteDataSource {

        override suspend fun addOrUpdateGoal(goal: GoalPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val goalId = goal.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            database.upsertDocument(
                databaseId = Goals.DATABASE_ID,
                collectionId = Goals.COLLECTION_ID,
                documentId = goalId,
                data = goal.copy(uid = goalId),
                permissions = Permission.onlyUserData(targetUser),
                nestedType = GoalPojo.serializer(),
            )

            return goalId
        }

        override suspend fun addDailyDailyGoals(dailyGoals: List<GoalPojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            dailyGoals.forEach { dailyGoal -> addOrUpdateGoal(dailyGoal, targetUser) }
        }

        override suspend fun fetchGoalById(uid: UID, targetUser: UID): Flow<GoalDetailsPojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val goalFlow = database.getDocumentFlow(
                databaseId = Goals.DATABASE_ID,
                collectionId = Goals.COLLECTION_ID,
                documentId = uid,
                nestedType = GoalPojo.serializer(),
            )

            return goalFlow.flatMapToDetails(targetUser)
        }

        override suspend fun fetchGoalByContentId(contentId: UID, targetUser: UID): Flow<GoalDetailsPojo?> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val goalsFlow = database.listDocumentsFlow(
                databaseId = Goals.DATABASE_ID,
                collectionId = Goals.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Goals.USER_ID, targetUser),
                    Query.equal(Goals.CONTENT_ID, contentId)
                ),
                nestedType = GoalPojo.serializer(),
            )

            return goalsFlow.flatMapListToDetails(targetUser).map { it.getOrNull(0) }
        }

        override suspend fun fetchDailyGoalsByTimeRange(
            from: Long,
            to: Long,
            targetUser: UID
        ): Flow<List<GoalDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val goalsFlow = database.listDocumentsFlow(
                databaseId = Goals.DATABASE_ID,
                collectionId = Goals.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Goals.USER_ID, targetUser),
                    Query.between(Goals.TARGET_DATE, from, to),
                    Query.orderDesc(Goals.TARGET_DATE)
                ),
                nestedType = GoalPojo.serializer(),
            )

            return goalsFlow.flatMapListToDetails(targetUser)
        }

        override suspend fun fetchShortDailyGoalsByTimeRange(
            from: Long,
            to: Long,
            targetUser: UID
        ): Flow<List<GoalPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val goalsFlow = database.listDocumentsFlow(
                databaseId = Goals.DATABASE_ID,
                collectionId = Goals.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Goals.USER_ID, targetUser),
                    Query.between(Goals.TARGET_DATE, from, to),
                    Query.orderDesc(Goals.TARGET_DATE)
                ),
                nestedType = GoalPojo.serializer(),
            )

            return goalsFlow
        }

        override suspend fun fetchShortActiveDailyGoals(targetUser: UID): Flow<List<GoalPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val goalsFlow = database.listDocumentsFlow(
                databaseId = Goals.DATABASE_ID,
                collectionId = Goals.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Goals.USER_ID, targetUser),
                    Query.greaterThanEqual(Goals.DONE, false),
                    Query.isNull(Goals.COMPLETE_DATE),
                ),
                nestedType = GoalPojo.serializer(),
            )

            return goalsFlow
        }

        override suspend fun fetchOverdueDailyGoals(
            currentDate: Long,
            targetUser: UID
        ): Flow<List<GoalDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val goalsFlow = database.listDocumentsFlow(
                databaseId = Goals.DATABASE_ID,
                collectionId = Goals.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Goals.USER_ID, targetUser),
                    Query.lessThan(Goals.TARGET_DATE, currentDate),
                    Query.equal(Goals.DONE, false),
                    Query.isNull(Goals.COMPLETE_DATE),
                    Query.orderDesc(Goals.TARGET_DATE)
                ),
                nestedType = GoalPojo.serializer(),
            )

            return goalsFlow.flatMapListToDetails(targetUser)
        }

        override suspend fun fetchDailyGoalsByDate(
            date: Long,
            targetUser: UID
        ): Flow<List<GoalDetailsPojo>> {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            val goalsFlow = database.listDocumentsFlow(
                databaseId = Goals.DATABASE_ID,
                collectionId = Goals.COLLECTION_ID,
                queries = listOf(
                    Query.equal(Goals.USER_ID, targetUser),
                    Query.equal(Goals.TARGET_DATE, date),
                    Query.orderDesc(Goals.TARGET_DATE)
                ),
                nestedType = GoalPojo.serializer(),
            )

            return goalsFlow.flatMapListToDetails(targetUser)
        }

        override suspend fun deleteGoal(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()

            database.deleteDocument(
                databaseId = Goals.DATABASE_ID,
                collectionId = Goals.COLLECTION_ID,
                documentId = targetUser,
            )
        }

        override suspend fun deleteAllDailyGoals(targetUser: UID) {
            if (targetUser.isEmpty()) throw AppwriteUserException()
            val goalsFlow = fetchDailyGoalsByTimeRange(Long.MIN_VALUE, Long.MAX_VALUE, targetUser)
            goalsFlow.first().forEach { goal -> deleteGoal(goal.uid, targetUser) }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<GoalPojo>>.flatMapListToDetails(targetUser: UID) = flatMapLatest { goals ->
            if (goals.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = goals.mapNotNull { it.contentOrganizationId }.toSet().toList()

                val todoIds = goals.filter { it.type == GoalType.TODO.name }.map { it.contentId }
                val todosMapFlow = if (todoIds.isNotEmpty()) {
                    database.listDocumentsFlow(
                        databaseId = Todo.DATABASE_ID,
                        collectionId = Todo.COLLECTION_ID,
                        queries = listOf(Query.equal(Todo.UID, todoIds)),
                        nestedType = TodoPojo.serializer(),
                    ).map { items ->
                        items.map { it.convertToDetails() }.associateBy { it.uid }
                    }
                } else {
                    flowOf(emptyMap())
                }

                val homeworkIds = goals.filter { it.type == GoalType.HOMEWORK.name }.map { it.contentId }
                val homeworksMapFlow = if (homeworkIds.isNotEmpty()) {
                    database.listDocumentsFlow(
                        databaseId = Homeworks.DATABASE_ID,
                        collectionId = Homeworks.COLLECTION_ID,
                        queries = listOf(Query.equal(Homeworks.UID, homeworkIds)),
                        nestedType = HomeworkPojo.serializer(),
                    ).map { items ->
                        items.associateBy { it.uid }
                    }
                } else {
                    flowOf(emptyMap())
                }

                val organizationsMapFlow = if (organizationsIds.isNotEmpty()) {
                    database.listDocumentsFlow(
                        databaseId = Organizations.DATABASE_ID,
                        collectionId = Organizations.COLLECTION_ID,
                        queries = listOf(Query.equal(Organizations.UID, organizationsIds)),
                        nestedType = OrganizationShortPojo.serializer(),
                    ).map { items ->
                        items.associateBy { it.uid }
                    }
                } else {
                    flowOf(emptyMap())
                }

                val subjectsMapFlow = if (organizationsIds.isNotEmpty()) {
                    database.listDocumentsFlow(
                        databaseId = Subjects.DATABASE_ID,
                        collectionId = Subjects.COLLECTION_ID,
                        queries = listOf(Query.equal(Subjects.ORGANIZATION_ID, organizationsIds)),
                        nestedType = SubjectPojo.serializer(),
                    ).map { items ->
                        items.associateBy { it.uid }
                    }
                } else {
                    flowOf(emptyMap())
                }

                val employeesMapFlow = if (organizationsIds.isNotEmpty()) {
                    database.listDocumentsFlow(
                        databaseId = Employee.DATABASE_ID,
                        collectionId = Employee.COLLECTION_ID,
                        queries = listOf(Query.equal(Employee.ORGANIZATION_ID, organizationsIds)),
                        nestedType = EmployeePojo.serializer(),
                    ).map { items ->
                        items.associateBy { it.uid }
                    }
                } else {
                    flowOf(emptyMap())
                }

                val homeworksDetailsMapFlow = combine(
                    homeworksMapFlow,
                    organizationsMapFlow,
                    subjectsMapFlow,
                    employeesMapFlow,
                ) { homeworksMap, organizationsMap, subjectsMap, employeesMap ->
                    homeworksMap.mapValues { homework ->
                        homework.value.mapToDetails(
                            organization = checkNotNull(organizationsMap[homework.value.organizationId]),
                            subject = subjectsMap[homework.value.subjectId]?.mapToDetails(
                                employee = employeesMap[subjectsMap[homework.value.subjectId]?.teacherId]
                            ),
                        )
                    }
                }

                combine(
                    flowOf(goals),
                    homeworksDetailsMapFlow,
                    todosMapFlow,
                ) { goalsList, homeworksMap, todosMap ->
                    goalsList.map { goal ->
                        goal.mapToDetails(
                            homeworksMapper = { homeworksMap[goal.contentId] },
                            todoMapper = { todosMap[goal.contentId] },
                        )
                    }
                }
            }
        }

        private fun Flow<GoalPojo?>.flatMapToDetails(targetUser: UID): Flow<GoalDetailsPojo?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails(targetUser)
                .map { it.getOrNull(0) }
        }
    }
}