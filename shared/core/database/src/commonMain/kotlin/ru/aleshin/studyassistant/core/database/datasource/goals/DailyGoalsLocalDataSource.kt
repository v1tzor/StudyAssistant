/*
 * Copyright 2026 Stanislav Aleshin
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.employee.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.goals.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.goals.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.goals.mapToEntity
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.subjects.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.tasks.mapToDetails
import ru.aleshin.studyassistant.core.database.models.goals.BaseGoalEntity
import ru.aleshin.studyassistant.core.database.models.goals.GoalEntityDetails
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.database.models.organizations.ScheduleTimeIntervalsEntity
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeQueries
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

    suspend fun addOrUpdateGoal(item: BaseGoalEntity)
    suspend fun addOrUpdateGoals(items: List<BaseGoalEntity>)
    suspend fun fetchGoalDetailsById(uid: UID): Flow<GoalEntityDetails?>
    suspend fun fetchGoalDetailsByDate(date: Long): Flow<List<GoalEntityDetails>>
    suspend fun fetchGoalDetailsByContentId(uid: UID): Flow<GoalEntityDetails?>
    suspend fun fetchGoalsDetailsByTimeRange(from: Long, to: Long): Flow<List<GoalEntityDetails>>
    suspend fun fetchShortGoalsByTimeRange(from: Long, to: Long): Flow<List<BaseGoalEntity>>
    suspend fun fetchShortActiveDailyGoals(): Flow<List<BaseGoalEntity>>
    suspend fun fetchOverdueGoalsDetails(currentDate: Long): Flow<List<GoalEntityDetails>>
    suspend fun deleteGoalsByIds(ids: List<String>)

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
            get() = coroutineManager.ioDispatcher

        override suspend fun addOrUpdateGoal(item: BaseGoalEntity) {
            val uid = item.uid.ifEmpty { randomUUID() }
            val updatedItem = item.copy(uid = uid).mapToEntity()
            goalQueries.addOrUpdateGoal(updatedItem)
        }

        override suspend fun addOrUpdateGoals(items: List<BaseGoalEntity>) {
            items.forEach { item -> addOrUpdateGoal(item) }
        }

        override suspend fun fetchGoalDetailsById(uid: UID): Flow<GoalEntityDetails?> {
            val query = goalQueries.fetchGoalById(uid)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }.flatMapToDetails()
        }

        override suspend fun fetchGoalDetailsByDate(date: Long): Flow<List<GoalEntityDetails>> {
            val query = goalQueries.fetchGoalsByDate(date)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
        }

        override suspend fun fetchGoalDetailsByContentId(uid: UID): Flow<GoalEntityDetails?> {
            val query = goalQueries.fetchGoalByContentId(uid)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }.flatMapToDetails()
        }

        override suspend fun fetchGoalsDetailsByTimeRange(from: Long, to: Long): Flow<List<GoalEntityDetails>> {
            val query = goalQueries.fetchDailyGoaslByTimeRange(from, to)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
        }

        override suspend fun fetchShortGoalsByTimeRange(from: Long, to: Long): Flow<List<BaseGoalEntity>> {
            val query = goalQueries.fetchDailyGoaslByTimeRange(from, to)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchShortActiveDailyGoals(): Flow<List<BaseGoalEntity>> {
            val query = goalQueries.fetchActiveGoals()
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchOverdueGoalsDetails(currentDate: Long): Flow<List<GoalEntityDetails>> {
            val query = goalQueries.fetchOverdueGoals(currentDate)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
        }

        override suspend fun deleteGoalsByIds(ids: List<String>) {
            goalQueries.deleteGoalsById(ids)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun Flow<List<BaseGoalEntity>>.flatMapListToDetails() = flatMapLatest { goals ->
            if (goals.isEmpty()) {
                flowOf(emptyList())
            } else {
                val organizationsIds = goals.mapNotNull { it.contentOrganizationId }.toSet()
                val fromDeadline = goals.minOf { it.contentDeadline ?: 0 }
                val toDeadline = goals.maxOf { it.contentDeadline ?: Long.MAX_VALUE }

                val todosMapFlow = todoQueries.fetchTodosByTimeRange(fromDeadline, toDeadline)
                    .mapToListFlow(coroutineContext) { it.mapToBase() }
                    .map { todos -> todos.associateBy { it.uid } }

                val homeworksMapFlow = homeworkQueries.fetchHomeworksByTimeRange(fromDeadline, toDeadline)
                    .mapToListFlow(coroutineContext) { it.mapToBase() }
                    .map { homeworks -> homeworks.associateBy { it.uid } }

                val organizationsMapFlow = organizationsQueries.fetchOrganizationsById(
                    uid = organizationsIds,
                    mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel, _, _, locationList, _, offices, _, updatedAt ->
                        val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsEntity>(timeIntervalsModel)
                        val locations = locationList.map {
                            Json.decodeFromString<ContactInfoEntity>(it)
                        }
                        OrganizationShortEntity(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals, updatedAt)
                    },
                ).asFlow()
                    .mapToList(coroutineContext)
                    .map { organization -> organization.associateBy { it.uid } }

                val subjectsMapFlow = subjectQueries.fetchSubjectsByOrganizations(organizationsIds)
                    .mapToListFlow(coroutineContext) { it.mapToBase() }
                    .map { subject -> subject.associateBy { it.uid } }

                val employeesMapFlow = employeeQueries.fetchEmployeesByOrganizations(organizationsIds)
                    .mapToListFlow(coroutineContext) { it.mapToBase() }
                    .map { employee -> employee.associateBy { it.uid } }

                val homeworksDetailsMapFlow = combine(
                    homeworksMapFlow,
                    organizationsMapFlow,
                    subjectsMapFlow,
                    employeesMapFlow,
                ) { homeworksMap, organizationsMap, subjectsMap, employeesMap ->
                    homeworksMap.mapValues { homework ->
                        homework.value.mapToDetails(
                            organization = organizationsMap[homework.value.organizationId],
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

        private fun Flow<BaseGoalEntity?>.flatMapToDetails(): Flow<GoalEntityDetails?> {
            return mapNotNull { it?.let { listOf(it) } ?: emptyList() }.flatMapListToDetails().map { it.getOrNull(0) }
        }
    }
}
