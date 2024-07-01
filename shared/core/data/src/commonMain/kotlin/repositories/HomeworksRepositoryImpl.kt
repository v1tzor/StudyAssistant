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

package repositories

import database.tasks.HomeworksLocalDataSource
import entities.tasks.Homework
import extensions.endThisDay
import extensions.startThisDay
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import mappers.tasks.mapToData
import mappers.tasks.mapToDomain
import payments.SubscriptionChecker
import remote.tasks.HomeworksRemoteDataSource

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
            remoteDataSource.addOrUpdateHomework(homework.mapToData(), targetUser)
        } else {
            localDataSource.addOrUpdateHomework(homework.mapToData())
        }
    }

    override suspend fun fetchHomeworkById(uid: UID, targetUser: UID): Flow<Homework?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val homeworkFlow = if (isSubscriber) {
            remoteDataSource.fetchHomeworkById(uid, targetUser)
        } else {
            localDataSource.fetchHomeworkById(uid)
        }

        return homeworkFlow.map { homeworkData ->
            homeworkData?.mapToDomain()
        }
    }

    override suspend fun fetchHomeworksByDate(date: Instant, targetUser: UID): Flow<List<Homework>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = date.startThisDay().toEpochMilliseconds()
        val timeEnd = date.endThisDay().toEpochMilliseconds()

        val homeworksFlow = if (isSubscriber) {
            remoteDataSource.fetchHomeworksByTimeRange(timeStart, timeEnd, targetUser)
        } else {
            localDataSource.fetchHomeworksByTimeRange(timeStart, timeEnd)
        }

        return homeworksFlow.map { homeworkListData ->
            homeworkListData.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange, targetUser: UID): Flow<List<Homework>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = timeRange.from.toEpochMilliseconds()
        val timeEnd = timeRange.to.toEpochMilliseconds()

        val homeworksFlow = if (isSubscriber) {
            remoteDataSource.fetchHomeworksByTimeRange(timeStart, timeEnd, targetUser)
        } else {
            localDataSource.fetchHomeworksByTimeRange(timeStart, timeEnd)
        }

        return homeworksFlow.map { homeworkListData ->
            homeworkListData.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchOverdueHomeworks(currentDate: Instant, targetUser: UID): Flow<List<Homework>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val date = currentDate.endThisDay().toEpochMilliseconds()

        val homeworksFlow = if (isSubscriber) {
            remoteDataSource.fetchOverdueHomeworks(date, targetUser)
        } else {
            localDataSource.fetchOverdueHomeworks(date)
        }

        return homeworksFlow.map { homeworkListData ->
            homeworkListData.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchActiveLinkedHomeworks(currentDate: Instant, targetUser: UID): Flow<List<Homework>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val homeworksFlow = if (isSubscriber) {
            remoteDataSource.fetchActiveLinkedHomeworks(currentDate.toEpochMilliseconds(), targetUser)
        } else {
            localDataSource.fetchActiveLinkedHomeworks(currentDate.toEpochMilliseconds())
        }

        return homeworksFlow.map { homeworkListData ->
            homeworkListData.map { it.mapToDomain() }
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
}