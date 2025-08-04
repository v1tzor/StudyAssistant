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

import app.cash.sqldelight.async.coroutines.awaitAsList
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
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.datasource.goals.DailyGoalsLocalDataSource.OfflineStorage
import ru.aleshin.studyassistant.core.database.datasource.goals.DailyGoalsLocalDataSource.SyncStorage
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
import ru.aleshin.studyassistant.core.database.utils.CombinedLocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalMultipleDocumentsCommands
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
interface DailyGoalsLocalDataSource : CombinedLocalDataSource<BaseGoalEntity, OfflineStorage, SyncStorage> {

    interface Commands : LocalMultipleDocumentsCommands<BaseGoalEntity> {

        suspend fun fetchGoalDetailsById(uid: UID): Flow<GoalEntityDetails?>
        suspend fun fetchGoalDetailsByDate(date: Long): Flow<List<GoalEntityDetails>>
        suspend fun fetchGoalDetailsByContentId(uid: UID): Flow<GoalEntityDetails?>
        suspend fun fetchGoalsDetailsByTimeRange(from: Long, to: Long): Flow<List<GoalEntityDetails>>
        suspend fun fetchShortGoalsByTimeRange(from: Long, to: Long): Flow<List<BaseGoalEntity>>
        suspend fun fetchShortActiveDailyGoals(): Flow<List<BaseGoalEntity>>
        suspend fun fetchOverdueGoalsDetails(currentDate: Long): Flow<List<GoalEntityDetails>>
        suspend fun fetchAllGoals(): Flow<List<BaseGoalEntity>>

