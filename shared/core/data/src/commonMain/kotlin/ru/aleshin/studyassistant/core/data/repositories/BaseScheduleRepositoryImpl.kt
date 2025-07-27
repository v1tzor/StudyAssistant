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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.dateTimeByWeek
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.Constants.Date.DAYS_IN_WEEK
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.schedules.convertToLocal
import ru.aleshin.studyassistant.core.data.mappers.schedules.convertToRemote
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToRemoteData
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.schedules.BaseScheduleLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.managers.sync.BaseScheduleSourceSyncManager.Companion.BASE_SCHEDULE_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.remote.datasources.schedules.BaseScheduleRemoteDataSource

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class BaseScheduleRepositoryImpl(
    private val remoteDataSource: BaseScheduleRemoteDataSource,
    private val localDataSource: BaseScheduleLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
    private val resultSyncHandler: RemoteResultSyncHandler,
    private val userSessionProvider: UserSessionProvider,
) : BaseScheduleRepository {

    override suspend fun addOrUpdateSchedule(schedule: BaseSchedule): UID {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModel = schedule.copy(uid = schedule.uid.ifBlank { randomUUID() })

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItem(upsertModel.mapToLocalData())
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModel.mapToRemoteData(userId = currentUser),
                type = OfflineChangeType.UPSERT,
                sourceKey = BASE_SCHEDULE_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItem(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItem(upsertModel.mapToLocalData())
        }

        return upsertModel.uid
    }

    override suspend fun addOrUpdateSchedulesGroup(schedules: List<BaseSchedule>) {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModels = schedules.map { schedule ->
            schedule.copy(uid = schedule.uid.ifBlank { randomUUID() })
        }

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModels.map { it.mapToRemoteData(userId = currentUser) },
                type = OfflineChangeType.UPSERT,
                sourceKey = BASE_SCHEDULE_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItems(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
        }
    }

    override suspend fun fetchScheduleById(uid: UID): Flow<BaseSchedule?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchScheduleDetailsById(uid).map { scheduleEntity ->
                    scheduleEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchScheduleDetailsById(uid).map { scheduleEntity ->
                    scheduleEntity?.mapToDomain()
                }
            }
        }
    }

    override suspend fun fetchScheduleByDate(date: Instant, numberOfWeek: NumberOfRepeatWeek): Flow<BaseSchedule?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchScheduleDetailsByDate(date, numberOfWeek).map { scheduleEntity ->
                    scheduleEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchScheduleDetailsByDate(date, numberOfWeek).map { scheduleEntity ->
                    scheduleEntity?.mapToDomain()
                }
            }
        }
    }

    override suspend fun fetchSchedulesByVersion(
        version: TimeRange,
        numberOfWeek: NumberOfRepeatWeek?,
    ): Flow<List<BaseSchedule>> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchSchedulesByVersion(
                    version.from,
                    version.to,
                    numberOfWeek
                ).map { schedules ->
                    schedules.map { baseSchedulePojo -> baseSchedulePojo.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchSchedulesByVersion(
                    version.from,
                    version.to,
                    numberOfWeek
                ).map { schedules ->
                    schedules.map { baseScheduleEntity -> baseScheduleEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchSchedulesByTimeRange(
        timeRange: TimeRange,
        maxNumberOfWeek: NumberOfRepeatWeek,
    ): Flow<Map<Instant, BaseSchedule?>> {
        val scheduleListFlow = fetchSchedulesByVersion(timeRange, null)
        return scheduleListFlow.map { schedules ->
            return@map buildMap<Instant, BaseSchedule?> {
                schedules.forEach { schedule ->
                    val firstDate = schedule.dayOfWeek.dateTimeByWeek(schedule.dateVersion.from)
                    val firstWeek = firstDate.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)
                    val targetDate = firstDate.shiftWeek(
                        schedule.week.isoRepeatWeekNumber - firstWeek.isoRepeatWeekNumber
                    )
                    val untilEnd = if (timeRange.to > schedule.dateVersion.to) {
                        targetDate.daysUntil(schedule.dateVersion.to, TimeZone.currentSystemDefault())
                    } else {
                        targetDate.daysUntil(timeRange.to, TimeZone.currentSystemDefault())
                    }
                    if (untilEnd >= 0) {
                        val repeats = untilEnd / (maxNumberOfWeek.isoRepeatWeekNumber * DAYS_IN_WEEK)
                        for (i in 0..repeats) {
                            val date = targetDate.shiftWeek(i * maxNumberOfWeek.isoRepeatWeekNumber)
                            if (timeRange.containsDate(date)) put(date.startThisDay(), schedule)
                        }
                    }
                }
            }
        }
    }

    override suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<Class?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchClassById(uid, scheduleId).map { classEntity ->
                    classEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchClassById(uid, scheduleId).map { classEntity ->
                    classEntity?.mapToDomain()
                }
            }
        }
    }

    override suspend fun deleteSchedulesByTimeRange(timeRange: TimeRange) {
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        return if (isSubscriber) {
            val deletableItems = localDataSource.sync().fetchSchedulesByTimeRangeEmpty(timeRange.from, timeRange.to)

            localDataSource.sync().deleteSchedulesByTimeRange(timeRange.from, timeRange.to)
            resultSyncHandler.executeOrAddToQueue(
                documentIds = deletableItems.map { it.id },
                type = OfflineChangeType.DELETE,
                sourceKey = BASE_SCHEDULE_SOURCE_KEY,
            ) {
                remoteDataSource.deleteItemsByIds(deletableItems.map { it.id })
            }
        } else {
            localDataSource.offline().deleteSchedulesByTimeRange(timeRange.from, timeRange.to)
        }
    }

    override suspend fun transferData(direction: DataTransferDirection) {
        val currentUser = userSessionProvider.getCurrentUserId()
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allSchedulesFlow = remoteDataSource.fetchAllItems(currentUser)
                val schedules = allSchedulesFlow.first().map { it.convertToLocal() }
                localDataSource.offline().deleteAllItems()
                localDataSource.offline().addOrUpdateItems(schedules)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allSchedules = localDataSource.offline().fetchAllSchedules().first()
                val schedulesRemote = allSchedules.map { it.convertToRemote(currentUser) }

                remoteDataSource.deleteAllItems(currentUser)
                remoteDataSource.addOrUpdateItems(schedulesRemote)

                localDataSource.sync().deleteAllItems()
                localDataSource.sync().addOrUpdateItems(allSchedules)
            }
        }
    }
}