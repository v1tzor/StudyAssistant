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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.Instant.Companion.DISTANT_FUTURE
import kotlinx.datetime.Instant.Companion.DISTANT_PAST
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.payments.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.tasks.HomeworksLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.remote.datasources.tasks.HomeworksRemoteDataSource

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class HomeworksRepositoryImpl(
    private val remoteDataSource: HomeworksRemoteDataSource,
    private val localDataSource: HomeworksLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : HomeworksRepository {

    override suspend fun addOrUpdateHomework(homework: Homework, targetUser: UID): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateHomework(homework.mapToRemoteData(), targetUser)
        } else {
            localDataSource.addOrUpdateHomework(homework.mapToLocalData())
        }
    }

    override suspend fun addHomeworksGroup(homeworks: List<Homework>, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateHomeworksGroup(homeworks.map { it.mapToRemoteData() }, targetUser)
        } else {
            localDataSource.addOrUpdateHomeworksGroup(homeworks.map { it.mapToLocalData() })
        }
    }

    override suspend fun fetchHomeworkById(uid: UID, targetUser: UID): Flow<Homework?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchHomeworkById(uid, targetUser).map { homeworkPojo -> homeworkPojo?.mapToDomain() }
        } else {
            localDataSource.fetchHomeworkById(uid).map { homeworkEntity -> homeworkEntity?.mapToDomain() }
        }
    }

    override suspend fun fetchHomeworksByDate(date: Instant, targetUser: UID): Flow<List<Homework>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = date.startThisDay().toEpochMilliseconds()
        val timeEnd = date.endThisDay().toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchHomeworksByTimeRange(timeStart, timeEnd, targetUser).map { homeworks ->
                homeworks.map { homeworkPojo -> homeworkPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchHomeworksByTimeRange(timeStart, timeEnd).map { homeworks ->
                homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange, targetUser: UID): Flow<List<Homework>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = timeRange.from.toEpochMilliseconds()
        val timeEnd = timeRange.to.toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchHomeworksByTimeRange(timeStart, timeEnd, targetUser).map { homeworks ->
                homeworks.map { homeworkPojo -> homeworkPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchHomeworksByTimeRange(timeStart, timeEnd).map { homeworks ->
                homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchOverdueHomeworks(currentDate: Instant, targetUser: UID): Flow<List<Homework>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val date = currentDate.endThisDay().toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchOverdueHomeworks(date, targetUser).map { homeworks ->
                homeworks.map { homeworkPojo -> homeworkPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchOverdueHomeworks(date).map { homeworks ->
                homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchActiveLinkedHomeworks(currentDate: Instant, targetUser: UID): Flow<List<Homework>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val date = currentDate.toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchActiveLinkedHomeworks(date, targetUser).map { homeworks ->
                homeworks.map { homeworkPojo -> homeworkPojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchActiveLinkedHomeworks(date).map { homeworks ->
                homeworks.map { homeworkEntity -> homeworkEntity.mapToDomain() }
            }
        }
    }

    override suspend fun deleteHomework(uid: UID, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.deleteHomework(uid, targetUser)
        } else {
            localDataSource.deleteHomework(uid)
        }
    }

    override suspend fun deleteAllHomeworks(targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.deleteAllHomework(targetUser)
        } else {
            localDataSource.deleteAllHomework()
        }
    }

    override suspend fun transferData(direction: DataTransferDirection, targetUser: UID) {
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allHomeworks = remoteDataSource.fetchHomeworksByTimeRange(
                    from = DISTANT_PAST.toEpochMilliseconds(),
                    to = DISTANT_FUTURE.toEpochMilliseconds(),
                    targetUser = targetUser,
                ).let { homeworksFlow ->
                    return@let homeworksFlow.first().map { it.mapToDomain().mapToLocalData() }
                }
                localDataSource.deleteAllHomework()
                localDataSource.addOrUpdateHomeworksGroup(allHomeworks)
                remoteDataSource.deleteAllHomework(targetUser)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allHomeworks = localDataSource.fetchHomeworksByTimeRange(
                    from = DISTANT_PAST.toEpochMilliseconds(),
                    to = DISTANT_FUTURE.toEpochMilliseconds(),
                ).let { homeworksFlow ->
                    return@let homeworksFlow.first().map { it.mapToDomain().mapToRemoteData() }
                }
                remoteDataSource.deleteAllHomework(targetUser)
                remoteDataSource.addOrUpdateHomeworksGroup(allHomeworks, targetUser)
            }
        }
    }
}