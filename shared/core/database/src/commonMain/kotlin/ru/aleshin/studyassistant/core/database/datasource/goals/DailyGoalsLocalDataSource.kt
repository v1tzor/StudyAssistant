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

package ru.aleshin.studyassistant.core.database.datasource.goals

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.goals.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToDetails
import ru.aleshin.studyassistant.core.database.models.goals.GoalEntityDetails
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.database.models.organizations.ScheduleTimeIntervalsEntity
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
import ru.aleshin.studyassistant.sqldelight.goals.GoalEntity
import ru.aleshin.studyassistant.sqldelight.goals.GoalQueries
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import ru.aleshin.studyassistant.sqldelight.subjects.SubjectQueries
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkQueries
import ru.aleshin.studyassistant.sqldelight.tasks.TodoQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 19.04.2025.
 */
interface DailyGoalsLocalDataSource {

    suspend fun addOrUpdateGoal(goal: GoalEntity): UID
    suspend fun addDailyDailyGoals(dailyGoals: List<GoalEntity>)
    suspend fun fetchGoalById(uid: UID): Flow<GoalEntityDetails?>
    suspend fun fetchGoalByContentId(uid: UID): Flow<GoalEntityDetails?>
    suspend fun fetchDailyGoalsByTimeRange(from: Long, to: Long): Flow<List<GoalEntityDetails>>
    suspend fun fetchShortDailyGoalsByTimeRange(from: Long, to: Long): Flow<List<GoalEntity>>
    suspend fun fetchShortActiveDailyGoals(): Flow<List<GoalEntity>>
    suspend fun fetchOverdueDailyGoals(currentDate: Long): Flow<List<GoalEntityDetails>>
    suspend fun fetchDailyGoalsByDate(date: Long): Flow<List<GoalEntityDetails>>
    suspend fun deleteGoal(uid: UID)
    suspend fun deleteAllDailyGoals()

    class Base(
        private val goalQueries: GoalQueries,
        private val homeworkQueries: HomeworkQueries,
        private val organizationsQueries: OrganizationQueries,
        private val employeeQueries: EmployeeQueries,
        private val subjectQueries: SubjectQueries,
        private val todoQueries: TodoQueries,
        private val coroutineManager: CoroutineManager,
    ) : DailyGoalsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateGoal(goal: GoalEntity): UID {
            val uid = goal.uid.ifEmpty { randomUUID() }
            goalQueries.addOrUpdateGoal(goal.copy(uid = uid))

            return uid
        }

        override suspend fun addDailyDailyGoals(dailyGoals: List<GoalEntity>) {
            dailyGoals.forEach { goalEntity -> addOrUpdateGoal(goalEntity) }
        }

        override suspend fun fetchGoalById(uid: UID): Flow<GoalEntityDetails?> {
            val query = goalQueries.fetchGoalById(uid)
            return query.asFlow().mapToOneOrNull(coroutineContext).flatMapToDetails()
        }

        override suspend fun fetchGoalByContentId(uid: UID): Flow<GoalEntityDetails?> {
            val query = goalQueries.fetchGoalByContentId(uid)
            return query.asFlow().mapToOneOrNull(coroutineContext).flatMapToDetails()
        }

        override suspend fun fetchDailyGoalsByTimeRange(from: Long, to: Long): Flow<List<GoalEntityDetails>> {
            val query = goalQueries.fetchDailyGoaslByTimeRange(from, to)
            return query.asFlow().mapToList(coroutineContext).flatMapListToDetails()
        }

        override suspend fun fetchShortDailyGoalsByTimeRange(from: Long, to: Long): Flow<List<GoalEntity>> {
            val query = goalQueries.fetchDailyGoaslByTimeRange(from, to)
            return query.asFlow().mapToList(coroutineContext)
        }

        override suspend fun fetchShortActiveDailyGoals(): Flow<List<GoalEntity>> {
            val query = goalQueries.fetchActiveGoals()
            return query.asFlow().mapToList(coroutineContext)
        }

        override suspend fun fetchOverdueDailyGoals(currentDate: Long): Flow<List<GoalEntityDetails>> {
            val query = goalQueries.fetchOverdueGoals(currentDate)
            return query.asFlow().mapToList(coroutineContext).flatMapListToDetails()
        }

        override suspend fun fetchDailyGoalsByDate(date: Long): Flow<List<GoalEntityDetails>> {
            val query = goalQueries.fetchGoalsByDate(date)
            return query.asFlow().mapToList(coroutineContext).flatMapListToDetails()
        }

        override suspend fun deleteGoal(uid: UID) {
            goalQueries.deleteGoal(uid)
        }

        override suspend fun deleteAllDailyGoals() {
            goalQueries.deleteAllGoals()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<GoalEntity>>.flatMapListToDetails() = flatMapLatest { goals ->
            if (goals.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = goals.mapNotNull { it.content_organization_id }.toSet()
                val fromDeadline = goals.minOf { it.content_deadline ?: 0 }
                val toDeadline = goals.maxOf { it.content_deadline ?: Long.MAX_VALUE }

                val todosMapFlow = todoQueries.fetchTodosByTimeRange(fromDeadline, toDeadline)
                    .asFlow()
                    .mapToList(coroutineContext)
                    .map { todos -> todos.associateBy { it.uid } }

                val homeworksMapFlow = homeworkQueries.fetchHomeworksByTimeRange(fromDeadline, toDeadline)
                    .asFlow()
                    .mapToList(coroutineContext)
                    .map { homeworks -> homeworks.associateBy { it.uid } }

                val organizationsMapFlow = organizationsQueries
                    .fetchOrganizationsById(
                        uid = organizationsIds,
                        mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel,
                                   _, _, locationList, _, offices, _ ->
                            val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsEntity>(timeIntervalsModel)
                            val locations = locationList.map { Json.decodeFromString<ContactInfoEntity>(it) }
                            OrganizationShortEntity(
                                uid,
                                isMain == 1L,
                                name,
                                type,
                                avatar,
                                locations,
                                offices,
                                timeIntervals
                            )
                        },
                    )
                    .asFlow()
                    .mapToList(coroutineContext)
                    .map { organization -> organization.associateBy { it.uid } }

                val subjectsMapFlow = subjectQueries.fetchSubjectsByOrganizations(organizationsIds)
                    .asFlow()
                    .mapToList(coroutineContext)
                    .map { subject -> subject.associateBy { it.uid } }

                val employeesMapFlow = employeeQueries.fetchEmployeesByOrganizations(organizationsIds)
                    .asFlow()
                    .mapToList(coroutineContext)
                    .map { employee -> employee.associateBy { it.uid } }

                val homeworksDetailsMapFlow = combine(
                    homeworksMapFlow,
                    organizationsMapFlow,
                    subjectsMapFlow,
                    employeesMapFlow,
                ) { homeworksMap, organizationsMap, subjectsMap, employeesMap ->
                    homeworksMap.mapValues { homework ->
                        homework.value.mapToDetails(
                            organization = checkNotNull(organizationsMap[homework.value.organization_id]),
                            subject = subjectsMap[homework.value.subject_id]?.mapToDetails(
                                employee = employeesMap[subjectsMap[homework.value.subject_id]?.teacher_id]
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
                            homeworksMapper = { homeworksMap[goal.content_id] },
                            todoMapper = { todosMap[goal.content_id] },
                        )
                    }
                }
            }
        }

        private fun Flow<GoalEntity?>.flatMapToDetails(): Flow<GoalEntityDetails?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                .flatMapListToDetails()
                .map { it.getOrNull(0) }
        }
    }
}