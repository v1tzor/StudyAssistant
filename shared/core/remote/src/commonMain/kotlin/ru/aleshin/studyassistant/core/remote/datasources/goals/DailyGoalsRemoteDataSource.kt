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
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.extensions.deleteAll
import ru.aleshin.studyassistant.core.common.extensions.observeCollectionMapByField
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.snapshotFlowGet
import ru.aleshin.studyassistant.core.common.extensions.snapshotListFlowGet
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.UserData
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantFirebase.UserData.GOAL_CONTENT_ID
import ru.aleshin.studyassistant.core.remote.mappers.goals.mapToDetails
import ru.aleshin.studyassistant.core.remote.mappers.subjects.mapToDetails
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
        private val database: FirebaseFirestore,
    ) : DailyGoalsRemoteDataSource {

        override suspend fun addOrUpdateGoal(goal: GoalPojo, targetUser: UID): UID {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.GOALS)

            val goalId = goal.uid.takeIf { it.isNotBlank() } ?: randomUUID()

            return reference.document(goalId).set(goal.copy(uid = goalId)).let {
                return@let goalId
            }
        }

        override suspend fun addDailyDailyGoals(dailyGoals: List<GoalPojo>, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.GOALS)

            database.batch().apply {
                dailyGoals.forEach { goal ->
                    val goalId = goal.uid.takeIf { it.isNotBlank() } ?: randomUUID()
                    set(reference.document(goalId), goal.copy(uid = goalId))
                }
                return@apply commit()
            }
        }

        override suspend fun fetchGoalById(uid: UID, targetUser: UID): Flow<GoalDetailsPojo?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.GOALS).document(uid)

            return reference.snapshotFlowGet<GoalPojo>().flatMapToDetails(userDataRoot)
        }

        override suspend fun fetchGoalByContentId(contentId: UID, targetUser: UID): Flow<GoalDetailsPojo?> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val goal = userDataRoot.collection(UserData.GOALS)
                .where { GOAL_CONTENT_ID equalTo contentId }
                .snapshotListFlowGet<GoalPojo>()
                .flatMapListToDetails(userDataRoot)
                .map { it.getOrNull(0) }

            return goal
        }

        override suspend fun fetchDailyGoalsByTimeRange(
            from: Long,
            to: Long,
            targetUser: UID
        ): Flow<List<GoalDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val goalsFlow = userDataRoot.collection(UserData.GOALS)
                .where {
                    val fromDateFilter = UserData.GOAL_TARGET_DATA greaterThanOrEqualTo from
                    val toDateFilter = UserData.GOAL_TARGET_DATA lessThanOrEqualTo to
                    return@where fromDateFilter and toDateFilter
                }
                .orderBy(UserData.GOAL_TARGET_DATA, Direction.DESCENDING)
                .snapshotListFlowGet<GoalPojo>()
                .flatMapListToDetails(userDataRoot)

            return goalsFlow
        }

        override suspend fun fetchShortDailyGoalsByTimeRange(
            from: Long,
            to: Long,
            targetUser: UID
        ): Flow<List<GoalPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val goalsFlow = userDataRoot.collection(UserData.GOALS)
                .where {
                    val fromDateFilter = UserData.GOAL_TARGET_DATA greaterThanOrEqualTo from
                    val toDateFilter = UserData.GOAL_TARGET_DATA lessThanOrEqualTo to
                    return@where fromDateFilter and toDateFilter
                }
                .orderBy(UserData.GOAL_TARGET_DATA, Direction.DESCENDING)
                .snapshotListFlowGet<GoalPojo>()

            return goalsFlow
        }

        override suspend fun fetchShortActiveDailyGoals(targetUser: UID): Flow<List<GoalPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val goalsFlow = userDataRoot.collection(UserData.GOALS)
                .where {
                    (UserData.GOAL_DONE equalTo false) and (UserData.GOAL_COMPLETE_DATE equalTo null)
                }
                .orderBy(UserData.GOAL_TARGET_DATA, Direction.DESCENDING)
                .snapshotListFlowGet<GoalPojo>()

            return goalsFlow
        }

        override suspend fun fetchOverdueDailyGoals(
            currentDate: Long,
            targetUser: UID
        ): Flow<List<GoalDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val goalsFlow = userDataRoot.collection(UserData.GOALS)
                .where {
                    val dateFilter = UserData.GOAL_TARGET_DATA lessThan currentDate
                    val doneFilter = UserData.GOAL_DONE equalTo false
                    val completeDateFilter = UserData.GOAL_COMPLETE_DATE equalTo null
                    return@where dateFilter and doneFilter and completeDateFilter
                }
                .orderBy(UserData.GOAL_TARGET_DATA, Direction.DESCENDING)
                .snapshotListFlowGet<GoalPojo>()
                .flatMapListToDetails(userDataRoot)

            return goalsFlow
        }

        override suspend fun fetchDailyGoalsByDate(
            date: Long,
            targetUser: UID
        ): Flow<List<GoalDetailsPojo>> {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val goalsFlow = userDataRoot.collection(UserData.GOALS)
                .where { UserData.GOAL_TARGET_DATA equalTo date }
                .orderBy(UserData.GOAL_TARGET_DATA, Direction.DESCENDING)
                .snapshotListFlowGet<GoalPojo>()
                .flatMapListToDetails(userDataRoot)

            return goalsFlow
        }

        override suspend fun deleteGoal(uid: UID, targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.GOALS).document(uid)

            return reference.delete()
        }

        override suspend fun deleteAllDailyGoals(targetUser: UID) {
            if (targetUser.isEmpty()) throw FirebaseUserException()
            val userDataRoot = database.collection(UserData.ROOT).document(targetUser)

            val reference = userDataRoot.collection(UserData.GOALS)

            database.deleteAll(reference)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<GoalPojo>>.flatMapListToDetails(
            userDataRoot: DocumentReference
        ): Flow<List<GoalDetailsPojo>> = flatMapLatest { goals ->
            if (goals.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = goals.mapNotNull { it.contentOrganizationId }.toSet()
                val fromDeadline = goals.minOf { it.contentDeadline ?: 0 }
                val toDeadline = goals.maxOf { it.contentDeadline ?: Long.MAX_VALUE }
                val todosMapFlow = userDataRoot.collection(UserData.TODOS)
                    .where {
                        (
                            (UserData.TODO_DEADLINE greaterThanOrEqualTo fromDeadline) and
                                (UserData.TODO_DEADLINE lessThanOrEqualTo toDeadline)
                            ) or
                            (UserData.TODO_DEADLINE equalTo null)
                    }
                    .orderBy(UserData.TODO_DEADLINE, Direction.DESCENDING)
                    .snapshotListFlowGet<TodoPojo>()
                    .map { todoPojos -> todoPojos.associateBy { it.uid } }

                val homeworksMapFlow = userDataRoot.collection(UserData.TODOS)
                    .where {
                        (UserData.HOMEWORK_DEADLINE greaterThanOrEqualTo fromDeadline) and
                            (UserData.HOMEWORK_DEADLINE lessThanOrEqualTo toDeadline)
                    }
                    .orderBy(UserData.HOMEWORK_DEADLINE, Direction.DESCENDING)
                    .snapshotListFlowGet<HomeworkPojo>()
                    .map { homeworkPojos -> homeworkPojos.associateBy { it.uid } }

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

                val homeworksDetailsMapFlow = combine(
                    homeworksMapFlow,
                    organizationsMapFlow,
                    subjectsMapFlow,
                    employeesMapFlow,
                ) { homeworksList, organizationsMap, subjectsMap, employeesMap ->
                    homeworksList.mapValues { homework ->
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

        private fun Flow<GoalPojo?>.flatMapToDetails(
            userDataRoot: DocumentReference
        ): Flow<GoalDetailsPojo?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails(userDataRoot)
                .map { it.getOrNull(0) }
        }
    }
}