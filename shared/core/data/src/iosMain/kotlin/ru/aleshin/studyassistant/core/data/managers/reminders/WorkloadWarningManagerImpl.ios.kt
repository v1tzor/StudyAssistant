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

package ru.aleshin.studyassistant.core.data.managers.reminders

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.getString
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyWorkload
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.managers.reminders.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.high_workload_warning_body as core_high_workload_warning_body
import ru.aleshin.studyassistant.core.ui.resources.high_workload_warning_title as core_high_workload_warning_title

/**
 * @author Stanislav Aleshin on 24.08.2024.
 */
actual class WorkloadWarningManagerImpl(
    private val notificationScheduler: NotificationScheduler,
    private val dateManager: DateManager,
    private val calendarSettingsRepository: CalendarSettingsRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val baseScheduleRepository: BaseScheduleRepository,
    private val customScheduleRepository: CustomScheduleRepository,
    private val homeworksRepository: HomeworksRepository,
    private val todoRepository: TodoRepository,
) : WorkloadWarningManager {

    actual override suspend fun startOrRetryWarningService() {
        val warningTitle = getString(CoreRes.string.core_high_workload_warning_title)
        val warningBody = getString(CoreRes.string.core_high_workload_warning_body)
        val threshold = notificationSettingsRepository.fetchSettings().first().highWorkload
            ?: return notificationScheduler.clearNotificationGroup(NOTIFICATION_GROUP)
        val currentTime = dateManager.fetchCurrentInstant()
        val currentDate = dateManager.fetchBeginningCurrentInstant()

        val notificationIds = buildList {
            repeat(SCHEDULE_DAYS) { dayShift ->
                val notificationDate = currentDate.shiftDay(dayShift)
                val notificationTime = notificationDate.setHoursAndMinutes(
                    hour = CHECK_TIME_HOUR,
                    minute = CHECK_TIME_MINUTE,
                )
                if (notificationTime <= currentTime) return@repeat

                val targetDate = notificationDate.shiftDay(1)
                if (fetchDailyWorkload(targetDate).isHigh(threshold)) {
                    val id = "$NOTIFICATION_PREFIX:$notificationDate".hashCode()
                    add(id)
                    notificationScheduler.scheduleNotification(
                        id = id,
                        title = warningTitle,
                        body = warningBody,
                        time = notificationTime,
                    )
                }
            }
        }
        notificationScheduler.updateNotificationGroup(NOTIFICATION_GROUP, notificationIds)
    }

    actual override suspend fun stopWarningService() {
        notificationScheduler.clearNotificationGroup(NOTIFICATION_GROUP)
    }

    private suspend fun fetchDailyWorkload(date: Instant): DailyWorkload {
        val calendarSettings = calendarSettingsRepository.fetchSettings().first()
        val numberOfWeek = date.dateTime().date.numberOfRepeatWeek(calendarSettings.numberOfWeek)
        val baseSchedule = baseScheduleRepository.fetchScheduleByDate(date, numberOfWeek).first()
        val customSchedule = customScheduleRepository.fetchScheduleByDate(date).first()
        val homeworks = homeworksRepository.fetchHomeworksByDate(date).first()
        val todos = todoRepository.fetchTodosByDate(date).first()
        val classes = (customSchedule?.classes ?: baseSchedule?.classes).orEmpty().filter { classModel ->
            calendarSettings.holidays.none { holiday ->
                val dateFilter = TimeRange(holiday.start, holiday.end).containsDate(date)
                val organizationFilter = holiday.organizations.contains(classModel.organization.uid)
                dateFilter && organizationFilter
            }
        }
        return DailyWorkload.calculate(classes, homeworks, todos)
    }

    companion object {
        private const val SCHEDULE_DAYS = 5
        private const val CHECK_TIME_HOUR = 19
        private const val CHECK_TIME_MINUTE = 0
        private const val NOTIFICATION_GROUP = "IOS_WORKLOAD_WARNINGS"
        private const val NOTIFICATION_PREFIX = "workload"
    }
}