        abstract class Abstract(
            isCacheSource: Boolean,
            private val goalQueries: GoalQueries,
            private val homeworkQueries: HomeworkQueries,
            private val organizationsQueries: OrganizationQueries,
            private val employeeQueries: EmployeeQueries,
            private val subjectQueries: SubjectQueries,
            private val todoQueries: TodoQueries,
            private val coroutineManager: CoroutineManager,
        ) : Commands {

            private val coroutineContext: CoroutineContext
                get() = coroutineManager.backgroundDispatcher

            private val isCacheData = if (isCacheSource) 1L else 0L

            override suspend fun addOrUpdateItem(item: BaseGoalEntity) {
                val uid = item.uid.ifEmpty { randomUUID() }
                val updatedItem = item.copy(uid = uid, isCacheData = isCacheData).mapToEntity()
                goalQueries.addOrUpdateGoal(updatedItem).await()
            }

            override suspend fun addOrUpdateItems(items: List<BaseGoalEntity>) {
                items.forEach { item -> addOrUpdateItem(item) }
            }

            override suspend fun fetchItemById(id: String): Flow<BaseGoalEntity?> {
                val query = goalQueries.fetchGoalById(id, isCacheData)
                return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchItemsById(ids: List<String>): Flow<List<BaseGoalEntity>> {
                val query = goalQueries.fetchGoalsById(ids, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchGoalDetailsById(uid: UID): Flow<GoalEntityDetails?> {
                return fetchItemById(uid).flatMapToDetails()
            }

            override suspend fun fetchGoalDetailsByDate(date: Long): Flow<List<GoalEntityDetails>> {
                val query = goalQueries.fetchGoalsByDate(date, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
            }

            override suspend fun fetchGoalDetailsByContentId(uid: UID): Flow<GoalEntityDetails?> {
                val query = goalQueries.fetchGoalByContentId(uid, isCacheData)
                return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }.flatMapToDetails()
            }

            override suspend fun fetchGoalsDetailsByTimeRange(from: Long, to: Long): Flow<List<GoalEntityDetails>> {
                val query = goalQueries.fetchDailyGoaslByTimeRange(from, to, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
            }

            override suspend fun fetchShortGoalsByTimeRange(from: Long, to: Long): Flow<List<BaseGoalEntity>> {
                val query = goalQueries.fetchDailyGoaslByTimeRange(from, to, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchShortActiveDailyGoals(): Flow<List<BaseGoalEntity>> {
                val query = goalQueries.fetchActiveGoals(isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchOverdueGoalsDetails(currentDate: Long): Flow<List<GoalEntityDetails>> {
                val query = goalQueries.fetchOverdueGoals(currentDate, isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }.flatMapListToDetails()
            }

            override suspend fun fetchAllGoals(): Flow<List<BaseGoalEntity>> {
                val query = goalQueries.fetchAllGoals(isCacheData)
                return query.mapToListFlow(coroutineContext) { it.mapToBase() }
            }

            override suspend fun fetchAllMetadata(): List<MetadataModel> {
                val query = goalQueries.fetchEmptyGoals()
                return query.awaitAsList().map { entity ->
                    MetadataModel(entity.uid, entity.updated_at)
                }
            }

            override suspend fun deleteItemsById(ids: List<String>) {
                goalQueries.deleteGoalsById(ids, isCacheData).await()
            }

            override suspend fun deleteAllItems() {
                goalQueries.deleteAllGoals(isCacheData).await()
            }

            @OptIn(ExperimentalCoroutinesApi::class)
            private fun Flow<List<BaseGoalEntity>>.flatMapListToDetails() = flatMapLatest { goals ->
                if (goals.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val organizationsIds = goals.mapNotNull { it.contentOrganizationId }.toSet()
                    val fromDeadline = goals.minOf { it.contentDeadline ?: 0 }
                    val toDeadline = goals.maxOf { it.contentDeadline ?: Long.MAX_VALUE }

                    val todosMapFlow = todoQueries.fetchTodosByTimeRange(fromDeadline, toDeadline, isCacheData)
                        .mapToListFlow(coroutineContext) { it.mapToBase() }
                        .map { todos -> todos.associateBy { it.uid } }

                    val homeworksMapFlow = homeworkQueries.fetchHomeworksByTimeRange(fromDeadline, toDeadline, isCacheData)
                        .mapToListFlow(coroutineContext) { it.mapToBase() }
                        .map { homeworks -> homeworks.associateBy { it.uid } }

                    val organizationsMapFlow = organizationsQueries.fetchOrganizationsById(
                        uid = organizationsIds,
                        is_cache_data = isCacheData,
                        mapper = { uid, isMain, name, _, type, avatar, timeIntervalsModel, _, _, locationList, _, offices, _, updatedAt, _ ->
                            val timeIntervals = Json.decodeFromString<ScheduleTimeIntervalsEntity>(
                                timeIntervalsModel
                            )
                            val locations = locationList.map {
                                Json.decodeFromString<ContactInfoEntity>(it)
                            }
                            OrganizationShortEntity(uid, isMain == 1L, name, type, avatar, locations, offices, timeIntervals, updatedAt)
                        },
                    ).asFlow()
                        .mapToList(coroutineContext)
                        .map { organization -> organization.associateBy { it.uid } }

                    val subjectsMapFlow = subjectQueries.fetchSubjectsByOrganizations(organizationsIds, isCacheData)
                        .mapToListFlow(coroutineContext) { it.mapToBase() }
                        .map { subject -> subject.associateBy { it.uid } }

                    val employeesMapFlow = employeeQueries.fetchEmployeesByOrganizations(organizationsIds, isCacheData)
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
                return mapNotNull { it?.let { listOf(it) } ?: emptyList() }
                    .flatMapListToDetails()
                    .map { it.getOrNull(0) }
            }
        }
    }

    interface OfflineStorage : LocalDataSource.OnlyOffline, Commands {

        class Base(
            goalQueries: GoalQueries,
            homeworkQueries: HomeworkQueries,
            organizationsQueries: OrganizationQueries,
            employeeQueries: EmployeeQueries,
            subjectQueries: SubjectQueries,
            todoQueries: TodoQueries,
            coroutineManager: CoroutineManager,
        ) : OfflineStorage, Commands.Abstract(
            isCacheSource = false,
            goalQueries = goalQueries,
            homeworkQueries = homeworkQueries,
            organizationsQueries = organizationsQueries,
            employeeQueries = employeeQueries,
            subjectQueries = subjectQueries,
            todoQueries = todoQueries,
            coroutineManager = coroutineManager,
        )
    }

    interface SyncStorage : LocalDataSource.FullSynced.MultipleDocuments<BaseGoalEntity>, Commands {

        class Base(
            goalQueries: GoalQueries,
            homeworkQueries: HomeworkQueries,
            organizationsQueries: OrganizationQueries,
            employeeQueries: EmployeeQueries,
            subjectQueries: SubjectQueries,
            todoQueries: TodoQueries,
            coroutineManager: CoroutineManager,
        ) : SyncStorage, Commands.Abstract(
            isCacheSource = true,
            goalQueries = goalQueries,
            homeworkQueries = homeworkQueries,
            organizationsQueries = organizationsQueries,
            employeeQueries = employeeQueries,
            subjectQueries = subjectQueries,
            todoQueries = todoQueries,
            coroutineManager = coroutineManager,
        )
    }

    class Base(
        private val offlineStorage: OfflineStorage,
        private val syncStorage: SyncStorage
    ) : DailyGoalsLocalDataSource {
        override fun offline() = offlineStorage
        override fun sync() = syncStorage
    }
}