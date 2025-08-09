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

@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.tasks.convertToLocal
import ru.aleshin.studyassistant.core.data.mappers.tasks.convertToRemote
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToRemoteData
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.tasks.HomeworksLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.managers.sync.HomeworkSourceSyncManager.Companion.HOMEWORK_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.remote.datasources.tasks.HomeworksRemoteDataSource

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class HomeworksRepositoryImpl(
    private val remoteDataSource: HomeworksRemoteDataSource,
    private val localDataSource: HomeworksLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
    private val userSessionProvider: UserSessionProvider,
    private val resultSyncHandler: RemoteResultSyncHandler,
) : HomeworksRepository {

    override suspend fun addOrUpdateHomework(homework: Homework): UID {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriptionActive()

        val upsertModel = homework.copy(uid = homework.uid.ifBlank { randomUUID() })

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItem(upsertModel.mapToLocalData())
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModel.mapToRemoteData(userId = currentUser),
                type = OfflineChangeType.UPSERT,
                sourceKey = HOMEWORK_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItem(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItem(upsertModel.mapToLocalData())
        }

        return upsertModel.uid
    }

    override suspend fun addHomeworksGroup(homeworks: List<Homework>) {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriptionActive()

        val upsertModels = homeworks.map { homework ->
            homework.copy(uid = homework.uid.ifBlank { randomUUID() })
        }

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModels.map { it.mapToRemoteData(userId = currentUser) },
                type = OfflineChangeType.UPSERT,
                sourceKey = HOMEWORK_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItems(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
        }
    }

    override suspend fun fetchHomeworkById(uid: UID): Flow<Homework?> {
        return subscriptionChecker.getSubscriptionActiveFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchHomeworkDetailsById(uid).map { homeworkEntity ->
                    homeworkEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchHomeworkDetailsById(uid).map { homeworkEntity ->
                    homeworkEntity?.mapToDomain()
                }
            }
        }
    }

    override suspend fun fetchHomeworksByDate(date: Instant): Flow<List<Homework>> {
        val timeStart = date.startThisDay().toEpochMilliseconds()
        val timeEnd = date.endThisDay().toEpochMilliseconds()

        return subscriptionChecker.getSubscriptionActiveFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchHomeworksDetailsByTimeRange(timeStart, timeEnd).map { homeworks ->
                    homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchHomeworksDetailsByTimeRange(timeStart, timeEnd).map { homeworks ->
                    homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange): Flow<List<Homework>> {
        val timeStart = timeRange.from.toEpochMilliseconds()
        val timeEnd = timeRange.to.toEpochMilliseconds()

        return subscriptionChecker.getSubscriptionActiveFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchHomeworksDetailsByTimeRange(timeStart, timeEnd).map { homeworks ->
                    homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchHomeworksDetailsByTimeRange(timeStart, timeEnd).map { homeworks ->
                    homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchOverdueHomeworks(currentDate: Instant): Flow<List<Homework>> {
        val date = currentDate.endThisDay().toEpochMilliseconds()

        return subscriptionChecker.getSubscriptionActiveFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchOverdueHomeworksDetails(date).map { homeworks ->
                    homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchOverdueHomeworksDetails(date).map { homeworks ->
                    homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchActiveLinkedHomeworks(currentDate: Instant): Flow<List<Homework>> {
        val date = currentDate.toEpochMilliseconds()

        return subscriptionChecker.getSubscriptionActiveFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchActiveLinkedHomeworksDetails(date).map { homeworks ->
                    homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchActiveLinkedHomeworksDetails(date).map { homeworks ->
                    homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchCompletedHomeworksCount(): Flow<Int> {
        return subscriptionChecker.getSubscriptionActiveFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchCompletedHomeworksCount()
            } else {
                localDataSource.offline().fetchCompletedHomeworksCount()
            }
        }
    }

    override suspend fun deleteHomework(uid: UID) {
        val isSubscriber = subscriptionChecker.getSubscriptionActive()

        return if (isSubscriber) {
            localDataSource.sync().deleteItemsById(listOf(uid))
            resultSyncHandler.executeOrAddToQueue(
                documentId = uid,
                type = OfflineChangeType.DELETE,
                sourceKey = HOMEWORK_SOURCE_KEY,
            ) {
                remoteDataSource.deleteItemById(uid)
            }
        } else {
            localDataSource.offline().deleteItemsById(listOf(uid))
        }
    }

    override suspend fun transferData(direction: DataTransferDirection, mergeData: Boolean) {
        val currentUser = userSessionProvider.getCurrentUserId()
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allHomeworksFlow = remoteDataSource.fetchAllItems(currentUser)
                val homeworks = allHomeworksFlow.first().map { it.convertToLocal() }

                if (!mergeData) {
                    localDataSource.offline().deleteAllItems()
                }
                localDataSource.offline().addOrUpdateItems(homeworks)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allHomeworks = localDataSource.offline().fetchAllHomeworks().first()
                val homeworksRemote = allHomeworks.map { it.convertToRemote(currentUser) }

                if (!mergeData) {
                    remoteDataSource.deleteAllItems(currentUser)
                }
                remoteDataSource.addOrUpdateItems(homeworksRemote)

                localDataSource.sync().deleteAllItems()
                localDataSource.sync().addOrUpdateItems(allHomeworks)
            }
        }
    }
}