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
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange, targetUser: UID): Flow<List<Homework>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = timeRange.from.toEpochMilliseconds()
        val timeEnd = timeRange.to.toEpochMilliseconds()

        val homeworksFlow = if (isSubscriber) {
            remoteDataSource.fetchHomeworksByTime(timeStart.toInt(), timeEnd.toInt(), targetUser)
        } else {
            localDataSource.fetchHomeworksByTime(timeStart, timeEnd)
        }

        return homeworksFlow.map { homeworkListData ->
            homeworkListData.map { it.mapToDomain() }
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

    override suspend fun addOrUpdateHomework(homework: Homework, targetUser: UID): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateHomework(homework.mapToData(), targetUser)
        } else {
            localDataSource.addOrUpdateHomework(homework.mapToData())
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